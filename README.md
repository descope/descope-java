[![CI](https://github.com/descope/java-sdk/actions/workflows/ci.yml/badge.svg)](https://github.com/descope/java-sdk/actions/workflows/ci.yml)

# Descope SDK for Java

The Descope SDK for Java provides convenient access to the Descope user management and authentication API
for a backend written in Java. You can read more on the [Descope Website](https://descope.com).

## Requirements

The SDK supports Java version 11 and above.

## Installing the SDK

Using [maven](https://maven.apache.org) add the following dependency to your pom.xml:

```xml
<dependencies>
    ...
    <dependency>
        <artifactId>java-sdk</artifactId>
        <groupId>com.descope</groupId>
        <version>1.0</version>
    </dependency>
    ...
</dependencies>
```

## Setup

A Descope `Project ID` is required to initialize the SDK. Find it on the
[project page in the Descope Console](https://app.descope.com/settings/project).

```java
import com.descope.client;

// Initialized after setting the DESCOPE_PROJECT_ID env var (and optionally DESCOPE_MANAGEMENT_KEY)
var descopeClient = new DescopeClient();

// ** Or directly **
var descopeClient = new DescopeClient(Config.builder().projectId("Your-project").build());
```

## Authentication Functions

These sections show how to use the SDK to perform various authentication/authorization functions:

1. [OTP Authentication](#otp-authentication)
2. [Magic Link](#magic-link)
3. [Enchanted Link](#enchanted-link)
4. [OAuth](#oauth)
5. [SSO/SAML](#ssosaml)
6. [TOTP Authentication](#totp-authentication)
7. [Passwords](#passwords)
8. [Session Validation](#session-validation)
9. [Roles & Permission Validation](#roles--permission-validation)
10. [Logging Out](#logging-out)

## Management Functions

These sections show how to use the SDK to perform API management functions. Before using any of them, you will need to
create a Management Key. The instructions for this can be found under [Setup](#setup-1).

1. [Manage Tenants](#manage-tenants)
2. [Manage Users](#manage-users)
3. [Manage Access Keys](#manage-access-keys)
4. [Manage SSO Setting](#manage-sso-setting)
5. [Manage Permissions](#manage-permissions)
6. [Manage Roles](#manage-roles)
7. [Query SSO Groups](#query-sso-groups)
8. [Manage Flows](#manage-flows)
9. [Manage JWTs](#manage-jwts)
10. [Search Audit](#search-audit)

If you wish to run any of our code samples and play with them, check out our [Code Examples](#code-examples) section.

If you're developing unit tests, see how you can use our mocks package underneath
the [Unit Testing and Data Mocks](#unit-testing-and-data-mocks) section.

If you're performing end-to-end testing, check out
the [Utils for your end to end (e2e) tests and integration tests](#utils-for-your-end-to-end-e2e-tests-and-integration-tests)
section. You will need to use the `descopeClient` object created under [Setup](#setup-1) guide.

For rate limiting information, please confer to the [API Rate Limits](#api-rate-limits) section.

---

### OTP Authentication

Send a user a one-time password (OTP) using your preferred delivery method (_email / SMS_). An email address or phone
number must be provided accordingly.

The user can either `sign up`, `sign in` or `sign up or in`

```java
// Every user must have a loginID. All other user information is optional
String loginId = "desmond@descope.com"
User user = User.builder()
    .name("Desmond Copeland")
    .phone("212-555-1234")
    .email(loginId)
    .build();

try {
  String maskedAddress = descopeClient.getAuthenticationServices(config, client).getOTPService().signUp(DeliveryMethod.EMAIL, loginId, user);
} catch (DescopeException de) {
  // Handle the error
}
```

The user will receive a code using the selected delivery method. Verify that code using:

```java
// Will throw DescopeException if there is an error with update
try {
  AuthenticationInfo info = descopeClient.getAuthenticationServices(config, client).getOTPService().verifyCode(DeliveryMethod.EMAIL, loginId, code);    
} catch (DescopeException de) {
  // Handle the error
}
```

The session and refresh JWTs should be returned to the caller, and passed with every request in the session. Read more
on [session validation](#session-validation)

### Magic Link

Send a user a Magic Link using your preferred delivery method (_email / SMS_).
The Magic Link will redirect the user to page where the its token needs to be verified.
This redirection can be configured in code, or globally in
the [Descope Console](https://app.descope.com/settings/authentication/magiclink)

The user can either `sign up`, `sign in` or `sign up or in`

```java
// If configured globally, the redirect URI is optional. If provided however, it will be used
// instead of any global configuration

// Every user must have a loginID. All other user information is optional
String loginId = "desmond@descope.com"
User user = User.builder()
    .name("Desmond Copeland")
    .phone("212-555-1234")
    .email(loginId)
    .build();

MagicLinkService mls = descopeClient.getAuthenticationServices(config, client).getMagicLinkService();

try {
    String uri = "http://myapp.com/verify-magic-link";
    String maskedAddress = mls.signUp(DeliveryMethod.EMAIL, loginId, uri, user);
} catch (DescopeException de) {
    // Handle the error
}

```

To verify a magic link, your redirect page must call the validation function on the token (`t`)
parameter (`https://your-redirect-address.com/verify?t=<token>`):

```java
// The optional `w http.ResponseWriter` adds the session and refresh cookies to the response automatically.
// Otherwise they're available via authInfo
try {
    AuthenticationInfo info = mls.verify(token);
} catch (DescopeException de) {
    // Handle the error
}

```

The session and refresh JWTs should be returned to the caller, and passed with every request in the session. Read more
on [session validation](#session-validation)

### Enchanted Link

Using the Enchanted Link APIs enables users to sign in by clicking a link
delivered to their email address. The email will include 3 different links,
and the user will have to click the right one, based on the 2-digit number that is
displayed when initiating the authentication process.

This method is similar to [Magic Link](#magic-link) but differs in two major ways:

- The user must choose the correct link out of the three, instead of having just one
  single link.
- This supports cross-device clicking, meaning the user can try to log in on one device,
  like a computer, while clicking the link on another device, for instance a mobile phone.

The Enchanted Link will redirect the user to page where the its token needs to be verified.
This redirection can be configured in code per request, or set globally in
the [Descope Console](https://app.descope.com/settings/authentication/enchantedlink).

The user can either `sign up`, `sign in` or `sign up or in`

```java
// If configured globally, the redirect URI is optional. If provided however, it will be used
// instead of any global configuration.

EnchantedLinkResponse res = new EnchantedLinkResponse();
try {
    String uri = "http://myapp.com/verify-enchanted-link";
    res = descopeClient.getAuthenticationServices(config, client).getEnchantedLinkService().signUp(loginId, uri, user);
} catch (DescopeException de) {
    // Handle the error
}

```

After sending the link, you must poll to receive a valid session using the `PendingRef` from
the previous step. A valid session will be returned only after the user clicks the right link.

```java
// Poll for a certain number of tries / time frame
for (int i = retriesCount; i > 0; i--) {
    try {
        EnchantedLinkService els = descopeClient.getAuthenticationServices(config, client).getEnchantedLinkService();
        AuthenticationInfo info = els.getSession(res.getPendingRef());
    } catch (DescopeException de) {
        if (i > 1) {
            // Poll again after X seconds
            TimeUnit.SECONDS.sleep(retryInterval);
            continue;
        }
        else {
            // Handle the error
            break;
        }
    }
}

```

To verify an enchanted link, your redirect page must call the validation function on the token (`t`)
parameter (`https://your-redirect-address.com/verify?t=<token>`). Once the token is verified, the session polling will
receive a valid response.

```java

try {
    descopeClient.getAuthenticationServices(config, client).getEnchantedLinkService().verify(token);
} catch (DescopeException de) {
    // Token is invalid, handle the error
}

```

The session and refresh JWTs should be returned to the caller, and passed with every request in the session. Read more
on [session validation](#session-validation)

### OAuth

Users can authenticate using their social logins, using the OAuth protocol. Configure your OAuth settings on
the [Descope console](https://app.descope.com/settings/authentication/social). To start a flow call:

```java
// Choose an oauth provider out of the supported providers
// If configured globally, the return URL is optional. If provided however, it will be used
// instead of any global configuration.
// Redirect the user to the returned URL to start the OAuth redirect chain
OAuthService oas = descopeClient.getAuthenticationServices(config, client).getOAuthService();

try {
    String returnUrl = "https://my-app.com/handle-oauth";
    oas.start("google", returnUrl, loginOptions);
} catch (DescopeException de) {
    // Handle the error
}

```

The user will authenticate with the authentication provider, and will be redirected back to the redirect URL, with an
appended `code` HTTP URL parameter. Exchange it to validate the user:

```java

try {
    AuthenticationInfo info = oas.exchangeToken(code);
} catch (DescopeException de) {
    // Handle the error
}

```

The session and refresh JWTs should be returned to the caller, and passed with every request in the session. Read more
on [session validation](#session-validation)

### SSO/SAML

Users can authenticate to a specific tenant using SAML or Single Sign On. Configure your SSO/SAML settings on
the [Descope console](https://app.descope.com/settings/authentication/sso). To start a flow call:

```java
// Choose which tenant to log into
// Redirect the user to the returned URL to start the SSO/SAML redirect chain
SAMLService ss = descopeClient.getAuthenticationServices(config, client).getSAMLService();

try {
    String returnURL = "https://my-app.com/handle-saml";
    String url = ss.start("my-tenant-ID", returnURL, loginOptions);
} catch (DescopeException de) {
    // Handle the error
}

```

The user will authenticate with the authentication provider configured for that tenant, and will be redirected back to
the redirect URL, with an appended `code` HTTP URL parameter. Exchange it to validate the user:

```java
// The optional `w http.ResponseWriter` adds the session and refresh cookies to the response automatically.
// Otherwise they're available via authInfo
try {
    String url = ss.exchangeToken(code);
} catch (DescopeException de) {
    // Handle the error
}

```

The session and refresh JWTs should be returned to the caller, and passed with every request in the session. Read more
on [session validation](#session-validation)

### TOTP Authentication

The user can authenticate using an authenticator app, such as Google Authenticator.
Sign up like you would using any other authentication method. The sign up response
will then contain a QR code `Image` that can be displayed to the user to scan using
their mobile device camera app, or the user can enter the `Key` manually or click
on the link provided by the `ProvisioningURL`.

Existing users can add TOTP using the `update` function.

```java
// Every user must have a loginID. All other user information is optional
String loginId = "desmond@descope.com"
User user = User.builder()
    .name("Desmond Copeland")
    .phone("212-555-1234")
    .email(loginId)
    .build();

TOTPService ts = descopeClient.getAuthenticationServices(config, client).getTOTPService();

try {
    TOTPResponse resp = ts.signUp(loginId, user);
} catch (DescopeException de) {
    // Handle the error
}

// Use one of the provided options to have the user add their credentials to the authenticator
// resp.getProvisioningURL()
// resp.getImage()
// resp.getKey()
```

There are 3 different ways to allow the user to save their credentials in
their authenticator app - either by clicking the provisioning URL, scanning the QR
image or inserting the key manually. After that, signing in is done using the code
the app produces.

```java
// The optional `w http.ResponseWriter` adds the session and refresh cookies to the response automatically.
// Otherwise they're available via authInfo
try {
    AuthenticationInfo info = ts.signInCode(loginId, code, loginOptions);
} catch (DescopeException de) {
    // Handle the error
}

```

The session and refresh JWTs should be returned to the caller, and passed with every request in the session. Read more
on [session validation](#session-validation)

### Passwords

The user can also authenticate with a password, though it's recommended to
prefer passwordless authentication methods if possible. Sign up requires the
caller to provide a valid password that meets all the requirements configured
for the [password authentication method](https://app.descope.com/settings/authentication/password) in the Descope
console.

```java
// Every user must have a loginID. All other user information is optional
String loginId = "desmond@descope.com";
User user = User.builder()
    .name("Desmond Copeland")
    .phone("212-555-1234")
    .email(loginId)
    .build();
String password = "qYlvi65KaX";

PasswordService ps = descopeClient.getAuthenticationServices(config, client).getPasswordService();

try {
    AuthenticationInfo info = ps.signUp(loginId, user, password);
} catch (DescopeException de) {
    // Handle the error
}

```

The user can later sign in using the same loginID and password.

```java
try {
    AuthenticationInfo info = ps.signIn(loginId, password);
} catch (DescopeException de) {
    // Handle the error
}

```

The session and refresh JWTs should be returned to the caller, and passed with every request in the session. Read more
on [session validation](#session-validation)

In case the user needs to update their password, one of two methods are available: Resetting their password or replacing
their password

**Changing Passwords**

_NOTE: SendPasswordReset will only work if the user has a validated email address. Otherwise password reset prompts
cannot be sent._

In the [password authentication method](https://app.descope.com/settings/authentication/password) in the Descope
console, it is possible to define which alternative authentication method can be used in order to authenticate the user,
in order to reset and update their password.

```java
// Start the reset process by sending a password reset prompt. In this example we'll assume
// that magic link is configured as the reset method. The optional redirect URL is used in the
// same way as in regular magic link authentication.
PasswordService ps = descopeClient.getAuthenticationServices(config, client).getPasswordService();
String loginId = "desmond@descope.com";
String redirectUrl = "https://myapp.com/password-reset";
try {
    ps.sendPasswordReset(loginId, redirectUrl);
} catch (DescopeException de) {
    // Handle the error
}

```

The magic link, in this case, must then be verified like any other magic link (see the [magic link section](#magic-link)
for more details). However, after verifying the user, it is expected
to allow them to provide a new password instead of the old one. Since the user is now authenticated, this is possible
via:

```java
// The request (r) is required to make sure the user is authenticated.
try {
    ps.updateUserPassword(loginId, newPassword);
} catch (DescopeException de) {
    // Handle the error
}

```

`UpdateUserPassword` can always be called when the user is authenticated and has a valid session.

Alternatively, it is also possible to replace an existing active password with a new one.

```java
// Replaces the user's current password with a new one
try {
    ps.replaceUserPassword(loginId, oldPassword, newPassword);
} catch (DescopeException de) {
    // Handle the error
}

```

### Session Validation

Every secure request performed between your client and server needs to be validated.

Tokens can be validated directly:

```java
// Validate the session. Will return an error if expired
AuthenticationService as = descopeClient.getAuthenticationServices(config, client).getAuthenticationService();
try {
    Token t = as.validateSessionWithToken(sessionToken);
} catch (DescopeException de) {
    // Handle the unauthorized error
}

// If ValidateSessionWithRequest raises an exception, you will need to refresh the session using
try {
    Token t = as.refreshSessionWithToken(refreshToken);
} catch (DescopeException de) {
    // Handle the unauthorized error
}

// Alternatively, you could combine the two and
// have the session validated and automatically refreshed when expired
try {
    Token t = as.validateAndRefreshSessionWithTokens(sessionToken, refreshToken);
} catch (DescopeException de) {
    // unauthorized error
}

```

Choose the right session validation and refresh combination that suits your needs.

Refreshed sessions return the same response as is returned when users first sign up / log in,
Make sure to return the session token from the response to the client if tokens are validated directly.

Usually, the tokens can be passed in and out via HTTP headers or via a cookie.
The implementation can defer according to your implementation. See our [examples](#code-examples) for a few examples.

If Roles & Permissions are used, validate them immediately after validating the session. See
the [next section](#roles--permission-validation)
for more information.

#### Session Validation Using Middleware

Alternatively, you can validate the session using any supported builtin Go middleware (for example Chi or Mux)
instead of using the ValidateSessions function. This middleware will automatically detect the cookies from the
request and save the current user ID in the context for further usage. On failure, it will respond
with `401 Unauthorized`.

```go
import "github.com/descope/go-sdk/descope/sdk"

// ...

r.Use(sdk.AuthenticationMiddleware(descopeClient.Auth, nil, nil))
```

### Roles & Permission Validation

When using Roles & Permission, it's important to validate the user has the required
authorization immediately after making sure the session is valid. Taking the `sessionToken`
received by the [session validation](#session-validation), call the following functions:

For multi-tenant uses:

```java
// You can validate specific permissions
AuthenticationService as = descopeClient.getAuthenticationServices(config, client).getAuthenticationService();
try {
    if (!as.validatePermissions(sessionToken, "my-tenant-ID", Arrays.asList("Permission to validate"))) {
        // Deny access
    }
} catch (DescopeException de) {
    // Handle the error
}

// Or validate roles directly
try {
    if (!as.validateRoles(sessionToken, "my-tenant-ID", Arrays.asList("Role to validate"))) {
        // Deny access
    }
} catch (DescopeException de) {
    // Handle the error
}

```

When not using tenants use:

```java
// You can validate specific permissions
AuthenticationService as = descopeClient.getAuthenticationServices(config, client).getAuthenticationService();
try {
    if (!as.validatePermissions(sessionToken, Arrays.asList("Permission to validate"))) {
        // Deny access
    }
} catch (DescopeException de) {
    // Handle the error
}

// Or validate roles directly
try {
    if (!as.validateRoles(sessionToken, Arrays.asList("Role to validate"))) {
        // Deny access
    }
} catch (DescopeException de) {
    // Handle the error
}

```

### Logging Out

You can log out a user from an active session by providing their `refreshToken` for that session.
After calling this function, you must invalidate or remove any cookies you have created. Providing
a `http.ResponseWriter` will do this automatically.

```java
AuthenticationService as = descopeClient.getAuthenticationServices(config, client).getAuthenticationService();
// Refresh token will be taken from the request header or cookies automatically

try {
    as.logout(refreshToken);
} catch (DescopeException de) {
    // Handle the error
}

```

It is also possible to sign the user out of all the devices they are currently signed-in with. Calling `logoutAll` will
invalidate all user's refresh tokens. After calling this function, you must invalidate or remove any cookies you have
created.

```java
// Refresh token will be taken from the request header or cookies automatically

try {
    as.logoutAll(refreshToken);
} catch (DescopeException de) {
    // Handle the error
}

```

## Management Functions

It is very common for some form of management or automation to be required. These can be performed
using the management functions. Please note that these actions are more sensitive as they are administrative
in nature. Please use responsibly.

### Setup

To use the management API you'll need a `Management Key` along with your `Project ID`.
Create one in the [Descope Console](https://app.descope.com/settings/company/managementkeys).

```java
import com.descope.client;

// Initialized after setting the DESCOPE_PROJECT_ID env var (and optionally DESCOPE_MANAGEMENT_KEY)
var descopeClient = new DescopeClient();

// ** Or directly **
var descopeClient = new DescopeClient(Config.builder()
        .projectId("Your-project")
        .managementKey("management-key")
        .build());

```

### Manage Tenants

You can create, update, delete or load tenants:

```java
TenantService ts = descopeClient.getManagementServices(config, projectId, client).getTenantService();
// The self provisioning domains or optional. If given they'll be used to associate
// Users logging in to this tenant
try {
    ts.create("My Tenant", Arrays.asList("domain.com"));
} catch (DescopeException de) {
    // Handle the error
}

// You can optionally set your own ID when creating a tenant
try {
    ts.createWithId("my-custom-id", "My Tenant", Arrays.asList("domain.com"));
} catch (DescopeException de) {
    // Handle the error
}

// Update will override all fields as is. Use carefully.
try {
    ts.update("my-custom-id", "My Tenant", Arrays.asList("domain.com", "another-domain.com"));
} catch (DescopeException de) {
    // Handle the error
}

// Tenant deletion cannot be undone. Use carefully.
try {
    ts.delete("my-custom-id");
} catch (DescopeException de) {
    // Handle the error
}

// Load all tenants
try {
    List<Tenant> tenants = ts.loadAll();
    for (Tenant t : tenants) {
        // Do something
    }
} catch (DescopeException de) {
    // Handle the error
}

```

### Manage Users

You can create, update, delete or load users, as well as search according to filters:

```java
// A user must have a loginID, other fields are optional.
// Roles should be set directly if no tenants exist, otherwise set
// on a per-tenant basis.
UserService us = descopeClient.getManagementServices(config, projectId, client).getUserService();
try {
    us.create("desmond@descope.com", UserRequest.builder()
            .email("desmond@descope.com")
            .displayName("Desmond Copeland")
            .tenants(Arrays.asList(
                AssociatedTenant.builder()
                    .tenantId("tenant-ID1")
                    .roleNames(Arrays.asList("role-name1"),
                AssociatedTenant.builder()
                    .tenantId("tenant-ID2")))));
} catch (DescopeException de) {
    // Handle the error
}

// Alternatively, a user can be created and invited via an email message.
// Make sure to configure the invite URL in the Descope console prior to using this function,
// and that an email address is provided in the information.
try {
    us.invite("desmond@descope.com", UserRequest.builder()
            .email("desmond@descope.com")
            .displayName("Desmond Copeland")
            .tenants(Arrays.asList(
                AssociatedTenant.builder()
                    .tenantId("tenant-ID1")
                    .roleNames(Arrays.asList("role-name1"),
                AssociatedTenant.builder()
                    .tenantId("tenant-ID2")))));
} catch (DescopeException de) {
    // Handle the error
}

// Update will override all fields as is. Use carefully.
try {
    us.update("desmond@descope.com", UserRequest.builder()
            .email("desmond@descope.com")
            .displayName("Desmond Copeland")
            .tenants(Arrays.asList(
                AssociatedTenant.builder()
                    .tenantId("tenant-ID1")
                    .roleNames(Arrays.asList("role-name1"),
                AssociatedTenant.builder()
                    .tenantId("tenant-ID2")))));
} catch (DescopeException de) {
    // Handle the error
}

// User deletion cannot be undone. Use carefully.
try {
    us.delete("desmond@descope.com");
} catch (DescopeException de) {
    // Handle the error
}

// Load specific user
try {
    us.load("desmond@descope.com");
} catch (DescopeException de) {
    // Handle the error
}

// If needed, users can be loaded using their ID as well
try {
    us.loadByUserId("<user-id>");
} catch (DescopeException de) {
    // Handle the error
}

// Search all users, optionally according to tenant and/or role filter
// Results can be paginated using the limit and page parameters
try {
    List<AllUsersResponsibleDetails> users = us.searchAll(UserRequest.builder()
            .tenants(Arrays.asList(
                AssociatedTenant.builder()
                    .tenantId("tenant-ID1"),
                AssociatedTenant.builder()
                    .tenantId("tenant-ID2"))));
    for (AllUsersResponsibleDetails u : users) {
        // Do something
    }
}

```

#### Set or Expire User Password

You can set or expire a user's password.
Note: When setting a password, it will automatically be set as expired.
The user will not be able log-in using an expired password, and will be required replace it on next login.

```java
UserService us = descopeClient.getManagementServices(config, projectId, client).getUserService();

// Set a user's password
try {
    us.setPassword("my-custom-id", "some-password");
} catch (DescopeException de) {
    // Handle the error
}

// Or alternatively, expire a user password
try {
    us.expirePassword("my-custom-id");
} catch (DescopeException de) {
    // Handle the error
}

// Later, if the user is signing in with an expired password, the returned error will be ErrPasswordExpired
PasswordService ps = descopeClient.getAuthenticationServices(config, client).getPasswordService();
try {
    AuthenticationInfo info = ps.signIn("my-custom-id", "some-password");
} catch (DescopeException de) {
    // Handle the error
    // Handle a case when the error is expired, the user should replace/reset the password
}

```

### Manage Access Keys

You can create, update, delete or load access keys, as well as search according to filters:

```java
// Roles should be set directly if no tenants exist, otherwise set
// on a per-tenant basis.
AccessKeyService aks = descopeClient.getManagementServices(config, projectId, client).getAccessKeyService();
try {
    // Create a new access key with a name, delay time, and tenant
    AccessKeyResponse resp = aks.create("access-key-1", 0, 
            Arrays.asList("Role names"), 
            Arrays.asList(
                new Tenant("tenant-ID1", 
                    "Key Tenant", 
                    Arrays.asList(new AssociatedTenant("tenant-ID2", Arrays.asList("Role names"))))));
} catch (DescopeException de) {
    // Handle the error
}

// Load specific user
try {
    AccessKeyResponse resp = aks.load("access-key-1");
} catch (DescopeException de) {
    // Handle the error
}

// Search all users, optionally according to tenant and/or role filter
try {
    AccessKeyResponseList resp = aks.searchAll(Arrays.asList("Tenant IDs"));
    for (AccessKeyResponse r : aks.getKeys()) {
        // Do something
    }
} catch (DescopeException de) {
    // Handle the error
}

// Update will override all fields as is. Use carefully.
try {
    AccessKeyResponse resp = aks.update("access-key-1", "updated-name");
} catch (DescopeException de) {
    // Handle the error
}

// Access keys can be deactivated to prevent usage. This can be undone using "activate".
try {
    AccessKeyResponse resp = aks.deactivate("access-key-1");
} catch (DescopeException de) {
    // Handle the error
}

// Disabled access keys can be activated once again.
try {
    AccessKeyResponse resp = aks.activate("access-key-1");
} catch (DescopeException de) {
    // Handle the error
}

// Access key deletion cannot be undone. Use carefully.
try {
    aks.delete("access-key-1");
} catch (DescopeException de) {
    // Handle the error
}

```

### Manage SSO Setting

You can manage SSO settings and map SSO group roles and user attributes.

```java
SsoService ss = descopeClient.getManagementServices(config, projectId, client).getSsoService();
// You can get SSO settings for a specific tenant ID
try {
    SSOSettingsResponse resp = ss.getSettings("tenant-id");
} catch (DescopeException de) {
    // Handle the error
}

// You can configure SSO settings manually by setting the required fields directly
String tenantId = "tenant-id"; // Which tenant this configuration is for
String idpUrl = "https://idp.com";
String entityId = "my-idp-entity-id";
String idpCert = "<your-cert-here>";
String redirectUrl = "https://my-app.com/handle-saml"; // Global redirect URL for SSO/SAML
String domain = "domain.com"; // Users logging in from this domain will be logged in to this tenant

try {
    ss.configureSettings(tenantId, idpUrl, idpCert, entityId, redirectUrl, domain);
} catch (DescopeException de) {
    // Handle the error
}

// Alternatively, configure using an SSO metadata URL
try {
    ss.configureMetadata(tenantId, "https://idp.com/my-idp-metadata");
} catch (DescopeException de) {
    // Handle the error
}

// Map IDP groups to Descope roles, or map user attributes.
// This function overrides any previous mapping (even when empty). Use carefully.
List<RoleMapping> rm = Arrays.asList(new RoleMapping(Arrays.asList("Groups"), "Tenant Role"));
AttributeMapping am = new AttributeMapping("Tenant Name", "Tenant Email", "Tenant Phone Num", "Tenant Group");
try {
    ss.configureMapping(tenantId, rm, am);
} catch (DescopeException de) {
    // Handle the error
}

```

Note: Certificates should have a similar structure to:

```
-----BEGIN CERTIFICATE-----
Certifcate contents
-----END CERTIFICATE-----
```

// To delete SSO settings, call the following method
err := descopeClient.Management.SSO().DeleteSettings("tenant-id")

### Manage Permissions

You can create, update, delete or load permissions:

```java
// You can optionally set a description for a permission.
PermissionService ps = descopeClient.getManagementServices(config, projectId, client).getPermissionService();

String name = "My Permission";
String description = "Optional description to briefly explain what this permission allows.";

try {
    ps.create(name, description);
} catch (DescopeException de) {
    // Handle the error
}

// Update will override all fields as is. Use carefully.
String newName = "My Updated Permission";
description = "A revised description";

try {
    ps.update(name, newName, description);
} catch (DescopeException de) {
    // Handle the error
}

// Permission deletion cannot be undone. Use carefully.
try {
    ps.delete(newName);
} catch (DescopeException de) {
    // Handle the error
}

// Load all permissions
try {
    PermissionResponse resp = ps.loadAll();
    for (Permission p : resp.getPermissions()) {
        // Do something
    }
} catch (DescopeException de) {
    // Handle the error
}

```

### Manage Roles

You can create, update, delete or load roles:

```java
// You can optionally set a description and associated permission for a roles.
RolesService rs = descopeClient.getManagementServices(config, projectId, client).getRolesService();

String name = "My Role";
String description = "Optional description to briefly explain what this role allows.";
List<String> permissionNames = Arrays.asList("My Updated Permission");

try {
    rs.create(name, description, permissionNames);
} catch (DescopeException de) {
    // Handle the error
}

// Update will override all fields as is. Use carefully.
String newName = "My Updated Role";
description = "A revised description";
permissionNames.add("Another Permission");

try {
    rs.update(name, newName, description, permissionNames);
} catch (DescopeException de) {
    // Handle the error
}

// Role deletion cannot be undone. Use carefully.
try {
    rs.delete(newName);
} catch (DescopeException de) {
    // Handle the error
}

// Load all roles
try {
    RoleResponse resp = rs.loadAll();
    for (Role r : resp.getRoles()) {
        // Do something
    }
} catch (DescopeException de) {
    // Handle the error
}

```

### Query SSO Groups

You can query SSO groups:

```java
// Load all groups for a given tenant id
GroupService gs = descopeClient.getManagementServices(config, projectId, client).getGroupService();
try {
    List<Group> groups = gs.loadAllGroups("tenant-id");
    for (Group g : groups) {
        // Do something
    }
} catch (DescopeException de) {
    // Handle the error
}

// Load all groups for the given user/login IDs (can be found in the user's JWT, used for sign-in)
try {
    List<Group> groups = gs.loadAllGroupsForMembers("tenant-id", 
            Arrays.asList("user-id-1", "user-id-2"), 
            Arrays.asList("login-id-1", "login-id-2"));
    for (Group g : groups) {
        // Do something
    }
} catch (DescopeException de) {
    // Handle the error
}

// Load all group's members by the given group id
try {
    List<Group> groups = gs.loadAllGroupMembers("tenant-id", "group-id");
    for (Group g : groups) {
        // Do something
    }
} catch (DescopeException de) {
    // Handle the error
}

```

### Manage Flows

You can list, import and export flows and screens, or the project theme:

```java
FlowService fs = descopeClient.getManagementServices(config, projectId, client).getFlowService();

// List all your flows
try {
    List<Flow> flows = fs.listFlows();
    for (Flow f : flows) {
        // Do something
    }
} catch (DescopeException de) {
    // Handle the error
}

// Export the flow and it's matching screens based on the given id
try {
    FlowResponse resp = fs.exportFlow("sign-up");
    Flow flow = resp.getFlow();
    List<Screen> screens = resp.getScreens();
    for (Screen s : screens) {
        // Do something
    }
} catch (DescopeException de) {
    // Handle the error
}

// Import the given flow and screens as the given id
try {
    FlowResponse resp = fs.importFlow("sign-up", flow, screens);
    Flow flow = resp.getFlow();
    List<Screen> screens = resp.getScreens();
    for (Screen s : screens) {
        // Do something
    }
} catch (DescopeException de) {
    // Handle the error
}

// Export the current theme of the project
try {
    Theme t = fs.exportTheme();
    System.out.println(t.getId());
} catch (DescopeException de) {
    // Handle the error
}

// Import the given theme to the project
try {
    Theme theme = fs.importTheme(t);
    System.out.println(theme.getId());
} catch (DescopeException de) {
    // Handle the error
}

```

### Manage JWTs

You can add custom claims to a valid JWT.

```java
JwtService jwts = descopeClient.getManagementServices(config, projectId, client).getJwtService();
try {
    String res = jwts.updateJWTWithCustomClaims("original-jwt", 
            new HashMap<String, Object>() {{
                put("custom-key1", "custom-value1");
                put("custom-key2", "custom-value2");
            }});
} catch (DescopeException de) {
    // Handle the error
}

```

### Search Audit

You can perform an audit search for either specific values or full-text across the fields. Audit search is limited to
the last 30 days.

```java
AuditService as = descopeClient.getManagementServices(config, projectId, client).getAuditService();
// Full text search on the last 10 days
try {
    AuditSearchResponse resp = as.search(AuditSearchRequest.builder()
            .from(Instant.now().minus(Duration.ofDays(10))));
} catch (DescopeException de) {
    // Handle the error
}

// Search successful logins in the last 30 days
try {
    AuditSearchResponse resp = as.search(AuditSearchRequest.builder()
            .from(Instant.now().minus(Duration.ofDays(30)))
            .actions(Arrays.asList("LoginSucceed")));
} catch (DescopeException de) {
    // Handle the error
}

```

## Code Examples

You can find various usage examples in the [examples folder](https://github.com/descope/java-sdk/blob/main/examples).

### Setup

To run the examples, set your `Project ID` by setting the `DESCOPE_PROJECT_ID` env var or directly
in the sample code.
Find your Project ID in the [Descope console](https://app.descope.com/settings/project).

```bash
export DESCOPE_PROJECT_ID=<ProjectID>
```

TODO: alternative configuration

### Run an example

1. Run this command in your project to build the examples.

   ```bash
   mvn package
   ```

2. Run a specific example

   ```bash
   # CLI example
   java -jar target/management-cli.jar command-name -option1 -option2
   ```

### Using Visual Studio Code

To run Run and Debug using Visual Studio Code open the examples folder and run the ManagementCLI class

## Unit Testing and Data Mocks

Simplify your unit testing by using our mocks package for testing your app without the need of going out to Descope
services. By that, you can simply mock responses and errors and have assertion for the incoming data of each SDK method.
You can find all mocks [here](https://github.com/descope/go-sdk/blob/main/descope/tests/mocks).

Mock usage examples:

- [Authentication](https://github.com/descope/go-sdk/blob/main/descope/tests/mocks/auth/authenticationmock_test.go)
- [Management](https://github.com/descope/go-sdk/blob/main/descope/tests/mocks/mgmt/managementmock_test.go)

In the following snippet we mocked the Descope Authentication and Management SDKs, and have assertions to check the
actual inputs passed to the SDK:

```go
updateJWTWithCustomClaimsCalled := false
validateSessionResponse := "test1"
updateJWTWithCustomClaimsResponse := "test2"
api := DescopeClient{
    Auth: &mocksauth.MockAuthentication{
        MockSession: mocksauth.MockSession{
            ValidateSessionResponseSuccess: false,
            ValidateSessionResponse:        &descope.Token{JWT: validateSessionResponse},
            ValidateSessionError:           descope.ErrPublicKey,
        },
    },
    Management: &mocksmgmt.MockManagement{
        MockJWT: &mocksmgmt.MockJWT{
            UpdateJWTWithCustomClaimsResponse: updateJWTWithCustomClaimsResponse,
            UpdateJWTWithCustomClaimsAssert: func(jwt string, customClaims map[string]any) {
                updateJWTWithCustomClaimsCalled = true
                assert.EqualValues(t, "some jwt", jwt)
            },
        },
    },
}
ok, info, err := api.Auth.ValidateAndRefreshSessionWithRequest(nil, nil)
assert.False(t, ok)
assert.NotEmpty(t, info)
assert.EqualValues(t, validateSessionResponse, info.JWT)
assert.ErrorIs(t, err, descope.ErrPublicKey)

res, err := api.Management.JWT().UpdateJWTWithCustomClaims("some jwt", nil)
require.NoError(t, err)
assert.True(t, updateJWTWithCustomClaimsCalled)
assert.EqualValues(t, updateJWTWithCustomClaimsResponse, res)
```

### Utils for your end to end (e2e) tests and integration tests

To ease your e2e tests, we exposed dedicated management methods,
that way, you don't need to use 3rd party messaging services in order to receive sign-in/up Emails or SMS, and avoid the
need of parsing the code and token from them.

```java
// User for test can be created, this user will be able to generate code/link without
// the need of 3rd party messaging services.
// Test user must have a loginID, other fields are optional.
// Roles should be set directly if no tenants exist, otherwise set
// on a per-tenant basis.
UserService us = descopeClient.getManagementServices(config, projectId, client).getUserService();
try {
    UserResponseDetails resp = us.createTestUser("desmond@descope.com", UserRequest.builder()
            .email("desmond@descope.com")
            .displayName("Desmond Copeland")
            .tenants(Arrays.asList(
                AssociatedTenant.builder()
                    .tenantId("tenant-ID1")
                    .roleNames(Arrays.asList("role-name1"),
                AssociatedTenant.builder()
                    .tenantId("tenant-ID2")))));
} catch (DescopeException de) {
    // Handle the error
}

// Now test user got created, and this user will be available until you delete it,
// you can use any management operation for test user CRUD.
// You can also delete all test users.
try {
    us.deleteAllTestUsers();
} catch (DescopeException de) {
    // Handle the error
}

// OTP code can be generated for test user, for example:
try {
    String code = us.generateOtpForTestUser("desmond@descope.com", DeliveryMethod.EMAIL);
} catch (DescopeException de) {
    // Handle the error
}

// Same as OTP, magic link can be generated for test user, for example:
try {
    us.generateMagicLinkForTestUser("desmond@descope.com", "", DeliveryMethod.EMAIL);
} catch (DescopeException de) {
    // Handle the error
}

// Enchanted link can be generated for test user, for example:
try {
    us.generateEnchantedLinkForTestUser("desmond@descope.com", "");
} catch (DescopeException de) {
    // Handle the error
}

// Note 1: The generate code/link methods, work only for test users, will not work for regular users.
// Note 2: In case of testing sign-in / sign-up methods with test users, need to make sure to generate the code prior calling the sign-in / sign-up methods (such as: descopeClient.Auth.MagicLink().SignUpOrIn)
```

# API Rate Limits

Handle API rate limits by comparing the error to the ErrRateLimitExceeded error, which includes the Info map with the
key "RateLimitExceededRetryAfter." This key indicates how many seconds until the next valid API call can take place.

```java
MagicLinkService mls = descopeClient.getAuthenticationServices(config, client).getMagicLinkService();
try {
    mls.signUpOrIn(DeliveryMethod.EMAIL, "desmond@descope.com", "http://myapp.com/verify-magic-link");
} catch (DescopeException de) {
    if (de.isErrorLimitException()) {
        // TODO: How many seconds until next valid API call can take place
        // Handle the error
    }
}

```

## Learn More

To learn more please see the [Descope Documentation and API reference page](https://docs.descope.com/).

## Contact Us

If you need help you can email [Descope Support](mailto:support@descope.com)

## License

The Descope SDK for Java is licensed for use under the terms and conditions of
the [MIT license Agreement](https://github.com/descope/java-sdk/blob/main/LICENSE).
