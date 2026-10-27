# TalentStream: Spring Boot Microservices Recruitment Platform

**An event-driven recruitment platform built with Spring Boot, Spring Cloud, JWT, RabbitMQ and Docker.**

[![Java](https://img.shields.io/badge/Java-17-orange?style=flat-square&logo=openjdk)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-6DB33F?style=flat-square&logo=spring-boot)](https://spring.io/projects/spring-boot)
[![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-2023.0.1-6DB33F?style=flat-square&logo=spring)](https://spring.io/projects/spring-cloud)
[![RabbitMQ](https://img.shields.io/badge/RabbitMQ-3.13-FF6600?style=flat-square&logo=rabbitmq)](https://www.rabbitmq.com/)
[![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?style=flat-square&logo=docker)](https://www.docker.com/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow?style=flat-square)](#license)

TalentStream connects employers and job seekers through a set of independently deployable microservices. Employers publish and manage openings, candidates search, subscribe to categories and apply, and every state change is delivered as an asynchronous email notification.

---

## Table of Contents

- [Features](#features)
- [Architecture](#architecture)
- [Tech Stack](#tech-stack)
- [Getting Started](#getting-started)
- [Configuration](#configuration)
- [API Reference](#api-reference)
- [Testing](#testing)
- [Project Structure](#project-structure)
- [Roadmap](#roadmap)
- [Contributing](#contributing)
- [License](#license)

---

## Features

| Area | Capabilities |
|------|--------------|
| **Employers** | Post, update and delete job listings; review applicants; move applications through `SHORTLISTED`, `INTERVIEWED`, `ACCEPTED` or `REJECTED` |
| **Job seekers** | Browse and search jobs; filter by category, type, salary and experience; apply with a cover letter; track or withdraw applications |
| **Subscriptions** | Subscribe to job categories and receive targeted email alerts when matching jobs are posted |
| **Security** | Stateless JWT authentication, BCrypt password hashing, role-based access control (`USER`, `EMPLOYER`, `ADMIN`) |
| **Platform** | Service discovery, single API gateway entry point, event-driven messaging, database-per-service, paginated REST APIs, request validation |

---

## Architecture

```
                 ┌───────────────────────────────┐
                 │   Clients (Web / Mobile / API) │
                 └───────────────┬───────────────┘
                                 │ HTTP
                                 ▼
                 ┌───────────────────────────────┐
                 │      API Gateway  :8080        │
                 │   routing · load balancing     │
                 └───────┬───────────────┬───────┘
                         │               │
                         ▼               ▼
              ┌──────────────────┐   ┌──────────────────┐
              │  Auth Service    │◄──│  Job Service     │
              │  :8081           │   │  :8082           │
              │  JWT · users     │   │  jobs · apps     │
              └──────────────────┘   └────────┬─────────┘
                                              │ publish events
                                              ▼
                                     ┌──────────────────┐
                                     │    RabbitMQ      │
                                     │  :5672 / :15672  │
                                     └────────┬─────────┘
                                              │ consume events
                                              ▼
                                     ┌──────────────────┐
                                     │  Notification    │
                                     │  Service  :8083  │
                                     │  SMTP email      │
                                     └──────────────────┘

   All services register with Eureka Server (:8761) for service discovery.
```

**Synchronous:** Client → API Gateway → services over REST; Job Service → Auth Service through OpenFeign.
**Asynchronous:** Job Service → RabbitMQ → Notification Service for job-posted, application and status-change events.

Further detail is in [docs/ARCHITECTURE.md](./docs/ARCHITECTURE.md).

### Services

| Service | Port | Responsibility |
|---------|------|----------------|
| `eureka-server` | 8761 | Service registry |
| `api-gateway` | 8080 | Single entry point and request routing |
| `auth-service` | 8081 | Registration, login, JWT issuing, user preferences |
| `job-service` | 8082 | Jobs, applications, search and filtering |
| `notification-service` | 8083 | Consumes events and sends email |

---

## Tech Stack

| Layer | Technology |
|-------|------------|
| Language / build | Java 17, Maven |
| Framework | Spring Boot 3.2.5, Spring Cloud 2023.0.1 |
| Microservices | Eureka, Spring Cloud Gateway, OpenFeign |
| Security | Spring Security 6, JWT, BCrypt |
| Messaging | RabbitMQ 3.13, Spring AMQP |
| Persistence | Spring Data JPA, Hibernate, H2 (development) |
| Delivery | Docker, Docker Compose, multi-stage builds |
| Tooling | Lombok, Bean Validation, Actuator, SLF4J/Logback |

---

## Getting Started

### Prerequisites

- Java 17+
- Maven 3.8+
- Docker and Docker Compose
- Postman (optional)

### Run with Docker Compose (recommended)

```bash
git clone https://github.com/mudasirmujtaba/talentstream-spring-microservices.git
cd talentstream-spring-microservices

docker-compose up --build -d
docker-compose ps          # allow ~60 seconds for all services to become healthy
```

On Windows, `start-all.bat` runs the same steps and prints the endpoints.

| Component | URL |
|-----------|-----|
| API Gateway | http://localhost:8080 |
| Eureka dashboard | http://localhost:8761 |
| RabbitMQ management | http://localhost:15672 (`guest` / `guest`) |

Stop everything with `docker-compose down`.

### Run locally without Docker for the services

```bash
mvn clean install -DskipTests

# RabbitMQ is still required
docker run -d --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:3-management
```

Start each service in its own terminal, **in this order**, using `mvn spring-boot:run`:

1. `eureka-server`
2. `api-gateway`
3. `auth-service`
4. `job-service`
5. `notification-service`

Open http://localhost:8761 and confirm all services are registered.

---

## Configuration

Secrets are read from environment variables; nothing sensitive is committed.

| Variable | Service | Purpose |
|----------|---------|---------|
| `MAIL_USERNAME` | notification-service | SMTP account used to send email |
| `MAIL_PASSWORD` | notification-service | SMTP password or Gmail App Password |
| `JWT_SECRET` | auth-service | JWT signing key (256-bit or longer). A development default exists and **must be overridden** in any shared or production environment |

With Docker Compose, export the variables (or place them in a `.env` file next to `docker-compose.yml`) before running `docker-compose up`.

### Before going to production

- Replace H2 with PostgreSQL or MySQL
- Provide real SMTP credentials and a strong `JWT_SECRET`
- Terminate TLS in front of the gateway
- Add rate limiting, metrics (Prometheus/Grafana) and distributed tracing

See [docs/DEPLOYMENT.md](./docs/DEPLOYMENT.md) for the full guide.

---

## API Reference

All requests go through the gateway at `http://localhost:8080`. Protected endpoints require `Authorization: Bearer <token>`.

### Authentication and profile

| Method | Endpoint | Access | Description |
|--------|----------|--------|-------------|
| POST | `/api/auth/register` | Public | Register a `USER`, `EMPLOYER` or `ADMIN` |
| POST | `/api/auth/login` | Public | Authenticate and receive a JWT |
| GET | `/api/auth/preferences` | Authenticated | Get notification preferences |
| PUT | `/api/auth/preferences` | `USER` | Update subscribed categories and email settings |

### Jobs and applications

| Method | Endpoint | Access | Description |
|--------|----------|--------|-------------|
| GET | `/api/jobs` | Public | List jobs (`page`, `size`, `sortBy`, `sortDir`) |
| GET | `/api/jobs/search?keyword=` | Public | Search by title, company or location |
| GET | `/api/jobs/category/{category}` | Public | Filter by category |
| POST | `/api/jobs` | `EMPLOYER` | Create a job |
| POST | `/api/jobs/{id}/apply` | `USER` | Apply to a job |
| GET | `/api/jobs/applications/my-applications` | `USER` | List own applications |
| PATCH | `/api/jobs/applications/{id}/status?status=` | `EMPLOYER` | Update application status |

Application statuses: `PENDING`, `REVIEWED`, `SHORTLISTED`, `INTERVIEWED`, `ACCEPTED`, `REJECTED`, `WITHDRAWN`.

### Example

```bash
# Register
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"jane","email":"jane@example.com","password":"password123","fullName":"Jane Doe","role":"USER"}'

# Browse jobs
curl "http://localhost:8080/api/jobs?page=0&size=10"
```

The complete reference with request and response bodies is in [docs/API_DOCUMENTATION.md](./docs/API_DOCUMENTATION.md).

---

## Testing

Import [`postman/Job-Board-APIs.postman_collection.json`](./postman/Job-Board-APIs.postman_collection.json) into Postman and set `baseUrl` to `http://localhost:8080`. A typical end-to-end run:

1. Register an `EMPLOYER` and create a job
2. Register a `USER` and subscribe to the job's category
3. Apply to the job
4. Update the application status as the employer
5. Check the notification-service logs (or inbox) for the resulting emails

---

## Project Structure

```
talentstream-spring-microservices/
├── eureka-server/          Service discovery
├── api-gateway/            Routing and entry point
├── auth-service/           Authentication, authorization, preferences
├── job-service/            Jobs and applications
├── notification-service/   Event-driven email delivery
├── docs/                   Architecture, API and deployment guides
├── postman/                API collection
├── docker-compose.yml      Local orchestration
└── pom.xml                 Parent POM (multi-module build)
```

---

## Roadmap

- [ ] Automated unit and integration test coverage
- [ ] CI/CD with GitHub Actions
- [ ] Redis caching and gateway rate limiting
- [ ] Resilience4j circuit breakers
- [ ] Elasticsearch-backed job search
- [ ] Distributed tracing and monitoring (Zipkin, Prometheus, Grafana)
- [ ] Resume uploads to object storage
- [ ] Real-time notifications over WebSocket
- [ ] Admin dashboard

---

## Contributing

Contributions are welcome.

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/your-feature`
3. Commit your changes with a clear message
4. Push the branch and open a pull request

---

## License

Released under the MIT License.
