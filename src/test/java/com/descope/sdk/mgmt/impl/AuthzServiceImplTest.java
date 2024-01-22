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
import com.descope.model.authz.Modified;
import com.descope.model.authz.Namespace;
import com.descope.model.authz.Relation;
import com.descope.model.authz.RelationDefinition;
import com.descope.model.authz.RelationQuery;
import com.descope.model.authz.RelationsResponse;
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
import java.time.Instant;
import java.time.Period;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.RetryingTest;
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
    ApiProxy apiProxy = mock(ApiProxy.class);
    doReturn(new RelationsResponse(Arrays.asList(new Relation()))).when(apiProxy).post(any(), any(), any());
    try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
      mockedApiProxyBuilder.when(
        () -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      authzService.whatCanTargetAccess("kiki");
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
  }
}
