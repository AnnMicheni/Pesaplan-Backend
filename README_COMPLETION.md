# PesaPlan backend — merged completion pack

This package is based on the PesaPlan backend ZIP supplied for this task. It preserves the existing Spring Boot 4.1.2, JdbcTemplate, PostgreSQL, JWT and package structure, and adds:

- KAN-13: update the authenticated user's profile
- KAN-50: logout by revoking the current JWT until its expiry
- KAN-16 to KAN-21: transaction/income/expense model, persistence, CRUD, categories, history and filters
- Automated unit tests for existing auth/admin/password-reset behavior plus transaction, profile and logout service/controller behavior

## IMPORTANT — merge safely

This archive deliberately does not contain `.git`, `target/`, or a real `application.properties` file.

1. Close VS Code or stop the running backend.
2. Make a backup copy of your existing project folder.
3. Extract this ZIP into your existing project folder, allowing the `src` folders to merge and source files to be replaced.
4. Keep your existing `.git` directory and your existing `src/main/resources/application.properties` file. Do not replace it with the `.example` file unless your local one is missing.
5. The ZIP includes the existing `pom.xml` and Maven wrapper scripts plus `.mvn/wrapper/maven-wrapper.properties`. If you prefer not to replace your local wrapper setup, keep your existing `mvnw`, `mvnw.cmd`, and `.mvn` folder instead.

Do not delete your original project folder and do not create a new Git repository for this merge. This lets you retain your Git history and environment variables.

## Environment variables

Your existing `application.properties` should continue using environment variables, not hard-coded credentials:

- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `JWT_SECRET`

A safe template is provided as `src/main/resources/application.properties.example`. Do not commit secrets, passwords, access tokens, or `.env` files.

## Database migrations

In Neon SQL Editor, select the **pesaplan_dev** database and run these files in order:

1. `sql/001_transactions.sql`
2. `sql/002_revoked_tokens.sql`
3. `sql/003_password_reset_tokens.sql`

All three scripts use `IF NOT EXISTS` where appropriate. The password-reset migration is included for reproducibility; your existing `password_reset_tokens` table should remain intact.

Do not run these scripts against production as part of this development task.

## Build and test

From the project root in Windows CMD:

```cmd
mvnw.cmd clean test
```

Then start the backend:

```cmd
mvnw.cmd spring-boot:run
```

Run the database migrations before starting the application if they have not already been applied.

## Profile endpoints (KAN-13)

- `GET /api/users/me`
- `PUT /api/users/me`

Example update body:

```json
{
  "fullName": "Updated Test User",
  "currencyPreference": "KES"
}
```

Email and password are not editable through this profile endpoint.

## Logout endpoint (KAN-50)

- `POST /api/auth/logout` — requires a Bearer token.

The presented token is stored as a SHA-256 hash in `revoked_tokens` until its expiry. The raw JWT is not stored. Reusing the logged-out token on a protected endpoint should return 401. A new login issues a new token.

## Transaction endpoints (KAN-16 to KAN-21)

All transaction endpoints require authentication and scope records to the signed-in user's ID:

- `POST /api/transactions` — create an income or expense
- `GET /api/transactions` — list transaction history; optional `type`, `category`, `fromDate`, `toDate` filters
- `GET /api/transactions/{id}` — retrieve one owned transaction
- `PUT /api/transactions/{id}` — update one owned transaction
- `DELETE /api/transactions/{id}` — delete one owned transaction
- `GET /api/transactions/categories` — categories used by the current user
- `GET /api/transactions/category-totals` — totals grouped by category, optionally filtered by type/date

Example create/update JSON:

```json
{
  "type": "EXPENSE",
  "amount": 850.00,
  "category": "Food",
  "description": "Lunch",
  "transactionDate": "2026-11-27"
}
```

`type` must be `INCOME` or `EXPENSE`; `amount` must be greater than zero; `category` and `transactionDate` are required. Dates use `YYYY-MM-DD`.

## Automated tests and manual verification

- `docs/AUTOMATED_TESTS.md` describes the isolated unit tests and their limitations.
- `docs/POSTMAN_CHECKLIST.md` lists manual API requests for profile, logout and transaction testing.

The tests use mocks for repositories/services and do not write to Neon. They are not a substitute for a database integration test or full end-to-end HTTP security test. Run `mvnw.cmd clean test` locally before committing and use the actual Maven summary to record the result.

## Important limitations

- The current password reset is a simulated class-project flow that returns the reset token in the API response for Postman testing. It is not production-safe. A production implementation should email a one-time link and must not return the token in the API response.
- Confirm the transaction API contract with your frontend teammates before merging to a shared branch, especially the field names and filter parameter names.
- The unit tests do not prove every endpoint or SQL query works against PostgreSQL. Apply the migrations to the dev database and complete the Postman checklist.
