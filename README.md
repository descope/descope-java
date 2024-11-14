[![CI](https://github.com/descope/descope-java/actions/workflows/ci.yaml/badge.svg)](https://github.com/descope/descope-java/actions/workflows/ci.yaml)

# Descope SDK for Java

The Descope SDK for Java provides convenient access to the Descope user management and authentication API
for a backend written in Java. You can read more on the [Descope Website](https://descope.com).

## Requirements

The SDK supports Java version 8 and above.

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
// We can also take these variables from .env file in the running directory
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

These sections show how to use the SDK to perform API management functions. Before using any of them, you will need to create a Management Key. The instructions for this can be found under [Setup](#setup-1).

1. [Manage Tenants](#manage-tenants)
2. [Manage Users](#manage-users)
3. [Manage Access Keys](#manage-access-keys)
4. [Manage SSO Setting](#manage-sso-setting)
5. [Manage Permissions](#manage-permissions)
6. [Manage Roles](#manage-roles)
7. [Query SSO Groups](#query-sso-groups)
8. [Manage Flows](#manage-flows)
9. [Manage JWTs](#manage-jwts)
10. [Audit](#audit)
11. [Manage Project](#manage-project)

If you wish to run any of our code samples and play with them, check out our [Code Examples](#code-examples) section.

If you're developing unit tests, see how you can use our mocks package underneath the [Unit Testing and Data Mocks](#unit-testing-and-data-mocks) section.

If you're performing end-to-end testing, check out the [Utils for your end to end (e2e) tests and integration tests](#utils-for-your-end-to-end-e2e-tests-and-integration-tests) section. You will need to use the `descopeClient` object created under [Setup](#setup-1) guide.

For rate limiting information, please confer to the [API Rate Limits](#api-rate-limits) section.

---

### OTP Authentication

Send a user a one-time password (OTP) using your preferred delivery method (_email / SMS / Voice call / WhatsApp_). An email address or phone number must be provided accordingly.

The user can either `sign up`, `sign in` or `sign up or in`

```java
// Every user must have a loginID. All other user information is optional
String loginId = "desmond@descope.com"
User user = User.builder()
    .name("Desmond Copeland")
    .phone("212-555-1234")
    .email(loginId)
    .build();
OTPService otps = descopeClient.getAuthenticationServices().getOTPService();
try {
  String maskedAddress = otps.signUp(DeliveryMethod.EMAIL, loginId, user);
} catch (DescopeException de) {
  // Handle the error
}
```

The user will receive a code using the selected delivery method. Verify that code using:

```java
// Will throw DescopeException if there is an error with update
try {
  AuthenticationInfo info = otps.verifyCode(DeliveryMethod.EMAIL, loginId, code);
} catch (DescopeException de) {
  // Handle the error
}
```

The session and refresh JWTs should be returned to the caller, and passed with every request in the session. Read more on [session validation](#session-validation)

### Magic Link

Send a user a Magic Link using your preferred delivery method (_email / SMS / WhatsApp_).
The Magic Link will redirect the user to page where the its token needs to be verified.
This redirection can be configured in code, or globally in the [Descope Console](https://app.descope.com/settings/authentication/magiclink)

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

MagicLinkService mls = descopeClient.getAuthenticationServices().getMagicLinkService();

try {
    String uri = "http://myapp.com/verify-magic-link";
    String maskedAddress = mls.signUp(DeliveryMethod.EMAIL, loginId, uri, user);
} catch (DescopeException de) {
    // Handle the error
}

```

To verify a magic link, your redirect page must call the validation function on the token (`t`) parameter (`https://your-redirect-address.com/verify?t=<token>`):

```java
try {
    AuthenticationInfo info = mls.verify(token);
} catch (DescopeException de) {
    // Handle the error
}

```

The session and refresh JWTs should be returned to the caller, and passed with every request in the session. Read more on [session validation](#session-validation)

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
This redirection can be configured in code per request, or set globally in the [Descope Console](https://app.descope.com/settings/authentication/enchantedlink).

The user can either `sign up`, `sign in` or `sign up or in`

```java
// If configured globally, the redirect URI is optional. If provided however, it will be used
// instead of any global configuration.

EnchantedLinkService els = descopeClient.getAuthenticationServices().getEnchantedLinkService();
EnchantedLinkResponse res = null;
try {
    String uri = "http://myapp.com/verify-enchanted-link";
    res = els.signUp(loginId, uri, user);
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

To verify an enchanted link, your redirect page must call the validation function on the token (`t`) parameter (`https://your-redirect-address.com/verify?t=<token>`). Once the token is verified, the session polling will receive a valid response.

```java

try {
    els.verify(token);
} catch (DescopeException de) {
    // Token is invalid, handle the error
}

```

The session and refresh JWTs should be returned to the caller, and passed with every request in the session. Read more on [session validation](#session-validation)

### OAuth

Users can authenticate using their social logins, using the OAuth protocol. Configure your OAuth settings on the [Descope console](https://app.descope.com/settings/authentication/social). To start a flow call:

```java
// Choose an oauth provider out of the supported providers
// If configured globally, the return URL is optional. If provided however, it will be used
// instead of any global configuration.
// Redirect the user to the returned URL to start the OAuth redirect chain
OAuthService oas = descopeClient.getAuthenticationServices().getOAuthService();

try {
    String returnUrl = "https://my-app.com/handle-oauth";
    oas.start("google", returnUrl, loginOptions);
} catch (DescopeException de) {
    // Handle the error
}

```

The user will authenticate with the authentication provider, and will be redirected back to the redirect URL, with an appended `code` HTTP URL parameter. Exchange it to validate the user:

```java

try {
    AuthenticationInfo info = oas.exchangeToken(code);
} catch (DescopeException de) {
    // Handle the error
}

```

The session and refresh JWTs should be returned to the caller, and passed with every request in the session. Read more on [session validation](#session-validation)

### SSO/SAML

Users can authenticate to a specific tenant using SAML or Single Sign On. Configure your SSO/SAML settings on the [Descope console](https://app.descope.com/settings/authentication/sso). To start a flow call:

```java
// Choose which tenant to log into
// Redirect the user to the returned URL to start the SSO/SAML redirect chain
SAMLService ss = descopeClient.getAuthenticationServices().getSAMLService();

try {
    String returnURL = "https://my-app.com/handle-saml";
    String url = ss.start("my-tenant-ID", returnURL, loginOptions);
} catch (DescopeException de) {
    // Handle the error
}

```

The user will authenticate with the authentication provider configured for that tenant, and will be redirected back to the redirect URL, with an appended `code` HTTP URL parameter. Exchange it to validate the user:

```java
// The optional `w http.ResponseWriter` adds the session and refresh cookies to the response automatically.
// Otherwise they're available via authInfo
try {
    String url = ss.exchangeToken(code);
} catch (DescopeException de) {
    // Handle the error
}

```

The session and refresh JWTs should be returned to the caller, and passed with every request in the session. Read more on [session validation](#session-validation)

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

TOTPService ts = descopeClient.getAuthenticationServices().getTOTPService();

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

The session and refresh JWTs should be returned to the caller, and passed with every request in the session. Read more on [session validation](#session-validation)

### Passwords

The user can also authenticate with a password, though it's recommended to
prefer passwordless authentication methods if possible. Sign up requires the
caller to provide a valid password that meets all the requirements configured
for the [password authentication method](https://app.descope.com/settings/authentication/password) in the Descope console.

```java
// Every user must have a loginID. All other user information is optional
String loginId = "desmond@descope.com";
User user = User.builder()
    .name("Desmond Copeland")
    .phone("212-555-1234")
    .email(loginId)
    .build();
String password = "qYlvi65KaX";

PasswordService ps = descopeClient.getAuthenticationServices().getPasswordService();

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

The session and refresh JWTs should be returned to the caller, and passed with every request in the session. Read more on [session validation](#session-validation)

In case the user needs to update their password, one of two methods are available: Resetting their password or replacing their password

**Changing Passwords**

_NOTE: SendPasswordReset will only work if the user has a validated email address. Otherwise password reset prompts cannot be sent._

In the [password authentication method](https://app.descope.com/settings/authentication/password) in the Descope console, it is possible to define which alternative authentication method can be used in order to authenticate the user, in order to reset and update their password.

```java
// Start the reset process by sending a password reset prompt. In this example we'll assume
// that magic link is configured as the reset method. The optional redirect URL is used in the
// same way as in regular magic link authentication.
PasswordService ps = descopeClient.getAuthenticationServices().getPasswordService();
String loginId = "desmond@descope.com";
String redirectUrl = "https://myapp.com/password-reset";
try {
    ps.sendPasswordReset(loginId, redirectUrl);
} catch (DescopeException de) {
    // Handle the error
}

```

The magic link, in this case, must then be verified like any other magic link (see the [magic link section](#magic-link) for more details).
However, after verifying the user, it is expected to allow them to provide a new password instead of the old one.
Since the user is now authenticated, this is possible with the refresh token received from the verify:

```java
try {
    ps.updateUserPassword(loginId, newPassword, refreshToken);
} catch (DescopeException de) {
    // Handle the error
}

```

`UpdateUserPassword` can always be called when the user is authenticated and has a valid session.

Alternatively, it is also possible to replace an existing active password with a new one.

```java
// Replaces the user's current password with a new one
try {
    AuthenticationInfo info = ps.replaceUserPassword(loginId, oldPassword, newPassword);
} catch (DescopeException de) {
    // Handle the error
}

```

### Session Validation

Every secure request performed between your client and server needs to be validated.

Tokens can be validated directly:

```java
// Validate the session. Will return an error if expired
AuthenticationService as = descopeClient.getAuthenticationServices().getAuthenticationService();
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

If Roles & Permissions are used, validate them immediately after validating the session. See the [next section](#roles--permission-validation)
for more information.

#### Session Validation Using Middleware

Alternatively, you can validate the session using Spring Framework middleware. See example using [java-spring](https://github.com/descope/java-spring).

### Roles & Permission Validation

When using Roles & Permission, it's important to validate the user has the required
authorization immediately after making sure the session is valid. Taking the `sessionToken`
received by the [session validation](#session-validation), call the following functions:

For multi-tenant uses:

```java
// You can validate specific permissions
AuthenticationService as = descopeClient.getAuthenticationServices().getAuthenticationService();
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

// Or get the matched roles/permissions
List<String> matchedPermissions = as.getMatchedPermissions(sessionToken, "my-tenant-ID", Arrays.asList("Permission1", "Permission2"));

List<String> matchedRoles = as.getMatchedRoles(sessionToken, "my-tenant-ID", Arrays.asList("Role1", "Role2"));
```

When not using tenants use:

```java
// You can validate specific permissions
AuthenticationService as = descopeClient.getAuthenticationServices().getAuthenticationService();
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

// Or get the matched roles/permissions
List<String> matchedPermissions = as.getMatchedPermissions(sessionToken, Arrays.asList("Permission1", "Permission2"));

List<String> matchedRoles = as.getMatchedRoles(sessionToken, Arrays.asList("Role1", "Role2"));
```

### Logging Out

You can log out a user from an active session by providing their `refreshToken` for that session.
After calling this function, you must invalidate or remove any cookies you have created.

```java
AuthenticationService as = descopeClient.getAuthenticationServices().getAuthenticationService();
try {
    as.logout(refreshToken);
} catch (DescopeException de) {
    // Handle the error
}

```

It is also possible to sign the user out of all the devices they are currently signed-in with. Calling `logoutAll` will
invalidate all user's refresh tokens. After calling this function, you must invalidate or remove any cookies you have created.

```java
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
TenantService ts = descopeClient.getManagementServices().getTenantService();
// The self provisioning domains or optional. If given they'll be used to associate
// Users logging in to this tenant
try {
    ts.create("My Tenant", Arrays.asList("domain.com"), new HashMap<String, Object>() {{
                put("custom-attribute-1", "custom-value1");
                put("custom-attribute-2", "custom-value2");
            }});
} catch (DescopeException de) {
    // Handle the error
}

// You can optionally set your own ID when creating a tenant
try {
    ts.createWithId("my-custom-id", "My Tenant", Arrays.asList("domain.com"), new HashMap<String, Object>() {{
                put("custom-attribute-1", "custom-value1");
                put("custom-attribute-2", "custom-value2");
            }});
} catch (DescopeException de) {
    // Handle the error
}

// Update will override all fields as is. Use carefully.
try {
    ts.update("my-custom-id", "My Tenant", Arrays.asList("domain.com", "another-domain.com"), new HashMap<String, Object>() {{
                put("custom-attribute-1", "custom-value1");
                put("custom-attribute-2", "custom-value2");
            }});
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

// Search tenants
try {
    List<Tenant> tenants = ts.searchAll(TenantSearchRequest.builder()
            .ids(Arrays.asList("my-custom-id"))
            .names(Arrays.asList("My Tenant"))
            .customAttributes(Map.of("custom-attribute-1", "custom-value1"))
            .selfProvisioningDomains(Arrays.asList("domain.com", "another-domain.com")));
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
// Roles should be set directly if no tenants exist, otherwise set on a per-tenant basis.
UserService us = descopeClient.getManagementServices().getUserService();
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
// You can configure the invite URL in the Descope console prior to using this function, or pass inviteUrl in the options parameter.
// and that an email address is provided in the information.
try {
    us.invite("desmond@descope.com",
						UserRequest.builder()
            .email("desmond@descope.com")
            .displayName("Desmond Copeland")
            .tenants(Arrays.asList(
                AssociatedTenant.builder()
                    .tenantId("tenant-ID1")
                    .roleNames(Arrays.asList("role-name1"),
                AssociatedTenant.builder()
                    .tenantId("tenant-ID2")))),
						InviteOptions.builder()
						.inviteUrl("https://my-app.com/invite")
					);
} catch (DescopeException de) {
    // Handle the error
}

// Update will override all fields as is. Use carefully. Use patch instead if providing select fields.
try {
    us.update("desmond@descope.com", UserRequest.builder()
            .email("desmond@descope.com")
            .displayName("Desmond Copeland")
            .tenants(Arrays.asList(
                AssociatedTenant.builder()
                    .tenantId("tenant-ID1")
                    .roleNames(Arrays.asList("role-name1"),
                AssociatedTenant.builder()
                    .tenantId("tenant-ID2"))))
            .build());
} catch (DescopeException de) {
    // Handle the error
}

// Patch will override provided fields but will leave other fields untouched.
try {
    us.patch("desmond@descope.com", PatchUserRequest.builder()
            .name("Desmond Copeland")
            .build());
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

You can set a new active password for a user, which they can then use to sign in. You can also set a temporary
password that the user will be forced to change on the next login.

```java
UserService us = descopeClient.getManagementServices().getUserService();

// Set a temporary user's password
try {
    us.setTemporaryPassword("my-custom-id", "some-password");
} catch (DescopeException de) {
    // Handle the error
}

// Set a user's password
try {
    us.setActivePassword("my-custom-id", "some-password");
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
PasswordService ps = descopeClient.getAuthenticationServices().getPasswordService();
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
AccessKeyService aks = descopeClient.getManagementServices().getAccessKeyService();
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
SsoService ss = descopeClient.getManagementServices().getSsoService();
// You can get SSO settings for a specific tenant ID
try {
    SSOSettingsResponse resp = ss.loadSettings("tenant-id");
} catch (DescopeException de) {
    // Handle the error
}

// Configure SSO - SAML
String tenantId = "tenant-id"; // Which tenant this configuration is for
String idpUrl = "https://idp.com";
String entityId = "my-idp-entity-id";
String idpCert = "<your-cert-here>";
String idpMetadataUrl = "https://idp.com/metadata";
String redirectUrl = "https://my-app.com/handle-saml"; // Global redirect URL for SSO/SAML
List<String> domains = Arrays.asList("domain.com"); // Users logging in from this domain will be logged in to this tenant

// Map IDP groups to Descope roles, or map user attributes.
// This function overrides any previous mapping (even when empty). Use carefully.
List<RoleMapping> rm = Arrays.asList(new RoleMapping(Arrays.asList("Groups"), "Tenant Role"));
AttributeMapping am = new AttributeMapping("Tenant Name", "Tenant Email", "Tenant Phone Num", "Tenant Group");


// Using Manual Configuration
SSOSAMLSettings manualSettings = new SSOSAMLSettings(idpUrl, entityId, idpCert, am, rm);

try {
    ss.configureSAMLSettings(tenantId, manualSettings, domains);
} catch (DescopeException de) {
    // Handle the error
}

// Using metadata URL
SSOSAMLSettingsByMetadata metadataSettings = new SSOSAMLSettingsByMetadata(idpMetadataUrl ,am, rm);

try {
    ss.configureSAMLSettingsByMetadata(tenantId, metadataSettings, domains);
} catch (DescopeException de) {
    // Handle the error
}

// Configure SSO - OIDC
String name = "Provider"; // Name of the provider
String clientId = "<oidc-client-id>"; // The client id set on the IdP
String clientSecret = "<oidc-client-secret>"; // The client secret on the IdP
String redirectUrl = "https://my-app.com/redirect"; // Optional - a custom redirect url
String authUrl = "https://idp.com/auth"; // The IdP's authentication endpoint
String tokenUrl = "https://idp.com/token"; // The IdP's token endpoint
String userDataUrl = "https://idp.com/user"; // The IdP's user endpoint
List<String> scope = Arrays.asList("openid", "profile"); // The scopes
String grantType = "implicit"; // The grant type
List<String> domains = Arrays.asList("domain.com"); // Users logging in from this domain will be logged in to this tenant


SSOOIDCSettings oidcSettings = new SSOOIDCSettings(name, clientId, clientSecret, redirectUrl, authUrl, tokenUrl, userDataUrl, scope, grantType);

try {
    ss.configureSAMLSettingsByMetadata(tenantId, oidcSettings, domains);
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

```java
// To delete SSO settings, call the following method
try {
    ss.deleteSettings(tenantId);
} catch (DescopeException de) {
    // Handle the error
}
```

### Manage Permissions

You can create, update, delete or load permissions:

```java
// You can optionally set a description for a permission.
PermissionService ps = descopeClient.getManagementServices().getPermissionService();

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
RolesService rs = descopeClient.getManagementServices().getRolesService();

String name = "My Role";
String description = "Optional description to briefly explain what this role allows.";
List<String> permissionNames = Arrays.asList("My Updated Permission");

// In case roles are on tenant scope, use the overloaded functions that has the tenantId parameter

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

// Search roles
try {
    RoleResponse resp = rs.search(RoleSearchOptions.builder().tenantIds(Arrays.asList(tid)).build());
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
GroupService gs = descopeClient.getManagementServices().getGroupService();
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
FlowService fs = descopeClient.getManagementServices().getFlowService();

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
JwtService jwts = descopeClient.getManagementServices().getJwtService();
try {
    String res = jwts.updateJWTWithCustomClaims("original-jwt",
            new HashMap<String, Object>() {{
                put("custom-key1", "custom-value1");
                put("custom-key2", "custom-value2");
            }}).getJwt();
} catch (DescopeException de) {
    // Handle the error
}

```

### Audit

You can perform an audit search for either specific values or full-text across the fields. Audit search is limited to the last 30 days.

```java
AuditService as = descopeClient.getManagementServices().getAuditService();
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
            .actions(Arrays.asList("LoginSucceed"))
            .build());
} catch (DescopeException de) {
    // Handle the error
}

You can also create audit event with data

```java
try {
    as.createEvent(AuditCreateRequest.builder()
            .userId("some-id")
            .actorId("some-actor-id")
            .type(AuditType.INFO)
            .action("some-action-name")
            .build());
} catch (DescopeException de) {
    // Handle the error
}
```
### Manage Project

You can change the project name, as well as to clone the current project to a new one.

```java
ProjectService ps = descopeClient.getManagementServices().getProjectService();
// Change the project name
try {
    ps.updateName("New Project Name");
} catch (DescopeException de) {
    // Handle the error
}

// Clone the current project to a new one
// Note that this action is supported only with a pro license or above.
try {
    NewProjectResponse resp = ps.cloneProject("New Project Name", ProjectTag.None);
} catch (DescopeException de) {
    // Handle the error
}

```

You can manage your project's settings and configurations by exporting your project's environment.

```java
// Exports the current state of the project
try {
    ExportProjectResponse resp = ps.exportProject();
} catch (DescopeException de) {
    // Handle the error
}

```

You can also import previously exported data into the same project or a different one.

```java
try {
    // Load data from a previous export of this project or some other one
    Map<String, Object> files = ...

    // Update the current project's settings to mirror those in the exported data
    ps.importProject(files);
} catch (DescopeException de) {
    // Handle the error
}

```

## Code Examples

You can find various usage examples in the [examples folder](https://github.com/descope/java-sdk/blob/main/examples).

### Setup

To run the examples, set your `Project ID` and `Management Key` by setting the `DESCOPE_PROJECT_ID` and `DESCOPE_MANAGEMENT_KEY` env vars or directly in the sample code.
Find your Project ID in the [Descope console](https://app.descope.com/settings/project).
Find your management key in the [Descope console](https://app.descope.com/settings/company/managementkeys).

```bash
export DESCOPE_PROJECT_ID=<ProjectID>
export DESCOPE_MANAGEMENT_KEY=<ManagementKey>
```

Alternatively, you can create a `.env` file in the working folder with your project ID and management key.

```
DESCOPE_PROJECT_ID=<ProjectID>
DESCOPE_MANAGEMENT_KEY=<ManagementKey>
```

### Run an example

1. Make sure that the main Descope java-sdk is installed in the local Maven. Run this in the main folder.
   ```bash
   mvn install
   ```
2. Run this command in your project to build the example in the `examples/management-cli` folder.

   ```bash
   mvn package
   ```

3. Run a specific example

   ```bash
   # CLI example
   java -jar target/management-cli-1.0.jar command-name -option1 -option2
   # For example to display 10 users:
   java -jar target/management-cli-1.0.jar user-search-all -l 10
   # Help is available on all commands and within the command itself:
   java -jar target/management-cli-1.0.jar -h
   java -jar target/management-cli-1.0.jar user-search-all -h
   ```

### Using Visual Studio Code

To run Run and Debug using Visual Studio Code open the examples folder and run the ManagementCLI class

## Unit Testing and Data Mocks

Java provides a very simple way to mock services and objects using the Mockito package.
Here is a simple example of how you can mock a magic link verify response.

```java
User user = new User("someUserName", MOCK_EMAIL, "+1-555-555-5555");
ApiProxy apiProxy = mock(ApiProxy.class); // Mock the proxy that actually sends the HTTP requests
var maskedEmailRes = new MaskedEmailRes(MOCK_MASKED_EMAIL); // Define expected response
doReturn(maskedEmailRes).when(apiProxy).post(any(), any(), any()); // When post is called, return mock response
try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) { // Mock proxy builder
    mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy); // to return our mock proxy
    String signUp = magicLinkService.signUp(DeliveryMethod.EMAIL, MOCK_EMAIL, MOCK_DOMAIN, user); // call the service as you usually would
}

// Now mock the verify
ApiProxy apiProxy = mock(ApiProxy.class);
doReturn(MOCK_JWT_RESPONSE).when(apiProxy).post(any(), any(), any()); // Return the mock JWT response
doReturn(new SigningKeysResponse(Arrays.asList(MOCK_SIGNING_KEY))).when(apiProxy).get(any(), eq(SigningKeysResponse.class)); // Return mock key
var provider = mock(Provider.class);
when(provider.getProvidedKey()).thenReturn(mock(Key.class));

AuthenticationInfo authenticationInfo;
try (MockedStatic<ApiProxyBuilder> mockedApiProxyBuilder = mockStatic(ApiProxyBuilder.class)) {
    mockedApiProxyBuilder.when(() -> ApiProxyBuilder.buildProxy(any(), any())).thenReturn(apiProxy); // Return proxy when building
    try (MockedStatic<JwtUtils> mockedJwtUtils = mockStatic(JwtUtils.class)) {
        mockedJwtUtils.when(() -> JwtUtils.getToken(anyString(), any())).thenReturn(MOCK_TOKEN); // Return mock token instead of parsing
        authenticationInfo = magicLinkService.verify("SomeToken");
    }
}

```

### Utils for your end to end (e2e) tests and integration tests

To ease your e2e tests, we exposed dedicated management methods.
That way, you don't need to use 3rd party messaging services in order to receive sign-in/up Email, SMS, Voice call or WhatsApp,
and avoid the need of parsing the code and token from them.

```java
// User for test can be created, this user will be able to generate code/link without
// the need of 3rd party messaging services.
// Test user must have a loginID, other fields are optional.
// Roles should be set directly if no tenants exist, otherwise set
// on a per-tenant basis.
UserService us = descopeClient.getManagementServices().getUserService();
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
    OTPTestUserResponse res = us.generateOtpForTestUser("desmond@descope.com", DeliveryMethod.EMAIL);
    // Use res.getCode() for verify and establishing a session
} catch (DescopeException de) {
    // Handle the error
}

// Same as OTP, magic link can be generated for test user, for example:
try {
    MagicLinkTestUserResponse res = us.generateMagicLinkForTestUser("desmond@descope.com", "", DeliveryMethod.EMAIL);
    // Use res.getLink() to get the generated link. To get the actual token, use:
    // var params = UriUtils.splitQuery("https://example.com" + res.getLink());
    // var authInfo = magicLinkService.verify(params.get("t").get(0));

} catch (DescopeException de) {
    // Handle the error
}

// Enchanted link can be generated for test user, for example:
try {
    EnchantedLinkTestUserResponse res = us.generateEnchantedLinkForTestUser("desmond@descope.com", "");
    // Use res.getLink() to get the generated link. To get the actual token, use:
    // var params = UriUtils.splitQuery("https://example.com" + res.getLink());
    // enchantedLinkService.verify(params.get("t").get(0));
    // var authInfo = enchantedLinkService.getSession(res.getPendingRef());
} catch (DescopeException de) {
    // Handle the error
}

// Note 1: The generate code/link methods, work only for test users, will not work for regular users.
// Note 2: In case of testing sign-in / sign-up methods with test users, need to make sure to generate the code prior calling the sign-in / sign-up methods

// Embedded links can be created to directly receive a verifiable token without sending it.
// This token can then be verified using the magic link 'verify' function, either directly or through a flow.
UserService us = descopeClient.getManagementServices().getUserService();
MagicLinkService mls = descopeClient.getAuthenticationServices().getMagicLinkService();
try {
    String token - us.generateEmbeddedLink("desmond@descope.com", null /*custom claims if any */);
    var authInfo = mls.verify(token);
} catch (DescopeException de) {
    // Handle the error
}
token, err := descopeClient.Management.User().GenerateEmbeddedLink("desmond@descope.com", map[string]any{"key1":"value1"})
```

# API Rate Limits

Handle API rate limits by catching the RateLimitExceededException.
The exception includes the number of seconds until the next valid API call can take place.

```java
MagicLinkService mls = descopeClient.getAuthenticationServices().getMagicLinkService();
try {
    mls.signUpOrIn(DeliveryMethod.EMAIL, "desmond@descope.com", "http://myapp.com/verify-magic-link");
} catch (RateLimitExceededException re) {
    // Use re.getRetryAfterSeconds() to determine time until retry
} catch (DescopeException de) {
    // Handle the error
}

```

## Learn More

To learn more please see the [Descope Documentation and API reference page](https://docs.descope.com/).

## Contact Us

If you need help you can email [Descope Support](mailto:support@descope.com)

## License

The Descope SDK for Java is licensed for use under the terms and conditions of the [MIT license Agreement](https://github.com/descope/descope-java/blob/main/LICENSE).
