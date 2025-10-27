# Task Management Service

This is Java based microservice for managing tasks, built with Quarkus. It provides a RESTful API
for creating, updating, retrieving, and deleting tasks.

This service is built as a native executable and containerized to run entirely within Docker, with a PostgreSQL database
for persistence and Flyway for schema management.

## Tech Stack & Design

* **Framework:** Quarkus (for high-performance, low-memory native executables)
* **Language:** Java 21
* **Database:** PostgreSQL (LTS version 17)
* **Persistence:** Hibernate ORM with the Panache Repository pattern
* **Database Migrations:** Flyway (for version-controlled schema)
* **Containerization:** Docker & Docker Compose
* **API Documentation:** OpenAPI (Swagger UI) is automatically generated.
* **Design:** Task uniqueness is enforced by a `UNIQUE` constraint in the database. All development, testing, and
  production environments are configured to use Flyway for a consistent schema.

## Prerequisites

Before you begin, you will need the following tools installed:

* Docker Engine: Version 20.10 or newer.
* Your local shell must have the `DOCKER_HOST` variable correctly **exported** to point to your container runtime (e.g.,
  `export DOCKER_HOST=unix:///run/user/1000/podman/podman.sock`).
* `curl` or an API client (like Postman) for testing.
* The project uses the Maven Wrapper (`./mvnw`), so a local Maven installation is not required.

## How to Build and Run the Application

Follow these three steps to get the entire application running.

### 1. Configure Credentials

The application requires database credentials, which are managed using a `.env` file. This file is ignored by Git.

Create the file:

```bash
touch .env
```

Now, add your chosen username and password to it:

```bash
echo 'export DATASOURCE_USERNAME="my_prod_user"' >> .env
echo 'export DATASOURCE_PASSWORD="my_prod_secret"' >> .env
```

After creating the file, you **must load the variables** into your current shell session by running:

```bash
source .env
```

or

```bash
. .env
```

### 2. Build the Native Docker Image
This command will compile the application, run all tests, build a native Linux executable, and package it into a minimal Docker image.

```bash
./mvnw package -Dnative
```

### 3. Run with Docker Compose
Now, you can start the entire stack (your app and the database) with a single command:

```bash
docker compose up -d
```

The application will be available at http://localhost:8080.

### How to Stop
To stop and remove the containers, run:

```bash
docker compose down
```

## API Documentation & Examples
Interactive API (Swagger UI)
For complete, interactive API documentation where you can try out every endpoint, run the service and navigate to the Swagger UI in your browser:

* [http://localhost:8080/q/swagger-ui](http://localhost:8080/q/swagger-ui)


### Client Generation (via OpenAPI)

This service automatically generates an OpenAPI 3.0 specification, which can be used by code-generation tools (like [OpenAPI Generator](https://openapi-generator.tech/)) to create client libraries in various languages.

The specification is available at:

* [http://localhost:8080/q/openapi](http://localhost:8080/q/openapi)


### Quick Examples
Here are a few curl examples for common operations.

**Create a New Task**
```bash
curl -X POST 'http://localhost:8080/api/tasks' \
-H 'Content-Type: application/json' \
-d '{
    "title": "My First Task",
    "description": "This is a test task from curl"
}'
```

**Get All Tasks**
```bash
curl -X GET 'http://localhost:8080/api/tasks'
```

**Update a Task's Status**
(First, get a valid id from the "Get All Tasks" command.)

```bash
curl -X PUT 'http://localhost:8080/api/tasks/YOUR-TASK-ID-HERE' \
-H 'Content-Type: application/json' \
-d '{
    "title": "My First Task (Updated)",
    "description": "This task is now complete.",
    "status": "COMPLETED"
}'
```