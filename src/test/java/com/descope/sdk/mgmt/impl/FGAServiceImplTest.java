package com.descope.sdk.mgmt.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.descope.exception.RateLimitExceededException;
import com.descope.exception.ServerCommonException;
import com.descope.model.client.Client;
import com.descope.model.fga.FGAAuthzSchema;
import com.descope.model.fga.FGACheckInfo;
import com.descope.model.fga.FGACheckOptions;
import com.descope.model.fga.FGACheckPathStep;
import com.descope.model.fga.FGACheckResponse;
import com.descope.model.fga.FGACheckResponse.FGACheckResponseTuple;
import com.descope.model.fga.FGACheckResult;
import com.descope.model.fga.FGACheckTuple;
import com.descope.model.fga.FGACondition;
import com.descope.model.fga.FGAConditionParam;
import com.descope.model.fga.FGALoadSchemaResponse;
import com.descope.model.fga.FGALoadSchemaResponse.FGALoadSchemaConditions;
import com.descope.model.fga.FGAMappableResourceQuery;
import com.descope.model.fga.FGAMappableResources;
import com.descope.model.fga.FGAMappableSchema;
import com.descope.model.fga.FGANode;
import com.descope.model.fga.FGANodeExpression;
import com.descope.model.fga.FGARelation;
import com.descope.model.fga.FGAResourceDetails;
import com.descope.model.fga.FGAResourceIdentifier;
import com.descope.model.fga.FGASchema;
import com.descope.model.fga.FGASchemaDryDeletes;
import com.descope.model.fga.FGASchemaDryRunResponse;
import com.descope.model.fga.FGASearchMappableResourcesResponse;
import com.descope.model.mgmt.ManagementServices;
import com.descope.proxy.ApiProxy;
import com.descope.proxy.impl.ApiProxyBuilder;
import com.descope.sdk.TestUtils;
import com.descope.sdk.mgmt.FGAService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

  private static final String CHECK_RESPONSE_JSON = "{\"tuples\":[{\"allowed\":true,"
      + "\"tuple\":{\"resource\":\"doc1\",\"resourceType\":\"doc\",\"relation\":\"viewer\",\"target\":\"u1\","
      + "\"targetType\":\"user\"},\"info\":{\"direct\":false,\"path\":{\"steps\":[{\"stepType\":\"RELATION\","
      + "\"tuple\":{\"resource\":\"f1\",\"resourceType\":\"folder\",\"relation\":\"viewer\",\"target\":\"u1\","
      + "\"targetType\":\"user\"},\"permission\":\"can_view\",\"subPaths\":[{\"steps\":[]}]}]},"
      + "\"trueConditions\":[1,2],\"falseConditions\":[3]}}],\"schemaVersion\":\"v-abc-123\"}";

  private static final String MAPPABLE_SCHEMA_JSON = "{\"schema\":{\"name\":\"s\",\"namespaces\":[{\"name\":\"doc\","
      + "\"relationDefinitions\":[{\"name\":\"viewer\",\"complexDefinition\":{\"nType\":\"union\",\"children\":"
      + "[{\"nType\":\"child\",\"expression\":{\"neType\":\"self\",\"relationDefinition\":\"owner\","
      + "\"relationDefinitionNamespace\":\"doc\",\"targetRelationDefinition\":\"member\","
      + "\"targetRelationDefinitionNamespace\":\"group\"}}]}}]}],\"conditions\":[{\"name\":\"IsAdmin\","
      + "\"params\":[{\"name\":\"role\",\"type\":\"string\"}],\"expression\":\"role == 'admin'\",\"builtin\":true,"
      + "\"checkedExpr\":\"AAEC\",\"id\":7}]},\"mappableResources\":[{\"type\":\"doc\",\"resources\":"
      + "[{\"resource\":\"doc1\",\"displayName\":\"Doc 1\"}]}]}";

  private static <T> T fromJson(String json, Class<T> clz) throws IOException {
    return new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .readValue(json, clz);
  }

  @SuppressWarnings("unchecked")
  private static String capturedAuthHeader(MockedStatic<ApiProxyBuilder> mockedStatic) {
    ArgumentCaptor<Supplier<String>> captor = ArgumentCaptor.forClass(Supplier.class);
    mockedStatic.verify(() -> ApiProxyBuilder.buildProxy(captor.capture(), any()));
    return captor.getValue().get();
  }

  @SuppressWarnings("unchecked")
  @Test
  void testCheck_WithOptions() throws Exception {
    when(client.getUri()).thenReturn("https://api.descope.com");
    List<FGARelation> relations = Arrays.asList(new FGARelation("doc1", "doc", "viewer", "u1", "user"));
    Map<String, Object> context = new HashMap<>();
    context.put("role", "admin");

    try (MockedStatic<ApiProxyBuilder> mockedStatic = Mockito.mockStatic(ApiProxyBuilder.class)) {
      mockedStatic.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      when(apiProxy.post(any(), any(), eq(FGACheckResponse.class)))
          .thenReturn(fromJson(CHECK_RESPONSE_JSON, FGACheckResponse.class));

      final List<FGACheckResult> results = fgaService.check(relations, context,
          FGACheckOptions.builder().computePaths(true).listConditions(true).build());

      ArgumentCaptor<URI> uriCaptor = ArgumentCaptor.forClass(URI.class);
      ArgumentCaptor<Map<String, Object>> bodyCaptor = ArgumentCaptor.forClass(Map.class);
      verify(apiProxy).post(uriCaptor.capture(), bodyCaptor.capture(), eq(FGACheckResponse.class));
      assertEquals("https://api.descope.com/v1/mgmt/fga/check", uriCaptor.getValue().toString());
      assertEquals("Bearer test-project:test-key", capturedAuthHeader(mockedStatic));
      Map<String, Object> body = bodyCaptor.getValue();
      assertEquals(relations, body.get("tuples"));
      assertEquals(context, body.get("context"));
      assertEquals(true, body.get("computePaths"));
      assertEquals(true, body.get("listConditions"));
      assertFalse(body.containsKey("checkTuples"));

      assertEquals(1, results.size());
      FGACheckResult result = results.get(0);
      assertTrue(result.isAllowed());
      assertEquals(relations.get(0), result.getRelation());
      FGACheckInfo info = result.getInfo();
      assertEquals("v-abc-123", info.getSchemaVersion());
      assertEquals(Arrays.asList(1, 2), info.getTrueConditions());
      assertEquals(Arrays.asList(3), info.getFalseConditions());
      FGACheckPathStep step = info.getPath().getSteps().get(0);
      assertEquals(FGACheckPathStep.STEP_TYPE_RELATION, step.getStepType());
      assertEquals(new FGARelation("f1", "folder", "viewer", "u1", "user"), step.getTuple());
      assertEquals("can_view", step.getPermission());
      assertEquals(1, step.getSubPaths().size());
    }
  }

  @SuppressWarnings("unchecked")
  @Test
  void testCheck_OptionsNotSentByDefault() throws Exception {
    try (MockedStatic<ApiProxyBuilder> mockedStatic = Mockito.mockStatic(ApiProxyBuilder.class)) {
      mockedStatic.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      fgaService.check(Arrays.asList(new FGARelation("doc1", "doc", "viewer", "u1", "user")), null,
          new FGACheckOptions());
      ArgumentCaptor<Map<String, Object>> bodyCaptor = ArgumentCaptor.forClass(Map.class);
      verify(apiProxy).post(any(), bodyCaptor.capture(), eq(FGACheckResponse.class));
      assertFalse(bodyCaptor.getValue().containsKey("computePaths"));
      assertFalse(bodyCaptor.getValue().containsKey("listConditions"));
    }
  }

  @Test
  void testCheck_WithOptionsEmptyList() {
    assertThrows(ServerCommonException.class,
        () -> fgaService.check(Arrays.asList(), null, new FGACheckOptions()));
  }

  @SuppressWarnings("unchecked")
  @Test
  void testCheckTuples_Success() throws Exception {
    when(client.getUri()).thenReturn("https://api.descope.com");
    FGARelation relation = new FGARelation("doc1", "doc", "viewer", "u1", "user");
    Map<String, Object> tupleContext = new HashMap<>();
    tupleContext.put("ip", "10.0.0.1");
    Map<String, Object> context = new HashMap<>();
    context.put("role", "admin");

    try (MockedStatic<ApiProxyBuilder> mockedStatic = Mockito.mockStatic(ApiProxyBuilder.class)) {
      mockedStatic.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      when(apiProxy.post(any(), any(), eq(FGACheckResponse.class)))
          .thenReturn(fromJson(CHECK_RESPONSE_JSON, FGACheckResponse.class));

      final List<FGACheckResult> results = fgaService.checkTuples(
          Arrays.asList(new FGACheckTuple(relation, tupleContext), new FGACheckTuple(relation, null)), context,
          FGACheckOptions.builder().listConditions(false).build());

      ArgumentCaptor<URI> uriCaptor = ArgumentCaptor.forClass(URI.class);
      ArgumentCaptor<Map<String, Object>> bodyCaptor = ArgumentCaptor.forClass(Map.class);
      verify(apiProxy).post(uriCaptor.capture(), bodyCaptor.capture(), eq(FGACheckResponse.class));
      assertEquals("https://api.descope.com/v1/mgmt/fga/check", uriCaptor.getValue().toString());
      assertEquals("Bearer test-project:test-key", capturedAuthHeader(mockedStatic));
      Map<String, Object> body = bodyCaptor.getValue();
      assertFalse(body.containsKey("tuples"));
      assertEquals(context, body.get("context"));
      assertEquals(false, body.get("listConditions"));
      assertFalse(body.containsKey("computePaths"));
      List<Map<String, Object>> checkTuples = (List<Map<String, Object>>) body.get("checkTuples");
      assertEquals(2, checkTuples.size());
      assertEquals(relation, checkTuples.get(0).get("tuple"));
      assertEquals(tupleContext, checkTuples.get(0).get("context"));
      assertFalse(checkTuples.get(1).containsKey("context"));

      assertTrue(results.get(0).isAllowed());
      assertEquals("v-abc-123", results.get(0).getInfo().getSchemaVersion());
    }
  }

  @Test
  void testCheckTuples_EmptyList() {
    assertThrows(ServerCommonException.class, () -> fgaService.checkTuples(Arrays.asList(), null, null));
    assertThrows(ServerCommonException.class, () -> fgaService.checkTuples(null, null, null));
  }

  @Test
  void testCheckTuples_MissingTuple() {
    assertThrows(ServerCommonException.class,
        () -> fgaService.checkTuples(Arrays.asList(new FGACheckTuple(null, null)), null, null));
  }

  @Test
  void testDeleteAllRelations_Success() throws Exception {
    when(client.getUri()).thenReturn("https://api.descope.com");
    lenient().when(client.getFgaCacheUri()).thenReturn("https://cache.example.com");
    try (MockedStatic<ApiProxyBuilder> mockedStatic = Mockito.mockStatic(ApiProxyBuilder.class)) {
      mockedStatic.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);

      fgaService.deleteAllRelations();

      ArgumentCaptor<URI> uriCaptor = ArgumentCaptor.forClass(URI.class);
      verify(apiProxy).delete(uriCaptor.capture(), isNull(), eq(Void.class));
      assertEquals("https://api.descope.com/v1/mgmt/fga/relations", uriCaptor.getValue().toString());
      assertEquals("Bearer test-project:test-key", capturedAuthHeader(mockedStatic));
    }
  }

  @SuppressWarnings("unchecked")
  @Test
  void testSearchMappableResources_Success() throws Exception {
    when(client.getUri()).thenReturn("https://api.descope.com");
    lenient().when(client.getFgaCacheUri()).thenReturn("https://cache.example.com");
    List<FGAMappableResourceQuery> queries = Arrays.asList(
        new FGAMappableResourceQuery("doc", Arrays.asList("Doc")));

    try (MockedStatic<ApiProxyBuilder> mockedStatic = Mockito.mockStatic(ApiProxyBuilder.class)) {
      mockedStatic.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      when(apiProxy.post(any(), any(), eq(FGASearchMappableResourcesResponse.class)))
          .thenReturn(fromJson(MAPPABLE_SCHEMA_JSON, FGASearchMappableResourcesResponse.class));

      final List<FGAMappableResources> result = fgaService.searchMappableResources("t1", queries, 5L);

      ArgumentCaptor<URI> uriCaptor = ArgumentCaptor.forClass(URI.class);
      ArgumentCaptor<Map<String, Object>> bodyCaptor = ArgumentCaptor.forClass(Map.class);
      verify(apiProxy).post(uriCaptor.capture(), bodyCaptor.capture(),
          eq(FGASearchMappableResourcesResponse.class));
      assertEquals("https://api.descope.com/v1/mgmt/fga/mappable/resources", uriCaptor.getValue().toString());
      assertEquals("Bearer test-project:test-key", capturedAuthHeader(mockedStatic));
      Map<String, Object> body = bodyCaptor.getValue();
      assertEquals("t1", body.get("tenantId"));
      assertEquals(queries, body.get("resourcesQueries"));
      assertEquals("5", body.get("resourcesLimit"));

      assertEquals(1, result.size());
      assertEquals("doc", result.get(0).getType());
      assertEquals("doc1", result.get(0).getResources().get(0).getResource());
      assertEquals("Doc 1", result.get(0).getResources().get(0).getDisplayName());
    }
  }

  @SuppressWarnings("unchecked")
  @Test
  void testSearchMappableResources_OptionalFieldsNotSent() throws Exception {
    try (MockedStatic<ApiProxyBuilder> mockedStatic = Mockito.mockStatic(ApiProxyBuilder.class)) {
      mockedStatic.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      final List<FGAMappableResources> result = fgaService.searchMappableResources("t1", null, null);
      ArgumentCaptor<Map<String, Object>> bodyCaptor = ArgumentCaptor.forClass(Map.class);
      verify(apiProxy).post(any(), bodyCaptor.capture(), eq(FGASearchMappableResourcesResponse.class));
      assertFalse(bodyCaptor.getValue().containsKey("resourcesQueries"));
      assertFalse(bodyCaptor.getValue().containsKey("resourcesLimit"));
      assertTrue(result.isEmpty());
    }
  }

  @Test
  void testSearchMappableResources_MissingTenantId() {
    assertThrows(ServerCommonException.class, () -> fgaService.searchMappableResources("", null, null));
  }

  @Test
  void testLoadMappableSchema_Success() throws Exception {
    when(client.getUri()).thenReturn("https://api.descope.com");
    lenient().when(client.getFgaCacheUri()).thenReturn("https://cache.example.com");

    try (MockedStatic<ApiProxyBuilder> mockedStatic = Mockito.mockStatic(ApiProxyBuilder.class)) {
      mockedStatic.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      when(apiProxy.get(any(), eq(FGAMappableSchema.class)))
          .thenReturn(fromJson(MAPPABLE_SCHEMA_JSON, FGAMappableSchema.class));

      final FGAMappableSchema result = fgaService.loadMappableSchema("t1", 10L);

      ArgumentCaptor<URI> uriCaptor = ArgumentCaptor.forClass(URI.class);
      verify(apiProxy).get(uriCaptor.capture(), eq(FGAMappableSchema.class));
      assertEquals("/v1/mgmt/fga/mappable/schema", uriCaptor.getValue().getPath());
      assertTrue(uriCaptor.getValue().toString().startsWith("https://api.descope.com/"));
      assertTrue(Arrays.asList(uriCaptor.getValue().getQuery().split("&")).contains("tenantId=t1"));
      assertTrue(Arrays.asList(uriCaptor.getValue().getQuery().split("&")).contains("resourcesLimit=10"));
      assertEquals("Bearer test-project:test-key", capturedAuthHeader(mockedStatic));

      FGAAuthzSchema schema = result.getSchema();
      assertEquals("s", schema.getName());
      FGANode node = schema.getNamespaces().get(0).getRelationDefinitions().get(0).getComplexDefinition();
      assertEquals("union", node.getNType());
      FGANodeExpression expression = node.getChildren().get(0).getExpression();
      assertEquals("child", node.getChildren().get(0).getNType());
      assertEquals("self", expression.getNeType());
      assertEquals("owner", expression.getRelationDefinition());
      assertEquals("doc", expression.getRelationDefinitionNamespace());
      assertEquals("member", expression.getTargetRelationDefinition());
      assertEquals("group", expression.getTargetRelationDefinitionNamespace());
      FGACondition condition = schema.getConditions().get(0);
      assertEquals("IsAdmin", condition.getName());
      assertEquals("role", condition.getParams().get(0).getName());
      assertTrue(condition.isBuiltin());
      assertEquals("AAEC", condition.getCheckedExpr());
      assertEquals(Integer.valueOf(7), condition.getId());
      assertEquals("Doc 1", result.getMappableResources().get(0).getResources().get(0).getDisplayName());
    }
  }

  @Test
  void testLoadMappableSchema_LimitNotSentByDefault() throws Exception {
    try (MockedStatic<ApiProxyBuilder> mockedStatic = Mockito.mockStatic(ApiProxyBuilder.class)) {
      mockedStatic.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      fgaService.loadMappableSchema("t1", null);
      ArgumentCaptor<URI> uriCaptor = ArgumentCaptor.forClass(URI.class);
      verify(apiProxy).get(uriCaptor.capture(), eq(FGAMappableSchema.class));
      assertEquals("tenantId=t1", uriCaptor.getValue().getQuery());
    }
  }

  @Test
  void testLoadMappableSchema_MissingTenantId() {
    assertThrows(ServerCommonException.class, () -> fgaService.loadMappableSchema(null, null));
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
  
  private String loadTestSchema() throws IOException {
    return new String(Files.readAllBytes(Paths.get("src/test/data/fga-schema.txt")));
  }
}
