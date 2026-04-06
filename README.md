# Finance Dashboard Backend

A robust, scalable Spring Boot REST API for managing personal or enterprise finances. It acts as the core backend for a "Finance Dashboard," providing secure authentication, role-based access, fast analytics using Redis caching, and intelligent data management.

## 🌟 Key Features

*   **Security First:** Stateless JWT-based authentication and Role-Based Access Control (RBAC). Roles include `VIEWER`, `ANALYST`, and `ADMIN`, providing strict authorization on controller endpoints.
*   **High Performance Caching:** Integrated with Redis (`@Cacheable`, `@CacheEvict`) to cache heavy dashboard summary computations. Modifying records seamlessly expires stale caches.
*   **Universal Soft Delete:** Ensures data integrity while adhering to audit/compliance requirements. Deleted accounts and financial records are intelligently persisted and gracefully handled via custom Hibernate `@SQLDelete` modifiers. Soft-deleted elements auto-purge after a 30-Day Grace Period via a scheduled background task.
*   **Optimized Data Retrieval:** Cursor/Offset based **Pagination** integrated on major endpoints (like User list and Record list) natively via Spring Data's `PageRequest`, resulting in lightweight, high-performance data fetches regardless of table size.
*   **Interactive Documentation:** Automated API documentation powered by OpenAPI 3.0 / Swagger UI. Full visualization of Request/Response DTOs and one-click testing using built-in JWT Bearer tokens authorization.
*   **Containerized Production Ready Setup:** Full orchestration of the App, MySQL, and Redis utilizing Docker and `docker-compose`.

## 🛠 Tech Stack

*   **Java 21**
*   **Spring Boot 3.x**
    *   Spring Web
    *   Spring Security
    *   Spring Data JPA
    *   Spring Cache & Data Redis
    *   Springdoc OpenAPI (Swagger)
*   **MySQL 8.0**
*   **Redis 7** (Alpine Stack)
*   **Lombok** (Boilerplate reduction)
*   **Docker & Docker Compose**

---

## 🚀 Quick Start / Deployment

This application is fully dockarized, making local deployment seamless.

### Prerequisites

*   Docker Desktop or Docker Engine installed on your machine.

### Instructions

1.  Clone the repository.
2.  Open your terminal inside the root directory.
3.  Run the orchestration command:

    ```bash
    docker-compose up --build -d
    ```

Docker will:
1.  Spin up a customized `finance-mysql` container.
2.  Spin up a heavily optimized `finance-redis` container.
3.  Compile your Java Application inside a Maven multi-stage docker image and host it at `localhost:8080`.

To stop the services:
```bash
docker-compose down
```

---

## 📄 API Documentation

Once the app is running (via Docker or local IDE), access the Interactive Swagger UI Interface:

👉 **[http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)**

### How to Authenticate & Test endpoints:

1.  Go to `POST /api/auth/register` to create a new `ACTIVE` user. (Requires a generic body matching the displayed DTO schema).
2.  Copy the generated JSON Web Token (`token`) from the response.
3.  Click the **"Authorize"** button near the top of the Swagger UI dashboard.
4.  Paste your token into the Value box.
5.  Now you can freely invoke and test `GET /api/records`, `GET /api/dashboard/summary`, `GET /api/users` (Admin only), etc!

---

## 🏗 System Architecture Details

### Pagination Details
All record and user lists are structured using generic `PageResponse` DTO wrappers.
You can append query arguments like `?pageNo=1&pageSize=5`. (Defaults to `pageNo=0, pageSize=10`).

### Caching Strategy
A user's heavy `DashboardSummary` calculations are actively cached inside Redis as serialized bytes under `dashboardSummary::[username]`. Adding, modifying, or deleting a `FinancialRecord` executes a direct cache invalidation routine.

### Reactivation & Soft Delete Protocol
When a user is deleted, their `deleted` column flips. If that user attempts a future login during the 30-Day grace duration, the Application intercepts the authentication cycle, auto-reactivates the profile by zeroing out the date fields, and completes the sign-in uninterrupted.
