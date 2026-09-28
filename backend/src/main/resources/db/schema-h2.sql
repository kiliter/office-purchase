CREATE TABLE IF NOT EXISTS `user` (
  `user_id` BIGINT NOT NULL AUTO_INCREMENT,
  `username` VARCHAR(50) NOT NULL,
  `password` VARCHAR(100) NOT NULL,
  `real_name` VARCHAR(50) NOT NULL,
  `role` VARCHAR(20) NOT NULL,
  `phone` VARCHAR(20) DEFAULT NULL,
  `create_time` DATETIME NOT NULL,
  PRIMARY KEY (`user_id`),
  UNIQUE (`username`)
);

CREATE TABLE IF NOT EXISTS `goods` (
  `goods_id` BIGINT NOT NULL AUTO_INCREMENT,
  `goods_name` VARCHAR(100) NOT NULL,
  `goods_type` VARCHAR(50) NOT NULL,
  `spec` VARCHAR(100) DEFAULT NULL,
  `price` DECIMAL(10,2) NOT NULL,
  `stock` INT NOT NULL,
  `create_time` DATETIME NOT NULL,
  PRIMARY KEY (`goods_id`)
);

CREATE TABLE IF NOT EXISTS `purchase_apply` (
  `apply_id` BIGINT NOT NULL AUTO_INCREMENT,
  `apply_user_id` BIGINT NOT NULL,
  `apply_reason` VARCHAR(500) NOT NULL,
  `apply_time` DATETIME NOT NULL,
  `audit_status` VARCHAR(20) NOT NULL,
  `audit_user_id` BIGINT DEFAULT NULL,
  `audit_opinion` VARCHAR(500) DEFAULT NULL,
  `audit_time` DATETIME DEFAULT NULL,
  PRIMARY KEY (`apply_id`)
);

CREATE TABLE IF NOT EXISTS `purchase_apply_item` (
  `item_id` BIGINT NOT NULL AUTO_INCREMENT,
  `apply_id` BIGINT NOT NULL,
  `goods_id` BIGINT NOT NULL,
  `buy_num` INT NOT NULL,
  PRIMARY KEY (`item_id`)
);

CREATE TABLE IF NOT EXISTS `purchase_order` (
  `order_id` BIGINT NOT NULL AUTO_INCREMENT,
  `apply_id` BIGINT NOT NULL,
  `order_status` VARCHAR(20) NOT NULL,
  `create_time` DATETIME NOT NULL,
  PRIMARY KEY (`order_id`),
  UNIQUE (`apply_id`)
);

CREATE TABLE IF NOT EXISTS `notice` (
  `notice_id` BIGINT NOT NULL AUTO_INCREMENT,
  `title` VARCHAR(100) NOT NULL,
  `content` CLOB NOT NULL,
  `publish_time` DATETIME NOT NULL,
  PRIMARY KEY (`notice_id`)
);
