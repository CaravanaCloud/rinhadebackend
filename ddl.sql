create table Pessoa (nascimento date, id uuid not null, apelido varchar(255) unique, nome varchar(255), stack jsonb, primary key (id));
