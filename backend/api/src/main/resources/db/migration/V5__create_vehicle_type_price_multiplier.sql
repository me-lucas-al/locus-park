CREATE TABLE vehicle_type_price_multipliers
(
    id           BINARY(16)     NOT NULL,
    company_id   BINARY(16)     NOT NULL,
    vehicle_type VARCHAR(50)    NOT NULL,
    multiplier   DECIMAL(5, 2)  NOT NULL,
    CONSTRAINT pk_vehicle_type_price_multipliers PRIMARY KEY (id)
);

ALTER TABLE vehicle_type_price_multipliers
    ADD CONSTRAINT FK_VEHICLE_TYPE_PRICE_MULTIPLIERS_ON_COMPANY FOREIGN KEY (company_id) REFERENCES companies (id);

ALTER TABLE vehicle_type_price_multipliers
    ADD CONSTRAINT uc_vehicle_type_price_multipliers UNIQUE (company_id, vehicle_type);
