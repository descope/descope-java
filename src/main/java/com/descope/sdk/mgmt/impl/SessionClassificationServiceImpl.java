package com.descope.sdk.mgmt.impl;

import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_SESSION_CLASSIFICATION_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_SESSION_CLASSIFICATION_LOAD_ALL_LINK;
import static com.descope.utils.CollectionUtils.mapOf;

import com.descope.exception.DescopeException;
import com.descope.exception.ServerCommonException;
import com.descope.model.client.Client;
import com.descope.model.sessionclassification.LoadAllSessionClassificationsResponse;
import com.descope.model.sessionclassification.SessionClassification;
import com.descope.proxy.ApiProxy;
import com.descope.sdk.mgmt.SessionClassificationService;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

class SessionClassificationServiceImpl extends ManagementsBase implements SessionClassificationService {

  SessionClassificationServiceImpl(Client client) {
    super(client);
  }

  @Override
  public SessionClassification load(String name) throws DescopeException {
    if (StringUtils.isBlank(name)) {
      throw ServerCommonException.invalidArgument("name");
    }
    ApiProxy apiProxy = getApiProxy();
    return apiProxy.get(getQueryParamUri(MANAGEMENT_SESSION_CLASSIFICATION_LINK, mapOf("name", name)),
        SessionClassification.class);
  }

  @Override
  public SessionClassification set(SessionClassification classification) throws DescopeException {
    if (classification == null) {
      throw ServerCommonException.invalidArgument("classification");
    }
    if (StringUtils.isBlank(classification.getName())) {
      throw ServerCommonException.invalidArgument("classification.name");
    }
    ApiProxy apiProxy = getApiProxy();
    return apiProxy.post(getUri(MANAGEMENT_SESSION_CLASSIFICATION_LINK), classification,
        SessionClassification.class);
  }

  @Override
  public void delete(String name) throws DescopeException {
    if (StringUtils.isBlank(name)) {
      throw ServerCommonException.invalidArgument("name");
    }
    ApiProxy apiProxy = getApiProxy();
    apiProxy.delete(getQueryParamUri(MANAGEMENT_SESSION_CLASSIFICATION_LINK, mapOf("name", name)), null,
        Void.class);
  }

  @Override
  public List<SessionClassification> loadAll() throws DescopeException {
    ApiProxy apiProxy = getApiProxy();
    LoadAllSessionClassificationsResponse response = apiProxy.get(
        getUri(MANAGEMENT_SESSION_CLASSIFICATION_LOAD_ALL_LINK), LoadAllSessionClassificationsResponse.class);
    return response.getClassifications();
  }
}
