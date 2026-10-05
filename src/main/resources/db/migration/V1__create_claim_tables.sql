CREATE TABLE claim (
    id UUID PRIMARY KEY,
    claim_number VARCHAR(50) NOT NULL UNIQUE,
    claimant_id VARCHAR(100) NOT NULL,
    market VARCHAR(50) NOT NULL,
    claim_type VARCHAR(50) NOT NULL,
    description TEXT NOT NULL,

    status VARCHAR(30) NOT NULL,

    estimated_liability NUMERIC(15, 2) NOT NULL DEFAULT 0,
    approved_amount NUMERIC(15, 2),
    settlement_amount NUMERIC(15, 2),

    assigned_officer_id VARCHAR(100),

    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,

    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_claim_status
    ON claim(status);

CREATE INDEX idx_claim_officer_status
    ON claim(assigned_officer_id, status);

CREATE INDEX idx_claim_market_status
    ON claim(market, status);

CREATE INDEX idx_claim_created_at
    ON claim(created_at);


CREATE TABLE claim_history (
    id UUID PRIMARY KEY,

    claim_id UUID NOT NULL,

    from_status VARCHAR(30),
    to_status VARCHAR(30) NOT NULL,

    changed_by VARCHAR(100) NOT NULL,
    reason TEXT,

    changed_at TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT fk_claim_history_claim
        FOREIGN KEY (claim_id)
        REFERENCES claim(id)
);


CREATE INDEX idx_claim_history_claim
    ON claim_history(claim_id);

CREATE INDEX idx_claim_history_changed_at
    ON claim_history(changed_at);


CREATE TABLE claim_assessment (
    id UUID PRIMARY KEY,

    claim_id UUID NOT NULL UNIQUE,

    estimated_liability NUMERIC(15, 2) NOT NULL,
    approved_amount NUMERIC(15, 2),

    assessment_notes TEXT,

    assessed_by VARCHAR(100) NOT NULL,
    assessed_at TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT fk_claim_assessment_claim
        FOREIGN KEY (claim_id)
        REFERENCES claim(id)
);


CREATE INDEX idx_claim_assessment_claim
    ON claim_assessment(claim_id);