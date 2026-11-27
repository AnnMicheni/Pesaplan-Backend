# PesaPlan Postman checklist

Use Postman Desktop. Replace `YOUR_TOKEN` with a fresh access token kept locally in Postman. Do not paste tokens into chat or commit them.

## 1. Update profile (KAN-13)
PUT `http://localhost:8080/api/users/me`
Authorization: Bearer Token

```json
{
  "fullName": "Updated Test User",
  "currencyPreference": "KES"
}
```
Expected: 200. Then GET `/api/users/me` and verify the changed fields.

## 2. Logout (KAN-50)
POST `http://localhost:8080/api/auth/logout`
Authorization: Bearer Token
Expected: 200 and `Logout successful.`

Then reuse the same token on GET `/api/users/me`. Expected: 401.
Log in again to get a fresh token for the transaction tests.

## 3. Create an expense (KAN-16/17)
POST `http://localhost:8080/api/transactions`
Authorization: Bearer Token

```json
{
  "type": "EXPENSE",
  "amount": 850.00,
  "category": "Food",
  "description": "Lunch",
  "transactionDate": "2026-11-27"
}
```
Expected: 201. Save the returned `id` for later requests.

## 4. Create income (KAN-16/17)
POST `/api/transactions`

```json
{
  "type": "INCOME",
  "amount": 45000.00,
  "category": "Salary",
  "description": "Monthly salary",
  "transactionDate": "2026-11-27"
}
```
Expected: 201.

## 5. View transaction history (KAN-20)
GET `/api/transactions`
Expected: 200 with only the signed-in user's records.

## 6. Filter history (KAN-20)
GET `/api/transactions?type=EXPENSE&category=Food&fromDate=2026-11-01&toDate=2026-11-30`
Expected: 200 with matching records.

## 7. View categories and category totals (KAN-19)
GET `/api/transactions/categories`
GET `/api/transactions/category-totals`
GET `/api/transactions/category-totals?type=EXPENSE`
Expected: 200.

## 8. View one transaction (KAN-18)
GET `/api/transactions/TRANSACTION_ID`
Expected: 200 for an owned record; 404 for another user's record or unknown ID.

## 9. Edit transaction (KAN-18)
PUT `/api/transactions/TRANSACTION_ID`

```json
{
  "type": "EXPENSE",
  "amount": 900.00,
  "category": "Food",
  "description": "Updated lunch amount",
  "transactionDate": "2026-11-27"
}
```
Expected: 200 with updated data.

## 10. Delete transaction (KAN-18)
DELETE `/api/transactions/TRANSACTION_ID`
Expected: 204. GET the same ID afterward; expected 404.

## 11. Validation and ownership checks (KAN-22)
- Try amount `0`: expected 400.
- Try type `TRANSFER`: expected 400.
- Try a date range where `fromDate` is after `toDate`: expected 400.
- Try using User A's token to access User B's transaction ID: expected 404.
- Try all transaction routes without a token: expected 401.
