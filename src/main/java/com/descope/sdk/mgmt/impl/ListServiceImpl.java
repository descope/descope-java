package com.descope.sdk.mgmt.impl;

import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_LIST_CLEAR_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_LIST_CREATE_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_LIST_DELETE_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_LIST_IMPORT_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_LIST_IP_ADD_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_LIST_IP_CHECK_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_LIST_IP_REMOVE_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_LIST_LOAD_ALL_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_LIST_LOAD_BY_NAME_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_LIST_LOAD_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_LIST_TEXT_ADD_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_LIST_TEXT_CHECK_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_LIST_TEXT_REMOVE_LINK;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_LIST_UPDATE_LINK;
import static com.descope.utils.CollectionUtils.addIfNotNull;
import static com.descope.utils.CollectionUtils.mapOf;

import com.descope.exception.DescopeException;
import com.descope.exception.ServerCommonException;
import com.descope.model.client.Client;
import com.descope.model.list.DescopeList;
import com.descope.model.list.ListCheckResponse;
import com.descope.model.list.ListRequest;
import com.descope.model.list.ListResponse;
import com.descope.model.list.ListsResponse;
import com.descope.proxy.ApiProxy;
import com.descope.sdk.mgmt.ListService;
import java.net.URI;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.SneakyThrows;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

class ListServiceImpl extends ManagementsBase implements ListService {

  ListServiceImpl(Client client) {
    super(client);
  }

  @Override
  public DescopeList create(ListRequest request) throws DescopeException {
    if (request == null) {
      throw ServerCommonException.invalidArgument("request");
    }
    if (StringUtils.isBlank(request.getName())) {
      throw ServerCommonException.invalidArgument("request.name");
    }
    ApiProxy apiProxy = getApiProxy();
    return toList(apiProxy.post(getUri(MANAGEMENT_LIST_CREATE_LINK), request, ListResponse.class));
  }

  @Override
  public DescopeList update(String id, ListRequest request) throws DescopeException {
    if (StringUtils.isBlank(id)) {
      throw ServerCommonException.invalidArgument("id");
    }
    if (request == null) {
      throw ServerCommonException.invalidArgument("request");
    }
    Map<String, Object> req = new HashMap<>();
    req.put("id", id);
    addIfNotNull(req, "name", request.getName());
    addIfNotNull(req, "description", request.getDescription());
    addIfNotNull(req, "type", request.getType());
    addIfNotNull(req, "data", request.getData());
    ApiProxy apiProxy = getApiProxy();
    return toList(apiProxy.post(getUri(MANAGEMENT_LIST_UPDATE_LINK), req, ListResponse.class));
  }

  @Override
  public void delete(String id) throws DescopeException {
    if (StringUtils.isBlank(id)) {
      throw ServerCommonException.invalidArgument("id");
    }
    ApiProxy apiProxy = getApiProxy();
    apiProxy.post(getUri(MANAGEMENT_LIST_DELETE_LINK), mapOf("id", id), Void.class);
  }

  @Override
  public DescopeList load(String id) throws DescopeException {
    if (StringUtils.isBlank(id)) {
      throw ServerCommonException.invalidArgument("id");
    }
    ApiProxy apiProxy = getApiProxy();
    return toList(apiProxy.get(pathUri(MANAGEMENT_LIST_LOAD_LINK, id), ListResponse.class));
  }

  @Override
  public DescopeList loadByName(String name) throws DescopeException {
    if (StringUtils.isBlank(name)) {
      throw ServerCommonException.invalidArgument("name");
    }
    ApiProxy apiProxy = getApiProxy();
    return toList(apiProxy.get(pathUri(MANAGEMENT_LIST_LOAD_BY_NAME_LINK, name), ListResponse.class));
  }

  @Override
  public List<DescopeList> loadAll() throws DescopeException {
    ApiProxy apiProxy = getApiProxy();
    ListsResponse resp = apiProxy.get(getUri(MANAGEMENT_LIST_LOAD_ALL_LINK), ListsResponse.class);
    if (resp == null || resp.getLists() == null) {
      return Collections.emptyList();
    }
    return resp.getLists();
  }

  @Override
  public void importLists(List<DescopeList> lists) throws DescopeException {
    Map<String, Object> req = new HashMap<>();
    addIfNotNull(req, "lists", lists);
    ApiProxy apiProxy = getApiProxy();
    apiProxy.post(getUri(MANAGEMENT_LIST_IMPORT_LINK), req, Void.class);
  }

  @Override
  public void clear(String id) throws DescopeException {
    if (StringUtils.isBlank(id)) {
      throw ServerCommonException.invalidArgument("id");
    }
    ApiProxy apiProxy = getApiProxy();
    apiProxy.post(getUri(MANAGEMENT_LIST_CLEAR_LINK), mapOf("id", id), Void.class);
  }

  @Override
  public void addIPs(String id, List<String> ips) throws DescopeException {
    postValues(MANAGEMENT_LIST_IP_ADD_LINK, id, "ips", ips);
  }

  @Override
  public void removeIPs(String id, List<String> ips) throws DescopeException {
    postValues(MANAGEMENT_LIST_IP_REMOVE_LINK, id, "ips", ips);
  }

  @Override
  public boolean checkIP(String id, String ip) throws DescopeException {
    if (StringUtils.isBlank(id)) {
      throw ServerCommonException.invalidArgument("id");
    }
    if (StringUtils.isBlank(ip)) {
      throw ServerCommonException.invalidArgument("ip");
    }
    ApiProxy apiProxy = getApiProxy();
    return exists(apiProxy.post(getUri(MANAGEMENT_LIST_IP_CHECK_LINK), mapOf("id", id, "ip", ip),
        ListCheckResponse.class));
  }

  @Override
  public void addTexts(String id, List<String> texts) throws DescopeException {
    postValues(MANAGEMENT_LIST_TEXT_ADD_LINK, id, "texts", texts);
  }

  @Override
  public void removeTexts(String id, List<String> texts) throws DescopeException {
    postValues(MANAGEMENT_LIST_TEXT_REMOVE_LINK, id, "texts", texts);
  }

  @Override
  public boolean checkText(String id, String text) throws DescopeException {
    if (StringUtils.isBlank(id)) {
      throw ServerCommonException.invalidArgument("id");
    }
    Map<String, Object> req = new HashMap<>();
    req.put("id", id);
    addIfNotNull(req, "text", text);
    ApiProxy apiProxy = getApiProxy();
    return exists(apiProxy.post(getUri(MANAGEMENT_LIST_TEXT_CHECK_LINK), req, ListCheckResponse.class));
  }

  @Override
  public boolean checkTexts(String id, List<String> texts, boolean matchAll) throws DescopeException {
    if (StringUtils.isBlank(id)) {
      throw ServerCommonException.invalidArgument("id");
    }
    Map<String, Object> req = new HashMap<>();
    req.put("id", id);
    addIfNotNull(req, "texts", texts);
    req.put("matchAll", matchAll);
    ApiProxy apiProxy = getApiProxy();
    return exists(apiProxy.post(getUri(MANAGEMENT_LIST_TEXT_CHECK_LINK), req, ListCheckResponse.class));
  }

  private void postValues(String path, String id, String key, List<String> values) {
    if (StringUtils.isBlank(id)) {
      throw ServerCommonException.invalidArgument("id");
    }
    if (CollectionUtils.isEmpty(values)) {
      throw ServerCommonException.invalidArgument(key);
    }
    ApiProxy apiProxy = getApiProxy();
    apiProxy.post(getUri(path), mapOf("id", id, key, values), Void.class);
  }

  // Path-escaped, so names with spaces or slashes stay one segment
  @SneakyThrows
  private URI pathUri(String path, String param) {
    return getUri(path + "/" + URLEncoder.encode(param, "UTF-8").replace("+", "%20"));
  }

  private static DescopeList toList(ListResponse resp) {
    return resp == null ? null : resp.getList();
  }

  private static boolean exists(ListCheckResponse resp) {
    return resp != null && Boolean.TRUE.equals(resp.getExists());
  }
}
