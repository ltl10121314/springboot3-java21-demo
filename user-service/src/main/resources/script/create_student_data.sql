create database if not exists `test`;
-- 学生/成员档案表：保存学生基础资料和档案起止时间。
create table if not exists `test`.`student`
(
    `id`        varchar(100) not null comment '学生或成员档案主键',
    `name`      varchar(100) default null comment '学生或成员姓名',
    `age`       int          default null comment '学生或成员年龄',
    `beginDate` datetime not null default current_timestamp
                             comment '当前档案记录开始时间，默认使用创建时间',
    `endDate`   datetime not null default current_timestamp on update current_timestamp
                             comment '当前档案记录结束时间，随记录更新自动刷新',
    primary key (`id`)
) engine = innodb
  default charset = utf8mb4
  collate = utf8mb4_general_ci;
truncate `test`.`student`;
-- 示例数据字段顺序：主键、姓名、年龄；日期字段使用表默认值。
insert into `test`.`student` (id, name, age)
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
