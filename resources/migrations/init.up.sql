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
    'bcrypt+sha512$72593cc5eb80014d20835af5aef54463$12$84808a99d62438d6c66c03d2b04fb3e6acb0cb9e06caafde',
    'admin@admin.com'
  ),
  (
    'user',
    'bcrypt+sha512$6e16bfdef2743707cbd9667ea5972155$12$a658687a6250efc8d8656cc60bbfd3c68806c448929b3817',
    'user@user.com'
  ),
  (
    'guest',
    'bcrypt+sha512$71d84b8619a69d10995981ac254ea74b$12$6a7efa878ae912b25cd916e78e1924ca044405eba77b6363',
    'guest@guest.com'
  );