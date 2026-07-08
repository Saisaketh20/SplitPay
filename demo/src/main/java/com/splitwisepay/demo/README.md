# SplitPay

A group expense splitting REST API built with Java Spring Boot.
Think Splitwise — track who paid what and who owes whom.

## Tech Stack
Java 17, Spring Boot 3.2.5, Spring Security + JWT,
MySQL 8, Flyway, Spring Data JPA

## Features
- JWT Authentication (register/login)
- Group management with role-based access
- Expense tracking with EQUAL/PERCENT/EXACT split types
- Auto balance calculation
- Settlement tracking
- Admin dashboard

## API Endpoints
### Auth
POST /api/v1/auth/register
POST /api/v1/auth/login

### Groups
POST   /api/v1/groups
GET    /api/v1/groups
GET    /api/v1/groups/{id}
POST   /api/v1/groups/{id}/members
DELETE /api/v1/groups/{id}/members/{userId}
DELETE /api/v1/groups/{id}

### Expenses
POST   /api/v1/groups/{id}/expenses
GET    /api/v1/groups/{id}/expenses
GET    /api/v1/groups/{id}/expenses/{expenseId}
DELETE /api/v1/groups/{id}/expenses/{expenseId}
GET    /api/v1/groups/{id}/expenses/balances
POST   /api/v1/groups/{id}/expenses/settle

### Users
GET /api/v1/users/me
PUT /api/v1/users/me
GET /api/v1/users/search?email=

### Admin
GET /api/v1/admin/users
PUT /api/v1/admin/users/{id}/deactivate
GET /api/v1/admin/stats

## Setup
1. Install MySQL 8, create database `splitpay_db`
2. Update application.properties with your DB credentials
3. Run — Flyway auto-creates all tables on startup
4. Import Postman collection from /postman folder