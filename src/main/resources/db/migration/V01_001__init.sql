create table channel
(
    id   int not null primary key,
    name varchar(50)
);

create table post
(
    id            bigint not null primary key,
    content       text unique,
    channel_id    int    not null references channel,
    creation_date timestamp,
    category      varchar(15),
    probability   real
);

create sequence post_sq increment by 1 start with 1 owned by post.id;