package com.descope.sdk.mgmt.impl;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import com.descope.exception.RateLimitExceededException;
import com.descope.exception.ServerCommonException;
import com.descope.model.client.Client;
import com.descope.model.fga.FGACheckResult;
import com.descope.model.fga.FGARelation;
import com.descope.model.fga.FGAResourceDetails;
import com.descope.model.fga.FGAResourceIdentifier;
import com.descope.model.fga.FGASchema;
import com.descope.model.fga.FGASchemaDryRunResponse;
import com.descope.model.mgmt.ManagementServices;
import com.descope.sdk.TestUtils;
import com.descope.sdk.mgmt.AuthzService;
import com.descope.sdk.mgmt.FGAService;
import com.descope.utils.EnvironmentUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junitpioneer.jupiter.RetryingTest;

/**
 * Live coverage for the FGA surface, mirroring integrationtests/tests/fga_test.go.
 * Requires DESCOPE_PROJECT_ID and DESCOPE_MANAGEMENT_KEY. Server errors are retried because these
 * tests replace the project's schema, which is shared with the other live tests.
 */
class FGALiveTest {

  private static final String GDRIVE_SCHEMA = "model AuthZ 1.0\n"
      + "\n"
      + "type user\n"
      + "\n"
      + "type group\n"
      + "  relation member: user\n"
      + "\n"
      + "type doc\n"
      + "  relation owner: user | group#member\n"
      + "  relation parent: folder\n"
      + "\n"
      + "  permission can_create: owner | parent.owner\n"
      + "\n"
      + "type folder\n"
      + "  relation parent: folder\n"
      + "  relation owner: user | group#member\n"
      + "  relation editor: user\n"
      + "\n"
      + "  permission can_create: owner | parent.owner\n"
      + "  permission can_edit: editor | parent.editor | can_create\n";

  private static final String ABAC_SCHEMA = "model AuthZ 1.0\n"
      + "\n"
      + "condition IsAdmin(role string) { role == \"admin\" }\n"
      + "condition CanEdit(action string) { action == \"write\" }\n"
      + "\n"
      + "type user\n"
      + "\n"
      + "type doc\n"
      + "  relation viewer: user with IsAdmin\n"
      + "  permission can_edit: viewer with CanEdit\n";

  private static final String SIMPLE_SCHEMA = "model AuthZ 1.0\n"
      + "\n"
      + "type user\n"
      + "\n"
      + "type document\n"
      + "  relation viewer: user\n"
      + "\n"
      + "  permission can_view: viewer\n";

  private static final String REDUCED_SCHEMA = "model AuthZ 1.0\n"
      + "\n"
      + "type user\n";

  private FGAService fgaService;
  private AuthzService authzService;

  @BeforeEach
  void setUp() {
    ManagementServices services = ManagementServiceBuilder.buildServices(TestUtils.getClient());
    fgaService = services.getFgaService();
    authzService = services.getAuthzService();
  }

  // No schema cleanup on purpose: every test here saves the schema it needs, and the project is
  // shared with the other live tests, some of which read the schema without a null guard.

  @RetryingTest(value = 3, suspendForMs = 10000,
      onExceptions = {RateLimitExceededException.class, ServerCommonException.class})
  void testGdriveSchemaRelationsAndChecks() {
    fgaService.saveSchema(new FGASchema(GDRIVE_SCHEMA));

    List<FGARelation> relations = Arrays.asList(
        new FGARelation("folder1", "folder", "owner", "u1", "user"),
        new FGARelation("folder1", "folder", "editor", "u2", "user"),
        new FGARelation("folder1", "folder", "parent", "rootFolder", "folder"),
        new FGARelation("rootFolder", "folder", "owner", "u9", "user"),
        new FGARelation("group1", "group", "member", "ug1", "user"),
        new FGARelation("group1", "group", "member", "ug2", "user"),
        new FGARelation("folder2", "folder", "owner", "group1", "group#member"),
        new FGARelation("folder2", "folder", "owner", "u1", "user"),
        new FGARelation("folder2", "folder", "parent", "rootFolder", "folder"));
    fgaService.createRelations(relations);

    List<FGACheckResult> expected = Arrays.asList(
        expect("folder1", "owner", "u1", true),
        expect("folder1", "editor", "u2", true),
        expect("folder1", "editor", "u1", false),
        expect("folder1", "can_create", "u1", true),
        expect("folder1", "can_create", "u2", false),
        expect("folder1", "can_create", "u9", true),
        expect("folder1", "can_edit", "u2", true),
        expect("folder1", "can_edit", "u1", true),
        expect("folder1", "can_edit", "u3", false),
        expect("folder1", "can_edit", "u9", true),
        expect("folder1", "owner", "u9", false),
        expect("rootFolder", "can_edit", "u1", false),
        expect("folder2", "owner", "ug1", true),
        expect("folder2", "owner", "u1", true),
        expect("folder2", "can_create", "ug1", true),
        expect("folder2", "can_edit", "ug1", true),
        expect("folder2", "can_edit", "u9", true));

    List<FGARelation> toCheck = new ArrayList<>();
    for (FGACheckResult check : expected) {
      toCheck.add(check.getRelation());
    }

    List<FGACheckResult> results = fgaService.check(toCheck);

    assertEquals(expected.size(), results.size());
    for (int i = 0; i < expected.size(); i++) {
      FGARelation relation = expected.get(i).getRelation();
      assertEquals(expected.get(i).isAllowed(), results.get(i).isAllowed(), "check " + relation);
      assertEchoes(relation, results.get(i).getRelation());
    }

    fgaService.deleteRelations(relations);
  }

  @RetryingTest(value = 3, suspendForMs = 10000,
      onExceptions = {RateLimitExceededException.class, ServerCommonException.class})
  void testAbacCheckWithContext() {
    fgaService.saveSchema(new FGASchema(ABAC_SCHEMA));

    FGARelation viewer = new FGARelation("doc1", "doc", "viewer", "u1", "user");
    fgaService.createRelations(Arrays.asList(viewer));

    List<FGACheckResult> allowed = fgaService.check(Arrays.asList(viewer), contextOf("role", "admin"));
    assertEquals(1, allowed.size());
    assertTrue(allowed.get(0).isAllowed(), "admin role should be allowed");
    assertTrue(allowed.get(0).getInfo().isConditional(), "result should be marked conditional");
    assertEchoes(viewer, allowed.get(0).getRelation());

    List<FGACheckResult> denied = fgaService.check(Arrays.asList(viewer), contextOf("role", "user"));
    assertFalse(denied.get(0).isAllowed(), "non-admin role should be denied");
    assertTrue(denied.get(0).getInfo().isConditional());

    List<FGACheckResult> noContext = fgaService.check(Arrays.asList(viewer));
    assertFalse(noContext.get(0).isAllowed());
    assertTrue(noContext.get(0).getInfo().getMissingContext().contains("role"),
        "role should be reported as missing context");

    FGARelation canEdit = new FGARelation("doc1", "doc", "can_edit", "u1", "user");
    Map<String, Object> writeContext = contextOf("role", "admin");
    writeContext.put("action", "write");
    List<FGACheckResult> editAllowed = fgaService.check(Arrays.asList(canEdit), writeContext);
    assertTrue(editAllowed.get(0).isAllowed(), "admin with write action should be allowed via can_edit");
    assertTrue(editAllowed.get(0).getInfo().isConditional());

    Map<String, Object> readContext = contextOf("role", "admin");
    readContext.put("action", "read");
    List<FGACheckResult> editDenied = fgaService.check(Arrays.asList(canEdit), readContext);
    assertFalse(editDenied.get(0).isAllowed(), "admin with read action should be denied via can_edit");
    assertTrue(editDenied.get(0).getInfo().isConditional());

    // Same context, through the authz queries that evaluate conditions.
    assertTrue(authzService.whoCanAccess("doc1", "viewer", "doc", contextOf("role", "admin")).contains("u1"),
        "admin role should satisfy the viewer condition");
    List<String> denied1 = authzService.whoCanAccess("doc1", "viewer", "doc", contextOf("role", "user"));
    assertFalse(denied1 != null && denied1.contains("u1"),
        "non-admin role should not satisfy the viewer condition");
    assertNotNull(authzService.whatCanTargetAccess("u1", contextOf("role", "admin")));

    FGASchema loaded = fgaService.loadSchema();
    assertTrue(StringUtils.isNotBlank(loaded.getVersion()), "schema version should be returned");
    assertTrue(loaded.getConditions().stream().anyMatch(c -> "IsAdmin".equals(c.getName())),
        "IsAdmin condition should be returned");

    fgaService.deleteRelations(Arrays.asList(viewer));
  }

  @RetryingTest(value = 3, suspendForMs = 10000,
      onExceptions = {RateLimitExceededException.class, ServerCommonException.class})
  void testDryRunSchemaDoesNotSave() {
    fgaService.saveSchema(new FGASchema(SIMPLE_SCHEMA));

    FGASchemaDryRunResponse deleting = fgaService.dryRunSchema(new FGASchema(REDUCED_SCHEMA));
    assertNotNull(deleting.getDeletesPreview());
    assertTrue(deleting.getDeletesPreview().isHasDeletes(), "dropping the document type should report deletes");

    FGASchemaDryRunResponse unchanged = fgaService.dryRunSchema(new FGASchema(SIMPLE_SCHEMA));
    assertFalse(unchanged.getDeletesPreview() != null && unchanged.getDeletesPreview().isHasDeletes(),
        "an unchanged schema should report no deletes");

    assertTrue(fgaService.loadSchema().getDsl().contains("document"), "dry run must not save the schema");
  }

  @RetryingTest(value = 3, suspendForMs = 10000,
      onExceptions = {RateLimitExceededException.class, ServerCommonException.class})
  void testFgaCacheUrlPointingAtTheApiHostStillWorks() {
    Client client = TestUtils.getClient();
    // Not a real cache, but it proves the config reaches the request and the six routed calls
    // keep working through it. The trailing slash covers the URL normalization.
    client.setFgaCacheUri(client.getUri() + "/");
    assertRoundTripWorks(client);
  }

  @RetryingTest(value = 3, suspendForMs = 10000,
      onExceptions = {RateLimitExceededException.class, ServerCommonException.class})
  void testBadFgaCacheUrlFailsOnlyTheRoutedCalls() {
    fgaService.saveSchema(new FGASchema(SIMPLE_SCHEMA));

    Client client = TestUtils.getClient();
    client.setFgaCacheUri("http://localhost:1");
    ManagementServices services = ManagementServiceBuilder.buildServices(client);
    FGAService badFga = services.getFgaService();
    final AuthzService badAuthz = services.getAuthzService();

    List<FGARelation> relations = Arrays.asList(new FGARelation("doc1", "document", "viewer", "u1", "user"));
    final Map<String, Object> context = contextOf("role", "admin");

    // Transport failures are not mapped into DescopeException, they propagate as-is.
    assertThrows(Exception.class, () -> badFga.saveSchema(new FGASchema(SIMPLE_SCHEMA)));
    assertThrows(Exception.class, () -> badFga.createRelations(relations));
    assertThrows(Exception.class, () -> badFga.deleteRelations(relations));
    assertThrows(Exception.class, () -> badFga.check(relations));
    assertThrows(Exception.class, () -> badFga.check(relations, context));
    assertThrows(Exception.class, () -> badAuthz.whoCanAccess("doc1", "viewer", "document"));
    assertThrows(Exception.class, () -> badAuthz.whatCanTargetAccess("u1"));

    List<FGAResourceDetails> details = Arrays.asList(new FGAResourceDetails("doc1", "document", "Doc One"));
    assertNotNull(badFga.loadSchema().getDsl());
    assertNotNull(badFga.dryRunSchema(new FGASchema(SIMPLE_SCHEMA)));
    badFga.saveResourcesDetails(details);
    assertNotNull(badFga.loadResourcesDetails(Arrays.asList(new FGAResourceIdentifier("doc1", "document"))));
    assertDoesNotThrow(() -> badAuthz.resourceRelations("doc1"));
  }

  @RetryingTest(value = 3, suspendForMs = 10000,
      onExceptions = {RateLimitExceededException.class, ServerCommonException.class})
  void testAgainstRealFgaCache() {
    String fgaCacheUrl = EnvironmentUtils.getFgaCacheURL();
    assumeTrue(StringUtils.isNotBlank(fgaCacheUrl), "DESCOPE_FGA_CACHE_URL is not set");

    Client client = TestUtils.getClient();
    client.setFgaCacheUri(fgaCacheUrl);
    assertRoundTripWorks(client);
  }

  // Saves a schema, creates a relation and checks it, all through whatever FGA cache the client
  // is configured with.
  private static void assertRoundTripWorks(Client client) {
    FGAService cachedFga = ManagementServiceBuilder.buildServices(client).getFgaService();

    cachedFga.saveSchema(new FGASchema(SIMPLE_SCHEMA));
    FGARelation relation = new FGARelation("doc1", "document", "viewer", "u1", "user");
    cachedFga.createRelations(Arrays.asList(relation));

    List<FGACheckResult> results = cachedFga.check(Arrays.asList(relation));
    assertEquals(1, results.size());
    assertTrue(results.get(0).isAllowed());

    cachedFga.deleteRelations(Arrays.asList(relation));
  }

  // The server echoes the tuple it evaluated, with targetType normalized, so compare the rest.
  private static void assertEchoes(FGARelation requested, FGARelation echoed) {
    assertNotNull(echoed);
    assertEquals(requested.getResource(), echoed.getResource());
    assertEquals(requested.getResourceType(), echoed.getResourceType());
    assertEquals(requested.getRelation(), echoed.getRelation());
    assertEquals(requested.getTarget(), echoed.getTarget());
  }

  private static FGACheckResult expect(String resource, String relation, String target, boolean allowed) {
    return new FGACheckResult(allowed, new FGARelation(resource, "folder", relation, target, "user"), null);
  }

  private static Map<String, Object> contextOf(String key, Object value) {
    Map<String, Object> context = new HashMap<>();
    context.put(key, value);
    return context;
  }
}
