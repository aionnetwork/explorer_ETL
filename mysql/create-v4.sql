create database aionv4;

use aionv4;

create table block(
	block_number bigint(64) primary key,  
	block_hash varchar(64),
	miner_address varchar(64), 
	parent_hash varchar(64),
	receipt_tx_root varchar(64),
	state_root varchar(64), 
	tx_trie_root varchar(64), 
	extra_data varchar(64),
	nonce text, 
	bloom text, 
	solution text, 
	difficulty text, 
	total_difficulty text, 
	nrg_consumed bigint(64), 
	nrg_limit bigint(64), 
	size bigint(64), 
	block_timestamp bigint(64), 
	num_transactions bigint(64), 
	block_time bigint(64),
	nrg_reward text,
	transaction_id bigint(64),
	transaction_list longtext)
partition by range columns(block_number)(
		partition p0 values less than (1000000),
		partition p1 values less than (2000000),
		partition p2 values less than (3000000),
		partition p3 values less than (4000000),
		partition p4 values less than (5000000),
		partition p5 values less than (6000000),
		partition p6 values less than (7000000),
		partition p7 values less than (8000000),
		partition p8 values less than (9000000),
		partition p9 values less than (10000000),
		partition p10 values less than (11000000),
		partition p11 values less than (12000000),
		partition p12 values less than (13000000),
		partition p13 values less than (14000000),
		partition p14 values less than (15000000),
		partition p15 values less than (16000000),
		partition p16 values less than (17000000),
		partition p17 values less than (18000000),
		partition p18 values less than (19000000),
		partition p19 values less than (20000000),
		partition p20 values less than (21000000),
		partition p21 values less than (22000000),
		partition p22 values less than (23000000),
		partition p23 values less than (24000000),
		partition p24 values less than (25000000),
		partition p25 values less than (26000000),
		partition p26 values less than (27000000),
		partition p27 values less than (28000000),
		partition p28 values less than (29000000),
		partition p29 values less than (30000000),
		partition p30 values less than (31000000),
		partition p31 values less than (32000000),
		partition p32 values less than (33000000),
		partition p33 values less than (34000000),
		partition p34 values less than (35000000),
		partition p35 values less than (36000000),
		partition p36 values less than (37000000),
		partition p37 values less than (38000000),
		partition p38 values less than (39000000),
		partition p39 values less than (40000000),
		partition p40 values less than (41000000),
		partition p41 values less than (42000000),
		partition p42 values less than (43000000),
		partition p43 values less than (44000000),
		partition p44 values less than (45000000),
		partition p45 values less than (46000000),
		partition p46 values less than (47000000),
		partition p47 values less than (48000000),
		partition p48 values less than (49000000),
		partition p49 values less than (50000000),
		partition p50 values less than (51000000),
		partition p51 values less than (52000000),
		partition p52 values less than (53000000),
		partition p53 values less than (54000000),
		partition p54 values less than (55000000),
		partition p55 values less than (56000000),
		partition p56 values less than (57000000),
		partition p57 values less than (58000000),
		partition p58 values less than (59000000),
		partition p59 values less than (60000000),
		partition p60 values less than (61000000),
		partition p61 values less than (62000000),
		partition p62 values less than (63000000),
		partition p63 values less than (64000000),
		partition p64 values less than (65000000),
		partition p65 values less than (66000000),
		partition p66 values less than (67000000),
		partition p67 values less than (68000000),
		partition p68 values less than (69000000),
		partition p69 values less than (70000000),
		partition p70 values less than (71000000),
		partition p71 values less than (72000000),
		partition p72 values less than (73000000),
		partition p73 values less than (74000000),
		partition p74 values less than (75000000),
		partition p75 values less than (76000000),
		partition p76 values less than (77000000),
		partition p77 values less than (78000000),
		partition p78 values less than (79000000),
		partition p79 values less than (80000000),
		partition p80 values less than (81000000),
		partition p81 values less than (82000000),
		partition p82 values less than (83000000),
		partition p83 values less than (84000000),
		partition p84 values less than (85000000),
		partition p85 values less than (86000000),
		partition p86 values less than (87000000),
		partition p87 values less than (88000000),
		partition p88 values less than (89000000),
		partition p89 values less than (90000000),
		partition p90 values less than (91000000),
		partition p91 values less than (92000000),
		partition p92 values less than (93000000),
		partition p93 values less than (94000000),
		partition p94 values less than (95000000),
		partition p95 values less than (96000000),
		partition p96 values less than (97000000),
		partition p97 values less than (98000000),
		partition p98 values less than (99000000),
		partition p99 values less than (100000000),
		partition p100 values less than (101000000),
		partition p101 values less than (102000000),
		partition p102 values less than (103000000),
		partition p103 values less than (104000000),
		partition p104 values less than (105000000),
		partition p105 values less than (106000000),
		partition p106 values less than (107000000),
		partition p107 values less than (108000000),
		partition p108 values less than (109000000),
		partition p109 values less than (110000000),
		partition p110 values less than (111000000),
		partition p111 values less than (112000000),
		partition p112 values less than (113000000),
		partition p113 values less than (114000000),
		partition p114 values less than (115000000),
		partition p115 values less than (116000000),
		partition p116 values less than (117000000),
		partition p117 values less than (118000000),
		partition p118 values less than (119000000),
		partition p119 values less than (120000000),
		partition p120 values less than (121000000),
		partition p121 values less than (122000000),
		partition p122 values less than (123000000),
		partition p123 values less than (124000000),
		partition p124 values less than (125000000),
		partition p125 values less than (126000000),
		partition p126 values less than (127000000),
		partition p127 values less than (128000000),
		partition p128 values less than (129000000),
		partition p129 values less than (130000000),
		partition p130 values less than (131000000),
		partition p131 values less than (132000000),
		partition p132 values less than (133000000),
		partition p133 values less than (134000000),
		partition p134 values less than (135000000),
		partition p135 values less than (136000000),
		partition p136 values less than (137000000),
		partition p137 values less than (138000000),
		partition p138 values less than (139000000),
		partition p139 values less than (140000000),
		partition p140 values less than (141000000),
		partition p141 values less than (142000000),
		partition p142 values less than (143000000),
		partition p143 values less than (144000000),
		partition p144 values less than (145000000),
		partition p145 values less than (146000000),
		partition p146 values less than (147000000),
		partition p147 values less than (148000000),
		partition p148 values less than (149000000),
		partition p149 values less than (150000000)
);
create index block_hash_block on block(block_hash);
create index miner_address_block on block(miner_address);

create table block_map(
	block_hash varchar(64)  primary key,
	block_number bigint(64));

create table transaction(
	id bigint(64) primary key,
	transaction_hash varchar(64),
	block_hash varchar(64),
	block_number bigint(64),
	transaction_index bigint(64),
	from_addr varchar(64), 
	to_addr varchar(64),
	nrg_consumed bigint(64), 
	nrg_price bigint(64), 
	transaction_timestamp bigint(64),
	block_timestamp bigint(64),
	value text, 
	transaction_log text,
	data text, 
	nonce text,
	tx_error text,
	contract_addr varchar(64))
partition by range columns(id)(
		partition p0 values less than (1000000),
		partition p1 values less than (2000000),
		partition p2 values less than (3000000),
		partition p3 values less than (4000000),
		partition p4 values less than (5000000),
		partition p5 values less than (6000000),
		partition p6 values less than (7000000),
		partition p7 values less than (8000000),
		partition p8 values less than (9000000),
		partition p9 values less than (10000000),
		partition p10 values less than (11000000),
		partition p11 values less than (12000000),
		partition p12 values less than (13000000),
		partition p13 values less than (14000000),
		partition p14 values less than (15000000),
		partition p15 values less than (16000000),
		partition p16 values less than (17000000),
		partition p17 values less than (18000000),
		partition p18 values less than (19000000),
		partition p19 values less than (20000000),
		partition p20 values less than (21000000),
		partition p21 values less than (22000000),
		partition p22 values less than (23000000),
		partition p23 values less than (24000000),
		partition p24 values less than (25000000),
		partition p25 values less than (26000000),
		partition p26 values less than (27000000),
		partition p27 values less than (28000000),
		partition p28 values less than (29000000),
		partition p29 values less than (30000000),
		partition p30 values less than (31000000),
		partition p31 values less than (32000000),
		partition p32 values less than (33000000),
		partition p33 values less than (34000000),
		partition p34 values less than (35000000),
		partition p35 values less than (36000000),
		partition p36 values less than (37000000),
		partition p37 values less than (38000000),
		partition p38 values less than (39000000),
		partition p39 values less than (40000000),
		partition p40 values less than (41000000),
		partition p41 values less than (42000000),
		partition p42 values less than (43000000),
		partition p43 values less than (44000000),
		partition p44 values less than (45000000),
		partition p45 values less than (46000000),
		partition p46 values less than (47000000),
		partition p47 values less than (48000000),
		partition p48 values less than (49000000),
		partition p49 values less than (50000000),
		partition p50 values less than (51000000),
		partition p51 values less than (52000000),
		partition p52 values less than (53000000),
		partition p53 values less than (54000000),
		partition p54 values less than (55000000),
		partition p55 values less than (56000000),
		partition p56 values less than (57000000),
		partition p57 values less than (58000000),
		partition p58 values less than (59000000),
		partition p59 values less than (60000000),
		partition p60 values less than (61000000),
		partition p61 values less than (62000000),
		partition p62 values less than (63000000),
		partition p63 values less than (64000000),
		partition p64 values less than (65000000),
		partition p65 values less than (66000000),
		partition p66 values less than (67000000),
		partition p67 values less than (68000000),
		partition p68 values less than (69000000),
		partition p69 values less than (70000000),
		partition p70 values less than (71000000),
		partition p71 values less than (72000000),
		partition p72 values less than (73000000),
		partition p73 values less than (74000000),
		partition p74 values less than (75000000),
		partition p75 values less than (76000000),
		partition p76 values less than (77000000),
		partition p77 values less than (78000000),
		partition p78 values less than (79000000),
		partition p79 values less than (80000000),
		partition p80 values less than (81000000),
		partition p81 values less than (82000000),
		partition p82 values less than (83000000),
		partition p83 values less than (84000000),
		partition p84 values less than (85000000),
		partition p85 values less than (86000000),
		partition p86 values less than (87000000),
		partition p87 values less than (88000000),
		partition p88 values less than (89000000),
		partition p89 values less than (90000000),
		partition p90 values less than (91000000),
		partition p91 values less than (92000000),
		partition p92 values less than (93000000),
		partition p93 values less than (94000000),
		partition p94 values less than (95000000),
		partition p95 values less than (96000000),
		partition p96 values less than (97000000),
		partition p97 values less than (98000000),
		partition p98 values less than (99000000),
		partition p99 values less than (100000000),
		partition p100 values less than (101000000),
		partition p101 values less than (102000000),
		partition p102 values less than (103000000),
		partition p103 values less than (104000000),
		partition p104 values less than (105000000),
		partition p105 values less than (106000000),
		partition p106 values less than (107000000),
		partition p107 values less than (108000000),
		partition p108 values less than (109000000),
		partition p109 values less than (110000000),
		partition p110 values less than (111000000),
		partition p111 values less than (112000000),
		partition p112 values less than (113000000),
		partition p113 values less than (114000000),
		partition p114 values less than (115000000),
		partition p115 values less than (116000000),
		partition p116 values less than (117000000),
		partition p117 values less than (118000000),
		partition p118 values less than (119000000),
		partition p119 values less than (120000000),
		partition p120 values less than (121000000),
		partition p121 values less than (122000000),
		partition p122 values less than (123000000),
		partition p123 values less than (124000000),
		partition p124 values less than (125000000),
		partition p125 values less than (126000000),
		partition p126 values less than (127000000),
		partition p127 values less than (128000000),
		partition p128 values less than (129000000),
		partition p129 values less than (130000000),
		partition p130 values less than (131000000),
		partition p131 values less than (132000000),
		partition p132 values less than (133000000),
		partition p133 values less than (134000000),
		partition p134 values less than (135000000),
		partition p135 values less than (136000000),
		partition p136 values less than (137000000),
		partition p137 values less than (138000000),
		partition p138 values less than (139000000),
		partition p139 values less than (140000000),
		partition p140 values less than (141000000),
		partition p141 values less than (142000000),
		partition p142 values less than (143000000),
		partition p143 values less than (144000000),
		partition p144 values less than (145000000),
		partition p145 values less than (146000000),
		partition p146 values less than (147000000),
		partition p147 values less than (148000000),
		partition p148 values less than (149000000),
		partition p149 values less than (150000000)
);
create index block_number_transaction on transaction(block_number);
create index from_addr_transaction on transaction(from_addr);
create index to_addr_transaction on transaction(to_addr);

create table transaction_map(
	transaction_hash varchar(64) primary key,
	id bigint(64));

create table parser_state(
	id int primary key,
	block_number bigint(64),
	transaction_id bigint(64));

# to track current block head
insert into parser_state values(1,-1,-1);
# to track current blockchain head
insert into parser_state values(2,-1,-1);
# to track integrity check head
insert into parser_state values(3,0,-1);
# to track the current state of the graph tables
insert into parser_state values(4,1,-1);
# to track the start of the rolling window
insert into parser_state values(5,1,-1);
# to tract the start of the rolling transaction window
insert into parser_state values(6,1,-1);


CREATE TABLE `token` (
  `contract_addr` varchar(64) NOT NULL,
  `transaction_hash` varchar(64) NOT NULL,
  -- `special_address` varchar(64) NOT NULL,
  `name` varchar(65535) DEFAULT NULL,
  `symbol` varchar(65535) DEFAULT NULL,
  `creator_address` varchar(64) NOT NULL,
  `total_supply` bigint(64) NOT NULL,
  `creation_timestamp` bigint(64) DEFAULT NULL,
  `granularity` bigint(64) NOT NULL,
  `liquid_supply` bigint(64) NOT NULL,
  PRIMARY KEY (`contract_addr`)
) ENGINE=InnoDB ;
create index name_token on token(`name`);
create index symbol_token on token(`symbol`);




CREATE TABLE `balance` (
  `address` varchar(64) NOT NULL,
  `contract` tinyint(1) NOT NULL,
  `balance` decimal(64,18) NOT NULL,
  `last_block_number` bigint(64) NOT NULL,
  `nonce` bigint(64) NOT NULL,
  `transaction_id` bigint(64) NOT NULL,
  PRIMARY KEY (`address`)
) ENGINE=InnoDB ;

CREATE TABLE `transfer` (
 `transfer_id` BIGINT(64) NOT NULL auto_increment,
 `to_addr` varchar(64) NOT NULL ,
 `from_addr` varchar(64) NOT NULL ,
 `operator_addr` varchar(64) NOT NULL ,
 `tkn_value` decimal(64,18) NOT NULL,
 `contract_addr` varchar(64) NOT NULL ,
 `transaction_id` bigint(64) NOT NULL,
 `block_number` bigint(64) NOT NULL,
 `transfer_timestamp` bigint(64),
 Primary KEY (`transfer_id`)
)ENGINE=InnoDB ;
create index from_addr_transfer on transfer(from_addr);
create index to_addr_transfer on transfer(to_addr);
create index contract_addr_transfer on transfer(contract_addr);

CREATE table token_balance(
  -- token_balance_id BIGINT(64) NOT NULL auto_increment,
  tkn_balance DECIMAL (64,18) NOT NULL ,
  holder_addr varchar(64) NOT NULL ,
  contract_addr varchar(64) NOT NULL ,
  block_number bigint(64) Not NULL ,
  primary key (holder_addr)
)ENGINE=InnoDB;
create index holder_addr_token_balance on token_balance(holder_addr);
create index contract_addr_token_balance on token_balance(contract_addr);

create table event(
  event_id BIGINT(64) auto_increment,
  name text NOT NULL,
  parameter_list text NOT NULL,
  input_list text NOT NULL ,
  transaction_id bigint(64) NOT NULL ,
  block_number bigint(64) NOT NULL ,
  contract_addr varchar(64) NOT NULL,
  event_timestamp bigint(64) NOT NULL,
  PRIMARY KEY (event_id)
)ENGINE=InnoDB;
create index contract_addr_event on token_balance(contract_addr);

create table contract(
  contract_addr VARCHAR(64) NOT NULL ,
  contract_name text,
  contract_creator_addr varchar(64) NOT NULL,
  contract_tx_hash varchar(64) NOT NULL ,
  block_number Bigint(64) NOT NULL,
  deploy_timestamp bigint(64) not null,
  primary key (contract_addr)
)ENGINE=InnoDB;
create index creator_addr_contract on contract(contract_creator_addr);

create table graphing(
  id bigint(64) auto_increment,
  value decimal(64,18) NOT NULL,
  graph_type varchar(64) NOT NULL,
  timestamp bigint(64) NOT NULL,
  block_number bigint(64) NOT NULL,
  detail varchar(64),
  year int(4) NOT NULL,
  month int(2) NOT NULL,
  date int(2) NOT NULL,
  primary key(id)
)ENGINE=InnoDB;



create index graph_type_graphing on graphing(graph_type);
create index year_graph_type_graphing on graphing(year, graph_type);
create index month_graph_type_graphing on graphing(month, graph_type);
create index date_graph_type_graphing on graphing(date, graph_type);



create table metrics(
	id tinyint not null primary key,
	total_transaction bigint(64) null ,
	transactions_per_second decimal(64, 10) null ,
	peak_transactions_per_block int(4) null ,
	start_block bigint(64) null ,
	end_block bigint(64) null ,
	average_nrg_consumed decimal(64, 10) null ,
	average_nrg_limit decimal(64, 10) null ,
	averaged_block_time decimal(14, 10) null ,
	average_difficulty decimal(64, 10) null ,
	end_timestamp bigint(32) null ,
	start_timestamp bigint(32) null ,
	averaged_hash_power decimal(64,10) null
);


insert into metrics (id) values (1);
insert into metrics (id) values (2);
