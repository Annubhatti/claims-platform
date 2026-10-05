# Chubb APAC Claims Platform

A Java 17 + Spring Boot claims management backend implementing the core claim lifecycle for motor/property insurance claims.

## Tech Stack

- Java 17
- Spring Boot
- Spring Data JPA
- PostgreSQL
- Flyway
- Apache Kafka
- Maven
- JUnit 5
- Mockito
- Docker Compose
- Spring Boot Actuator

## Architecture

The application uses a modular monolith architecture.

Core modules:

- Claim management
- Dashboard
- Domain events

PostgreSQL is the transactional system of record.

Kafka is used for asynchronous claim lifecycle events.

See:

`ARCHITECTURE.md`

for detailed architecture and trade-offs.

---

## Claim Lifecycle

```text
SUBMITTED
    |
    v
UNDER_REVIEW
    |
    +----------------------+
    |                      |
    v                      v
AWAITING_INFORMATION    ASSESSED
    |                      |
    |                 +----+----+
    |                 |         |
    v                 v         v
UNDER_REVIEW       APPROVED  REJECTED
                      |
                      v
                   SETTLED
