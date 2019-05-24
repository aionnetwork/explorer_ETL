contract BridgeEvents{
	
	event ChangedOwner(address indexed param0);
	event AddMember(address indexed param0);
	event RemoveMember(address indexed param0);
	event ProcessedBundle(bytes32 indexed param0, bytes32 indexed param1);
	event Distributed(bytes32 indexed param0, address indexed param1, uint128 indexed param2);
	event SuccessfulTxHash(bytes32 indexed param0);



}