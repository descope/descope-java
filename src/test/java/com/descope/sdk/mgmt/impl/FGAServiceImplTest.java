package com.descope.sdk.mgmt.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.descope.enums.FGACheckPathStepType;
import com.descope.exception.RateLimitExceededException;
import com.descope.exception.ServerCommonException;
import com.descope.model.client.Client;
import com.descope.model.fga.FGAAuthzSchema;
import com.descope.model.fga.FGACheckError;
import com.descope.model.fga.FGACheckInfo;
import com.descope.model.fga.FGACheckPath;
import com.descope.model.fga.FGACheckPathStep;
import com.descope.model.fga.FGACheckRequest;
import com.descope.model.fga.FGACheckResponse;
import com.descope.model.fga.FGACheckResponse.FGACheckResponseTuple;
import com.descope.model.fga.FGACheckResult;
import com.descope.model.fga.FGACheckTuple;
import com.descope.model.fga.FGACondition;
import com.descope.model.fga.FGAConditionParam;
import com.descope.model.fga.FGALoadSchemaResponse;
import com.descope.model.fga.FGALoadSchemaResponse.FGALoadSchemaConditions;
import com.descope.model.fga.FGAMappableResource;
import com.descope.model.fga.FGAMappableResources;
import com.descope.model.fga.FGAMappableResourcesQuery;
import com.descope.model.fga.FGAMappableResourcesResponse;
import com.descope.model.fga.FGAMappableSchema;
import com.descope.model.fga.FGANamespace;
import com.descope.model.fga.FGANode;
import com.descope.model.fga.FGANodeExpression;
import com.descope.model.fga.FGARelation;
import com.descope.model.fga.FGARelationDefinition;
import com.descope.model.fga.FGAResourceDetails;
import com.descope.model.fga.FGAResourceIdentifier;
import com.descope.model.fga.FGASchema;
import com.descope.model.fga.FGASchemaDryDeletes;
import com.descope.model.fga.FGASchemaDryRunResponse;
import com.descope.model.mgmt.ManagementServices;
import com.descope.proxy.ApiProxy;
import com.descope.proxy.impl.ApiProxyBuilder;
import com.descope.sdk.TestUtils;
import com.descope.sdk.mgmt.FGAService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junitpioneer.jupiter.RetryingTest;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FGAServiceImplTest {

  @Mock
  private ApiProxy apiProxy;
  @Mock
  private Client client;
  private FGAServiceImpl fgaService;
  private FGAService integrationFgaService;

  @BeforeEach
  void setUp() {
    lenient().when(client.getProjectId()).thenReturn("test-project");
    lenient().when(client.getManagementKey()).thenReturn("test-key");
    fgaService = new FGAServiceImpl(client);
    
    // Setup integration test service with real client
    Client realClient = TestUtils.getClient();
    ManagementServices mgmtServices = ManagementServiceBuilder.buildServices(realClient);
    integrationFgaService = mgmtServices.getFgaService();
  }

  @Test
  void testSaveSchema_Success() throws Exception {
    FGASchema schema = new FGASchema("model AuthZ 1.0\ntype user\ntype doc\n  relation owner: user");
    
    try (MockedStatic<ApiProxyBuilder> mockedStatic = Mockito.mockStatic(ApiProxyBuilder.class)) {
      mockedStatic.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);

      fgaService.saveSchema(schema);

      verify(apiProxy).post(any(), any(), eq(Void.class));
    }
  }

  @Test
  void testSaveSchema_NullSchema() {
    assertThrows(ServerCommonException.class, () -> fgaService.saveSchema(null));
  }

  @Test
  void testSaveSchema_EmptyDSL() {
    FGASchema schema = new FGASchema("");
    assertThrows(ServerCommonException.class, () -> fgaService.saveSchema(schema));
  }

  @Test
  void testDryRunSchema_Success() throws Exception {
    FGASchema schema = new FGASchema("model AuthZ 1.0\ntype user");
    FGASchemaDryRunResponse response = new FGASchemaDryRunResponse(
        new FGASchemaDryDeletes(true, Arrays.asList("doc#viewer"), Arrays.asList("doc")));

    try (MockedStatic<ApiProxyBuilder> mockedStatic = Mockito.mockStatic(ApiProxyBuilder.class)) {
      mockedStatic.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      when(apiProxy.post(any(), any(), eq(FGASchemaDryRunResponse.class))).thenReturn(response);

      FGASchemaDryRunResponse result = fgaService.dryRunSchema(schema);

      assertNotNull(result);
      assertTrue(result.getDeletesPreview().isHasDeletes());
      assertEquals(Arrays.asList("doc#viewer"), result.getDeletesPreview().getRelations());
      assertEquals(Arrays.asList("doc"), result.getDeletesPreview().getTypes());
    }
  }

  @Test
  void testDryRunSchema_NullSchema() {
    assertThrows(ServerCommonException.class, () -> fgaService.dryRunSchema(null));
  }

  @Test
  void testDryRunSchema_EmptyDSL() {
    assertThrows(ServerCommonException.class, () -> fgaService.dryRunSchema(new FGASchema("")));
  }

  @Test
  void testLoadSchema_Success() throws Exception {
    FGACondition condition = new FGACondition("IsAdmin",
        Arrays.asList(new FGAConditionParam("role", "string")), "role == \"admin\"");
    FGALoadSchemaResponse response = new FGALoadSchemaResponse("model AuthZ 1.0\ntype user", "v1",
        new FGALoadSchemaConditions(Arrays.asList(condition)));

    try (MockedStatic<ApiProxyBuilder> mockedStatic = Mockito.mockStatic(ApiProxyBuilder.class)) {
      mockedStatic.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      when(apiProxy.get(any(), eq(FGALoadSchemaResponse.class))).thenReturn(response);

      FGASchema result = fgaService.loadSchema();

      assertNotNull(result);
      assertEquals("model AuthZ 1.0\ntype user", result.getDsl());
      assertEquals("v1", result.getVersion());
      assertEquals(1, result.getConditions().size());
      assertEquals("IsAdmin", result.getConditions().get(0).getName());
      assertEquals("role", result.getConditions().get(0).getParams().get(0).getName());
    }
  }

  @Test
  void testLoadSchema_ConditionsAreNeverNull() throws Exception {
    FGALoadSchemaResponse response = new FGALoadSchemaResponse("model AuthZ 1.0\ntype user", "v1", null);

    try (MockedStatic<ApiProxyBuilder> mockedStatic = Mockito.mockStatic(ApiProxyBuilder.class)) {
      mockedStatic.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      when(apiProxy.get(any(), eq(FGALoadSchemaResponse.class))).thenReturn(response);

      assertTrue(fgaService.loadSchema().getConditions().isEmpty());
    }
  }

  @Test
  void testCreateRelations_Success() throws Exception {
    List<FGARelation> relations = Arrays.asList(
        new FGARelation("doc1", "document", "owner", "user1", "user")
    );

    try (MockedStatic<ApiProxyBuilder> mockedStatic = Mockito.mockStatic(ApiProxyBuilder.class)) {
      mockedStatic.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);

      fgaService.createRelations(relations);

      verify(apiProxy).post(any(), any(), eq(Void.class));
    }
  }

  @Test
  void testCreateRelations_EmptyList() {
    assertThrows(ServerCommonException.class, () -> fgaService.createRelations(Arrays.asList()));
  }

  @Test
  void testDeleteRelations_Success() throws Exception {
    List<FGARelation> relations = Arrays.asList(
        new FGARelation("doc1", "document", "owner", "user1", "user")
    );

    try (MockedStatic<ApiProxyBuilder> mockedStatic = Mockito.mockStatic(ApiProxyBuilder.class)) {
      mockedStatic.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);

      fgaService.deleteRelations(relations);

      verify(apiProxy).post(any(), any(), eq(Void.class));
    }
  }

  @Test
  void testCheck_Success() throws Exception {
    List<FGARelation> relations = Arrays.asList(
        new FGARelation("doc1", "document", "owner", "user1", "user")
    );

    FGACheckResponse response = new FGACheckResponse(Arrays.asList(
        new FGACheckResponseTuple(true, relations.get(0), FGACheckInfo.builder().direct(true).build())));

    try (MockedStatic<ApiProxyBuilder> mockedStatic = Mockito.mockStatic(ApiProxyBuilder.class)) {
      mockedStatic.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      when(apiProxy.post(any(), any(), eq(FGACheckResponse.class))).thenReturn(response);

      List<FGACheckResult> results = fgaService.check(relations);

      assertNotNull(results);
      assertEquals(1, results.size());
      assertTrue(results.get(0).isAllowed());
      assertEquals(relations.get(0), results.get(0).getRelation());
      assertTrue(results.get(0).getInfo().isDirect());
    }
  }

  @SuppressWarnings("unchecked")
  @Test
  void testCheck_WithContext() throws Exception {
    List<FGARelation> relations = Arrays.asList(
        new FGARelation("doc1", "doc", "viewer", "user1", "user")
    );
    Map<String, Object> context = new HashMap<>();
    context.put("role", "admin");

    FGACheckInfo info = FGACheckInfo.builder().conditional(true).conditionalErr("bad type")
        .missingContext(Arrays.asList("action")).factUsed(true).build();
    FGACheckResponse response = new FGACheckResponse(Arrays.asList(
        new FGACheckResponseTuple(false, relations.get(0), info)));

    try (MockedStatic<ApiProxyBuilder> mockedStatic = Mockito.mockStatic(ApiProxyBuilder.class)) {
      mockedStatic.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      when(apiProxy.post(any(), any(), eq(FGACheckResponse.class))).thenReturn(response);

      List<FGACheckResult> results = fgaService.check(relations, context);

      ArgumentCaptor<Map<String, Object>> bodyCaptor = ArgumentCaptor.forClass(Map.class);
      verify(apiProxy).post(any(), bodyCaptor.capture(), eq(FGACheckResponse.class));
      assertEquals(context, bodyCaptor.getValue().get("context"));

      FGACheckInfo resultInfo = results.get(0).getInfo();
      assertTrue(resultInfo.isConditional());
      assertTrue(resultInfo.isFactUsed());
      assertEquals("bad type", resultInfo.getConditionalErr());
      assertEquals(Arrays.asList("action"), resultInfo.getMissingContext());
    }
  }

  @SuppressWarnings("unchecked")
  @Test
  void testCheck_EmptyContextIsNotSent() throws Exception {
    List<FGARelation> relations = Arrays.asList(
        new FGARelation("doc1", "doc", "viewer", "user1", "user")
    );

    try (MockedStatic<ApiProxyBuilder> mockedStatic = Mockito.mockStatic(ApiProxyBuilder.class)) {
      mockedStatic.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      when(apiProxy.post(any(), any(), eq(FGACheckResponse.class)))
          .thenReturn(new FGACheckResponse(Arrays.asList()));

      fgaService.check(relations, new HashMap<>());

      ArgumentCaptor<Map<String, Object>> bodyCaptor = ArgumentCaptor.forClass(Map.class);
      verify(apiProxy).post(any(), bodyCaptor.capture(), eq(FGACheckResponse.class));
      assertFalse(bodyCaptor.getValue().containsKey("context"));
    }
  }

  @Test
  void testCheck_MissingInfoIsNotNull() throws Exception {
    List<FGARelation> relations = Arrays.asList(
        new FGARelation("doc1", "doc", "viewer", "user1", "user")
    );
    FGACheckResponse response = new FGACheckResponse(Arrays.asList(
        new FGACheckResponseTuple(true, relations.get(0), null)));

    try (MockedStatic<ApiProxyBuilder> mockedStatic = Mockito.mockStatic(ApiProxyBuilder.class)) {
      mockedStatic.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      when(apiProxy.post(any(), any(), eq(FGACheckResponse.class))).thenReturn(response);

      List<FGACheckResult> results = fgaService.check(relations);

      assertNotNull(results.get(0).getInfo());
      assertFalse(results.get(0).getInfo().isConditional());
      assertTrue(results.get(0).getInfo().getMissingContext().isEmpty());
    }
  }

  @SuppressWarnings("unchecked")
  @Test
  void testLoadResourcesDetails_Success() throws Exception {
    List<FGAResourceIdentifier> identifiers = Arrays.asList(
        new FGAResourceIdentifier("doc1", "document")
    );

    Map<String, Object> detailsMap = new HashMap<>();
    detailsMap.put("resourceId", "doc1");
    detailsMap.put("resourceType", "document");
    detailsMap.put("displayName", "Document 1");
    
    Map<String, Object> response = new HashMap<>();
    response.put("resourcesDetails", Arrays.asList(detailsMap));

    try (MockedStatic<ApiProxyBuilder> mockedStatic = Mockito.mockStatic(ApiProxyBuilder.class)) {
      mockedStatic.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      when(apiProxy.postAndGetArray(any(), any(), any(TypeReference.class))).thenReturn(response);

      List<FGAResourceDetails> results = fgaService.loadResourcesDetails(identifiers);

      assertNotNull(results);
      assertEquals(1, results.size());
      assertEquals("doc1", results.get(0).getResourceId());
      assertEquals("document", results.get(0).getResourceType());
      assertEquals("Document 1", results.get(0).getDisplayName());
    }
  }

  @Test
  void testFgaCacheRouting() throws Exception {
    when(client.getUri()).thenReturn("https://api.descope.com");
    when(client.getFgaCacheUri()).thenReturn("https://cache.example.com/");

    try (MockedStatic<ApiProxyBuilder> mockedStatic = Mockito.mockStatic(ApiProxyBuilder.class)) {
      mockedStatic.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);

      fgaService.check(Arrays.asList(new FGARelation("doc1", "doc", "viewer", "user1", "user")));
      fgaService.dryRunSchema(new FGASchema("model AuthZ 1.0\ntype user"));

      ArgumentCaptor<URI> uriCaptor = ArgumentCaptor.forClass(URI.class);
      verify(apiProxy, times(2)).post(uriCaptor.capture(), any(), any());
      assertEquals("https://cache.example.com/v1/mgmt/fga/check", uriCaptor.getAllValues().get(0).toString());
      assertEquals("https://api.descope.com/v1/mgmt/fga/schema/dryrun",
          uriCaptor.getAllValues().get(1).toString());
    }
  }

  @Test
  void testFgaCacheRoutingUsesBaseUrlWhenNotConfigured() throws Exception {
    when(client.getUri()).thenReturn("https://api.descope.com");

    try (MockedStatic<ApiProxyBuilder> mockedStatic = Mockito.mockStatic(ApiProxyBuilder.class)) {
      mockedStatic.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);

      fgaService.check(Arrays.asList(new FGARelation("doc1", "doc", "viewer", "user1", "user")));

      ArgumentCaptor<URI> uriCaptor = ArgumentCaptor.forClass(URI.class);
      verify(apiProxy).post(uriCaptor.capture(), any(), any());
      assertEquals("https://api.descope.com/v1/mgmt/fga/check", uriCaptor.getValue().toString());
    }
  }

  @Test
  void testSaveResourcesDetails_Success() throws Exception {
    List<FGAResourceDetails> details = Arrays.asList(
        new FGAResourceDetails("doc1", "document", "Document 1")
    );

    try (MockedStatic<ApiProxyBuilder> mockedStatic = Mockito.mockStatic(ApiProxyBuilder.class)) {
      mockedStatic.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);

      fgaService.saveResourcesDetails(details);

      verify(apiProxy).post(any(), any(), eq(Void.class));
    }
  }

  @RetryingTest(value = 3, suspendForMs = 30000, onExceptions = RateLimitExceededException.class)
  void testFunctionalFullCycle() throws Exception {
    // Define test relations that will be used throughout the test
    List<FGARelation> relations = Arrays.asList(
        // Organization membership
        new FGARelation("org1", "organization", "member", "user1", "user"),
        new FGARelation("org1", "organization", "admin", "user2", "user"),
        
        // Document ownership and hierarchy
        new FGARelation("doc1", "document", "parent", "org1", "organization"),
        new FGARelation("doc1", "document", "owner", "user1", "user"),
        new FGARelation("doc1", "document", "editor", "user3", "user")
    );
    
    // Clean up any existing test relations from previous failed runs
    try {
      integrationFgaService.deleteRelations(relations);
    } catch (Exception e) {
      // Ignore errors - relations might not exist
    }
    
    // Load test schema from file
    String schemaContent = loadTestSchema();
    FGASchema testSchema = new FGASchema(schemaContent);
    
    // Save the test schema
    integrationFgaService.saveSchema(testSchema);
    
    integrationFgaService.createRelations(relations);
    
    // Perform authorization checks
    List<FGARelation> checkRelations = Arrays.asList(
        // Test direct relations
        new FGARelation("doc1", "document", "owner", "user1", "user"),
        new FGARelation("doc1", "document", "editor", "user3", "user"),
        new FGARelation("org1", "organization", "admin", "user2", "user"),
        
        // Test can_edit permission (combines editor, owner, and parent.admin)
        new FGARelation("doc1", "document", "can_edit", "user1", "user"), // owner
        new FGARelation("doc1", "document", "can_edit", "user3", "user"), // editor  
        new FGARelation("doc1", "document", "can_edit", "user2", "user"), // parent.admin
        
        // Test access that should fail
        new FGARelation("doc1", "document", "can_edit", "user4", "user"), // no access
        new FGARelation("doc1", "document", "owner", "user4", "user") // no ownership
    );
    
    List<FGACheckResult> results = integrationFgaService.check(checkRelations);
    
    // Validate authorization results
    assertNotNull(results);
    assertEquals(8, results.size());
    
    // Test direct relations should succeed
    assertEquals(true, results.get(0).isAllowed()); // user1 is owner of doc1
    assertEquals(true, results.get(1).isAllowed()); // user3 is editor of doc1
    assertEquals(true, results.get(2).isAllowed()); // user2 is admin of org1
    
    // Test can_edit permission should succeed
    assertEquals(true, results.get(3).isAllowed()); // user1 can_edit (owner)
    assertEquals(true, results.get(4).isAllowed()); // user3 can_edit (editor)
    assertEquals(true, results.get(5).isAllowed()); // user2 can_edit (parent.admin)
    
    // Test access that should fail
    assertEquals(false, results.get(6).isAllowed()); // user4 cannot edit (no access)
    assertEquals(false, results.get(7).isAllowed()); // user4 is not owner
    
    // Test resource details
    List<FGAResourceIdentifier> identifiers = Arrays.asList(
        new FGAResourceIdentifier("doc1", "document"),
        new FGAResourceIdentifier("org1", "organization")
    );
    
    List<FGAResourceDetails> resourceDetails = Arrays.asList(
        new FGAResourceDetails("doc1", "document", "Test Document 1"),
        new FGAResourceDetails("org1", "organization", "Test Organization 1")
    );
    
    // Save resource details
    integrationFgaService.saveResourcesDetails(resourceDetails);
    
    // Load and verify resource details
    List<FGAResourceDetails> loadedDetails = integrationFgaService.loadResourcesDetails(identifiers);
    assertNotNull(loadedDetails);
    assertEquals(2, loadedDetails.size());
    
    // Clean up - delete the test relations
    integrationFgaService.deleteRelations(relations);
  }
  
  private static final String AUTH_HEADER = "Bearer test-project:test-key";
  private static final FGARelation RELATION = new FGARelation("doc1", "doc", "viewer", "user1", "user");

  @SuppressWarnings("unchecked")
  private AtomicReference<String> mockProxy(MockedStatic<ApiProxyBuilder> mockedStatic) {
    AtomicReference<String> authHeader = new AtomicReference<>();
    mockedStatic.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenAnswer(inv -> {
      authHeader.set(((Supplier<String>) inv.getArgument(0)).get());
      return apiProxy;
    });
    return authHeader;
  }

  @SuppressWarnings("unchecked")
  @Test
  void testDryRunSchema_TupleCount() throws Exception {
    when(client.getUri()).thenReturn("https://api.descope.com");
    FGASchemaDryRunResponse response = new FGASchemaDryRunResponse(new FGASchemaDryDeletes(true,
        Arrays.asList("doc#viewer"), Arrays.asList("doc"), 1000, true));

    try (MockedStatic<ApiProxyBuilder> mockedStatic = Mockito.mockStatic(ApiProxyBuilder.class)) {
      AtomicReference<String> authHeader = mockProxy(mockedStatic);
      when(apiProxy.post(any(), any(), eq(FGASchemaDryRunResponse.class))).thenReturn(response);

      FGASchemaDryDeletes result = fgaService.dryRunSchema(new FGASchema("model AuthZ 1.0\ntype user"))
          .getDeletesPreview();

      ArgumentCaptor<URI> uriCaptor = ArgumentCaptor.forClass(URI.class);
      ArgumentCaptor<Map<String, Object>> bodyCaptor = ArgumentCaptor.forClass(Map.class);
      verify(apiProxy).post(uriCaptor.capture(), bodyCaptor.capture(), eq(FGASchemaDryRunResponse.class));
      assertEquals("https://api.descope.com/v1/mgmt/fga/schema/dryrun", uriCaptor.getValue().toString());
      assertEquals("model AuthZ 1.0\ntype user", bodyCaptor.getValue().get("dsl"));
      assertEquals(AUTH_HEADER, authHeader.get());
      assertTrue(result.isHasDeletes());
      assertEquals(Arrays.asList("doc#viewer"), result.getRelations());
      assertEquals(Arrays.asList("doc"), result.getTypes());
      assertEquals(1000, result.getDeletedTupleCount());
      assertTrue(result.isTupleCountCapped());
    }
  }

  @Test
  void testLoadSchema_FullSchema() throws Exception {
    when(client.getUri()).thenReturn("https://api.descope.com");
    FGACondition condition = new FGACondition("IsAdmin", Arrays.asList(new FGAConditionParam("role", "string")),
        "role == \"admin\"", true, "Y2hlY2tlZA==", 3);
    FGANamespace namespace = new FGANamespace("doc", Arrays.asList(new FGARelationDefinition("viewer",
        new FGANode("union", Collections.emptyList(),
            new FGANodeExpression("self", "viewer", "doc", "member", "group")))));
    FGALoadSchemaResponse response = new FGALoadSchemaResponse("model AuthZ 1.0\ntype doc", "v1",
        new FGALoadSchemaConditions(Arrays.asList(condition), "my-schema", Arrays.asList(namespace)));

    try (MockedStatic<ApiProxyBuilder> mockedStatic = Mockito.mockStatic(ApiProxyBuilder.class)) {
      AtomicReference<String> authHeader = mockProxy(mockedStatic);
      when(apiProxy.get(any(), eq(FGALoadSchemaResponse.class))).thenReturn(response);

      FGASchema result = fgaService.loadSchema();

      ArgumentCaptor<URI> uriCaptor = ArgumentCaptor.forClass(URI.class);
      verify(apiProxy).get(uriCaptor.capture(), eq(FGALoadSchemaResponse.class));
      assertEquals("https://api.descope.com/v1/mgmt/fga/schema", uriCaptor.getValue().toString());
      assertEquals(AUTH_HEADER, authHeader.get());
      assertEquals("model AuthZ 1.0\ntype doc", result.getDsl());
      assertEquals("v1", result.getVersion());
      assertEquals("my-schema", result.getName());
      assertEquals(Arrays.asList(namespace), result.getNamespaces());
      assertEquals("union", result.getNamespaces().get(0).getRelationDefinitions().get(0)
          .getComplexDefinition().getNType());
      FGACondition loaded = result.getConditions().get(0);
      assertTrue(loaded.isBuiltin());
      assertEquals("Y2hlY2tlZA==", loaded.getCheckedExpr());
      assertEquals(3, loaded.getId());
    }
  }

  @SuppressWarnings("unchecked")
  @Test
  void testCheckWithRequest_Success() throws Exception {
    when(client.getUri()).thenReturn("https://api.descope.com");
    Map<String, Object> context = new HashMap<>();
    context.put("role", "admin");
    Map<String, Object> tupleContext = new HashMap<>();
    tupleContext.put("role", "viewer");
    FGACheckRequest request = FGACheckRequest.builder()
        .checkTuples(Arrays.asList(new FGACheckTuple(RELATION, tupleContext)))
        .context(context)
        .computePaths(true)
        .listConditions(true)
        .build();

    FGACheckPath path = new FGACheckPath(Arrays.asList(new FGACheckPathStep(FGACheckPathStepType.RELATION, RELATION,
        "can_view", Arrays.asList(new FGACheckPath(Collections.emptyList())))));
    FGACheckInfo info = FGACheckInfo.builder().direct(true).conditional(true).missingContext(Arrays.asList("x"))
        .conditionalErr("err").factUsed(true).path(path).error(new FGACheckError(400, "bad tuple"))
        .trueConditions(Arrays.asList(1, 2)).falseConditions(Arrays.asList(3)).build();
    FGACheckResponse response = new FGACheckResponse(
        Arrays.asList(new FGACheckResponseTuple(true, RELATION, info)), "schema-v1");

    try (MockedStatic<ApiProxyBuilder> mockedStatic = Mockito.mockStatic(ApiProxyBuilder.class)) {
      AtomicReference<String> authHeader = mockProxy(mockedStatic);
      when(apiProxy.post(any(), any(), eq(FGACheckResponse.class))).thenReturn(response);

      FGACheckResponse result = fgaService.checkWithRequest(request);

      ArgumentCaptor<URI> uriCaptor = ArgumentCaptor.forClass(URI.class);
      ArgumentCaptor<Map<String, Object>> bodyCaptor = ArgumentCaptor.forClass(Map.class);
      verify(apiProxy).post(uriCaptor.capture(), bodyCaptor.capture(), eq(FGACheckResponse.class));
      assertEquals("https://api.descope.com/v1/mgmt/fga/check", uriCaptor.getValue().toString());
      assertEquals(AUTH_HEADER, authHeader.get());
      Map<String, Object> body = bodyCaptor.getValue();
      assertEquals(request.getCheckTuples(), body.get("checkTuples"));
      assertEquals(context, body.get("context"));
      assertEquals(true, body.get("computePaths"));
      assertEquals(true, body.get("listConditions"));
      assertFalse(body.containsKey("tuples"));

      assertEquals("schema-v1", result.getSchemaVersion());
      FGACheckResponseTuple tuple = result.getTuples().get(0);
      assertTrue(tuple.isAllowed());
      assertEquals(RELATION, tuple.getTuple());
      assertEquals(info, tuple.getInfo());
      assertEquals(FGACheckPathStepType.RELATION, tuple.getInfo().getPath().getSteps().get(0).getStepType());
      assertEquals(400, tuple.getInfo().getError().getStatus());
      assertEquals("bad tuple", tuple.getInfo().getError().getMessage());
      assertEquals(Arrays.asList(1, 2), tuple.getInfo().getTrueConditions());
      assertEquals(Arrays.asList(3), tuple.getInfo().getFalseConditions());
    }
  }

  @SuppressWarnings("unchecked")
  @Test
  void testCheckWithRequest_TuplesAndUnsetOptionsAreNotSent() throws Exception {
    when(client.getUri()).thenReturn("https://api.descope.com");
    try (MockedStatic<ApiProxyBuilder> mockedStatic = Mockito.mockStatic(ApiProxyBuilder.class)) {
      mockProxy(mockedStatic);
      when(apiProxy.post(any(), any(), eq(FGACheckResponse.class))).thenReturn(null);

      FGACheckResponse result = fgaService.checkWithRequest(
          FGACheckRequest.builder().tuples(Arrays.asList(RELATION)).build());

      ArgumentCaptor<Map<String, Object>> bodyCaptor = ArgumentCaptor.forClass(Map.class);
      verify(apiProxy).post(any(), bodyCaptor.capture(), eq(FGACheckResponse.class));
      assertEquals(Collections.singletonMap("tuples", Arrays.asList(RELATION)), bodyCaptor.getValue());
      assertTrue(result.getTuples().isEmpty());
    }
  }

  @Test
  void testCheckWithRequest_MissingInfoIsNotNull() throws Exception {
    when(client.getUri()).thenReturn("https://api.descope.com");
    try (MockedStatic<ApiProxyBuilder> mockedStatic = Mockito.mockStatic(ApiProxyBuilder.class)) {
      mockProxy(mockedStatic);
      when(apiProxy.post(any(), any(), eq(FGACheckResponse.class))).thenReturn(
          new FGACheckResponse(Arrays.asList(new FGACheckResponseTuple(true, RELATION, null))));

      FGACheckResponse result = fgaService.checkWithRequest(
          FGACheckRequest.builder().tuples(Arrays.asList(RELATION)).build());

      assertTrue(result.getTuples().get(0).getInfo().getMissingContext().isEmpty());
    }
  }

  @Test
  void testCheckWithRequest_NullRequest() {
    assertThrows(ServerCommonException.class, () -> fgaService.checkWithRequest(null));
  }

  @Test
  void testCheckWithRequest_NoTuples() {
    assertThrows(ServerCommonException.class, () -> fgaService.checkWithRequest(new FGACheckRequest()));
  }

  @Test
  void testCheckWithRequest_BothTuplesAndCheckTuples() {
    FGACheckRequest request = FGACheckRequest.builder().tuples(Arrays.asList(RELATION))
        .checkTuples(Arrays.asList(new FGACheckTuple(RELATION, null))).build();
    assertThrows(ServerCommonException.class, () -> fgaService.checkWithRequest(request));
  }

  @Test
  void testCheckPathStepType_UnknownValue() throws Exception {
    FGACheckPathStep step = new ObjectMapper().readValue("{\"stepType\":\"SOMETHING_NEW\"}", FGACheckPathStep.class);
    assertEquals(FGACheckPathStepType.UNKNOWN, step.getStepType());
  }

  @Test
  void testLoadMappableSchema_Success() throws Exception {
    when(client.getUri()).thenReturn("https://api.descope.com");
    FGAMappableResources resources = new FGAMappableResources("doc",
        Arrays.asList(new FGAMappableResource("doc1", "Document 1")));
    FGAMappableSchema response = new FGAMappableSchema(
        new FGAAuthzSchema("my-schema", Arrays.asList(new FGANamespace("doc", Collections.emptyList())),
            Arrays.asList(new FGACondition("IsAdmin", Collections.emptyList(), "true", false, "", 1))),
        Arrays.asList(resources));

    try (MockedStatic<ApiProxyBuilder> mockedStatic = Mockito.mockStatic(ApiProxyBuilder.class)) {
      AtomicReference<String> authHeader = mockProxy(mockedStatic);
      when(apiProxy.get(any(), eq(FGAMappableSchema.class))).thenReturn(response);

      FGAMappableSchema result = fgaService.loadMappableSchema("tenant1", 10L);

      ArgumentCaptor<URI> uriCaptor = ArgumentCaptor.forClass(URI.class);
      verify(apiProxy).get(uriCaptor.capture(), eq(FGAMappableSchema.class));
      URI uri = uriCaptor.getValue();
      assertEquals("/v1/mgmt/fga/mappable/schema", uri.getPath());
      assertEquals(new HashSet<>(Arrays.asList("tenantId=tenant1", "resourcesLimit=10")),
          new HashSet<>(Arrays.asList(uri.getQuery().split("&"))));
      assertEquals(AUTH_HEADER, authHeader.get());
      assertEquals(response, result);
      assertEquals("my-schema", result.getSchema().getName());
      assertEquals("Document 1", result.getMappableResources().get(0).getResources().get(0).getDisplayName());
    }
  }

  @Test
  void testLoadMappableSchema_NoLimit() throws Exception {
    when(client.getUri()).thenReturn("https://api.descope.com");
    try (MockedStatic<ApiProxyBuilder> mockedStatic = Mockito.mockStatic(ApiProxyBuilder.class)) {
      mockProxy(mockedStatic);
      when(apiProxy.get(any(), eq(FGAMappableSchema.class))).thenReturn(null);

      assertNotNull(fgaService.loadMappableSchema("tenant1", null));

      ArgumentCaptor<URI> uriCaptor = ArgumentCaptor.forClass(URI.class);
      verify(apiProxy).get(uriCaptor.capture(), eq(FGAMappableSchema.class));
      assertEquals("tenantId=tenant1", uriCaptor.getValue().getQuery());
    }
  }

  @Test
  void testLoadMappableSchema_EmptyTenantId() {
    assertThrows(ServerCommonException.class, () -> fgaService.loadMappableSchema("", null));
  }

  @SuppressWarnings("unchecked")
  @Test
  void testSearchMappableResources_Success() throws Exception {
    when(client.getUri()).thenReturn("https://api.descope.com");
    List<FGAMappableResourcesQuery> queries = Arrays.asList(
        new FGAMappableResourcesQuery("doc", Arrays.asList("doc")));
    FGAMappableResources resources = new FGAMappableResources("doc",
        Arrays.asList(new FGAMappableResource("doc1", "Document 1")));

    try (MockedStatic<ApiProxyBuilder> mockedStatic = Mockito.mockStatic(ApiProxyBuilder.class)) {
      AtomicReference<String> authHeader = mockProxy(mockedStatic);
      when(apiProxy.post(any(), any(), eq(FGAMappableResourcesResponse.class)))
          .thenReturn(new FGAMappableResourcesResponse(Arrays.asList(resources)));

      List<FGAMappableResources> result = fgaService.searchMappableResources("tenant1", queries, 10L);

      ArgumentCaptor<URI> uriCaptor = ArgumentCaptor.forClass(URI.class);
      ArgumentCaptor<Map<String, Object>> bodyCaptor = ArgumentCaptor.forClass(Map.class);
      verify(apiProxy).post(uriCaptor.capture(), bodyCaptor.capture(), eq(FGAMappableResourcesResponse.class));
      assertEquals("https://api.descope.com/v1/mgmt/fga/mappable/resources", uriCaptor.getValue().toString());
      assertEquals(AUTH_HEADER, authHeader.get());
      Map<String, Object> body = bodyCaptor.getValue();
      assertEquals("tenant1", body.get("tenantId"));
      assertEquals(queries, body.get("resourcesQueries"));
      assertEquals("10", body.get("resourcesLimit"));
      assertEquals(Arrays.asList(resources), result);
    }
  }

  @SuppressWarnings("unchecked")
  @Test
  void testSearchMappableResources_UnsetOptionsAreNotSent() throws Exception {
    when(client.getUri()).thenReturn("https://api.descope.com");
    try (MockedStatic<ApiProxyBuilder> mockedStatic = Mockito.mockStatic(ApiProxyBuilder.class)) {
      mockProxy(mockedStatic);
      when(apiProxy.post(any(), any(), eq(FGAMappableResourcesResponse.class))).thenReturn(null);

      assertTrue(fgaService.searchMappableResources("tenant1", null, null).isEmpty());

      ArgumentCaptor<Map<String, Object>> bodyCaptor = ArgumentCaptor.forClass(Map.class);
      verify(apiProxy).post(any(), bodyCaptor.capture(), eq(FGAMappableResourcesResponse.class));
      assertEquals(Collections.singletonMap("tenantId", "tenant1"), bodyCaptor.getValue());
    }
  }

  @Test
  void testSearchMappableResources_EmptyTenantId() {
    assertThrows(ServerCommonException.class, () -> fgaService.searchMappableResources(" ", null, null));
  }

  private String loadTestSchema() throws IOException {
    return new String(Files.readAllBytes(Paths.get("src/test/data/fga-schema.txt")));
  }
}
