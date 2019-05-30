use aion; 
create table if not exists verified_contract
(
	contract_address varchar(64)       not null
		primary key,
	permission       tinyint default 0 null
);

INSERT INTO verified_contract (contract_address, permission) VALUES ('0000000000000000000000000000000000000000000000000000000000000200', 1);
INSERT INTO verified_contract (contract_address, permission) VALUES ('a0764dea1db22fa5e24895b746f8dd1825029d49e431cac570b4c3b4bf8b2995', 1);