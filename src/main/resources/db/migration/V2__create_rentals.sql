CREATE TABLE rentals (
     id BIGSERIAL PRIMARY KEY,
     bicycle_id BIGINT NOT NULL REFERENCES bicycles(id),
     customer_name VARCHAR(100) NOT NULL,
     start_time TIMESTAMP NOT NULL,
     end_time TIMESTAMP,
     estimated_duration_hours INT NOT NULL,
     base_cost BIGINT,
     late_fee BIGINT,
     total_cost BIGINT,
     finished BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_rentals_bicycle_id ON rentals(bicycle_id);
CREATE INDEX idx_rentals_finished ON rentals(finished);