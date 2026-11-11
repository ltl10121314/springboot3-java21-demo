CREATE DATABASE IF NOT EXISTS `test`;
-- 用户表：保存用户基础资料；字段说明同时写入MySQL列注释。
CREATE TABLE IF NOT EXISTS `test`.`user`
(
    `id`      varchar(100) NOT NULL COMMENT '用户主键',
    `code`    varchar(100) DEFAULT NULL COMMENT '用户业务编码或外部账号编号',
    `name`    varchar(100) DEFAULT NULL COMMENT '用户名称',
    `age`     varchar(100) DEFAULT NULL COMMENT '用户年龄，现有表使用字符串保存',
    `address` varchar(100) DEFAULT NULL COMMENT '用户地址',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci;
