create database if not exists `my_test`;
create table if not exists `my_test`.`student`(
    `id`   varchar(100) not null,
    `name` varchar(100) default null,
    `age`  int          default null,
    primary key (`id`)
) engine = innodb
  default charset = utf8mb4
  collate = utf8mb4_general_ci;
truncate my_test.student;
insert into my_test.student (id, name, age)
values ('0', '张三0', 29),
       ('1', '张三1', 6),
       ('2', '张三2', 11),
       ('3', '张三3', 24),
       ('4', '张三4', 22),
       ('5', '张三5', 16),
       ('6', '张三6', 23),
       ('7', '张三7', 24),
       ('8', '张三8', 2),
       ('9', '张三9', 24);
