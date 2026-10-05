# AI Working Journal

## Purpose

AI was used as a primary working interface during the implementation of this assessment.

The developer reviewed, validated, challenged, and accepted/modified AI-generated suggestions before applying them to the codebase.

---

## 1. Architecture

### AI suggestion

Use a modular monolith instead of multiple microservices because of the limited assessment implementation time.

### Decision

Accepted.

### Reason

The domain is small enough for a single deployable application, while clear package boundaries preserve future extraction options.

---

## 2. REST vs Kafka

### AI suggestion

Use REST for synchronous claim commands and Kafka for asynchronous domain events.

### Decision

Accepted.

### Reason

Claim creation and lifecycle operations require immediate responses, while notifications, analytics, and reporting can consume events asynchronously.

---

## 3. Database

### AI suggestion

Use PostgreSQL as the transactional system of record.

### Decision

Accepted.

### Reason

The claim domain requires ACID transactions, relational integrity, history, aggregation, and indexed queries.

---

## 4. Claim Lifecycle

### AI suggestion

Model explicit lifecycle states:

SUBMITTED
UNDER_REVIEW
AWAITING_INFORMATION
ASSESSED
APPROVED
REJECTED
SETTLED

### Decision

Accepted.

### Reason

Explicit states make business rules visible and allow invalid transitions to be rejected centrally in the service layer.

---

## 5. Business Logic Location

### AI suggestion

Keep lifecycle validation in the service layer rather than controllers.

### Decision

Accepted.

### Reason

Controllers should handle HTTP concerns. Business rules should remain independent of the transport layer and easier to test.

---

## 6. Optimistic Locking

### AI suggestion

Use JPA @Version to prevent lost updates.

### Decision

Accepted.

### Reason

Multiple claim handlers may operate on the same claim. Optimistic locking provides protection without pessimistic database locking for every operation.

---

## 7. Officer Retrieval

### AI suggestion

Filter claims in PostgreSQL rather than retrieving all claims and filtering in Java.

### Decision

Accepted.

### Reason

Database-side filtering reduces application memory usage and network transfer.

A composite index on:

assigned_officer_id + status

supports the primary workload query.

---

## 8. Pagination

### AI suggestion

Add pagination to officer and status-based claim retrieval.

### Decision

Accepted.

### Reason

The number of claims can grow significantly. Returning all claims in one request is not scalable.

Spring Data Pageable was used for the current implementation.

---

## 9. Kafka Reliability

### AI suggestion

Use a Transactional Outbox Pattern for production reliability.

### Decision

Challenged / deferred.

### Reason

The outbox pattern is appropriate for production but adds implementation complexity. The assessment prioritizes building the core functionality within a limited time.

The pattern is documented as a production improvement.

---

## 10. Testing

### AI suggestion

Prioritize service-level tests around business rules.

### Decision

Accepted.

### Tests cover

- Claim creation
- Assignment
- Invalid state transition
- Assessment
- Assessment amount validation
- Approval
- Settlement
- Settlement amount validation
- Kafka event publication

---

## 11. Error Handling

### AI suggestion

Use a centralized exception handler.

### Decision

Accepted.

### Result

The API returns consistent responses for:

- Not found
- Invalid lifecycle state
- Invalid request data
- Business validation failures

---

## 12. Features Deliberately Deferred

The following were intentionally not prioritized:

- Full authentication
- OAuth2/OIDC
- Frontend
- File storage
- Fraud detection
- OCR
- External payment integration
- Full transactional outbox
- Kubernetes
- Multi-region deployment

The reason was to maximize the quality of the core claims workflow within the assessment time.

---

## 13. Developer Review

AI-generated code was not accepted blindly.

The implementation was reviewed against:

- Business lifecycle requirements
- Database consistency
- API behavior
- Validation rules
- Error handling
- Scalability
- Maintainability
- Interview/demo clarity

Where appropriate, suggestions were modified or deferred rather than implemented automatically.