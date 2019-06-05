CREATE DATABASE `aion`;

USE `aion`;

CREATE TABLE `block` (
	`block_number` BIGINT,
	`block_hash` VARCHAR(64),
	`miner_address` VARCHAR(64),
	`parent_hash` VARCHAR(64),
	`receipt_tx_root` VARCHAR(64),
	`state_root` VARCHAR(64),
	`tx_trie_root` VARCHAR(64),
	`extra_data` VARCHAR(64),
	`nonce` TINYTEXT,
	`bloom` VARCHAR(512),
	`solution` TEXT,
	`difficulty` BIGINT,
	`total_difficulty` BIGINT,
	`nrg_consumed` BIGINT,
	`nrg_limit` BIGINT,
	`block_size` BIGINT,
	`block_timestamp` INT,
	`num_transactions` SMALLINT,
	`block_time` INT,
	`nrg_reward` DECIMAL(64,18),
  `approx_nrg_reward` DOUBLE(36, 18),
	`transaction_hash` VARCHAR(64),
	`transaction_hashes` TEXT,
	`year` SMALLINT,
	`month` TINYINT,
	`day` TINYINT,
	PRIMARY KEY(`block_number`,`year`,`month`)
) ENGINE = InnoDB
PARTITION BY RANGE(`year`)
SUBPARTITION BY HASH(`month`)
SUBPARTITIONS 12 (
	PARTITION p2018 VALUES LESS THAN (2019),
	PARTITION p2019 VALUES LESS THAN (2020),
	PARTITION p2020 VALUES LESS THAN (2021),
	PARTITION p2021 VALUES LESS THAN (2022),
	PARTITION p2022 VALUES LESS THAN (2023),
	PARTITION p2023_and_up VALUES LESS THAN MAXVALUE
);
CREATE INDEX `day_block` ON block(`day`);
CREATE INDEX `block_hash_block` ON block(`block_hash`);
CREATE INDEX `miner_address_block` ON block(`miner_address`);
CREATE INDEX `block_timestamp_block` ON block(`block_timestamp`);

ALTER TABLE `block` ADD CONSTRAINT `block_number_unique` UNIQUE KEY(`block_number`, `month`, `year`);
ALTER TABLE `block` ADD CONSTRAINT `block_timestamp_unique` UNIQUE KEY(`block_timestamp`, `month`, `year`);

CREATE TABLE `transaction` (
	`transaction_hash` VARCHAR(64),
	`block_hash` VARCHAR(64),
	`block_number` BIGINT,
	`block_timestamp` INT,
	`transaction_index` SMALLINT,
	`from_addr` VARCHAR(64),
	`to_addr` VARCHAR(64),
	`nrg_consumed` BIGINT,
  	`approx_value` DOUBLE(36,18),
  	`nrg_price` BIGINT,
	`transaction_timestamp` BIGINT,
	`value` DECIMAL(64,18),
	`transaction_log` TEXT,
	`data` MEDIUMTEXT,
	`nonce` TINYTEXT,
	`tx_error` TEXT,
	`contract_addr` VARCHAR(64),
	`year` SMALLINT,
	`month` TINYINT,
	`day` TINYINT,
	`type` VARCHAR(8),
	PRIMARY KEY(`transaction_hash`,`year`,`month`)
) ENGINE = InnoDB
PARTITION BY RANGE(`year`)
SUBPARTITION BY HASH(`month`)
SUBPARTITIONS 12 (
	PARTITION p2018 VALUES LESS THAN (2019),
	PARTITION p2019 VALUES LESS THAN (2020),
	PARTITION p2020 VALUES LESS THAN (2021),
	PARTITION p2021 VALUES LESS THAN (2022),
	PARTITION p2022 VALUES LESS THAN (2023),
	PARTITION p2023_and_up VALUES LESS THAN MAXVALUE
);
CREATE INDEX `block_timestamp_transaction` ON transaction(`block_timestamp`);
CREATE INDEX `block_number_transaction` ON transaction(`block_number`);
CREATE INDEX `from_addr_transaction` ON transaction(`from_addr`);
CREATE INDEX `to_addr_transaction` ON transaction(`to_addr`);
CREATE INDEX `day_transaction` ON transaction(`day`);

CREATE TABLE `parser_state` (
	`id` TINYINT,
	`block_number` BIGINT,
	PRIMARY KEY(`id`)
) ENGINE = InnoDB;
INSERT INTO `parser_state` VALUES(1,-1);		# to track current block head
INSERT INTO `parser_state` VALUES(2,-1);		# to track current blockchain head
INSERT INTO `parser_state` VALUES(3,0); 		# to track integrity check head
INSERT INTO `parser_state` VALUES(4,0); 		# to track the current state of the graph TABLEs
INSERT INTO `parser_state` VALUES(5,1); 		# to track the start of the rolling window
INSERT INTO `parser_state` VALUES(6,1); 		# to tract the start of the rolling transaction window

CREATE TABLE `account` (
`address` VARCHAR(64) NOT NULL,
`contract` TINYINT NOT NULL,
`balance` DECIMAL(64,18) NOT NULL,
`last_block_number` BIGINT NOT NULL,
`nonce` TINYTEXT NOT NULL,
`transaction_hash` VARCHAR(64) NOT NULL,
`approx_balance` double(48,18) NULL ,
PRIMARY KEY(`address`)
) ENGINE = InnoDB;

CREATE TABLE `token` (
	`contract_addr` VARCHAR(64) NOT NULL,
	`transaction_hash` VARCHAR(64) NOT NULL,
	`name` VARCHAR(45) DEFAULT NULL,
	`symbol` VARCHAR(45) DEFAULT NULL,
	`creator_address` VARCHAR(64) NOT NULL,
	`total_supply` DECIMAL(39,0) NOT NULL,
	`liquid_supply` DECIMAL(39,0) NOT NULL,
	`creation_timestamp` INT DEFAULT NULL,
	`granularity` DECIMAL(40,0) NOT NULL,
	`token_decimal` int(5) NOT NULL,
	`year` SMALLINT,
	`month` TINYINT,
	`day` TINYINT,
	PRIMARY KEY(`contract_addr`, `year`, `month`)
) ENGINE = InnoDB
PARTITION BY RANGE(`year`)
SUBPARTITION BY HASH(`month`)
SUBPARTITIONS 12 (
	PARTITION p2018 VALUES LESS THAN (2019),
	PARTITION p2019 VALUES LESS THAN (2020),
	PARTITION p2020 VALUES LESS THAN (2021),
	PARTITION p2021 VALUES LESS THAN (2022),
	PARTITION p2022 VALUES LESS THAN (2023),
	PARTITION p2023_and_up VALUES LESS THAN MAXVALUE
);
CREATE INDEX `name_token` ON token(`name`);
CREATE INDEX `symbol_token` ON token(`symbol`);

CREATE TABLE `token_transfers` (
	`transfer_id` BIGINT NOT NULL AUTO_INCREMENT,
	`to_addr` VARCHAR(64) NOT NULL,
	`from_addr` VARCHAR(64) NOT NULL,
	`operator_addr` VARCHAR(64) NOT NULL,
	`scaled_value` DECIMAL(64,18) NOT NULL,
	`raw_value` VARCHAR(40) NOT NULL,
	`granularity` DECIMAL(40,0) NOT NULL,
	`token_decimal` INT(5) NOT NULL,
	`contract_addr` VARCHAR(64) NOT NULL,
	`transaction_hash` VARCHAR(64) NOT NULL,
	`block_number` BIGINT NOT NULL,
	`transfer_timestamp` INT NOT NULL,
	`year` SMALLINT,
	`month` TINYINT,
	`day` TINYINT,
	`approx_value` double(64,18) default 0.0 not null ,
	PRIMARY KEY(`transfer_id`, `year`, `month`)
) ENGINE = InnoDB
PARTITION BY RANGE(`year`)
SUBPARTITION BY HASH(`month`)
SUBPARTITIONS 12 (
	PARTITION p2018 VALUES LESS THAN (2019),
	PARTITION p2019 VALUES LESS THAN (2020),
	PARTITION p2020 VALUES LESS THAN (2021),
	PARTITION p2021 VALUES LESS THAN (2022),
	PARTITION p2022 VALUES LESS THAN (2023),
	PARTITION p2023_and_up VALUES LESS THAN MAXVALUE
);
CREATE INDEX `to_addr_token_transfers` ON token_transfers(`to_addr`);
CREATE INDEX `from_addr_token_transfers` ON token_transfers(`from_addr`);
CREATE INDEX `contract_addr_token_transfers` ON token_transfers(`contract_addr`);
CREATE INDEX `transaction_hash_token_transfers` ON token_transfers(`transaction_hash`);

CREATE TABLE `token_holders` (
	`scaled_balance` DECIMAL(64,18) NOT NULL,
	`holder_addr` VARCHAR(64) NOT NULL,
	`contract_addr` VARCHAR(64) NOT NULL,
	`block_number` BIGINT NOT NULL,
	`raw_balance` VARCHAR(40) NOT NULL,
	`token_decimal` INT(4) NOT NULL,
	`granularity` DECIMAL(40,0) NOT NULL,
	PRIMARY KEY(`holder_addr`, `contract_addr`)
) ENGINE = InnoDB;
CREATE INDEX `holder_addr_token_holders` ON token_holders(`holder_addr`);
CREATE INDEX `contract_addr_token_holders` ON token_holders(`contract_addr`);

CREATE TABLE `event` (
	`event_id` BIGINT AUTO_INCREMENT,
	`name` TINYTEXT NOT NULL,
	`parameter_list` TEXT NOT NULL,
	`input_list` TEXT NOT NULL,
	`transaction_hash` VARCHAR(64) NOT NULL,
	`block_number` BIGINT NOT NULL,
	`contract_addr` VARCHAR(64) NOT NULL,
	`event_timestamp` INT NOT NULL,
	PRIMARY KEY(`event_id`)
) ENGINE = InnoDB;
CREATE INDEX `contract_addr_event` ON event(`contract_addr`);

CREATE TABLE `contract` (
	`contract_addr` VARCHAR(64) NOT NULL,
	`contract_name` TINYTEXT,
	`contract_creator_addr` VARCHAR(64) NOT NULL,
	`contract_tx_hash` VARCHAR(64) NOT NULL,
	`block_number` BIGINT NOT NULL,
	`deploy_timestamp` INT NOT NULL,
	`year` SMALLINT,
	`month` TINYINT,
	`day` TINYINT,
	`type` VARCHAR(8),
	PRIMARY KEY(`contract_addr`)
) ENGINE = InnoDB;
CREATE INDEX `creator_addr_contract` ON contract(`contract_creator_addr`);

ALTER TABLE `contract` ADD CONSTRAINT `contract_tx_hash_unique` UNIQUE KEY(`contract_tx_hash`);

CREATE TABLE `graphing` (
	`id` BIGINT AUTO_INCREMENT,
	`value` DECIMAL(64,18) NOT NULL,
	`graph_type` VARCHAR(64) NOT NULL,
	`timestamp` INT NOT NULL,
	`block_number` BIGINT NOT NULL,
	`detail` VARCHAR(64),
	`year` SMALLINT,
	`month` TINYINT,
	`day` TINYINT,
	PRIMARY KEY(`id`)
) ENGINE = InnoDB;
CREATE INDEX `graph_type_graphing` ON graphing(`graph_type`);
CREATE INDEX `year_graphing` ON graphing(`year`);
CREATE INDEX `month_graphing` ON graphing(`month`);
CREATE INDEX `day_graphing` ON graphing(`day`);

CREATE TABLE `metrics` (
	`id` TINYINT NOT NULL,
	`total_transaction` BIGINT NULL,
	`transactions_per_second` DECIMAL(64,10) NULL,
	`peak_transactions_per_block` SMALLINT NULL,
	`start_block` BIGINT NULL,
	`end_block` BIGINT NULL,
	`average_nrg_consumed` DECIMAL(64,10) NULL,
	`average_nrg_limit` DECIMAL(64,10) NULL,
	`averaged_block_time` DECIMAL(14,10) NULL,
	`average_difficulty` DECIMAL(64,10) NULL,
	`end_timestamp` INT NULL,
	`start_timestamp` INT NULL,
	`averaged_hash_power` DECIMAL(64,10) NULL,
	PRIMARY KEY(`id`)
) ENGINE = InnoDB;
INSERT INTO `metrics` (id) VALUES (1);
INSERT INTO `metrics` (id) VALUES (2);


CREATE TABLE `internal_transfer`(
	`transaction_hash` VARCHAR(64),
	`to_addr` VARCHAR(64),
	`from_addr` VARCHAR(64),
	`value_transferred` DECIMAL(40,0),
	`block_timestamp` BIGINT(64),
	`block_number` BIGINT(64),
	transfer_index INT(4),
	`approx_value` double default 0.00,
	PRIMARY KEY (transfer_index, `transaction_hash`)
) ENGINE = InnoDB;
CREATE INDEX `internal_transfer_block_number` on internal_transfer(`block_number`);
CREATE INDEX `internal_transfer_block_timestamp` on internal_transfer(`block_timestamp`);
CREATE INDEX `internal_transfer_from` on internal_transfer(`from_addr`);
CREATE INDEX `internal_transfer_to` on internal_transfer(`to_addr`);