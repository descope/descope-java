package com.descope.sdk.mgmt.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

import com.descope.exception.RateLimitExceededException;
import com.descope.exception.ServerCommonException;
import com.descope.model.authz.HasRelationsResponse;
import com.descope.model.authz.LoadSchemaResponse;
import com.descope.model.authz.Namespace;
import com.descope.model.authz.Relation;
import com.descope.model.authz.RelationDefinition;
import com.descope.model.authz.RelationQuery;
import com.descope.model.authz.RelationsResponse;
import com.descope.model.authz.Schema;
import com.descope.model.authz.WhoCanAccessResponse;
import com.descope.proxy.ApiProxy;
import com.descope.proxy.impl.ApiProxyBuilder;
import com.descope.sdk.TestUtils;
import com.descope.sdk.mgmt.AuthzService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.io.File;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.RetryingTest;
import org.mockito.MockedStatic;

public class AuthzServiceImplTest {
  private AuthzService authzService;

  @BeforeEach
  void setUp() {
    var authParams = TestUtils.getManagementParams();
    var client = TestUtils.getClient();
    var mgmtServices = ManagementServiceBuilder.buildServices(client, authParams);
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
    var apiProxy = mock(ApiProxy.class);
    doReturn(null).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
        () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      authzService.saveSchema(Schema.builder().namespaces(List.of(new Namespace())).build(), false);
    }
  }

  @Test
  void testDeleteSchemaForSuccess() {
    var apiProxy = mock(ApiProxy.class);
    doReturn(null).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
        () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      authzService.deleteSchema();
    }
  }

  @Test
  void testLoadSchemaForSuccess() {
    var schemaResponse = new LoadSchemaResponse(new Schema("kuku", null));
    var apiProxy = mock(ApiProxy.class);
    doReturn(schemaResponse).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
        () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      var schema = authzService.loadSchema();
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
    var apiProxy = mock(ApiProxy.class);
    doReturn(null).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
        () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      authzService.saveNamespace(new Namespace("kuku", List.of(new RelationDefinition())), null, null);
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
    var apiProxy = mock(ApiProxy.class);
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
    var apiProxy = mock(ApiProxy.class);
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
    var apiProxy = mock(ApiProxy.class);
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
    var apiProxy = mock(ApiProxy.class);
    doReturn(null).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
        () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      authzService.createRelations(List.of(new Relation()));
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
    var apiProxy = mock(ApiProxy.class);
    doReturn(null).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
        () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      authzService.deleteRelations(List.of(new Relation()));
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
    var apiProxy = mock(ApiProxy.class);
    doReturn(null).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
        () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      authzService.deleteRelationsForResources(List.of("kuku"));
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
    var apiProxy = mock(ApiProxy.class);
    doReturn(new HasRelationsResponse(List.of(new RelationQuery()))).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
        () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      authzService.hasRelations(List.of(new RelationQuery()));
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
    var apiProxy = mock(ApiProxy.class);
    doReturn(new WhoCanAccessResponse(List.of("kuku"))).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
        () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      authzService.whoCanAccess("kiki", "kuku", "kaka");
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
    var apiProxy = mock(ApiProxy.class);
    doReturn(new RelationsResponse(List.of(new Relation()))).when(apiProxy).post(any(), any(), any());
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
    var apiProxy = mock(ApiProxy.class);
    doReturn(new RelationsResponse(List.of(new Relation()))).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
        () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      authzService.targetsRelations(List.of("kiki"));
    }
  }

  @Test
  void testWhatCanUserAccessForNoUser() {
    ServerCommonException thrown =
        assertThrows(
            ServerCommonException.class,
            () -> authzService.whatCanTargetAccess(null));
    assertNotNull(thrown);
    assertEquals("The user argument is invalid", thrown.getMessage());
  }

  @Test
  void testWhatCanUserAccessForSuccess() {
    var apiProxy = mock(ApiProxy.class);
    doReturn(new RelationsResponse(List.of(new Relation()))).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
        () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      authzService.whatCanTargetAccess("kiki");
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
      authzService.createRelations(List.of(
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
    var resp = authzService.hasRelations(List.of(
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
    var respWho = authzService.whoCanAccess("roadmap.ppt", "editor", "doc");
    assertThat(respWho).hasSameElementsAs(List.of("u1", "u2"));
    var respResourceRelations = authzService.resourceRelations("roadmap.ppt");
    assertThat(respResourceRelations).size().isEqualTo(2);
    var respUsersRelations = authzService.targetsRelations(List.of("u1"));
    assertThat(respUsersRelations).size().isEqualTo(2);
    var respWhat = authzService.whatCanTargetAccess("u1");
    assertThat(respWhat).size().isEqualTo(7);
  }
}
