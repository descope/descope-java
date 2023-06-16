package com.descope.sdk.auth.impl;

import static com.descope.literals.Routes.AuthEndPoints.GET_KEYS_LINK;
import static com.descope.utils.UriUtils.addPath;

import com.descope.model.jwt.SigningKey;
import com.descope.proxy.impl.ApiProxyBuilder;
import com.descope.utils.UriUtils;
import java.math.BigInteger;
import java.security.Key;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.ArrayUtils;

@UtilityClass
public class KeyProvider {

  public static Key getKey(String projectId, String url) {
    var apiProxy = ApiProxyBuilder.buildProxy(() -> "Bearer " + projectId);
    var uri = addPath(UriUtils.getUri(url, GET_KEYS_LINK), projectId);
    var signingKeys = apiProxy.get(uri, SigningKey[].class);

    if (ArrayUtils.isEmpty(signingKeys)) {
      // TODO - Throw valid Exception | 18/04/23 | by keshavram
      throw new RuntimeException();
    }

    // TODO - Understand the concept | 18/04/23 | by keshavram
    // Will have rotating keys
    var signingKey = signingKeys[0];
    return getPublicKey(signingKey);
  }

  @SneakyThrows
  // https://mojoauth.com/blog/jwt-validation-with-jwks-java/
  private static PublicKey getPublicKey(SigningKey signingKey) {
    byte[] exponentB = Base64.getUrlDecoder().decode(signingKey.getE());
    byte[] modulusB = Base64.getUrlDecoder().decode(signingKey.getN());
    BigInteger bigExponent = new BigInteger(1, exponentB);
    BigInteger bigModulus = new BigInteger(1, modulusB);

    return KeyFactory.getInstance(signingKey.getKty())
        .generatePublic(new RSAPublicKeySpec(bigModulus, bigExponent));
  }
}
