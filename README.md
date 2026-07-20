# JavaEnterpriseProject

Enterprise platform built with **Spring Boot 3.2**, **JAX-RS / Jersey**, **Apache CXF (SOAP)**, **Kafka**, **OAuth2**, **MySQL 8**, and **Flyway** — structured as a Maven multi-module project.

---

## Table of Contents

1. [Prerequisites](#prerequisites)
2. [Project Modules](#project-modules)
3. [Service Ports](#service-ports)
4. [Running the Application](#running-the-application)
   - [Option A — H2 (zero dependencies)](#option-a--h2-zero-dependencies)
   - [Option B — Local (MySQL + Kafka on host)](#option-b--local-mysql--kafka-on-host)
   - [Option C — Docker Compose (full stack)](#option-c--docker-compose-full-stack)
5. [Spring Profiles](#spring-profiles)
6. [Default Credentials](#default-credentials)

---

## Prerequisites

### Required for all run modes

| Tool | Minimum Version | Notes |
|------|----------------|-------|
| **JDK** | 17 | [Eclipse Temurin 17](https://adoptium.net/) recommended |
| **Apache Maven** | 3.9+ | Or use the project's Maven Wrapper if present |

### Required for Option B (Local)

| Tool | Version | Notes |
|------|---------|-------|
| **MySQL** | 8.x | Database must be created before first run (see below) |
| **Apache Kafka** | 3.x | With Zookeeper on default ports |

### Required for Option C (Docker)

| Tool | Minimum Version | Notes |
|------|----------------|-------|
| **Docker** | 24+ | Docker Desktop or Docker Engine |
| **Docker Compose** | v2 (plugin) | `docker compose` command (not `docker-compose`) |

---

## Project Modules

| Module | Description |
|--------|-------------|
| `enterprise-common` | Shared constants, utilities, response wrappers (`ApiResponse`, `PagedResponse`) |
| `enterprise-domain` | JPA entities (`User`, `Product`, `Order`), DTOs, MapStruct mappers |
| `enterprise-repository` | Spring Data JPA repositories with JPQL queries |
| `enterprise-kafka` | Kafka producer, consumers, domain event classes |
| `enterprise-security` | OAuth2 resource server configuration, JWT validation, security filters |
| `enterprise-rest-api` | JAX-RS / Jersey REST API — Products, Orders, Users (**port 8080**) |
| `enterprise-soap-service` | Apache CXF JAX-WS SOAP service — Product info (**port 8081**) |
| `enterprise-gateway` | Spring Authorization Server — issues JWT tokens (**port 9000**) |

---

## Service Ports

| Service | Port | URL |
|---------|------|-----|
| REST API | 8080 | `http://localhost:8080/api/` |
| SOAP Service | 8081 | `http://localhost:8081/ws/product?wsdl` |
| OAuth2 Gateway | 9000 | `http://localhost:9000` |
| Kafka UI | 8090 | `http://localhost:8090` (Docker only) |
| MySQL | 3306 | `localhost:3306/enterprise_db` |
| Kafka | 9092 | `localhost:9092` |

---

## Running the Application

### Option A — H2 (zero dependencies)

The fastest way to start. Uses an in-memory H2 database; Kafka is disabled. No MySQL, Kafka, or Docker required.

**Step 1 — Build the project**

```bash
mvn clean install -DskipTests
```

**Step 2 — Start the REST API with the H2 profile**

```bash
mvn spring-boot:run -pl enterprise-rest-api -Dspring-boot.run.profiles=h2
```

**Step 3 — (Optional) Start the SOAP service with the H2 profile**

```bash
mvn spring-boot:run -pl enterprise-soap-service -Dspring-boot.run.profiles=h2
```

**Step 4 — Verify**

- REST API: `http://localhost:8080/api/products`
- H2 Console: `http://localhost:8080/h2-console`
  - JDBC URL: `jdbc:h2:mem:enterprise_db`
  - Username: `sa` / Password: *(leave blank)*

> **Note:** The OAuth2 Gateway is not needed in H2 mode. Kafka auto-configuration is excluded automatically.

---

### Option B — Local (MySQL + Kafka on host)

Use this when you have MySQL and Kafka running locally on your machine.

**Step 1 — Create the MySQL database and user**

```sql
CREATE DATABASE enterprise_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'enterprise_user'@'localhost' IDENTIFIED BY 'enterprise_pass';
GRANT ALL PRIVILEGES ON enterprise_db.* TO 'enterprise_user'@'localhost';
FLUSH PRIVILEGES;
```

**Step 2 — Start Zookeeper and Kafka**

```bash
# Start Zookeeper (default port 2181)
bin/zookeeper-server-start.sh config/zookeeper.properties

# Start Kafka (default port 9092)
bin/kafka-server-start.sh config/server.properties
```

**Step 3 — Build the project**

```bash
mvn clean install -DskipTests
```

**Step 4 — Start the OAuth2 Gateway**

```bash
mvn spring-boot:run -pl enterprise-gateway -Dspring-boot.run.profiles=local
```

**Step 5 — Start the REST API**

```bash
mvn spring-boot:run -pl enterprise-rest-api -Dspring-boot.run.profiles=local
```

**Step 6 — (Optional) Start the SOAP service**

```bash
mvn spring-boot:run -pl enterprise-soap-service -Dspring-boot.run.profiles=local
```

> **Note:** Flyway will automatically run `V1__initial_schema.sql` and `V2__seed_data.sql` on first startup to create and seed the database schema.

**Step 7 — Verify**

- REST API: `http://localhost:8080/api/products`
- SOAP WSDL: `http://localhost:8081/ws/product?wsdl`
- OAuth2 JWKS: `http://localhost:9000/oauth2/jwks`

---

### Option C — Docker Compose (full stack)

Starts all services — MySQL, Kafka, Zookeeper, Kafka UI, Gateway, REST API, and SOAP service — in containers with a single command.

**Step 1 — Build the JAR files**

```bash
mvn clean package -DskipTests
```

> The Dockerfiles copy from the `target/` directory, so the JARs must be built before running `docker compose`.

**Step 2 — Build and start all containers**

```bash
docker compose up --build
```

To run in the background (detached mode):

```bash
docker compose up --build -d
```

**Step 3 — Check container health**

```bash
docker compose ps
```

All services should show `healthy` or `running`. MySQL and Kafka have health checks; the application services depend on them being healthy before starting.

**Step 4 — Verify**

| Service | URL |
|---------|-----|
| REST API | `http://localhost:8080/api/products` |
| SOAP WSDL | `http://localhost:8081/ws/product?wsdl` |
| OAuth2 JWKS | `http://localhost:9000/oauth2/jwks` |
| Kafka UI | `http://localhost:8090` |

**Step 5 — Stop all containers**

```bash
docker compose down
```

To also remove all volumes (database data):

```bash
docker compose down -v
```

---

## Spring Profiles

Each runnable service supports three Spring profiles:

| Profile | Database | Kafka | OAuth2 JWKS | Use Case |
|---------|----------|-------|-------------|----------|
| `h2` | H2 in-memory | Disabled | Excluded | Quick local dev / no dependencies |
| `local` | MySQL on `localhost:3306` | `localhost:9092` | `localhost:9000` | Local dev with real infrastructure |
| `docker` | MySQL container (`mysql:3306`) | Kafka container (`kafka:29092`) | Gateway container (`gateway:9000`) | Full containerised stack |

The `docker` profile is set automatically by `docker-compose.yml` via the `SPRING_PROFILES_ACTIVE=docker` environment variable — no manual flag is needed when using Docker Compose.

---

## Default Credentials

### MySQL (Docker / Local)

| Field | Value |
|-------|-------|
| Host | `localhost:3306` |
| Database | `enterprise_db` |
| Username | `enterprise_user` |
| Password | `enterprise_pass` |
| Root password | `rootpass` |

### OAuth2 Gateway Admin

| Field | Value |
|-------|-------|
| Username | `admin` |
| Password | `Admin@1234` |

### H2 Console

| Field | Value |
|-------|-------|
| JDBC URL | `jdbc:h2:mem:enterprise_db` |
| Username | `sa` |
| Password | *(empty)* |
