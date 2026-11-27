# PesaPlan automated tests

The test suite contains 33 isolated unit tests using JUnit 5 and Mockito. Repository calls are mocked, so these tests do not connect to Neon or modify real user data.

Coverage includes:

- User registration, email normalization, password hashing, duplicate email, login, incorrect password and suspended accounts
- Password reset token creation, hashed storage, expiry, invalid/used tokens and password updates
- Forgot-password and reset-password controller behavior
- Admin user listing, suspension and activation
- JWT authentication for active, suspended, invalid and revoked tokens
- Logout token revocation
- Profile update behavior
- Transaction creation, listing/filter validation, ownership, and deletion

Run from the project root:

```cmd
mvnw.cmd clean test
```

The test summary from the local Maven run is authoritative. These are unit tests, not a complete integration suite. They do not execute SQL against PostgreSQL or drive the complete HTTP security filter chain through an embedded server. Use a dedicated test database and add integration tests before production deployment.

The simulated password reset endpoint returns a token in its response for class-project/Postman testing only. Do not deploy that behavior as a production password reset flow.
