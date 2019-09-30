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
	`signature` VARCHAR(64),
	`seed` varchar(128),
	`public_key` varchar(128),
	`seal_type` varchar(4),
	`difficulty` decimal(32,1),
	`total_difficulty` decimal(64,1),
	`nrg_consumed` decimal(64,1),
	`nrg_limit` BIGINT,
	`block_size` BIGINT,
	`block_timestamp` INT,
	`num_transactions` SMALLINT,
	`block_time` INT,
	`nrg_reward` DECIMAL(64,18),
	`block_reward` DECIMAL(64,18),
  	`approx_nrg_reward` DOUBLE(36, 18),
	`transaction_hash` VARCHAR(64),
	`transaction_hashes` TEXT,
	`year` SMALLINT,
	`month` TINYINT,
	`day` TINYINT,
	PRIMARY KEY(`block_number`)
) ENGINE = InnoDB;
CREATE INDEX `block_hash_block` ON block(`block_hash`);
CREATE INDEX `miner_address_block` ON block(`miner_address`);
CREATE INDEX `block_timestamp_block` ON block(`block_timestamp`);
CREATE UNIQUE INDEX block_block_timestamp_miner_address_index on block (miner_address, block_timestamp);

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
	`internal_transaction_count` SMALLINT DEFAULT 0,
	PRIMARY KEY(`transaction_hash`)
) ENGINE = InnoDB;
CREATE INDEX `block_timestamp_transaction` ON transaction(`block_timestamp`);
CREATE INDEX `block_number_transaction` ON transaction(`block_number`);
CREATE INDEX `from_addr_transaction` ON transaction(`from_addr`);
CREATE INDEX `to_addr_transaction` ON transaction(`to_addr`);
create index transaction_timestamp_to_addr_index on transaction(block_timestamp, to_addr);
create index transaction_timestamp_from_addr_index on transaction(block_timestamp, from_addr);
create index transaction_block_number_from_addr_index on transaction (block_number, from_addr);
create index transaction_block_number_to_addr_index on transaction (block_number, to_addr);

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
INSERT INTO `parser_state` VALUE (10,1);

CREATE TABLE `account` (
`address` VARCHAR(64) NOT NULL,
`contract` TINYINT NOT NULL,
`balance` DECIMAL(64,18) NOT NULL,
`last_block_number` BIGINT NOT NULL,
`nonce` TINYTEXT NOT NULL,
`transaction_hash` VARCHAR(64) NOT NULL,
`approx_balance` double(48,18) NULL ,
`first_block_number` bigint not null,
PRIMARY KEY(`address`)
) ENGINE = InnoDB;
create index first_block_number_account on account(first_block_number);
create index last_block_number_account on account(last_block_number);

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
	PRIMARY KEY(`contract_addr`)
) ENGINE = InnoDB;
CREATE INDEX `name_token` ON token(`name`);
CREATE INDEX `symbol_token` ON token(`symbol`);
CREATE INDEX `creation_timestamp_token` ON token(`creation_timestamp`);


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
	PRIMARY KEY(`transfer_id`)
) ENGINE = InnoDB;
CREATE INDEX `to_addr_token_transfers` ON token_transfers(`to_addr`);
CREATE INDEX `from_addr_token_transfers` ON token_transfers(`from_addr`);
CREATE INDEX `contract_addr_token_transfers` ON token_transfers(`contract_addr`);
CREATE INDEX `transaction_hash_token_transfers` ON token_transfers(`transaction_hash`);
CREATE INDEX `transfer_timestamp_token_transfers` ON token_transfers(`transfer_timestamp`);

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
	`internal` boolean,
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
	`end_block` BIGINT NOT NULL ,
	`average_nrg_consumed` DECIMAL(64,10) NULL,
	`average_nrg_limit` DECIMAL(64,10) NULL,
	`averaged_block_time` DECIMAL(14,1) NULL,
	`average_difficulty` DECIMAL(64,10) NULL,
	`end_timestamp` INT NULL,
	`start_timestamp` INT NULL,
	`averaged_hash_power` DECIMAL(64,10) NULL,
	`last_block_reward` DECIMAL(64,18) NULL,
	`pow_avg_difficulty` DECIMAL(32,1) NULL ,
	`pos_avg_difficulty` DECIMAL(32,1 ) NULL ,
	`pow_avg_block_time` DECIMAL(32,1) NULL ,
	`pos_avg_block_time` DECIMAL(32,1 ) NULL ,
	`avg_pos_issuance` DECIMAL(64,1) NULL,
	`percentage_of_network_staking` DECIMAL(9,6) NULL,
	`total_stake` DECIMAL(36, 19) NULL,
	PRIMARY KEY(`id`, `end_block`)
) ENGINE = InnoDB;


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


create table internal_transaction
(
	transaction_hash           varchar(64) not null,
	internal_transaction_index int         not null,
	nrg_price                  decimal(32)     not null,
	nrg_limit                  decimal(32)      not null,
	data                       mediumtext  not null,
	rejected                   boolean     not null,
	kind                       varchar(16) not null,
	from_addr                  varchar(64) not null,
	to_addr                    varchar(64) not null,
	nonce                      decimal     not null,
	block_number               bigint      not null,
	value                      decimal(40)      not null,
	timestamp                  bigint      not null,
	contract_address		   varchar(64) not null,
	primary key (transaction_hash, internal_transaction_index)
);

create index internal_transaction_block_number_index
	on internal_transaction (block_number);

create index internal_transaction_from_addr_index
	on internal_transaction (from_addr);

create index internal_transaction_to_addr_index
	on internal_transaction (to_addr);

create index internal_transaction_contract_address_uindex
	on internal_transaction (contract_address);


CREATE TABLE `tx_log` (
    transaction_hash varchar(64),
    log_index int,
    block_number bigint(64),
    block_timestamp bigint,
    topics text,
    data text,
    contract_addr varchar(64),
    from_addr varchar(64),
    to_addr varchar(64),
    contract_type varchar(8),
    primary key (transaction_hash, log_index)
) engine = InnoDB;

create index tx_log_block_timestamp_index on tx_log(block_timestamp);
create index tx_log_block_number_index on tx_log(block_number);
create index tx_log_contract_addr_index on tx_log(contract_addr);
create index tx_log_from_addr_index on tx_log(from_addr);
create index tx_log_to_addr_index on tx_log(to_addr);

create table update_state (
    table_id int primary key ,
    table_name varchar(40),
    run_update bool,
    start bigint,
    end bigint
);

create table reorg_details(
    id bigint auto_increment primary key,
    block_number bigint not null,
    server_timestamp timestamp not null ,
    block_depth bigint not null,
    affected_addresses mediumtext not null,
    number_of_affected_transactions bigint not null
) engine = InnoDB;
create index reorg_details_block_number_index on reorg_details(block_number);
create unique index reorg_details_server_timestamp_index on reorg_details(server_timestamp);

create table validator_stats(
							block_number bigint not null,
							miner_address varchar(64) not null,
							seal_type varchar(4) not null,
							block_count int not null,
							block_timestamp bigint not null,
							percentage_of_blocks_validated decimal(10,7) not null,
							primary key (block_number, miner_address, seal_type)
);