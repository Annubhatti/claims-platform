CREATE TABLE claim_information (
    id UUID PRIMARY KEY,
    claim_id UUID NOT NULL,
    information TEXT NOT NULL,
    provided_by VARCHAR(100) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_claim_information_claim
        FOREIGN KEY (claim_id) REFERENCES claim(id)
);

CREATE INDEX idx_claim_information_claim
    ON claim_information(claim_id);