package com.descope.sdk.auth.impl;

import static com.descope.literals.Routes.AuthEndPoints.GET_KEYS_LINK;
import static com.descope.utils.UriUtils.addPath;

import com.descope.exception.ServerCommonException;
import com.descope.model.client.SdkInfo;
import com.descope.model.jwt.SigningKey;
import com.descope.model.jwt.response.SigningKeysResponse;
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

@UtilityClass
public class KeyProvider {

  public static Key getKey(String projectId, String url, SdkInfo sdkInfo) {
    var apiProxy = ApiProxyBuilder.buildProxy(() -> "Bearer " + projectId, sdkInfo);
    var uri = addPath(UriUtils.getUri(url, GET_KEYS_LINK), projectId);
    var signingKeys = apiProxy.get(uri, SigningKeysResponse.class);

    if (signingKeys == null || signingKeys.getKeys() == null || signingKeys.getKeys().size() < 1) {
      throw ServerCommonException.invalidSigningKey("No keys were found in the response");
    }
    // Will have rotating keys
    var signingKey = signingKeys.getKeys().get(0);
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
