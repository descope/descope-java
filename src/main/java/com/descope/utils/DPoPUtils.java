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
    String alg = (String) header.get("alg");
    if (alg == null || !ALLOWED_ALGS.contains(alg)) {
      throw ClientFunctionalException.invalidToken(
          new IllegalArgumentException("rejected algorithm: " + alg));
    }

    // Step 10-13: check jwk
    @SuppressWarnings("unchecked")
    Map<String, Object> jwk = (Map<String, Object>) header.get("jwk");
    if (jwk == null) {
      throw ClientFunctionalException.invalidToken(
          new IllegalArgumentException("missing jwk header"));
    }
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
    String jti = (String) payload.get("jti");
    if (jti == null || jti.isEmpty()) {
      throw ClientFunctionalException.invalidToken(
          new IllegalArgumentException("missing jti"));
    }

    // Step 18-19: check htm
    String htm = (String) payload.get("htm");
    if (htm == null || htm.isEmpty()) {
      throw ClientFunctionalException.invalidToken(
          new IllegalArgumentException("missing htm"));
    }

    // Step 20: match htm
    if (!method.equals(htm)) {
      throw ClientFunctionalException.invalidToken(
          new IllegalArgumentException("htm mismatch"));
    }

    // Step 19/21: check htu
    String htu = (String) payload.get("htu");
    if (htu == null || htu.isEmpty()) {
      throw ClientFunctionalException.invalidToken(
          new IllegalArgumentException("missing htu"));
    }
    if (!htuMatches(htu, requestUrl)) {
      throw ClientFunctionalException.invalidToken(
          new IllegalArgumentException("htu mismatch"));
    }

    // Step 22-25: check iat
    Object iatObj = payload.get("iat");
    if (iatObj == null) {
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
    String ath = (String) payload.get("ath");
    if (ath == null || ath.isEmpty()) {
      throw ClientFunctionalException.invalidToken(
          new IllegalArgumentException("missing ath"));
    }
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
      case "P-256": curveName = "secp256r1"; break;
      case "P-384": curveName = "secp384r1"; break;
      case "P-521": curveName = "secp521r1"; break;
      default: throw new IllegalArgumentException("unsupported curve: " + crv);
    }

    byte[] xBytes = Base64.getUrlDecoder().decode(addPadding((String) jwk.get("x")));
    byte[] yBytes = Base64.getUrlDecoder().decode(addPadding((String) jwk.get("y")));

    AlgorithmParameters parameters = AlgorithmParameters.getInstance("EC");
    parameters.init(new ECGenParameterSpec(curveName));
    ECParameterSpec ecParameters = parameters.getParameterSpec(ECParameterSpec.class);

    ECPoint ecPoint = new ECPoint(new BigInteger(1, xBytes), new BigInteger(1, yBytes));
    ECPublicKeySpec spec = new ECPublicKeySpec(ecPoint, ecParameters);
    return (ECPublicKey) KeyFactory.getInstance("EC").generatePublic(spec);
  }

  /**
   * Imports an OKP (EdDSA) key. Requires Java 15+ or BouncyCastle provider.
   * Falls back to BouncyCastle's XDHPublicKeyParameters for Ed25519/Ed448.
   */
  private static PublicKey importOKPKey(Map<String, Object> jwk) throws Exception {
    String crv = (String) jwk.get("crv");
    byte[] xBytes = Base64.getUrlDecoder().decode(addPadding((String) jwk.get("x")));

    // Try standard Java 15+ EdDSA
    try {
      String algorithmName;
      if ("Ed25519".equals(crv)) {
        algorithmName = "Ed25519";
      } else if ("Ed448".equals(crv)) {
        algorithmName = "Ed448";
      } else {
        throw new IllegalArgumentException("unsupported OKP curve: " + crv);
      }
      // Use NamedParameterSpec via reflection to support Java 11+ with BC provider
      java.security.spec.NamedParameterSpec namedSpec =
          new java.security.spec.NamedParameterSpec(algorithmName);
      java.security.spec.EdECPublicKeySpec edSpec =
          new java.security.spec.EdECPublicKeySpec(namedSpec,
              new java.security.spec.EdECPoint(
                  (xBytes[xBytes.length - 1] & 0x80) != 0,
                  new BigInteger(1, reverseBytes(xBytes))
              ));
      return KeyFactory.getInstance("EdDSA").generatePublic(edSpec);
    } catch (Exception e) {
      throw new IllegalArgumentException("failed to import OKP key for curve " + crv + ": " + e.getMessage());
    }
  }

  private static byte[] reverseBytes(byte[] bytes) {
    byte[] reversed = Arrays.copyOf(bytes, bytes.length);
    for (int i = 0; i < reversed.length / 2; i++) {
      byte tmp = reversed[i];
      reversed[i] = reversed[reversed.length - 1 - i];
      reversed[reversed.length - 1 - i] = tmp;
    }
    return reversed;
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
   * Per RFC 7518, each of R and S has a fixed byte length based on the curve:
   *   ES256 → 32 bytes each, ES384 → 48 bytes each, ES512 → 66 bytes each.
   */
  private static byte[] rawToDerEC(byte[] rawSig, String alg) {
    int componentLen;
    switch (alg) {
      case "ES256": componentLen = 32; break;
      case "ES384": componentLen = 48; break;
      case "ES512": componentLen = 66; break;
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
