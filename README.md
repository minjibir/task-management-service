# **Task Management Service**

This project is a microservice for managing tasks. It provides a simple RESTful API for creating, retrieving, updating, and deleting tasks. It is designed to run as a stand-alone, containerized backend, making it easy to deploy and connect to any front-end application.

## Tech Stack & Design Decisions

This section provides an overview of the technology used and the key architectural decisions made.

### Tech Stack

* **Framework:** Quarkus
* **Language:** Java 21
* **Database:** PostgreSQL 17 (LTS)
* **Persistence:** Hibernate ORM (Panache) & Flyway
* **API:** RESTful (JAX-RS)
* **API Documentation:** OpenAPI (Swagger UI)

### Design Decisions

#### API Design: RESTful

A RESTful API was chosen over other patterns (like gRPC) for several key reasons:

* **Simplicity & Client Experience:** As this is a consumer-facing API, REST is universally supported and provides the simplest integration experience for web or mobile clients.
* **Simple Workflow:** The service's operations are simple request-response, which does not require the performance benefits or complex streaming capabilities of gRPC.
* **Browser-Native:** REST is natively supported by browsers, making it the natural choice for a service that will be consumed by a front-end application.

#### Framework: Quarkus

Quarkus was selected as the ideal framework for this project's container-first requirement.

* **Container-First:** Quarkus is specifically designed to build lightweight, fast-booting native executables, making it perfect for efficient Docker deployments.
* **Developer Experience:** It provides a familiar development model (based on Jakarta EE standards) while adding powerful features.
* **Integrated Tooling:** It includes out-of-the-box support for generating container images and native executables, which simplified the build process.
* **Out-of-the-Box OpenAPI:** The OpenAPI (Swagger UI) documentation is generated automatically via an extension, fulfilling the client generation requirement with minimal configuration.

#### Architecture: Simple & Focused

Given the straightforward nature of the domain (CRUD for tasks), the service implements a "Transaction Script" pattern. This means the business logic is handled directly within the resource layer (`TaskResource`), avoiding unnecessary layers of abstraction or the complexity of a full Domain-Driven Design (DDD) approach.

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

The application requires database credentials, which are managed using a `.env` file.

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
Now, you can start the entire stack (the app and the database) with a single command:

```bash
docker compose up -d
```

The application will be available at http://localhost:8080.

### How to Stop
To stop and remove the containers, run:

```bash
docker compose down
```

## API Documentation
For complete, interactive API documentation where you can try out every endpoint, run the service and navigate to the Swagger UI in your browser:

* [http://localhost:8080/q/swagger-ui](http://localhost:8080/q/swagger-ui)


### Client Generation (via OpenAPI)

This service automatically generates an OpenAPI 3.0 specification, which can be used by code-generation tools (like [OpenAPI Generator](https://openapi-generator.tech/)) to create client libraries in various languages.

The specification is available at:

* [http://localhost:8080/q/openapi](http://localhost:8080/q/openapi)

### Quick Examples
Here are a few curl examples for common operations.

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