use aion;
create table if not exists verified_contract
(
	contract_address varchar(64)       not null
		primary key,
	permission       tinyint default 0 null
);

INSERT INTO verified_contract (contract_address, permission) VALUES ('0000000000000000000000000000000000000000000000000000000000000200', 1);