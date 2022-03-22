create table users(
  id uuid not null default uuid_generate_v4() primary key,
  name text not null,
  password text,
  email text,
  created_at timestamp default current_timestamp,
  updated_at timestamp default current_timestamp,
  unique(name),
  unique(email)
);

