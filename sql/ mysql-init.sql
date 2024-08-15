-- 기존 사용자가 있다면 삭제
DROP USER IF EXISTS 'exporter'@'%';

-- exporter 사용자 생성 및 권한 부여
CREATE USER 'exporter'@'%' IDENTIFIED BY 'exporterpassword';
GRANT PROCESS, REPLICATION CLIENT, SELECT ON *.* TO 'exporter'@'%';
FLUSH PRIVILEGES;

-- 데이터베이스 생성
CREATE DATABASE IF NOT EXISTS ticketing CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE ticketing;

-- 시간대 설정
SET time_zone = '+00:00';

-- user 테이블
CREATE TABLE `user`
(
    `id`         bigint       NOT NULL AUTO_INCREMENT,
    `name`       varchar(255) NOT NULL,
    `created_at` datetime(6)  NOT NULL,
    `updated_at` datetime(6)  NOT NULL,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- concert 테이블
CREATE TABLE `concert`
(
    `id`          bigint       NOT NULL AUTO_INCREMENT,
    `title`       varchar(255) NOT NULL,
    `singer`      varchar(255) NOT NULL,
    `description` varchar(255) NOT NULL,
    `created_at`  datetime(6)  NOT NULL,
    `updated_at`  datetime(6)  NOT NULL,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- concert_event 테이블
CREATE TABLE `concert_event`
(
    `id`                   bigint       NOT NULL AUTO_INCREMENT,
    `concert_id`           bigint       NOT NULL,
    `venue`                varchar(255) NOT NULL,
    `start_at`             datetime(6)  NOT NULL,
    `duration`             bigint       NOT NULL,
    `max_seat_count`       int          NOT NULL,
    `available_seat_count` int          NOT NULL,
    `reservation_start_at` datetime(6)  NOT NULL,
    `reservation_end_at`   datetime(6)  NOT NULL,
    `created_at`           datetime(6)  NOT NULL,
    `updated_at`           datetime(6)  NOT NULL,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- seat 테이블
CREATE TABLE `seat`
(
    `id`               bigint         NOT NULL AUTO_INCREMENT,
    `concert_event_id` bigint         NOT NULL,
    `seat_number`      varchar(255)   NOT NULL,
    `price`            decimal(38, 2) NOT NULL,
    `is_available`     bit(1)         NOT NULL,
    `created_at`       datetime(6)    NOT NULL,
    `updated_at`       datetime(6)    NOT NULL,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- reservation 테이블
CREATE TABLE `reservation`
(
    `id`               bigint                                        NOT NULL AUTO_INCREMENT,
    `user_id`          bigint                                        NOT NULL,
    `concert_id`       bigint                                        NOT NULL,
    `concert_event_id` bigint                                        NOT NULL,
    `total_seats`      int                                           NOT NULL,
    `total_amount`     decimal(38, 2)                                NOT NULL,
    `status`           enum ('CONFIRMED','PAYMENT_FAILED','PENDING') NOT NULL,
    `created_at`       datetime(6)                                   NOT NULL,
    `updated_at`       datetime(6)                                   NOT NULL,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- occupation 테이블
CREATE TABLE `occupation`
(
    `id`               bigint                               NOT NULL AUTO_INCREMENT,
    `user_id`          bigint                               NOT NULL,
    `concert_event_id` bigint                               NOT NULL,
    `reservation_id`   bigint      DEFAULT NULL,
    `status`           enum ('ACTIVE','EXPIRED','RELEASED') NOT NULL,
    `expires_at`       datetime(6)                          NOT NULL,
    `expired_at`       datetime(6) DEFAULT NULL,
    `created_at`       datetime(6)                          NOT NULL,
    `updated_at`       datetime(6)                          NOT NULL,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- seat_allocation 테이블
CREATE TABLE `seat_allocation`
(
    `id`             bigint                                 NOT NULL AUTO_INCREMENT,
    `user_id`        bigint                                 NOT NULL,
    `seat_id`        bigint                                 NOT NULL,
    `reservation_id` bigint      DEFAULT NULL,
    `occupation_id`  bigint      DEFAULT NULL,
    `seat_number`    varchar(255)                           NOT NULL,
    `seat_price`     decimal(38, 2)                         NOT NULL,
    `status`         enum ('EXPIRED','OCCUPIED','RESERVED') NOT NULL,
    `occupied_at`    datetime(6) DEFAULT NULL,
    `reserved_at`    datetime(6) DEFAULT NULL,
    `expired_at`     datetime(6) DEFAULT NULL,
    `created_at`     datetime(6)                            NOT NULL,
    `updated_at`     datetime(6)                            NOT NULL,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- payment 테이블
CREATE TABLE `payment`
(
    `id`             bigint                              NOT NULL AUTO_INCREMENT,
    `user_id`        bigint                              NOT NULL,
    `reservation_id` bigint                              NOT NULL,
    `amount`         decimal(38, 2)                      NOT NULL,
    `payment_method` tinyint                             NOT NULL,
    `status`         enum ('FAILED','PENDING','SUCCESS') NOT NULL,
    `transaction_id` bigint       DEFAULT NULL,
    `failure_reason` varchar(255) DEFAULT NULL,
    `created_at`     datetime(6)                         NOT NULL,
    `updated_at`     datetime(6)                         NOT NULL,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- payment outbox 테이블
CREATE TABLE `payment_outbox`
(
    `id`                   bigint                       NOT NULL AUTO_INCREMENT,
    `payment_id`           bigint                       NOT NULL,
    `payload`              text                         NOT NULL,
    `published_time_milli` bigint                       NOT NULL,
    `status`               enum ('CREATED','PUBLISHED') NOT NULL,
    `created_at`           datetime(6)                  NOT NULL,
    `updated_at`           datetime(6)                  NOT NULL,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- wallet 테이블
CREATE TABLE `wallet`
(
    `id`         bigint         NOT NULL AUTO_INCREMENT,
    `user_id`    bigint         NOT NULL,
    `balance`    decimal(38, 2) NOT NULL DEFAULT 0.00,
    `created_at` datetime(6)    NOT NULL,
    `updated_at` datetime(6)    NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `idx_user_id` (`user_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- wallet_transaction 테이블
CREATE TABLE `wallet_transaction`
(
    `id`         bigint                  NOT NULL AUTO_INCREMENT,
    `wallet_id`  bigint                  NOT NULL,
    `amount`     decimal(38, 2)          NOT NULL,
    `type`       enum ('CHARGED','PAID') NOT NULL,
    `created_at` datetime(6)             NOT NULL,
    `updated_at` datetime(6)             NOT NULL,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
