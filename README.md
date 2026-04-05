# Finance Dashboard Backend

A Spring Boot backend for managing personal or team financial records with JWT-based authentication, role-based access control, and dashboard summaries for income and expenses.

## Overview

This project exposes REST APIs for:

- user registration and login
- secure access using JWT bearer tokens
- role-based authorization for `ADMIN`, `ANALYST`, and `VIEWER`
- creating, updating, listing, and deleting financial records
- generating a dashboard summary with totals and category-wise breakdowns
- administering users and their status/roles

The application uses Spring Security for authentication and authorization, Spring Data JPA for persistence, and MySQL as the default database.

## Current Status

The codebase is close to runnable, but the current build is not fully green with the checked-in configuration.

- `pom.xml` uses Spring Boot `4.0.1`
- `AppConfig` still creates `DaoAuthenticationProvider` using an older API style
- the existing `compile.log` shows the project currently fails during compilation for that reason

If you want this project to run immediately, the authentication provider setup needs a small compatibility fix first.

## Features

- JWT authentication with stateless sessions
- BCrypt password hashing
- input validation with Jakarta Validation
- global exception handling
- financial record ownership rules
- dashboard summary with:
  - total income
  - total expenses
  - net balance
  - category totals for income and expense
- admin-only user management endpoints

## Tech Stack

- Java 21
- Spring Boot 4.0.1
- Spring Web
- Spring Data JPA
- Spring Security
- MySQL
- Lombok
- JJWT (`0.11.5`)
- JUnit 5
- Mockito
- Maven

## Project Structure

```text
src
|-- main
|   |-- java/com/finance/dashboard
|   |   |-- config
|   |   |-- controller
|   |   |-- dto
|   |   |-- enums
|   |   |-- exception
|   |   |-- models
|   |   |-- repository
|   |   |-- security
|   |   |-- service
|   |   `-- Transformer
|   `-- resources
|       `-- application.properties
`-- test
    `-- java/com/finance/dashboard/service
```

## Domain Model

### User

Represents an authenticated platform user.

Fields:

- `id`
- `username`
- `email`
- `password`
- `role`
- `status`
- `createdAt`
- `updatedAt`

### FinancialRecord

Represents one income or expense entry owned by a user.

Fields:

- `id`
- `amount`
- `type`
- `category`
- `date`
- `description`
- `user`
- `createdAt`
- `updatedAt`

## Roles and Permissions

### Roles

- `ADMIN`
- `ANALYST`
- `VIEWER`

### Access Rules

| Action | VIEWER | ANALYST | ADMIN |
|---|---|---|---|
| Register / Login | Yes | Yes | Yes |
| View own records | Yes | Yes | Yes |
| Create record | No | Yes | Yes |
| Update own record | No | Yes | Yes |
| Update any record | No | No | Yes |
| Delete record | No | No | Yes |
| View dashboard summary | No | Yes | Yes |
| Manage users | No | No | Yes |

Notes:

- all endpoints except `/api/auth/**` require authentication
- records are user-scoped for normal access
- only admins can manage user roles and status

## API Base URL

```text
http://localhost:8080
```

## Authentication

After successful registration or login, the API returns a JWT token.

Use it in protected requests:

```http
Authorization: Bearer <your-jwt-token>
```

## API Endpoints

### Auth

| Method | Endpoint | Access | Description |
|---|---|---|---|
| `POST` | `/api/auth/register` | Public | Register a new user |
| `POST` | `/api/auth/login` | Public | Authenticate and get JWT |

### Records

| Method | Endpoint | Access | Description |
|---|---|---|---|
| `POST` | `/api/records` | `ADMIN`, `ANALYST` | Create a financial record |
| `GET` | `/api/records` | Authenticated | Get records for the logged-in user |
| `PUT` | `/api/records/{id}` | `ADMIN`, `ANALYST` | Update a record |
| `DELETE` | `/api/records/{id}` | `ADMIN` | Delete a record |

### Dashboard

| Method | Endpoint | Access | Description |
|---|---|---|---|
| `GET` | `/api/dashboard/summary` | `ADMIN`, `ANALYST` | Get financial summary for current user |

### Users

| Method | Endpoint | Access | Description |
|---|---|---|---|
| `GET` | `/api/users` | `ADMIN` | List all users |
| `GET` | `/api/users/{id}` | `ADMIN` | Get user by id |
| `PUT` | `/api/users/{id}/status?status=ACTIVE` | `ADMIN` | Update user status |
| `PUT` | `/api/users/{id}/role?role=ANALYST` | `ADMIN` | Update user role |

## Request Payloads

### Register

```json
{
  "username": "john_doe",
  "email": "john@example.com",
  "password": "secret123",
  "role": "ANALYST"
}
```

### Login

```json
{
  "username": "john_doe",
  "password": "secret123"
}
```

### Create or Update Record

```json
{
  "amount": 2500.00,
  "type": "INCOME",
  "category": "Salary",
  "date": "2026-04-05",
  "description": "Monthly salary"
}
```

## Sample Responses

### Auth Response

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "user": {
    "id": 1,
    "username": "john_doe",
    "email": "john@example.com",
    "role": "ANALYST",
    "status": "ACTIVE",
    "createdAt": "2026-04-05T21:00:00"
  }
}
```

### Record Response

```json
{
  "id": 10,
  "amount": 2500.00,
  "type": "INCOME",
  "category": "Salary",
  "date": "2026-04-05",
  "description": "Monthly salary",
  "createdAt": "2026-04-05T21:10:00"
}
```

### Dashboard Summary Response

```json
{
  "totalIncome": 5000.00,
  "totalExpenses": 2000.00,
  "netBalance": 3000.00,
  "categoryTotals": {
    "INCOME - Salary": 5000.00,
    "EXPENSE - Rent": 2000.00
  }
}
```

## Validation Rules

### `RegisterRequest`

- `username` is required
- `email` is required and must be valid
- `password` is required
- `role` is required and must be one of `VIEWER`, `ANALYST`, `ADMIN`

### `LoginRequest`

- `username` is required
- `password` is required

### `RecordRequest`

- `amount` is required
- `type` is required
- `category` is required
- `date` is required
- `description` is optional

Note:

- the code uses `@Min(0)` on `BigDecimal amount`; in practice `@DecimalMin("0.0")` is a better fit for decimal validation

## Configuration

Current application properties:

```properties
spring.application.name=finance-dashboard
server.port=8080

spring.datasource.url=jdbc:mysql://localhost:3306/finance_dashboard?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=root

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect

jwt.secret=94a08da1fecbb6e8b46990538c7b50b2a8d11d141e6a17b6a482b8146747192a
jwt.expiration=86400000
```

### Recommended Local Changes

Before using this in a real environment, update:

- database username and password
- JWT secret
- `spring.jpa.hibernate.ddl-auto`
- SQL logging settings as needed

A safer production-style pattern is to move secrets to environment variables, for example:

```properties
spring.datasource.url=${DB_URL}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
jwt.secret=${JWT_SECRET}
jwt.expiration=${JWT_EXPIRATION:86400000}
```

## Prerequisites

- Java 21
- Maven 3.9+
- MySQL 8+

## Database Setup

Create the database before starting the application:

```sql
CREATE DATABASE finance_dashboard;
```

Because JPA is configured with `ddl-auto=update`, the tables are created or updated automatically after the application starts successfully.

## Running the Project

### 1. Clone the repository

```bash
git clone <your-repository-url>
cd finance-dashboard
```

### 2. Configure MySQL and application properties

Edit `src/main/resources/application.properties` to match your local setup.

### 3. Start the application

```bash
mvn spring-boot:run
```

### 4. Build the project

```bash
mvn clean package
```

## Testing

Current automated test coverage in the repository includes:

- `DashboardServiceTest`

Run tests with:

```bash
mvn test
```

## Error Handling

The application includes a global exception handler that returns structured responses for:

- resource not found
- unauthorized access
- invalid arguments
- bean validation failures
- unexpected server errors

Example validation error shape:

```json
{
  "timestamp": "2026-04-05T21:15:00",
  "status": 400,
  "errors": {
    "username": "Username is required"
  }
}
```

## Security Notes

- authentication is stateless
- CSRF is disabled for API usage
- CORS is enabled with default Spring handling
- passwords are stored with BCrypt hashing
- only `/api/auth/**` is publicly accessible

## Known Issues

- the project currently fails compilation due to `DaoAuthenticationProvider` construction in [`AppConfig.java`](src/main/java/com/finance/dashboard/config/AppConfig.java)
- sensitive defaults are committed in `application.properties`:
  - MySQL username/password
  - JWT secret
- there is limited automated test coverage
- package name `Transformer` uses uppercase, which is unusual for Java package conventions

## Suggested Improvements

- fix Spring Security provider configuration for Spring Boot 4
- externalize secrets and database config
- add integration tests for authentication and controllers
- add pagination/filtering for records
- add Swagger or OpenAPI documentation
- add Docker support
- add audit logging and refresh token support

## Example Workflow

1. Register a user with role `ANALYST`
2. Login and copy the JWT token
3. Create income and expense records
4. Fetch `/api/records` to review stored entries
5. Fetch `/api/dashboard/summary` to get totals and balance
6. Use an `ADMIN` account to manage user roles and status

## License

Add your preferred license here if you plan to distribute the project publicly.
