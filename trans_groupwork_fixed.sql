CREATE DATABASE IF NOT EXISTS `transport_groupwork`
  DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `transport_groupwork`;

SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS `trans_task`;
DROP TABLE IF EXISTS `need`;
DROP TABLE IF EXISTS `route`;
DROP TABLE IF EXISTS `vehicle`;
DROP TABLE IF EXISTS `poi`;
DROP TABLE IF EXISTS `factory`;
DROP TABLE IF EXISTS `cargo`;
SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE `cargo` (
  `id` int NOT NULL AUTO_INCREMENT,
  `cargo_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `cargo_unit` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARACTER SET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `factory` (
  `id` int NOT NULL AUTO_INCREMENT,
  `factory_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '',
  `factory_address` varchar(255) DEFAULT NULL,
  `factory_latitude` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `factory_longitude` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARACTER SET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `need` (
  `id` int NOT NULL AUTO_INCREMENT,
  `need_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `cargo_id` int DEFAULT NULL,
  `cargo_total_weight` double DEFAULT NULL,
  `factory_id` int DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARACTER SET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `poi` (
  `id` int NOT NULL AUTO_INCREMENT,
  `poi_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `poi_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `poi_address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `poi_latitude` varchar(255) DEFAULT NULL,
  `poi_longitude` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARACTER SET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `route` (
  `id` int NOT NULL,
  `road_name` varchar(255) DEFAULT NULL,
  `road_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `distance` double DEFAULT NULL,
  `duration` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `poi_id` int DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARACTER SET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `vehicle` (
  `id` int NOT NULL AUTO_INCREMENT,
  `vehicle_type` varchar(255) DEFAULT NULL,
  `max_load` varchar(255) DEFAULT NULL,
  `vehicle_number` int DEFAULT NULL,
  `vehicle_user` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `vehicle_fuel_level` varchar(255) DEFAULT NULL,
  `vehicle_latitude` varchar(255) DEFAULT NULL,
  `vehicle_longitude` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARACTER SET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `trans_task` (
  `id` int NOT NULL AUTO_INCREMENT,
  `trans_task_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `need_id` int DEFAULT NULL,
  `start_time` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `duration` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL,
  `route_id` int DEFAULT NULL,
  `vehicle_id` int DEFAULT NULL,
  `trans_task_accident_rate` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARACTER SET=utf8mb4 COLLATE=utf8mb4_general_ci;

ALTER TABLE `need` ADD CONSTRAINT `fk_need_need_1` FOREIGN KEY (`factory_id`) REFERENCES `factory` (`id`);
ALTER TABLE `need` ADD CONSTRAINT `fk_need_need_2` FOREIGN KEY (`cargo_id`) REFERENCES `cargo` (`id`);
ALTER TABLE `trans_task` ADD CONSTRAINT `fk_trans_task_trans_task_1` FOREIGN KEY (`need_id`) REFERENCES `need` (`id`);
ALTER TABLE `trans_task` ADD CONSTRAINT `fk_trans_task_trans_task_2` FOREIGN KEY (`route_id`) REFERENCES `route` (`id`);
ALTER TABLE `route` ADD CONSTRAINT `fk_route_route_1` FOREIGN KEY (`poi_id`) REFERENCES `poi` (`id`);
ALTER TABLE `trans_task` ADD CONSTRAINT `fk_trans_task_trans_task_3` FOREIGN KEY (`vehicle_id`) REFERENCES `vehicle` (`id`);
