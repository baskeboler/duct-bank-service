create table withdrawals (
    id UUID NOT NULL DEFAULT uuid_generate_v4(),
    account_id UUID NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    currency_id VARCHAR(3) default null,
    PRIMARY KEY (id),
    FOREIGN key (id) REFERENCES transactions(id) on delete cascade on update cascade,
    FOREIGN KEY (account_id) REFERENCES accounts(id) on delete cascade on update cascade,
    FOREIGN KEY (currency_id) REFERENCES currencies(id) on delete cascade on update cascade

);
