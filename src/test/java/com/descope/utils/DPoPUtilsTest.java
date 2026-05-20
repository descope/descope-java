package com.descope.utils;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.descope.exception.ClientFunctionalException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.Signature;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import org.junit.jupiter.api.Test;

public class DPoPUtilsTest {

  private static final ObjectMapper MAPPER = new ObjectMapper();

  // -----------------------------------------------------------------------
  // getDPoPThumbprint tests
  // -----------------------------------------------------------------------

  @Test
  void testGetDPoPThumbprintReturnEmptyWhenNoCnf() {
    Map<String, Object> claims = new HashMap<>();
    claims.put("sub", "user123");
    assertEquals("", DPoPUtils.getDPoPThumbprint(claims));
  }

  @Test
  void testGetDPoPThumbprintReturnEmptyWhenNull() {
    assertEquals("", DPoPUtils.getDPoPThumbprint(null));
  }

  @Test
  void testGetDPoPThumbprintReturnJkt() {
    Map<String, Object> cnf = new HashMap<>();
    cnf.put("jkt", "abc123");
    Map<String, Object> claims = new HashMap<>();
    claims.put("cnf", cnf);
    assertEquals("abc123", DPoPUtils.getDPoPThumbprint(claims));
  }

  // -----------------------------------------------------------------------
  // validateDPoPProof - no-op when token has no cnf.jkt
  // -----------------------------------------------------------------------

  @Test
  void testValidateDPoPProofNoOpWhenNoCnfJkt() {
    // A token without cnf.jkt — method should do nothing regardless of proof
    String sessionToken = buildRawJwt(new HashMap<>());
    assertDoesNotThrow(() ->
        DPoPUtils.validateDPoPProof("garbage", "GET", "https://api.example.com/resource", sessionToken));
  }

  // -----------------------------------------------------------------------
  // validateDPoPProof - error cases when cnf.jkt is present
  // -----------------------------------------------------------------------

  @Test
  void testValidateDPoPProofRejectsEmptyProof() throws Exception {
    KeyPair kp = generateEC("secp256r1");
    ECPublicKey pub = (ECPublicKey) kp.getPublic();
    Map<String, Object> jwk = ecJwk(pub, "P-256");
    String jkt = computeThumbprint(jwk);
    String sessionToken = buildRawJwtWithJkt(jkt);

    assertThrows(ClientFunctionalException.class, () ->
        DPoPUtils.validateDPoPProof("", "GET", "https://api.example.com/resource", sessionToken));
  }

  @Test
  void testValidateDPoPProofRejectsMalformedProof() throws Exception {
    KeyPair kp = generateEC("secp256r1");
    ECPublicKey pub = (ECPublicKey) kp.getPublic();
    Map<String, Object> jwk = ecJwk(pub, "P-256");
    String jkt = computeThumbprint(jwk);
    String sessionToken = buildRawJwtWithJkt(jkt);

    assertThrows(ClientFunctionalException.class, () ->
        DPoPUtils.validateDPoPProof("not.a.valid.jwt.here", "GET",
            "https://api.example.com/resource", sessionToken));
  }

  @Test
  void testValidateDPoPProofRejectsExceedingMaxLength() throws Exception {
    KeyPair kp = generateEC("secp256r1");
    ECPublicKey pub = (ECPublicKey) kp.getPublic();
    Map<String, Object> jwk = ecJwk(pub, "P-256");
    String jkt = computeThumbprint(jwk);
    String sessionToken = buildRawJwtWithJkt(jkt);

    String longProof = new String(new char[8193]).replace('\0', 'a');
    assertThrows(ClientFunctionalException.class, () ->
        DPoPUtils.validateDPoPProof(longProof, "GET", "https://api.example.com/resource", sessionToken));
  }

  // -----------------------------------------------------------------------
  // validateDPoPProof - valid ES256 proof
  // -----------------------------------------------------------------------

  @Test
  void testValidateDPoPProofValidES256() throws Exception {
    KeyPair kp = generateEC("secp256r1");
    ECPrivateKey priv = (ECPrivateKey) kp.getPrivate();
    ECPublicKey pub = (ECPublicKey) kp.getPublic();
    Map<String, Object> jwk = ecJwk(pub, "P-256");
    String jkt = computeThumbprint(jwk);
    String method = "GET";
    String url = "https://api.example.com/resource";

    String sessionToken = buildRawJwtWithJkt(jkt);
    String ath = Base64.getUrlEncoder().withoutPadding()
        .encodeToString(MessageDigest.getInstance("SHA-256")
            .digest(sessionToken.getBytes(StandardCharsets.UTF_8)));

    String dpopProof = buildDPoPProof("ES256", jwk, method, url, ath, priv);

    assertDoesNotThrow(() ->
        DPoPUtils.validateDPoPProof(dpopProof, method, url, sessionToken));
  }

  @Test
  void testValidateDPoPProofValidRS256() throws Exception {
    KeyPair kp = generateRSA();
    RSAPrivateKey priv = (RSAPrivateKey) kp.getPrivate();
    RSAPublicKey pub = (RSAPublicKey) kp.getPublic();
    Map<String, Object> jwk = rsaJwk(pub);
    String jkt = computeThumbprint(jwk);
    String method = "POST";
    String url = "https://api.example.com/token";

    String sessionToken = buildRawJwtWithJkt(jkt);
    String ath = Base64.getUrlEncoder().withoutPadding()
        .encodeToString(MessageDigest.getInstance("SHA-256")
            .digest(sessionToken.getBytes(StandardCharsets.UTF_8)));

    String dpopProof = buildDPoPProofRSA("RS256", jwk, method, url, ath, priv);

    assertDoesNotThrow(() ->
        DPoPUtils.validateDPoPProof(dpopProof, method, url, sessionToken));
  }

  @Test
  void testValidateDPoPProofRejectsWrongMethod() throws Exception {
    KeyPair kp = generateEC("secp256r1");
    ECPrivateKey priv = (ECPrivateKey) kp.getPrivate();
    ECPublicKey pub = (ECPublicKey) kp.getPublic();
    Map<String, Object> jwk = ecJwk(pub, "P-256");
    String jkt = computeThumbprint(jwk);
    String url = "https://api.example.com/resource";
    String sessionToken = buildRawJwtWithJkt(jkt);
    String ath = Base64.getUrlEncoder().withoutPadding()
        .encodeToString(MessageDigest.getInstance("SHA-256")
            .digest(sessionToken.getBytes(StandardCharsets.UTF_8)));

    // Build proof for GET, but validate as POST
    String dpopProof = buildDPoPProof("ES256", jwk, "GET", url, ath, priv);

    assertThrows(ClientFunctionalException.class, () ->
        DPoPUtils.validateDPoPProof(dpopProof, "POST", url, sessionToken));
  }

  @Test
  void testValidateDPoPProofRejectsWrongUrl() throws Exception {
    KeyPair kp = generateEC("secp256r1");
    ECPrivateKey priv = (ECPrivateKey) kp.getPrivate();
    ECPublicKey pub = (ECPublicKey) kp.getPublic();
    Map<String, Object> jwk = ecJwk(pub, "P-256");
    String jkt = computeThumbprint(jwk);
    String sessionToken = buildRawJwtWithJkt(jkt);
    String ath = Base64.getUrlEncoder().withoutPadding()
        .encodeToString(MessageDigest.getInstance("SHA-256")
            .digest(sessionToken.getBytes(StandardCharsets.UTF_8)));

    String dpopProof = buildDPoPProof("ES256", jwk, "GET", "https://api.example.com/resource", ath, priv);

    assertThrows(ClientFunctionalException.class, () ->
        DPoPUtils.validateDPoPProof(dpopProof, "GET", "https://other.example.com/resource", sessionToken));
  }

  @Test
  void testValidateDPoPProofRejectsWrongKey() throws Exception {
    // Build proof with key1, but token is bound to key2
    KeyPair kp1 = generateEC("secp256r1");
    ECPrivateKey priv1 = (ECPrivateKey) kp1.getPrivate();
    ECPublicKey pub1 = (ECPublicKey) kp1.getPublic();
    Map<String, Object> jwk1 = ecJwk(pub1, "P-256");

    KeyPair kp2 = generateEC("secp256r1");
    ECPublicKey pub2 = (ECPublicKey) kp2.getPublic();
    Map<String, Object> jwk2 = ecJwk(pub2, "P-256");
    String jkt2 = computeThumbprint(jwk2);

    String sessionToken = buildRawJwtWithJkt(jkt2);
    String ath = Base64.getUrlEncoder().withoutPadding()
        .encodeToString(MessageDigest.getInstance("SHA-256")
            .digest(sessionToken.getBytes(StandardCharsets.UTF_8)));

    String dpopProof = buildDPoPProof("ES256", jwk1, "GET", "https://api.example.com/resource", ath, priv1);

    assertThrows(ClientFunctionalException.class, () ->
        DPoPUtils.validateDPoPProof(dpopProof, "GET", "https://api.example.com/resource", sessionToken));
  }

  // -----------------------------------------------------------------------
  // htu matching - default port normalization
  // -----------------------------------------------------------------------

  @Test
  void testValidateDPoPProofHtuDefaultPortIgnored() throws Exception {
    // Proof with htu containing port 443 should match URL without explicit port
    KeyPair kp = generateEC("secp256r1");
    ECPrivateKey priv = (ECPrivateKey) kp.getPrivate();
    ECPublicKey pub = (ECPublicKey) kp.getPublic();
    Map<String, Object> jwk = ecJwk(pub, "P-256");
    String jkt = computeThumbprint(jwk);
    String sessionToken = buildRawJwtWithJkt(jkt);
    String ath = Base64.getUrlEncoder().withoutPadding()
        .encodeToString(MessageDigest.getInstance("SHA-256")
            .digest(sessionToken.getBytes(StandardCharsets.UTF_8)));

    // Build proof htu with explicit :443
    String dpopProof = buildDPoPProof("ES256", jwk, "GET",
        "https://api.example.com:443/resource", ath, priv);

    // Validate against URL without explicit port
    assertDoesNotThrow(() ->
        DPoPUtils.validateDPoPProof(dpopProof, "GET", "https://api.example.com/resource", sessionToken));
  }

  // -----------------------------------------------------------------------
  // Helpers
  // -----------------------------------------------------------------------

  private KeyPair generateEC(String curve) throws Exception {
    KeyPairGenerator kpg = KeyPairGenerator.getInstance("EC");
    kpg.initialize(new ECGenParameterSpec(curve));
    return kpg.generateKeyPair();
  }

  private KeyPair generateRSA() throws Exception {
    KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
    kpg.initialize(2048);
    return kpg.generateKeyPair();
  }

  private Map<String, Object> ecJwk(ECPublicKey pub, String crv) {
    byte[] xBytes = pub.getW().getAffineX().toByteArray();
    byte[] yBytes = pub.getW().getAffineY().toByteArray();
    // ensure fixed length (strip or pad leading zeros)
    int len = crv.equals("P-521") ? 66 : crv.equals("P-384") ? 48 : 32;
    Map<String, Object> jwk = new HashMap<>();
    jwk.put("kty", "EC");
    jwk.put("crv", crv);
    jwk.put("x", Base64.getUrlEncoder().withoutPadding().encodeToString(fixedLen(xBytes, len)));
    jwk.put("y", Base64.getUrlEncoder().withoutPadding().encodeToString(fixedLen(yBytes, len)));
    return jwk;
  }

  private Map<String, Object> rsaJwk(RSAPublicKey pub) {
    Map<String, Object> jwk = new HashMap<>();
    jwk.put("kty", "RSA");
    jwk.put("n", Base64.getUrlEncoder().withoutPadding().encodeToString(
        pub.getModulus().toByteArray()));
    jwk.put("e", Base64.getUrlEncoder().withoutPadding().encodeToString(
        pub.getPublicExponent().toByteArray()));
    return jwk;
  }

  private byte[] fixedLen(byte[] bytes, int len) {
    if (bytes.length == len) return bytes;
    if (bytes.length > len) {
      // strip leading zero bytes (BigInteger sign byte)
      return java.util.Arrays.copyOfRange(bytes, bytes.length - len, bytes.length);
    }
    // pad with leading zeros
    byte[] padded = new byte[len];
    System.arraycopy(bytes, 0, padded, len - bytes.length, bytes.length);
    return padded;
  }

  private String computeThumbprint(Map<String, Object> jwk) throws Exception {
    String kty = (String) jwk.get("kty");
    TreeMap<String, String> canonical = new TreeMap<>();
    if ("EC".equals(kty)) {
      canonical.put("crv", (String) jwk.get("crv"));
      canonical.put("kty", "EC");
      canonical.put("x", (String) jwk.get("x"));
      canonical.put("y", (String) jwk.get("y"));
    } else if ("RSA".equals(kty)) {
      canonical.put("e", (String) jwk.get("e"));
      canonical.put("kty", "RSA");
      canonical.put("n", (String) jwk.get("n"));
    }
    String json = MAPPER.writeValueAsString(canonical);
    byte[] digest = MessageDigest.getInstance("SHA-256").digest(json.getBytes(StandardCharsets.UTF_8));
    return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
  }

  /** Build a raw JWT (no validation) with a cnf.jkt claim. */
  private String buildRawJwtWithJkt(String jkt) throws Exception {
    Map<String, Object> cnf = new HashMap<>();
    cnf.put("jkt", jkt);
    Map<String, Object> payload = new HashMap<>();
    payload.put("sub", "user123");
    payload.put("cnf", cnf);
    payload.put("exp", System.currentTimeMillis() / 1000L + 3600);
    return buildRawJwt(payload);
  }

  /** Build a raw JWT with given payload (dummy header+signature). */
  private String buildRawJwt(Map<String, Object> payload) {
    try {
      Map<String, String> header = new HashMap<>();
      header.put("alg", "RS256");
      header.put("typ", "JWT");
      String h = Base64.getUrlEncoder().withoutPadding()
          .encodeToString(MAPPER.writeValueAsBytes(header));
      String p = Base64.getUrlEncoder().withoutPadding()
          .encodeToString(MAPPER.writeValueAsBytes(payload));
      return h + "." + p + ".fakesig";
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Builds a DPoP proof JWT signed with an EC private key (ES256).
   * Uses raw R||S signature format as required by JWT.
   */
  private String buildDPoPProof(String alg, Map<String, Object> jwk, String htm, String htu,
      String ath, ECPrivateKey priv) throws Exception {
    Map<String, Object> header = new HashMap<>();
    header.put("typ", "dpop+jwt");
    header.put("alg", alg);
    header.put("jwk", jwk);

    long now = System.currentTimeMillis() / 1000L;
    Map<String, Object> payload = new HashMap<>();
    payload.put("jti", UUID.randomUUID().toString());
    payload.put("htm", htm);
    payload.put("htu", htu);
    payload.put("iat", now);
    payload.put("ath", ath);

    String h = Base64.getUrlEncoder().withoutPadding()
        .encodeToString(MAPPER.writeValueAsBytes(header));
    String p = Base64.getUrlEncoder().withoutPadding()
        .encodeToString(MAPPER.writeValueAsBytes(payload));
    String signingInput = h + "." + p;

    // Sign with Java (produces DER), then convert to raw R||S
    Signature sig = Signature.getInstance("SHA256withECDSA");
    sig.initSign(priv);
    sig.update(signingInput.getBytes(StandardCharsets.UTF_8));
    byte[] derSig = sig.sign();
    byte[] rawSig = derToRaw(derSig, 32);

    String s = Base64.getUrlEncoder().withoutPadding().encodeToString(rawSig);
    return signingInput + "." + s;
  }

  /**
   * Builds a DPoP proof JWT signed with an RSA private key (RS256).
   */
  private String buildDPoPProofRSA(String alg, Map<String, Object> jwk, String htm, String htu,
      String ath, RSAPrivateKey priv) throws Exception {
    Map<String, Object> header = new HashMap<>();
    header.put("typ", "dpop+jwt");
    header.put("alg", alg);
    header.put("jwk", jwk);

    long now = System.currentTimeMillis() / 1000L;
    Map<String, Object> payload = new HashMap<>();
    payload.put("jti", UUID.randomUUID().toString());
    payload.put("htm", htm);
    payload.put("htu", htu);
    payload.put("iat", now);
    payload.put("ath", ath);

    String h = Base64.getUrlEncoder().withoutPadding()
        .encodeToString(MAPPER.writeValueAsBytes(header));
    String p = Base64.getUrlEncoder().withoutPadding()
        .encodeToString(MAPPER.writeValueAsBytes(payload));
    String signingInput = h + "." + p;

    Signature sig = Signature.getInstance("SHA256withRSA");
    sig.initSign(priv);
    sig.update(signingInput.getBytes(StandardCharsets.UTF_8));
    byte[] rawSig = sig.sign();

    String s = Base64.getUrlEncoder().withoutPadding().encodeToString(rawSig);
    return signingInput + "." + s;
  }

  /**
   * Converts a DER-encoded ECDSA signature to raw R||S format.
   */
  private byte[] derToRaw(byte[] der, int componentLen) {
    // DER: 0x30 len 0x02 rLen r 0x02 sLen s
    int pos = 2; // skip SEQUENCE tag and length
    if ((der[1] & 0xFF) == 0x81) pos = 3; // long form length
    pos++; // skip INTEGER tag
    int rLen = der[pos++] & 0xFF;
    byte[] r = java.util.Arrays.copyOfRange(der, pos, pos + rLen);
    pos += rLen;
    pos++; // skip INTEGER tag
    int sLen = der[pos++] & 0xFF;
    byte[] s = java.util.Arrays.copyOfRange(der, pos, pos + sLen);

    byte[] raw = new byte[2 * componentLen];
    copyToFixed(r, raw, 0, componentLen);
    copyToFixed(s, raw, componentLen, componentLen);
    return raw;
  }

  private void copyToFixed(byte[] src, byte[] dst, int dstOffset, int len) {
    // src may have a leading 0x00 sign byte; skip it
    int srcStart = 0;
    while (srcStart < src.length - 1 && src[srcStart] == 0x00) {
      srcStart++;
    }
    int srcLen = src.length - srcStart;
    int dstStart = dstOffset + len - srcLen;
    System.arraycopy(src, srcStart, dst, dstStart, srcLen);
  }
}
