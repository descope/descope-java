package com.descope.utils;

import com.descope.exception.ClientFunctionalException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigInteger;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.AlgorithmParameters;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.PublicKey;
import java.security.Signature;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.ECPublicKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import lombok.experimental.UtilityClass;

@UtilityClass
public class DPoPUtils {

  private static final Set<String> ALLOWED_ALGS = new HashSet<>(Arrays.asList(
      "RS256", "RS384", "RS512",
      "ES256", "ES384", "ES512",
      "PS256", "PS384", "PS512",
      "EdDSA"
  ));

  private static final int MAX_PROOF_LEN = 8192;
  private static final long IAT_BACKWARD_WINDOW = 60L;
  private static final long IAT_FORWARD_WINDOW = 5L;

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  /**
   * Returns the DPoP JWK thumbprint (cnf.jkt) from a Token's claims, or an empty string if not present.
   *
   * @param claims - the token claims map
   * @return the JWK thumbprint string, or empty string if not a DPoP-bound token
   */
  @SuppressWarnings("unchecked")
  public static String getDPoPThumbprint(Map<String, Object> claims) {
    if (claims == null) {
      return "";
    }
    Object cnf = claims.get("cnf");
    if (!(cnf instanceof Map)) {
      return "";
    }
    Object jkt = ((Map<String, Object>) cnf).get("jkt");
    if (!(jkt instanceof String)) {
      return "";
    }
    return (String) jkt;
  }

  /**
   * Validates a DPoP proof JWT per RFC 9449.
   * If the session token does not have a cnf.jkt claim, this method does nothing.
   *
   * <p>Note: jti replay protection (RFC 9449 §11.1) is intentionally out of scope for this
   * stateless SDK. Replay detection requires server-side storage (e.g. a cache of seen jti
   * values) which a stateless library cannot provide. Callers that require replay protection
   * should track jti values in their own infrastructure.
   *
   * @param dpopProof    - the DPoP proof JWT string from the DPoP HTTP header
   * @param method       - the HTTP method (e.g. "GET", "POST")
   * @param requestUrl   - the full request URL
   * @param sessionToken - the raw session JWT string (used for ath verification and cnf.jkt check)
   * @throws ClientFunctionalException if the proof is invalid
   */
  public static void validateDPoPProof(String dpopProof, String method, String requestUrl,
      String sessionToken) {
    // Parse the session token to get cnf.jkt without full JWT validation
    String storedJKT = extractJKTFromRawJwt(sessionToken);
    if (storedJKT == null || storedJKT.isEmpty()) {
      // Token is not DPoP-bound; nothing to validate
      return;
    }

    // Step 1: trim
    if (dpopProof == null) {
      dpopProof = "";
    }
    dpopProof = dpopProof.trim();

    // Step 2: length check
    if (dpopProof.length() > MAX_PROOF_LEN) {
      throw ClientFunctionalException.invalidToken(
          new IllegalArgumentException("DPoP proof exceeds maximum length"));
    }

    // Step 3: empty check
    if (dpopProof.isEmpty()) {
      throw ClientFunctionalException.invalidToken(
          new IllegalArgumentException("DPoP proof required"));
    }

    // Step 4-5: split compact JWS
    String[] parts = dpopProof.split("\\.");
    if (parts.length != 3) {
      throw ClientFunctionalException.invalidToken(
          new IllegalArgumentException("malformed DPoP JWT"));
    }

    // Step 6: parse header
    Map<String, Object> header;
    try {
      byte[] headerBytes = Base64.getUrlDecoder().decode(addPadding(parts[0]));
      header = OBJECT_MAPPER.readValue(headerBytes, Map.class);
    } catch (Exception e) {
      throw ClientFunctionalException.invalidToken(
          new IllegalArgumentException("failed to parse DPoP header: " + e.getMessage()));
    }

    // Step 7: check typ
    if (!"dpop+jwt".equals(header.get("typ"))) {
      throw ClientFunctionalException.invalidToken(
          new IllegalArgumentException("typ must be dpop+jwt"));
    }

    // Step 8-9: check alg
    Object algObj = header.get("alg");
    if (!(algObj instanceof String)) {
      throw ClientFunctionalException.invalidToken(
          new IllegalArgumentException("rejected algorithm: " + algObj));
    }
    String alg = (String) algObj;
    if (!ALLOWED_ALGS.contains(alg)) {
      throw ClientFunctionalException.invalidToken(
          new IllegalArgumentException("rejected algorithm: " + alg));
    }

    // Step 10-13: check jwk
    Object jwkObj = header.get("jwk");
    if (!(jwkObj instanceof Map)) {
      throw ClientFunctionalException.invalidToken(
          new IllegalArgumentException("missing jwk header"));
    }
    @SuppressWarnings("unchecked")
    Map<String, Object> jwk = (Map<String, Object>) jwkObj;
    if ("oct".equals(jwk.get("kty"))) {
      throw ClientFunctionalException.invalidToken(
          new IllegalArgumentException("symmetric key not allowed"));
    }
    if (jwk.containsKey("d")) {
      throw ClientFunctionalException.invalidToken(
          new IllegalArgumentException("jwk must not contain a private key"));
    }

    // Step 14: import public key
    PublicKey publicKey;
    try {
      publicKey = importPublicKey(jwk);
    } catch (Exception e) {
      throw ClientFunctionalException.invalidToken(
          new IllegalArgumentException("failed to import JWK public key: " + e.getMessage()));
    }

    // Step 15: verify JWS signature
    try {
      byte[] signingInput = (parts[0] + "." + parts[1]).getBytes(StandardCharsets.UTF_8);
      byte[] signatureBytes = Base64.getUrlDecoder().decode(addPadding(parts[2]));
      verifySignature(alg, publicKey, signingInput, signatureBytes);
    } catch (ClientFunctionalException e) {
      throw e;
    } catch (Exception e) {
      throw ClientFunctionalException.invalidToken(
          new IllegalArgumentException("signature verification failed: " + e.getMessage()));
    }

    // Step 16: parse payload
    Map<String, Object> payload;
    try {
      byte[] payloadBytes = Base64.getUrlDecoder().decode(addPadding(parts[1]));
      payload = OBJECT_MAPPER.readValue(payloadBytes, Map.class);
    } catch (Exception e) {
      throw ClientFunctionalException.invalidToken(
          new IllegalArgumentException("failed to parse DPoP payload: " + e.getMessage()));
    }

    // Step 17: check jti
    Object jtiObj = payload.get("jti");
    if (!(jtiObj instanceof String) || ((String) jtiObj).isEmpty()) {
      throw ClientFunctionalException.invalidToken(
          new IllegalArgumentException("missing jti"));
    }

    // Step 18-19: check htm
    Object htmObj = payload.get("htm");
    if (!(htmObj instanceof String) || ((String) htmObj).isEmpty()) {
      throw ClientFunctionalException.invalidToken(
          new IllegalArgumentException("missing htm"));
    }
    String htm = (String) htmObj;

    // Step 20: match htm — use htm.equals(method) to avoid NPE if method is null
    if (!htm.equals(method)) {
      throw ClientFunctionalException.invalidToken(
          new IllegalArgumentException("htm mismatch"));
    }

    // Step 19/21: check htu
    Object htuObj = payload.get("htu");
    if (!(htuObj instanceof String) || ((String) htuObj).isEmpty()) {
      throw ClientFunctionalException.invalidToken(
          new IllegalArgumentException("missing htu"));
    }
    String htu = (String) htuObj;
    if (!htuMatches(htu, requestUrl)) {
      throw ClientFunctionalException.invalidToken(
          new IllegalArgumentException("htu mismatch"));
    }

    // Step 22-25: check iat
    Object iatObj = payload.get("iat");
    if (!(iatObj instanceof Number)) {
      throw ClientFunctionalException.invalidToken(
          new IllegalArgumentException("missing iat"));
    }
    long iat = ((Number) iatObj).longValue();
    long now = System.currentTimeMillis() / 1000L;
    long diff = now - iat;
    if (diff <= -IAT_FORWARD_WINDOW || diff >= IAT_BACKWARD_WINDOW) {
      throw ClientFunctionalException.invalidToken(
          new IllegalArgumentException("iat out of acceptable window"));
    }

    // Step 26-29: check ath
    Object athObj = payload.get("ath");
    if (!(athObj instanceof String) || ((String) athObj).isEmpty()) {
      throw ClientFunctionalException.invalidToken(
          new IllegalArgumentException("missing ath"));
    }
    String ath = (String) athObj;
    try {
      byte[] digest = MessageDigest.getInstance("SHA-256")
          .digest(sessionToken.getBytes(StandardCharsets.UTF_8));
      String expected = Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
      if (!ath.equals(expected)) {
        throw ClientFunctionalException.invalidToken(
            new IllegalArgumentException("ath mismatch"));
      }
    } catch (ClientFunctionalException e) {
      throw e;
    } catch (Exception e) {
      throw ClientFunctionalException.invalidToken(
          new IllegalArgumentException("ath computation failed: " + e.getMessage()));
    }

    // Step 30-32: compute and verify JWK thumbprint
    try {
      String thumbprint = computeJwkThumbprint(jwk);
      if (!thumbprint.equals(storedJKT)) {
        throw ClientFunctionalException.invalidToken(
            new IllegalArgumentException("key mismatch"));
      }
    } catch (ClientFunctionalException e) {
      throw e;
    } catch (Exception e) {
      throw ClientFunctionalException.invalidToken(
          new IllegalArgumentException("JWK thumbprint computation failed: " + e.getMessage()));
    }
  }

  // -------------------------------------------------------------------------
  // Private helpers
  // -------------------------------------------------------------------------

  /**
   * Extracts the cnf.jkt value from the payload of a raw (unvalidated) JWT.
   * Used only to determine if the session token is DPoP-bound before doing
   * full DPoP proof validation.
   */
  @SuppressWarnings("unchecked")
  private static String extractJKTFromRawJwt(String jwt) {
    if (jwt == null || jwt.isEmpty()) {
      return "";
    }
    String[] parts = jwt.split("\\.");
    if (parts.length != 3) {
      return "";
    }
    try {
      byte[] payloadBytes = Base64.getUrlDecoder().decode(addPadding(parts[1]));
      Map<String, Object> claims = OBJECT_MAPPER.readValue(payloadBytes, Map.class);
      return getDPoPThumbprint(claims);
    } catch (Exception e) {
      return "";
    }
  }

  /**
   * Adds Base64URL padding ('=') as needed.
   */
  static String addPadding(String s) {
    int mod = s.length() % 4;
    if (mod == 0) {
      return s;
    }
    StringBuilder sb = new StringBuilder(s);
    for (int i = mod; i < 4; i++) {
      sb.append('=');
    }
    return sb.toString();
  }

  /**
   * Checks whether the DPoP htu matches the request URL per RFC 9449 §7.1.
   * Scheme and host are lowercased; query and fragment are stripped; default
   * ports (80/http, 443/https) are removed.
   */
  private static boolean htuMatches(String htu, String requestUrl) {
    try {
      URI htuUri = new URI(htu);
      URI reqUri = new URI(requestUrl);

      String htuScheme = htuUri.getScheme();
      String reqScheme = reqUri.getScheme();
      if (htuScheme == null || reqScheme == null) {
        return false;
      }
      htuScheme = htuScheme.toLowerCase();
      reqScheme = reqScheme.toLowerCase();

      String htuHost = htuUri.getHost();
      String reqHost = reqUri.getHost();
      if (htuHost == null || reqHost == null) {
        return false;
      }
      htuHost = htuHost.toLowerCase();
      reqHost = reqHost.toLowerCase();

      if (!htuScheme.equals(reqScheme) || !htuHost.equals(reqHost)) {
        return false;
      }

      int htuPort = normalizePort(htuUri.getPort(), htuScheme);
      int reqPort = normalizePort(reqUri.getPort(), reqScheme);
      if (htuPort != reqPort) {
        return false;
      }

      String htuPath = htuUri.getPath();
      String reqPath = reqUri.getPath();
      if (htuPath == null) {
        htuPath = "";
      }
      if (reqPath == null) {
        reqPath = "";
      }
      return htuPath.equals(reqPath);
    } catch (Exception e) {
      return false;
    }
  }

  /**
   * Returns -1 if the port is the default for the scheme, otherwise returns the port.
   */
  private static int normalizePort(int port, String scheme) {
    if (port == -1) {
      return -1;
    }
    if ("https".equals(scheme) && port == 443) {
      return -1;
    }
    if ("http".equals(scheme) && port == 80) {
      return -1;
    }
    return port;
  }

  /**
   * Imports a public key from a JWK map. Supports RSA, EC, and OKP key types.
   */
  private static PublicKey importPublicKey(Map<String, Object> jwk) throws Exception {
    String kty = (String) jwk.get("kty");
    if ("RSA".equals(kty)) {
      return importRSAKey(jwk);
    } else if ("EC".equals(kty)) {
      return importECKey(jwk);
    } else if ("OKP".equals(kty)) {
      return importOKPKey(jwk);
    } else {
      throw new IllegalArgumentException("unsupported key type: " + kty);
    }
  }

  private static RSAPublicKey importRSAKey(Map<String, Object> jwk) throws Exception {
    byte[] n = Base64.getUrlDecoder().decode(addPadding((String) jwk.get("n")));
    byte[] e = Base64.getUrlDecoder().decode(addPadding((String) jwk.get("e")));
    RSAPublicKeySpec spec = new RSAPublicKeySpec(
        new BigInteger(1, n),
        new BigInteger(1, e)
    );
    return (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(spec);
  }

  private static ECPublicKey importECKey(Map<String, Object> jwk) throws Exception {
    String crv = (String) jwk.get("crv");
    String curveName;
    switch (crv) {
      case "P-256":
        curveName = "secp256r1";
        break;
      case "P-384":
        curveName = "secp384r1";
        break;
      case "P-521":
        curveName = "secp521r1";
        break;
      default: throw new IllegalArgumentException("unsupported curve: " + crv);
    }

    byte[] ecX = Base64.getUrlDecoder().decode(addPadding((String) jwk.get("x")));
    byte[] ecY = Base64.getUrlDecoder().decode(addPadding((String) jwk.get("y")));

    AlgorithmParameters parameters = AlgorithmParameters.getInstance("EC");
    parameters.init(new ECGenParameterSpec(curveName));
    ECParameterSpec ecParameters = parameters.getParameterSpec(ECParameterSpec.class);

    ECPoint ecPoint = new ECPoint(new BigInteger(1, ecX), new BigInteger(1, ecY));
    ECPublicKeySpec spec = new ECPublicKeySpec(ecPoint, ecParameters);
    return (ECPublicKey) KeyFactory.getInstance("EC").generatePublic(spec);
  }

  /**
   * Imports an OKP (EdDSA) public key using BouncyCastle (bcprov-jdk18on), which is
   * compatible with Java 8+. The JWK {@code x} parameter is the little-endian compressed
   * point encoding per RFC 8032 §5.1.2. The high bit of the last byte encodes the sign of
   * the x-coordinate and is part of the standard compressed encoding consumed by BouncyCastle
   * directly. The key is reconstructed via DER-encoded SubjectPublicKeyInfo so that the
   * returned PublicKey can be used with the standard JCA Signature API.
   */
  private static PublicKey importOKPKey(Map<String, Object> jwk) throws Exception {
    String crv = (String) jwk.get("crv");
    // x is the compressed public-key bytes in little-endian form (RFC 8032 §5.1.2).
    // BouncyCastle accepts these bytes directly, including the sign bit in the last byte.
    byte[] keyBytes = Base64.getUrlDecoder().decode(addPadding((String) jwk.get("x")));
    try {
      // Build a DER SubjectPublicKeyInfo and use X509EncodedKeySpec so the BC provider
      // can reconstruct the key via the standard KeyFactory API (Java 8 compatible).
      byte[] spki = buildEdDsaSpki(crv, keyBytes);
      java.security.spec.X509EncodedKeySpec spec = new java.security.spec.X509EncodedKeySpec(spki);
      // Use the BC provider explicitly to ensure EdDSA is supported on Java 8.
      java.security.Provider bcProvider =
          java.security.Security.getProvider("BC");
      if (bcProvider == null) {
        java.security.Security.addProvider(
            new org.bouncycastle.jce.provider.BouncyCastleProvider());
      }
      return KeyFactory.getInstance("Ed25519".equals(crv) ? "Ed25519" : "Ed448", "BC")
          .generatePublic(spec);
    } catch (IllegalArgumentException e) {
      throw e;
    } catch (Exception e) {
      throw new IllegalArgumentException(
          "failed to import OKP key for curve " + crv + ": " + e.getMessage());
    }
  }

  /**
   * Builds a minimal DER-encoded SubjectPublicKeyInfo for an Ed25519 or Ed448 key.
   * Structure: SEQUENCE { SEQUENCE { OID }, BIT STRING { 0x00, keyBytes } }
   */
  private static byte[] buildEdDsaSpki(String crv, byte[] keyBytes) {
    // OID for Ed25519: 1.3.101.112 = 06 03 2B 65 70
    // OID for Ed448:   1.3.101.113 = 06 03 2B 65 71
    byte[] oidBytes = "Ed25519".equals(crv)
        ? new byte[]{0x06, 0x03, 0x2B, 0x65, 0x70}
        : new byte[]{0x06, 0x03, 0x2B, 0x65, 0x71};
    // AlgorithmIdentifier SEQUENCE: 30 len OID
    byte[] algId = new byte[2 + oidBytes.length];
    algId[0] = 0x30;
    algId[1] = (byte) oidBytes.length;
    System.arraycopy(oidBytes, 0, algId, 2, oidBytes.length);
    // BIT STRING: 03 (1 + keyBytes.length) 00 keyBytes
    byte[] bitStr = new byte[3 + keyBytes.length];
    bitStr[0] = 0x03;
    bitStr[1] = (byte) (1 + keyBytes.length);
    bitStr[2] = 0x00; // no unused bits
    System.arraycopy(keyBytes, 0, bitStr, 3, keyBytes.length);
    // Outer SEQUENCE
    int totalLen = algId.length + bitStr.length;
    byte[] spki = new byte[2 + totalLen];
    spki[0] = 0x30;
    spki[1] = (byte) totalLen;
    System.arraycopy(algId, 0, spki, 2, algId.length);
    System.arraycopy(bitStr, 0, spki, 2 + algId.length, bitStr.length);
    return spki;
  }

  /**
   * Verifies a JWS signature. EC signatures must be DER-encoded before verification.
   */
  private static void verifySignature(String alg, PublicKey publicKey, byte[] signingInput,
      byte[] signatureBytes) throws Exception {
    String jcaAlg = toJcaAlgorithm(alg);
    Signature sig = Signature.getInstance(jcaAlg);
    sig.initVerify(publicKey);
    sig.update(signingInput);

    byte[] verifyBytes = signatureBytes;
    if (alg.startsWith("ES")) {
      // JWT ES* uses raw R||S; Java needs DER SEQUENCE { INTEGER r, INTEGER s }
      verifyBytes = rawToDerEC(signatureBytes, alg);
    }

    boolean valid = sig.verify(verifyBytes);
    if (!valid) {
      throw ClientFunctionalException.invalidToken(
          new IllegalArgumentException("DPoP proof signature is invalid"));
    }
  }

  /**
   * Maps JWT algorithm names to JCA algorithm names.
   */
  private static String toJcaAlgorithm(String alg) {
    switch (alg) {
      case "RS256": return "SHA256withRSA";
      case "RS384": return "SHA384withRSA";
      case "RS512": return "SHA512withRSA";
      case "ES256": return "SHA256withECDSA";
      case "ES384": return "SHA384withECDSA";
      case "ES512": return "SHA512withECDSA";
      case "PS256": return "SHA256withRSAandMGF1";
      case "PS384": return "SHA384withRSAandMGF1";
      case "PS512": return "SHA512withRSAandMGF1";
      case "EdDSA": return "EdDSA";
      default: throw new IllegalArgumentException("unsupported algorithm: " + alg);
    }
  }

  /**
   * Converts a raw R||S EC signature (as used in JWTs) to DER-encoded format
   * required by Java's Signature API.
   *
   * <p>Per RFC 7518, each of R and S has a fixed byte length based on the curve:
   *   ES256 → 32 bytes each, ES384 → 48 bytes each, ES512 → 66 bytes each.
   */
  private static byte[] rawToDerEC(byte[] rawSig, String alg) {
    int componentLen;
    switch (alg) {
      case "ES256":
        componentLen = 32;
        break;
      case "ES384":
        componentLen = 48;
        break;
      case "ES512":
        componentLen = 66;
        break;
      default: throw new IllegalArgumentException("not an EC algorithm: " + alg);
    }

    if (rawSig.length != 2 * componentLen) {
      throw new IllegalArgumentException(
          "invalid raw EC signature length " + rawSig.length + " for " + alg);
    }

    byte[] r = Arrays.copyOfRange(rawSig, 0, componentLen);
    byte[] s = Arrays.copyOfRange(rawSig, componentLen, 2 * componentLen);

    byte[] derR = positiveInteger(r);
    byte[] derS = positiveInteger(s);

    int seqLen = 2 + derR.length + 2 + derS.length;
    byte[] der;
    if (seqLen <= 127) {
      der = new byte[2 + seqLen];
      der[0] = 0x30;
      der[1] = (byte) seqLen;
      int pos = 2;
      der[pos++] = 0x02;
      der[pos++] = (byte) derR.length;
      System.arraycopy(derR, 0, der, pos, derR.length);
      pos += derR.length;
      der[pos++] = 0x02;
      der[pos++] = (byte) derS.length;
      System.arraycopy(derS, 0, der, pos, derS.length);
    } else {
      // seqLen needs 2-byte length encoding (rare but possible for P-521)
      der = new byte[4 + seqLen];
      der[0] = 0x30;
      der[1] = (byte) 0x81;
      der[2] = (byte) seqLen;
      int pos = 3;
      der[pos++] = 0x02;
      der[pos++] = (byte) derR.length;
      System.arraycopy(derR, 0, der, pos, derR.length);
      pos += derR.length;
      der[pos++] = 0x02;
      der[pos++] = (byte) derS.length;
      System.arraycopy(derS, 0, der, pos, derS.length);
    }
    return der;
  }

  /**
   * Returns the minimal positive DER INTEGER encoding of a big-endian unsigned byte array.
   * Strips leading zero bytes and prepends a zero byte if the high bit is set.
   */
  private static byte[] positiveInteger(byte[] bytes) {
    // Strip leading zeros
    int start = 0;
    while (start < bytes.length - 1 && bytes[start] == 0) {
      start++;
    }
    if ((bytes[start] & 0x80) != 0) {
      // Need to prepend a zero byte to indicate positive
      byte[] result = new byte[bytes.length - start + 1];
      result[0] = 0x00;
      System.arraycopy(bytes, start, result, 1, bytes.length - start);
      return result;
    } else {
      return Arrays.copyOfRange(bytes, start, bytes.length);
    }
  }

  /**
   * Computes the JWK thumbprint per RFC 7638.
   * Uses a TreeMap so keys are alphabetically sorted.
   */
  private static String computeJwkThumbprint(Map<String, Object> jwk) throws Exception {
    String kty = (String) jwk.get("kty");
    Map<String, String> canonical = new TreeMap<>();

    switch (kty) {
      case "RSA":
        canonical.put("e", (String) jwk.get("e"));
        canonical.put("kty", "RSA");
        canonical.put("n", (String) jwk.get("n"));
        break;
      case "EC":
        canonical.put("crv", (String) jwk.get("crv"));
        canonical.put("kty", "EC");
        canonical.put("x", (String) jwk.get("x"));
        canonical.put("y", (String) jwk.get("y"));
        break;
      case "OKP":
        canonical.put("crv", (String) jwk.get("crv"));
        canonical.put("kty", "OKP");
        canonical.put("x", (String) jwk.get("x"));
        break;
      default:
        throw new IllegalArgumentException("unsupported key type for thumbprint: " + kty);
    }

    String json = OBJECT_MAPPER.writeValueAsString(canonical);
    byte[] digest = MessageDigest.getInstance("SHA-256")
        .digest(json.getBytes(StandardCharsets.UTF_8));
    return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
  }
}
