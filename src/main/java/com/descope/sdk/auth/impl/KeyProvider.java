package com.descope.sdk.auth.impl;

import static com.descope.literals.Routes.AuthEndPoints.GET_KEYS_LINK;
import static com.descope.utils.UriUtils.addPath;

import com.descope.exception.ServerCommonException;
import com.descope.model.client.SdkInfo;
import com.descope.model.jwt.SigningKey;
import com.descope.model.jwt.response.SigningKeysResponse;
import com.descope.proxy.ApiProxy;
import com.descope.proxy.impl.ApiProxyBuilder;
import com.descope.utils.UriUtils;
import java.math.BigInteger;
import java.net.URI;
import java.security.Key;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

@UtilityClass
public class KeyProvider {

  public static Map<String, Key> getKeys(String projectId, String url, SdkInfo sdkInfo) {
    ApiProxy apiProxy = ApiProxyBuilder.buildProxy(() -> "Bearer " + projectId, sdkInfo);
    URI uri = addPath(UriUtils.getUri(url, GET_KEYS_LINK), projectId);
    SigningKeysResponse signingKeys = apiProxy.get(uri, SigningKeysResponse.class);

    if (signingKeys == null || signingKeys.getKeys() == null || signingKeys.getKeys().size() < 1) {
      throw ServerCommonException.invalidSigningKey("No keys were found in the response");
    }
    Map<String, Key> keys = new HashMap<>();
    for (SigningKey sk : signingKeys.getKeys()) {
      keys.put(sk.getKid(), getPublicKey(sk));
    }
    return keys;
  }

  @SneakyThrows
  // https://mojoauth.com/blog/jwt-validation-with-jwks-java/
  public static PublicKey getPublicKey(SigningKey signingKey) {
    byte[] exponentB = Base64.getUrlDecoder().decode(signingKey.getE());
    byte[] modulusB = Base64.getUrlDecoder().decode(signingKey.getN());
    BigInteger bigExponent = new BigInteger(1, exponentB);
    BigInteger bigModulus = new BigInteger(1, modulusB);

    return KeyFactory.getInstance(signingKey.getKty())
        .generatePublic(new RSAPublicKeySpec(bigModulus, bigExponent));
  }
}
