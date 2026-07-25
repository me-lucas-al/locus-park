ALTER TABLE tickets ADD gross_amount DECIMAL(10, 2) NULL;
ALTER TABLE tickets ADD discount_amount DECIMAL(10, 2) NULL;

CREATE INDEX idx_tickets_company_entered_at ON tickets (company_id, entered_at);
CREATE INDEX idx_tickets_company_exited_at  ON tickets (company_id, exited_at);
CREATE INDEX idx_tickets_company_status_entered_at ON tickets (company_id, status, entered_at);
