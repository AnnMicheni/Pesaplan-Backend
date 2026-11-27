# PesaPlan remaining backend implementation

This bundle is prepared against the Spring Boot/JdbcTemplate project structure in the uploaded `personal-finance-manager-backend.zip`.

It contains code for the remaining work discussed:
- KAN-13: update the signed-in user's profile
- KAN-50: revoke a JWT on logout
- KAN-16 to KAN-21: income/expense/transaction model, persistence, CRUD, categories, history and filtering
- KAN-22: a manual API test checklist is provided; automated transaction tests should be added after this code compiles in the project.

## Important: merge, do not overwrite your whole project

Copy the contents of `src/main/java` into the matching `src/main/java` folder in your project. Allow new files to be added and replace only the files with matching paths included in this bundle. This bundle does not include a replacement `pom.xml`, `application.properties`, `.git`, or Maven wrapper.

The replacement files are:
- `controller/AuthController.java`
- `controller/UserController.java`
- `repository/UserRepository.java`
- `service/UserService.java`
- `security/JwtService.java`
- `security/JwtAuthenticationFilter.java`
- `security/SecurityConfig.java`

New files include transaction DTOs, models, repository, service, controller, `UpdateProfileRequest`, and `TokenRevocationRepository`.

## Database changes

In the Neon SQL editor, select `pesaplan_dev` and run these files in order:
1. `sql/001_transactions.sql`
2. `sql/002_revoked_tokens.sql`

Do not run them against production until the project is tested and deployed intentionally.

## KAN-13 profile endpoint

`PUT /api/users/me` (Bearer token)

```json
{
  "fullName": "Updated Name",
  "currencyPreference": "KES"
}
```

Email and password are intentionally not editable through this profile endpoint.

## KAN-50 logout endpoint

`POST /api/auth/logout` (Bearer token)

Expected response: `200 OK` with `{"message":"Logout successful."}`.
The token is stored as a SHA-256 hash in `revoked_tokens` until its expiry. The raw JWT is not stored.

## Transaction endpoints

All transaction endpoints require a valid Bearer token. Records are scoped to the authenticated user's ID.

- `POST /api/transactions` — create an income or expense
- `GET /api/transactions` — list history; optional filters: `type`, `category`, `fromDate`, `toDate`
- `GET /api/transactions/{id}` — get one transaction owned by the signed-in user
- `PUT /api/transactions/{id}` — edit a transaction owned by the signed-in user
- `DELETE /api/transactions/{id}` — delete a transaction owned by the signed-in user
- `GET /api/transactions/categories` — categories already used by this user
- `GET /api/transactions/category-totals` — totals grouped by category; optional `type`, `fromDate`, `toDate`

Create/update JSON example:

```json
{
  "type": "EXPENSE",
  "amount": 1250.00,
  "category": "Food",
  "description": "Groceries",
  "transactionDate": "2026-11-27"
}
```

`type` must be `INCOME` or `EXPENSE`; amount must be greater than zero; category and transaction date are required. Dates use `YYYY-MM-DD`.

## Build and test

After merging the files and running both SQL scripts:

```cmd
mvnw.cmd clean test
```

Then start the application:

```cmd
mvnw.cmd spring-boot:run
```

Run the requests in `README/POSTMAN_CHECKLIST.md`. Do not commit `.env`, database passwords, JWT secrets, or real access tokens.

## Notes / limits

- Password reset in the current project returns a reset token in the HTTP response for Postman testing. This is a simulation only; do not deploy it as a production password reset mechanism. In production, send the token through email and never return it in the API response.
- Logout revocation requires the new `revoked_tokens` table and the new filter. Without that table, the updated application will fail at runtime when checking a presented token.
- The current password reset and JWT implementation are not changed beyond the logout-related JWT service addition.
- KAN-22 is not marked complete merely by adding endpoints. Run and document the test checklist, and add automated tests for transaction service/repository/controller before marking the test task complete.
- If your team has a formal API contract for transaction field names, categories, or endpoints, align this implementation with that contract before merging it into the shared branch.
