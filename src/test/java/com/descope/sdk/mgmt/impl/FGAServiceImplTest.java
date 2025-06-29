package com.descope.sdk.mgmt.impl;

import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_FGA_CHECK;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_FGA_CREATE_RELATIONS;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_FGA_DELETE_RELATIONS;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_FGA_LOAD_SCHEMA;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_FGA_RESOURCES_LOAD;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_FGA_RESOURCES_SAVE;
import static com.descope.literals.Routes.ManagementEndPoints.MANAGEMENT_FGA_SAVE_SCHEMA;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.descope.exception.RateLimitExceededException;
import com.descope.exception.ServerCommonException;
import com.descope.model.client.Client;
import com.descope.model.fga.FGACheckResult;
import com.descope.model.fga.FGARelation;
import com.descope.model.fga.FGAResourceDetails;
import com.descope.model.fga.FGAResourceIdentifier;
import com.descope.model.fga.FGASchema;
import com.descope.model.mgmt.ManagementServices;
import com.descope.proxy.ApiProxy;
import com.descope.proxy.impl.ApiProxyBuilder;
import com.descope.sdk.TestUtils;
import com.descope.sdk.mgmt.FGAService;
import com.descope.sdk.mgmt.impl.ManagementServiceBuilder;
import com.fasterxml.jackson.core.type.TypeReference;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junitpioneer.jupiter.RetryingTest;
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
  void testLoadSchema_Success() throws Exception {
    Map<String, Object> response = new HashMap<>();
    response.put("dsl", "model AuthZ 1.0\ntype user");

    try (MockedStatic<ApiProxyBuilder> mockedStatic = Mockito.mockStatic(ApiProxyBuilder.class)) {
      mockedStatic.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      when(apiProxy.getArray(any(), any(TypeReference.class))).thenReturn(response);

      FGASchema result = fgaService.loadSchema();

      assertNotNull(result);
      assertEquals("model AuthZ 1.0\ntype user", result.getDsl());
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

    Map<String, Object> checkResultMap = new HashMap<>();
    checkResultMap.put("allowed", true);

    Map<String, Object> response = new HashMap<>();
    response.put("tuples", Arrays.asList(checkResultMap));

    try (MockedStatic<ApiProxyBuilder> mockedStatic = Mockito.mockStatic(ApiProxyBuilder.class)) {
      mockedStatic.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);
      when(apiProxy.postAndGetArray(any(), any(), any(TypeReference.class))).thenReturn(response);

      List<FGACheckResult> results = fgaService.check(relations);

      assertNotNull(results);
      assertEquals(1, results.size());
      assertEquals(true, results.get(0).isAllowed());
    }
  }

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
    // Load test schema from file
    String schemaContent = loadTestSchema();
    FGASchema testSchema = new FGASchema(schemaContent);
    
    // Save the test schema
    integrationFgaService.saveSchema(testSchema);
    
    // Create test relations
    List<FGARelation> relations = Arrays.asList(
        // Organization membership
        new FGARelation("org1", "organization", "member", "user1", "user"),
        new FGARelation("org1", "organization", "admin", "user2", "user"),
        
        // Document ownership and hierarchy
        new FGARelation("doc1", "document", "parent", "org1", "organization"),
        new FGARelation("doc1", "document", "owner", "user1", "user"),
        new FGARelation("doc1", "document", "editor", "user3", "user")
    );
    
    integrationFgaService.createRelations(relations);
    
    // Perform authorization checks
    List<FGARelation> checkRelations = Arrays.asList(
        // Test owner access
        new FGARelation("doc1", "document", "owner", "user1", "user"),
        new FGARelation("doc1", "document", "editor", "user1", "user"),
        new FGARelation("doc1", "document", "viewer", "user1", "user"),
        
        // Test explicit editor access
        new FGARelation("doc1", "document", "editor", "user3", "user"),
        new FGARelation("doc1", "document", "viewer", "user3", "user"),
        
        // Test org member viewer access (should inherit from parent org)
        new FGARelation("doc1", "document", "viewer", "user1", "user"),
        
        // Test non-member access (should fail)
        new FGARelation("doc1", "document", "viewer", "user4", "user")
    );
    
    List<FGACheckResult> results = integrationFgaService.check(checkRelations);
    
    // Validate authorization results
    assertNotNull(results);
    assertEquals(7, results.size());
    
    // user1 should have owner access
    assertEquals(true, results.get(0).isAllowed());
    // user1 should have editor access (because owner)
    assertEquals(true, results.get(1).isAllowed());
    // user1 should have viewer access (because editor)
    assertEquals(true, results.get(2).isAllowed());
    
    // user3 should have explicit editor access
    assertEquals(true, results.get(3).isAllowed());
    // user3 should have viewer access (because editor)
    assertEquals(true, results.get(4).isAllowed());
    
    // user1 should have viewer access through org membership
    assertEquals(true, results.get(5).isAllowed());
    
    // user4 should NOT have access
    assertEquals(false, results.get(6).isAllowed());
    
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
