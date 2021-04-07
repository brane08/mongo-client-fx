create table CONNECTIONS
(
    ID INT auto_increment
        primary key,
    SERVERS VARCHAR(255) not null,
    NAME VARCHAR(50) not null
        constraint CONNECTIONS_NAME_IDX
            unique,
    USERNAME VARCHAR(50) not null,
    PASSWORD VARCHAR(50) not null,
    DATABASE VARCHAR(50) not null
);
