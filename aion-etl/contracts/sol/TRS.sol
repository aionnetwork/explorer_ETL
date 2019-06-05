pragma solidity ^0.4.15;

interface TRS{
    event Withdraws(address indexed who, uint amount);
    event Deposit(address indexed who, uint amount);
    event Mint(address indexed who, uint amount);
}