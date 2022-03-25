create table currencies (
    id VARCHAR(3) NOT NULL,
    name VARCHAR(255) NOT NULL,
    symbol VARCHAR(3) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);
insert into currencies(id, name, symbol)
values ('USD', 'US Dollar', '$'),
       ('EUR', 'Euro', '€'),
       ('GBP', 'British Pound', '£');
create table accounts (
    id UUID NOT NULL DEFAULT uuid_generate_v4(),
    name VARCHAR(255) NOT NULL,
    currency_id VARCHAR(3) NOT NULL,
    user_id UUID NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    balance DECIMAL(10, 2) NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    FOREIGN KEY (currency_id) REFERENCES currencies(id) on delete cascade on update cascade,
    FOREIGN KEY (user_id) REFERENCES users(id) on delete cascade on update cascade
);

insert into accounts (name, currency_id, user_id, balance)
select 'Savings', 'USD', id, 1000.0 from users;
create table transactions (
    id UUID NOT NULL DEFAULT uuid_generate_v4(),
    transaction_type VARCHAR(255) NOT NULL,
    -- deposit, withdrawal, transfer
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    account_id UUID NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (account_id) REFERENCES accounts(id) on delete cascade on update cascade
);
create table transfers (
    id UUID NOT NULL DEFAULT uuid_generate_v4(),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    from_account_id UUID NOT NULL,
    to_account_id UUID NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    currency_id VARCHAR(3) NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (from_account_id) REFERENCES accounts(id) on delete cascade on update cascade,
    FOREIGN KEY (to_account_id) REFERENCES accounts(id) on delete cascade on update cascade,
    FOREIGN KEY (currency_id) REFERENCES currencies(id) on delete cascade on update cascade
);