## Database Schema

### Decision
Use PostgreSQL with Flyway migrations for the claims platform.

### AI Recommendation
Use a relational transactional database because claims require lifecycle state,
audit history, assessment data, assignment information, and liability aggregation.

### Accepted
Accepted PostgreSQL because the core workflow is transactional and relational.

### Decision
Use three core tables:
- claim
- claim_history
- claim_assessment

### Accepted
Claim history provides an audit trail for lifecycle transitions.

### Decision
Use optimistic locking with a version column.

### Accepted
Added `version` to the claim table and will map it to JPA `@Version`.

### Decision
Add indexes on:
- status
- assigned_officer_id + status
- market + status
- created_at

### Accepted
These indexes support claim retrieval and workload/dashboard queries.

### Deliberately Not Built
A dedicated officer/identity table was not included in the initial implementation.
The assessment requires officer assignment and workload retrieval but does not require
building a complete identity-management domain.