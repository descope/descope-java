package com.descope.sdk.mgmt.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.descope.exception.RateLimitExceededException;
import com.descope.exception.ServerCommonException;
import com.descope.model.authz.Condition;
import com.descope.model.authz.ConditionParam;
import com.descope.model.authz.HasRelationsResponse;
import com.descope.model.authz.LoadSchemaResponse;
import com.descope.model.authz.Modified;
import com.descope.model.authz.Namespace;
import com.descope.model.authz.Relation;
import com.descope.model.authz.RelationDefinition;
import com.descope.model.authz.RelationQuery;
import com.descope.model.authz.RelationsResponse;
import com.descope.model.authz.ResourcesResponse;
import com.descope.model.authz.Schema;
import com.descope.model.authz.WhoCanAccessResponse;
import com.descope.model.client.Client;
import com.descope.model.mgmt.ManagementServices;
import com.descope.proxy.ApiProxy;
import com.descope.proxy.impl.ApiProxyBuilder;
import com.descope.sdk.TestUtils;
import com.descope.sdk.mgmt.AuthzService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.io.File;
import java.net.URI;
import java.time.Instant;
import java.time.Period;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.RetryingTest;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

public class AuthzServiceImplTest {
  private AuthzService authzService;

  @BeforeEach
  void setUp() {
    Client client = TestUtils.getClient();
    ManagementServices mgmtServices = ManagementServiceBuilder.buildServices(client);
    this.authzService = mgmtServices.getAuthzService();
  }

  @Test
  void testSaveSchemaForNoSchema() {
    ServerCommonException thrown =
        assertThrows(
            ServerCommonException.class,
            () -> authzService.saveSchema(null, false));
    assertNotNull(thrown);
    assertEquals("The schema argument is invalid", thrown.getMessage());
  }

  @Test
  void testSaveSchemaForEmptySchema() {
    ServerCommonException thrown =
        assertThrows(
            ServerCommonException.class,
            () -> authzService.saveSchema(new Schema(), false));
    assertNotNull(thrown);
    assertEquals("The schema argument is invalid", thrown.getMessage());
  }

  @Test
  void testSaveSchemaForSuccess() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(null).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
        () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      authzService.saveSchema(Schema.builder().namespaces(Arrays.asList(new Namespace())).build(), false);
    }
  }

  @Test
  void testDeleteSchemaForSuccess() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(null).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
        () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      authzService.deleteSchema();
    }
  }

  @Test
  void testLoadSchemaForSuccess() {
    LoadSchemaResponse schemaResponse = new LoadSchemaResponse(new Schema("kuku", null));
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(schemaResponse).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
        () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      Schema schema = authzService.loadSchema();
      assertNotNull(schema);
      assertEquals("kuku", schema.getName());
    }
  }

  @Test
  void testSaveNamespaceForInvalidNamespace() {
    ServerCommonException thrown =
        assertThrows(
            ServerCommonException.class,
            () -> authzService.saveNamespace(null, null, null));
    assertNotNull(thrown);
    assertEquals("The namespace argument is invalid", thrown.getMessage());
    thrown =
        assertThrows(
            ServerCommonException.class,
            () -> authzService.saveNamespace(new Namespace(), null, null));
    assertNotNull(thrown);
    assertEquals("The namespace argument is invalid", thrown.getMessage());
    thrown =
        assertThrows(
            ServerCommonException.class,
            () -> authzService.saveNamespace(Namespace.builder().name("kuku").build(), null, null));
    assertNotNull(thrown);
    assertEquals("The namespace argument is invalid", thrown.getMessage());
  }

  @Test
  void testSaveNamespaceForSuccess() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(null).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
        () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      authzService.saveNamespace(new Namespace("kuku", Arrays.asList(new RelationDefinition())), null, null);
    }
  }

  @Test
  void testDeleteNamespaceForNoName() {
    ServerCommonException thrown =
        assertThrows(
            ServerCommonException.class,
            () -> authzService.deleteNamespace(null, null));
    assertNotNull(thrown);
    assertEquals("The name argument is invalid", thrown.getMessage());
  }

  @Test
  void testDeleteNamespaceForSuccess() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(null).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
        () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      authzService.deleteNamespace("kuku", null);
    }
  }

  @Test
  void testSaveRelationDefinitionForInvalidRD() {
    ServerCommonException thrown =
        assertThrows(
            ServerCommonException.class,
            () -> authzService.saveRelationDefinition(null, null, null, null));
    assertNotNull(thrown);
    assertEquals("The relationDefinition argument is invalid", thrown.getMessage());
    thrown =
        assertThrows(
            ServerCommonException.class,
            () -> authzService.saveRelationDefinition(new RelationDefinition(), null, null, null));
    assertNotNull(thrown);
    assertEquals("The relationDefinition argument is invalid", thrown.getMessage());
    thrown =
        assertThrows(
            ServerCommonException.class,
            () -> authzService.saveRelationDefinition(RelationDefinition.builder().name("kuku").build(),
              null, null, null));
    assertNotNull(thrown);
    assertEquals("The namespace argument is invalid", thrown.getMessage());
  }

  @Test
  void testSaveRelationDefinitionForSuccess() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(null).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
        () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      authzService.saveRelationDefinition(new RelationDefinition("kuku", null), "kiki", null, null);
    }
  }

  @Test
  void testDeleteRelationDefinitionForNoName() {
    ServerCommonException thrown =
        assertThrows(
            ServerCommonException.class,
            () -> authzService.deleteRelationDefinition(null, null, null));
    assertNotNull(thrown);
    assertEquals("The name argument is invalid", thrown.getMessage());
    thrown =
        assertThrows(
            ServerCommonException.class,
            () -> authzService.deleteRelationDefinition("kuku", null, null));
    assertNotNull(thrown);
    assertEquals("The namespace argument is invalid", thrown.getMessage());
  }

  @Test
  void testDeleteRelationDefinitionForSuccess() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(null).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
        () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      authzService.deleteRelationDefinition("kuku", "kiki", null);
    }
  }

  @Test
  void testCreateRelationsForNoRelations() {
    ServerCommonException thrown =
        assertThrows(
            ServerCommonException.class,
            () -> authzService.createRelations(null));
    assertNotNull(thrown);
    assertEquals("The relations argument is invalid", thrown.getMessage());
  }

  @Test
  void testCreateRelationsForSuccess() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(null).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
        () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      authzService.createRelations(Arrays.asList(new Relation()));
    }
  }

  @Test
  void testDeleteRelationsForNoRelations() {
    ServerCommonException thrown =
        assertThrows(
            ServerCommonException.class,
            () -> authzService.deleteRelations(null));
    assertNotNull(thrown);
    assertEquals("The relations argument is invalid", thrown.getMessage());
  }

  @Test
  void testDeleteRelationsForSuccess() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(null).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
        () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      authzService.deleteRelations(Arrays.asList(new Relation()));
    }
  }

  @Test
  void testDeleteRelationsForResourcesForNoResources() {
    ServerCommonException thrown =
        assertThrows(
            ServerCommonException.class,
            () -> authzService.deleteRelationsForResources(null));
    assertNotNull(thrown);
    assertEquals("The resources argument is invalid", thrown.getMessage());
  }

  @Test
  void testDeleteRelationsForResourcesForSuccess() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(null).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
        () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      authzService.deleteRelationsForResources(Arrays.asList("kuku"));
    }
  }

  @Test
  void testHasRelationsForNoQueries() {
    ServerCommonException thrown =
        assertThrows(
            ServerCommonException.class,
            () -> authzService.hasRelations(null));
    assertNotNull(thrown);
    assertEquals("The relationQueries argument is invalid", thrown.getMessage());
  }

  @Test
  void testHasRelationsForSuccess() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(new HasRelationsResponse(Arrays.asList(new RelationQuery()))).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
        () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      authzService.hasRelations(Arrays.asList(new RelationQuery()));
    }
  }

  @Test
  void testWhoCanAccessForInvalidInputs() {
    ServerCommonException thrown =
        assertThrows(
            ServerCommonException.class,
            () -> authzService.whoCanAccess(null, null, null));
    assertNotNull(thrown);
    assertEquals("The resource argument is invalid", thrown.getMessage());
    thrown =
        assertThrows(
            ServerCommonException.class,
            () -> authzService.whoCanAccess("kuku", null, null));
    assertNotNull(thrown);
    assertEquals("The relationDefinition argument is invalid", thrown.getMessage());
    thrown =
        assertThrows(
            ServerCommonException.class,
            () -> authzService.whoCanAccess("kuku", "kiki", null));
    assertNotNull(thrown);
    assertEquals("The namespace argument is invalid", thrown.getMessage());
  }

  @Test
  void testWhoCanAccessForSuccess() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(new WhoCanAccessResponse(Arrays.asList("kuku"))).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
        () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      authzService.whoCanAccess("kiki", "kuku", "kaka");
    }
  }

  @SuppressWarnings("unchecked")
  @Test
  void testWhoCanAccessWithContext() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(new WhoCanAccessResponse(Arrays.asList("kuku"))).when(apiProxy).post(any(), any(), any());
    Map<String, Object> context = new HashMap<>();
    context.put("role", "admin");
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
        () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      authzService.whoCanAccess("kiki", "kuku", "kaka", context);

      ArgumentCaptor<Map<String, Object>> bodyCaptor = ArgumentCaptor.forClass(Map.class);
      verify(apiProxy).post(any(), bodyCaptor.capture(), any());
      assertEquals(context, bodyCaptor.getValue().get("context"));
    }
  }

  @SuppressWarnings("unchecked")
  @Test
  void testWhoCanAccessWithEmptyContext() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(new WhoCanAccessResponse(Arrays.asList("kuku"))).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
        () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      authzService.whoCanAccess("kiki", "kuku", "kaka", new HashMap<>());

      ArgumentCaptor<Map<String, Object>> bodyCaptor = ArgumentCaptor.forClass(Map.class);
      verify(apiProxy).post(any(), bodyCaptor.capture(), any());
      assertFalse(bodyCaptor.getValue().containsKey("context"));
    }
  }

  @Test
  void testResourceRelationsForNoResource() {
    ServerCommonException thrown =
        assertThrows(
            ServerCommonException.class,
            () -> authzService.resourceRelations(null));
    assertNotNull(thrown);
    assertEquals("The resource argument is invalid", thrown.getMessage());
  }

  @Test
  void testResourceRelationsForSuccess() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(new RelationsResponse(Arrays.asList(new Relation()))).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
        () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      authzService.resourceRelations("kiki");
    }
  }

  @Test
  void testUsersRelationsForNoUsers() {
    ServerCommonException thrown =
        assertThrows(
            ServerCommonException.class,
            () -> authzService.targetsRelations(null));
    assertNotNull(thrown);
    assertEquals("The targets argument is invalid", thrown.getMessage());
  }

  @Test
  void testUsersRelationsForSuccess() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(new RelationsResponse(Arrays.asList(new Relation()))).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
        () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      authzService.targetsRelations(Arrays.asList("kiki"));
    }
  }

  @Test
  void testWhatCanTargetAccessForNoTarget() {
    ServerCommonException thrown =
        assertThrows(
            ServerCommonException.class,
            () -> authzService.whatCanTargetAccess(null));
    assertNotNull(thrown);
    assertEquals("The target argument is invalid", thrown.getMessage());
  }

  @Test
  void testWhatCanTargetAccessForSuccess() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(new RelationsResponse(Arrays.asList(new Relation()))).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
        () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      authzService.whatCanTargetAccess("kiki");
    }
  }

  @Test
  void testFgaCacheRouting() {
    Client client = Client.builder().uri("https://api.descope.com").fgaCacheUri("https://cache.example.com/")
        .projectId("someProjectId").managementKey("someManagementKey").build();
    AuthzService cachedAuthzService = ManagementServiceBuilder.buildServices(client).getAuthzService();
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(new RelationsResponse(Arrays.asList(new Relation()))).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
        () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      cachedAuthzService.whatCanTargetAccess("kiki");
      cachedAuthzService.resourceRelations("kuku");

      ArgumentCaptor<URI> uriCaptor = ArgumentCaptor.forClass(URI.class);
      verify(apiProxy, times(2)).post(uriCaptor.capture(), any(), any());
      assertEquals("https://cache.example.com/v1/mgmt/authz/re/targetall",
          uriCaptor.getAllValues().get(0).toString());
      assertEquals("https://api.descope.com/v1/mgmt/authz/re/resource",
          uriCaptor.getAllValues().get(1).toString());
    }
  }

  @SuppressWarnings("unchecked")
  @Test
  void testWhatCanTargetAccessWithContext() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(new RelationsResponse(Arrays.asList(new Relation()))).when(apiProxy).post(any(), any(), any());
    Map<String, Object> context = new HashMap<>();
    context.put("role", "admin");
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
        () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      authzService.whatCanTargetAccess("kiki", context);

      ArgumentCaptor<Map<String, Object>> bodyCaptor = ArgumentCaptor.forClass(Map.class);
      verify(apiProxy).post(any(), bodyCaptor.capture(), any());
      assertEquals(context, bodyCaptor.getValue().get("context"));
    }
  }

  @Test
  void testWhatCanTargetAccessWithRelationForNoTarget() {
    ServerCommonException thrown =
        assertThrows(
            ServerCommonException.class,
            () -> authzService.whatCanTargetAccessWithRelation(null, null, null));
    assertNotNull(thrown);
    assertEquals("The target argument is invalid", thrown.getMessage());
    thrown =
        assertThrows(
            ServerCommonException.class,
            () -> authzService.whatCanTargetAccessWithRelation("t", null, null));
    assertNotNull(thrown);
    assertEquals("The relationDefinition argument is invalid", thrown.getMessage());
    thrown =
        assertThrows(
            ServerCommonException.class,
            () -> authzService.whatCanTargetAccessWithRelation("t", "rd", null));
    assertNotNull(thrown);
    assertEquals("The namespace argument is invalid", thrown.getMessage());
  }

  @Test
  void testWhatCanTargetAccessWithRelationForSuccess() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(new ResourcesResponse(Arrays.asList(""))).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
        () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      authzService.whatCanTargetAccessWithRelation("kiki", "rd", "ns");
    }
  }

  @Test
  void testGetModifiedForWrongSince() {
    ServerCommonException thrown =
        assertThrows(
            ServerCommonException.class,
            () -> authzService.getModified(Instant.now().minus(Period.ofDays(2))));
    assertNotNull(thrown);
    assertEquals("The since argument is invalid", thrown.getMessage());
  }

  @Test
  void testGetModifiedForSuccess() {
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(new Modified(null, null, true)).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
        () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      Modified modified = authzService.getModified(null);
      assertTrue(modified.isSchemaChanged());
    }
  }

  @Test
  void testHasRelationsWithDirectRelationsForNoQueries() {
    ServerCommonException thrown =
        assertThrows(
            ServerCommonException.class,
            () -> authzService.hasRelationsWithDirectRelations(null));
    assertEquals("The relationQueries argument is invalid", thrown.getMessage());
  }

  @Test
  void testHasRelationsWithDirectRelationsForSuccess() {
    RelationQuery query = new RelationQuery("doc1", "owner", "doc", "u1", false);
    HasRelationsResponse response = new HasRelationsResponse(
        Arrays.asList(new RelationQuery("doc1", "owner", "doc", "u1", true)), Arrays.asList(true));
    HasRelationsResponse[] result = new HasRelationsResponse[1];
    Map<String, Object> body = runAndCapture(response, "/v1/mgmt/authz/re/has",
        s -> result[0] = s.hasRelationsWithDirectRelations(Arrays.asList(query)));
    assertEquals(Arrays.asList(query), body.get("relationQueries"));
    assertTrue(result[0].getRelationQueries().get(0).isHasRelation());
    assertEquals(Arrays.asList(true), result[0].getDirectRelations());
  }

  @Test
  void testWhoCanAccessWithTargetTypeForInvalidInputs() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class,
        () -> authzService.whoCanAccess(null, "rd", "ns", null, "user"));
    assertEquals("The resource argument is invalid", thrown.getMessage());
    thrown = assertThrows(ServerCommonException.class,
        () -> authzService.whoCanAccess("r", null, "ns", null, "user"));
    assertEquals("The relationDefinition argument is invalid", thrown.getMessage());
    thrown = assertThrows(ServerCommonException.class,
        () -> authzService.whoCanAccess("r", "rd", null, null, "user"));
    assertEquals("The namespace argument is invalid", thrown.getMessage());
  }

  @Test
  void testWhoCanAccessWithTargetType() {
    Map<String, Object> context = new HashMap<>();
    context.put("role", "admin");
    List<?>[] result = new List<?>[1];
    Map<String, Object> body = runAndCapture(new WhoCanAccessResponse(Arrays.asList("u1")),
        "/v1/mgmt/authz/re/who", s -> result[0] = s.whoCanAccess("doc1", "viewer", "doc", context, "user"));
    assertEquals("doc1", body.get("resource"));
    assertEquals("viewer", body.get("relationDefinition"));
    assertEquals("doc", body.get("namespace"));
    assertEquals(context, body.get("context"));
    assertEquals("user", body.get("targetType"));
    assertEquals(Arrays.asList("u1"), result[0]);
  }

  @Test
  void testWhoCanAccessWithoutTargetType() {
    Map<String, Object> body = runAndCapture(new WhoCanAccessResponse(Arrays.asList("u1")),
        "/v1/mgmt/authz/re/who", s -> s.whoCanAccess("doc1", "viewer", "doc", null, ""));
    assertFalse(body.containsKey("targetType"));
    assertFalse(body.containsKey("context"));
  }

  @Test
  void testResourceRelationsWithFilterForNoResource() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class,
        () -> authzService.resourceRelations(null, true));
    assertEquals("The resource argument is invalid", thrown.getMessage());
  }

  @Test
  void testResourceRelationsWithFilter() {
    List<?>[] result = new List<?>[1];
    Map<String, Object> body = runAndCapture(new RelationsResponse(Arrays.asList(fullRelation())),
        "/v1/mgmt/authz/re/resource", s -> result[0] = s.resourceRelations("doc1", true));
    assertEquals("doc1", body.get("resource"));
    assertEquals(true, body.get("ignoreTargetSetRelations"));
    assertEquals(Arrays.asList(fullRelation()), result[0]);
  }

  @Test
  void testResourceRelationsWithoutFilter() {
    Map<String, Object> body = runAndCapture(new RelationsResponse(Arrays.asList(fullRelation())),
        "/v1/mgmt/authz/re/resource", s -> s.resourceRelations("doc1"));
    assertFalse(body.containsKey("ignoreTargetSetRelations"));
  }

  @Test
  void testTargetsRelationsWithFilterForNoTargets() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class,
        () -> authzService.targetsRelations(null, true));
    assertEquals("The targets argument is invalid", thrown.getMessage());
  }

  @Test
  void testTargetsRelationsWithFilter() {
    List<?>[] result = new List<?>[1];
    Map<String, Object> body = runAndCapture(new RelationsResponse(Arrays.asList(fullRelation())),
        "/v1/mgmt/authz/re/targets", s -> result[0] = s.targetsRelations(Arrays.asList("u1"), true));
    assertEquals(Arrays.asList("u1"), body.get("targets"));
    assertEquals(true, body.get("includeTargetSetRelations"));
    assertEquals(Arrays.asList(fullRelation()), result[0]);
  }

  @Test
  void testTargetsRelationsWithoutFilter() {
    Map<String, Object> body = runAndCapture(new RelationsResponse(Arrays.asList(fullRelation())),
        "/v1/mgmt/authz/re/targets", s -> s.targetsRelations(Arrays.asList("u1")));
    assertFalse(body.containsKey("includeTargetSetRelations"));
  }

  @Test
  void testWhatCanTargetAccessWithParentForInvalidInputs() {
    ServerCommonException thrown = assertThrows(ServerCommonException.class,
        () -> authzService.whatCanTargetAccessWithRelation(null, "rd", "ns", "p", "prd", null));
    assertEquals("The target argument is invalid", thrown.getMessage());
    thrown = assertThrows(ServerCommonException.class,
        () -> authzService.whatCanTargetAccessWithRelation("t", null, "ns", "p", "prd", null));
    assertEquals("The relationDefinition argument is invalid", thrown.getMessage());
    thrown = assertThrows(ServerCommonException.class,
        () -> authzService.whatCanTargetAccessWithRelation("t", "rd", null, "p", "prd", null));
    assertEquals("The namespace argument is invalid", thrown.getMessage());
  }

  @SuppressWarnings("unchecked")
  @Test
  void testWhatCanTargetAccessWithParent() {
    Map<String, Object> context = new HashMap<>();
    context.put("role", "admin");
    List<Relation>[] result = new List[1];
    Map<String, Object> body = runAndCapture(new ResourcesResponse(Arrays.asList("doc1")),
        "/v1/mgmt/authz/re/targetwithrelation",
        s -> result[0] = s.whatCanTargetAccessWithRelation("u1", "viewer", "doc", "folder1", "parent", context));
    assertEquals("u1", body.get("target"));
    assertEquals("viewer", body.get("relationDefinition"));
    assertEquals("doc", body.get("namespace"));
    assertEquals("folder1", body.get("ParentTarget"));
    assertEquals("parent", body.get("ParentRelationDefinition"));
    assertEquals(context, body.get("context"));
    assertEquals(Arrays.asList(Relation.builder().resource("doc1").target("u1").relationDefinition("viewer")
        .namespace("doc").build()), result[0]);
  }

  @SuppressWarnings("unchecked")
  @Test
  void testWhatCanTargetAccessWithRelationForNoResources() {
    List<Relation>[] result = new List[1];
    Map<String, Object> body = runAndCapture(new ResourcesResponse(null), "/v1/mgmt/authz/re/targetwithrelation",
        s -> result[0] = s.whatCanTargetAccessWithRelation("u1", "viewer", "doc"));
    assertFalse(body.containsKey("ParentTarget"));
    assertFalse(body.containsKey("ParentRelationDefinition"));
    assertFalse(body.containsKey("context"));
    assertTrue(result[0].isEmpty());
  }

  @Test
  void testCreateRelationsWithTargetNamespace() {
    Map<String, Object> body = runAndCapture(null, "/v1/mgmt/authz/re/create",
        s -> s.createRelations(Arrays.asList(fullRelation())));
    assertEquals(Arrays.asList(fullRelation()), body.get("relations"));
  }

  @Test
  void testDeleteRelationsWithTargetNamespace() {
    Map<String, Object> body = runAndCapture(null, "/v1/mgmt/authz/re/delete",
        s -> s.deleteRelations(Arrays.asList(fullRelation())));
    assertEquals(Arrays.asList(fullRelation()), body.get("relations"));
  }

  @Test
  void testWhatCanTargetAccessWithTargetNamespace() {
    List<?>[] result = new List<?>[1];
    runAndCapture(new RelationsResponse(Arrays.asList(fullRelation())), "/v1/mgmt/authz/re/targetall",
        s -> result[0] = s.whatCanTargetAccess("u1"));
    assertEquals(Arrays.asList(fullRelation()), result[0]);
  }

  @Test
  void testSaveSchemaWithConditions() {
    Schema schema = schemaWithConditions();
    Map<String, Object> body = runAndCapture(null, "/v1/mgmt/authz/schema/save", s -> s.saveSchema(schema, true));
    assertEquals(schema, body.get("schema"));
    assertEquals(true, body.get("upgrade"));
  }

  @Test
  void testLoadSchemaWithConditions() {
    Schema[] result = new Schema[1];
    runAndCapture(new LoadSchemaResponse(schemaWithConditions()), "/v1/mgmt/authz/schema/load",
        s -> result[0] = s.loadSchema());
    assertEquals(schemaWithConditions(), result[0]);
  }

  @Test
  void testNewFieldsJson() throws Exception {
    ObjectMapper mapper = new ObjectMapper();
    String json = mapper.writeValueAsString(schemaWithConditions());
    assertThat(json).contains("\"conditions\":[{\"name\":\"DuringShift\",\"params\":[{\"name\":\"hour\","
        + "\"type\":\"int\"}],\"expression\":\"hour > 8\",\"builtin\":true,\"checkedExpr\":\"AQI=\",\"id\":1}]");
    assertThat(mapper.writeValueAsString(fullRelation())).contains("\"targetNamespace\":\"user\"");
    assertThat(mapper.writeValueAsString(new Schema("s", null))).isEqualTo("{\"name\":\"s\"}");
    assertThat(mapper.writeValueAsString(new Relation("r", "rd", "ns", "t", null, null, null, null)))
        .doesNotContain("targetNamespace");
    Schema parsed = mapper.readValue("{\"name\":\"s\",\"namespaces\":null,\"conditions\":[{\"name\":\"DuringShift\","
        + "\"params\":[{\"name\":\"hour\",\"type\":\"int\"}],\"expression\":\"hour > 8\",\"builtin\":true,"
        + "\"checkedExpr\":\"AQI=\",\"id\":1}]}", Schema.class);
    assertEquals(schemaWithConditions().getConditions(), parsed.getConditions());
    HasRelationsResponse has = mapper.readValue("{\"relationQueries\":[],\"directRelations\":[true,false]}",
        HasRelationsResponse.class);
    assertEquals(Arrays.asList(true, false), has.getDirectRelations());
    assertNull(new HasRelationsResponse(Arrays.asList(new RelationQuery())).getDirectRelations());
  }

  private static Relation fullRelation() {
    return Relation.builder().resource("doc1").relationDefinition("viewer").namespace("doc").target("u1")
        .targetNamespace("user").build();
  }

  private static Schema schemaWithConditions() {
    return Schema.builder().name("s").namespaces(Arrays.asList(new Namespace("doc", null)))
        .conditions(Arrays.asList(Condition.builder().name("DuringShift")
            .params(Arrays.asList(new ConditionParam("hour", "int"))).expression("hour > 8").builtin(true)
            .checkedExpr("AQI=").id(1).build()))
        .build();
  }

  @SuppressWarnings("unchecked")
  private static Map<String, Object> runAndCapture(Object response, String expectedPath,
      Consumer<AuthzService> call) {
    Client client = Client.builder().uri("https://api.descope.com").projectId("someProjectId")
        .managementKey("someManagementKey").build();
    AuthzService service = ManagementServiceBuilder.buildServices(client).getAuthzService();
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(response).when(apiProxy).post(any(), any(), any());
    AtomicReference<String> authHeader = new AtomicReference<>();
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenAnswer(inv -> {
        authHeader.set(((Supplier<String>) inv.getArgument(0)).get());
        return apiProxy;
      });
      call.accept(service);
      ArgumentCaptor<URI> uriCaptor = ArgumentCaptor.forClass(URI.class);
      ArgumentCaptor<Map<String, Object>> bodyCaptor = ArgumentCaptor.forClass(Map.class);
      verify(apiProxy).post(uriCaptor.capture(), bodyCaptor.capture(), any());
      assertEquals(expectedPath, uriCaptor.getValue().getPath());
      assertEquals("Bearer someProjectId:someManagementKey", authHeader.get());
      return bodyCaptor.getValue();
    }
  }

  @RetryingTest(value = 3, suspendForMs = 30000, onExceptions = RateLimitExceededException.class)
  void testFunctionalFullCycle() throws Exception {
    ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
    mapper.findAndRegisterModules();
    Schema s = mapper.readValue(new File("src/test/data/files.yaml"), Schema.class);
    Schema existingSchema = authzService.loadSchema();
    if (!existingSchema.getName().equals(s.getName())) {
      authzService.deleteSchema();
      authzService.saveSchema(s, true);
      authzService.createRelations(Arrays.asList(
        new Relation("Dev", "parent", "org", "Descope", null, null, null, null),
        new Relation("Sales", "parent", "org", "Descope", null, null, null, null),
        new Relation("Dev", "member", "org", "u1", null, null, null, null),
        new Relation("Dev", "member", "org", "u3", null, null, null, null),
        new Relation("Sales", "member", "org", "u2", null, null, null, null),
        new Relation("Presentations", "parent", "folder", "Internal", null, null, null, null),
        new Relation("roadmap.ppt", "parent", "doc", "Presentations", null, null, null, null),
        new Relation("roadmap.ppt", "owner", "doc", "u1", null, null, null, null),
        new Relation("Internal", "viewer", "folder", null, "Descope", "member", "org", null),
        new Relation("Presentations", "editor", "folder", null, "Sales", "member", "org", null)
      ));
    }
    List<RelationQuery> resp = authzService.hasRelations(Arrays.asList(
      new RelationQuery("roadmap.ppt", "owner", "doc", "u1", false),
      new RelationQuery("roadmap.ppt", "editor", "doc", "u1", false),
      new RelationQuery("roadmap.ppt", "viewer", "doc", "u1", false),
      new RelationQuery("roadmap.ppt", "viewer", "doc", "u3", false),
      new RelationQuery("roadmap.ppt", "editor", "doc", "u3", false),
      new RelationQuery("roadmap.ppt", "editor", "doc", "u2", false)
    ));
    assertTrue(resp.get(0).isHasRelation());
    assertTrue(resp.get(1).isHasRelation());
    assertTrue(resp.get(2).isHasRelation());
    assertTrue(resp.get(3).isHasRelation());
    assertFalse(resp.get(4).isHasRelation());
    assertTrue(resp.get(5).isHasRelation());
    List<String> respWho = authzService.whoCanAccess("roadmap.ppt", "editor", "doc");
    assertThat(respWho).hasSameElementsAs(Arrays.asList("u1", "u2"));
    List<Relation> respResourceRelations = authzService.resourceRelations("roadmap.ppt");
    assertThat(respResourceRelations).size().isEqualTo(2);
    List<Relation> respUsersRelations = authzService.targetsRelations(Arrays.asList("u1"));
    assertThat(respUsersRelations).size().isEqualTo(2);
    List<Relation> respWhat = authzService.whatCanTargetAccess("u1");
    assertThat(respWhat).size().isEqualTo(7);
    respWhat = authzService.whatCanTargetAccessWithRelation("u1", "viewer", "doc");
    assertThat(respWhat).size().isEqualTo(1);
  }
}
