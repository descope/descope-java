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

import com.descope.exception.ServerCommonException;
import com.descope.model.client.Client;
import com.descope.model.fga.FGACheckResult;
import com.descope.model.fga.FGARelation;
import com.descope.model.fga.FGAResourceDetails;
import com.descope.model.fga.FGAResourceIdentifier;
import com.descope.model.fga.FGASchema;
import com.descope.proxy.ApiProxy;
import com.descope.proxy.impl.ApiProxyBuilder;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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

  @BeforeEach
  void setUp() {
    lenient().when(client.getProjectId()).thenReturn("test-project");
    lenient().when(client.getManagementKey()).thenReturn("test-key");
    fgaService = new FGAServiceImpl(client);
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
    List<FGARelation> relations = List.of(
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
    assertThrows(ServerCommonException.class, () -> fgaService.createRelations(List.of()));
  }

  @Test
  void testDeleteRelations_Success() throws Exception {
    List<FGARelation> relations = List.of(
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
    List<FGARelation> relations = List.of(
        new FGARelation("doc1", "document", "owner", "user1", "user")
    );

    Map<String, Object> checkResultMap = new HashMap<>();
    checkResultMap.put("allowed", true);

    Map<String, Object> response = new HashMap<>();
    response.put("tuples", List.of(checkResultMap));

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
    List<FGAResourceIdentifier> identifiers = List.of(
        new FGAResourceIdentifier("doc1", "document")
    );

    Map<String, Object> detailsMap = new HashMap<>();
    detailsMap.put("resourceId", "doc1");
    detailsMap.put("resourceType", "document");
    detailsMap.put("displayName", "Document 1");
    
    Map<String, Object> response = new HashMap<>();
    response.put("resourcesDetails", List.of(detailsMap));

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
    List<FGAResourceDetails> details = List.of(
        new FGAResourceDetails("doc1", "document", "Document 1")
    );

    try (MockedStatic<ApiProxyBuilder> mockedStatic = Mockito.mockStatic(ApiProxyBuilder.class)) {
      mockedStatic.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy);

      fgaService.saveResourcesDetails(details);

      verify(apiProxy).post(any(), any(), eq(Void.class));
    }
  }
}
