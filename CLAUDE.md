# Claude Development Notes

This file contains important information for Claude about this codebase.

## Build and Test Commands

### Maven via Docker
Since Maven may not be directly installed, use this Docker alias:
```bash
alias mvn='docker run -it --rm --name my-maven-project -v "$HOME/.m2":/root/.m2 -v "$(pwd)":/usr/src/mymaven -w /usr/src/mymaven maven:3.8.1-openjdk-11 mvn'
```

For non-interactive use (in scripts/automation), remove the `-it` flags:
```bash
docker run --rm --name my-maven-project -v "$HOME/.m2":/root/.m2 -v "$(pwd)":/usr/src/mymaven -w /usr/src/mymaven maven:3.8.1-openjdk-11 mvn <commands>
```

### Common Build Commands
- **Compile**: `mvn clean compile test-compile`
- **Run tests**: `mvn test`
- **Run specific test**: `mvn test -Dtest=ClassName`
- **Clean build**: `mvn clean install`

## Code Style Guidelines

### Model Classes
All model classes in `src/main/java/com/descope/model/` should follow these conventions:

1. **Required Lombok Annotations**:
   ```java
   @Data
   @Builder
   @NoArgsConstructor
   @AllArgsConstructor
   ```

2. **Minimal Comments**: Avoid excessive JavaDoc comments. Keep classes clean and concise.

3. **Example Pattern**:
   ```java
   package com.descope.model.example;

   import lombok.AllArgsConstructor;
   import lombok.Builder;
   import lombok.Data;
   import lombok.NoArgsConstructor;

   @Data
   @Builder
   @NoArgsConstructor
   @AllArgsConstructor
   public class ExampleModel {
     private String field1;
     private String field2;
   }
   ```

### Test Classes
Management service tests should follow this pattern:

1. **For real integration tests**: Use `TestUtils.getClient()` and build services through `ManagementServiceBuilder.buildServices(client)`
2. **For unit tests with mocks**: Mock `ApiProxyBuilder.buildProxy(any(), any())` to return mock `ApiProxy`
3. **Client mocking**: When using mock clients, ensure you mock `getProjectId()` and `getManagementKey()` 
4. **Use lenient mocking** to avoid unnecessary stubbing warnings:
   ```java
   lenient().when(client.getProjectId()).thenReturn("test-project");
   lenient().when(client.getManagementKey()).thenReturn("test-key");
   ```

## Recent Fixes

### FGA Service Compilation Issues (2025-06-26)
**Problem**: `FGAServiceImplTest.java` had compilation errors and test failures.

**Root Cause**:
1. Incorrect `ApiProxyBuilder.buildProxy()` method signature usage
2. Wrong ApiProxy method calls (incorrect parameters)
3. Boolean getter method name issue (`getAllowed()` vs `isAllowed()`)
4. Improper client mocking causing NullPointerExceptions

**Solution**:
1. Fixed `ApiProxyBuilder.buildProxy()` calls to use `any(), any()` parameters
2. Updated ApiProxy method calls to match correct signatures (`post()`, `getArray()`, `postAndGetArray()`)
3. Changed `getAllowed()` to `isAllowed()` for boolean fields with Lombok `@Data`
4. Added proper client mocking with required values (`projectId`, `managementKey`)
5. Used lenient mocking to avoid unnecessary stubbing warnings

**Files Modified**:
- `src/test/java/com/descope/sdk/mgmt/impl/FGAServiceImplTest.java`
- All FGA model classes updated to match project conventions:
  - `src/main/java/com/descope/model/fga/FGACheckInfo.java`
  - `src/main/java/com/descope/model/fga/FGACheckResult.java`
  - `src/main/java/com/descope/model/fga/FGARelation.java`
  - `src/main/java/com/descope/model/fga/FGAResourceDetails.java`
  - `src/main/java/com/descope/model/fga/FGAResourceIdentifier.java`
  - `src/main/java/com/descope/model/fga/FGASchema.java`

**Result**: 
- ✅ `mvn clean compile test-compile` succeeds
- ✅ All 10 FGAServiceImplTest tests pass (0 failures, 0 errors)
- ✅ FGA model classes now consistent with project code style

## Architecture Notes

### Management Services
- All management services extend `ManagementsBase`
- Services are built through `ManagementServiceBuilder.buildServices(client)`
- The `getApiProxy()` method handles authentication and proxy creation
- FGA service is registered in `ManagementServices` and `ManagementServiceBuilder`

### API Communication
- All API calls go through `ApiProxy` interface
- `ApiProxyBuilder` creates proxy instances with proper authentication
- Test mocking should target `ApiProxyBuilder.buildProxy()` static method calls