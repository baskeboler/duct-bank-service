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
insert into users(name, password, email)
values 
  (
    'admin',
    crypt('admin', gen_salt('md5')),
    'admin@admin.com'
  ),
  (
    'user',
    crypt('user', gen_salt('md5')),
    'user@user.com'
  ),
  (
    'guest',
    crypt('guest', gen_salt('md5')),
    'guest@guest.com'
  );