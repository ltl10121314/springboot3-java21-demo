CREATE DATABASE IF NOT EXISTS `my_test`;
CREATE TABLE IF NOT EXISTS `my_test`.`user`
(
    `id`      varchar(100) NOT NULL,
    `code`    varchar(100) DEFAULT NULL,
    `name`    varchar(100) DEFAULT NULL,
    `age`     varchar(100) DEFAULT NULL,
    `address` varchar(100) DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci;
