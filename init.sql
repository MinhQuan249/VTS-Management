CREATE DATABASE IF NOT EXISTS QLKH;

USE QLKH;

-- Tạo bảng nv
CREATE TABLE IF NOT EXISTS `nv` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  `position` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

-- Tạo bảng kh
CREATE TABLE IF NOT EXISTS `kh` (
  `khid` int(11) NOT NULL AUTO_INCREMENT,
  `address` varchar(255) DEFAULT NULL,
  `id` int(11) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `phone` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`khid`),
  KEY `FKrc2evbog4bl6aswiffm8k3lir` (`id`),
  CONSTRAINT `FKrc2evbog4bl6aswiffm8k3lir` FOREIGN KEY (`id`) REFERENCES `nv` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

-- Tạo bảng hd
CREATE TABLE IF NOT EXISTS `hd` (
  `hd_id` int(11) NOT NULL AUTO_INCREMENT,
  `amount` int(11) NOT NULL,
  `contract_date` date NOT NULL,
  `kh_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`hd_id`),
  KEY `FK3m5dajokx5c8shm2uw6dah1s5` (`kh_id`),
  CONSTRAINT `FK3m5dajokx5c8shm2uw6dah1s5` FOREIGN KEY (`kh_id`) REFERENCES `kh` (`khid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;
