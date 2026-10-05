# Claims Platform - Architecture

## 1. Overview

The Claims Platform is a Java 17 and Spring Boot application designed to support the core motor/property claims lifecycle.

The system allows:

- Claimants to submit claims
- Claims to be assigned to officers
- Officers to review and assess claims
- Officers to request additional information
- Managers/officers to approve or reject claims
- Approved claims to be settled
- Managers to view workload and outstanding liability exposure
- Downstream systems to consume claim lifecycle events through Kafka

The implementation intentionally uses a modular monolith rather than multiple deployable microservices.

This keeps the implementation simple enough for the assessment while maintaining clear business boundaries that can later be extracted into services.

---

## 2. High-Level Architecture

```text
                    Client / API Consumer
                            |
                            v
                    Spring Boot REST API
                            |
                            v
                    Claim Service Layer
                            |
              +-------------+-------------+
              |                           |
              v                           v
         PostgreSQL                    Kafka
              |                           |
       +------+------+                    |
       |             |                    |
    Claims       Claim History             |
    Assessment   Information               |
                                            v
                                   Downstream Consumers
                                   Notifications
                                   Analytics
                                   Reporting
