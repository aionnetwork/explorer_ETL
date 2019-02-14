
pragma solidity 0.4.15;

interface ATS {

    function name() public constant returns (string);
    function symbol() public constant returns (string);
    function granularity() public constant returns (uint128);
    function totalSupply() public constant returns (uint128);

    function balanceOf(address tokenHolder) public constant returns (uint128);
    function isOperatorFor(address operator, address tokenHolder) public constant returns (bool);

    function authorizeOperator(address operator) public;
    function revokeOperator(address operator) public;

    function send(address to, uint128 amount, bytes senderData) public;
    function burn(uint128 amount, bytes senderData) public;

    function operatorSend(address from, address to, uint128 amount, bytes senderData, bytes operatorData) public;
    function operatorBurn(address from, uint128 amount, bytes senderData, bytes operatorData) public;

    function liquidSupply() public constant returns (uint128);
    function thaw(address localRecipient, uint128 amount, bytes32 bridgeId, bytes bridgeData, bytes32 remoteSender, bytes32 remoteBridgeId, bytes remoteData) public;
    function freeze(bytes32 remoteRecipient, uint128 amount, bytes32 bridgeId, bytes localData) public;
    function operatorFreeze(address localSender, bytes32 remoteRecipient, uint128 amount, bytes32 bridgeId, bytes localData) public;


    event Sent(
        address indexed operator,
        address indexed from,
        address indexed to,
        uint128 amount,
        bytes senderData,
        bytes operatorData
    );

    event Thawed(address indexed localRecipient, uint128 amount, bytes32 indexed bridgeId, bytes bridgeData, bytes32 indexed remoteSender, bytes32 remoteBridgeId, bytes remoteData);
    event Froze(address indexed localSender, bytes32 indexed remoteRecipient, uint128 amount, bytes32 indexed bridgeId, bytes localData);
    event Minted(address indexed operator, address indexed to, uint128 amount, bytes operatorData);
    event Burned(address indexed operator, address indexed from, uint128 amount, bytes senderData, bytes operatorData);

    event AuthorizedOperator(address indexed operator, address indexed tokenHolder);
    event RevokedOperator(address indexed operator, address indexed tokenHolder);
}