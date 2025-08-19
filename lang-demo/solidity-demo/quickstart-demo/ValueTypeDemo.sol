// SPDX-License-Identifier: MIT
pragma solidity ^0.8.20;

contract ValueTypeDemo {
    // 整数
    uint256 public uIntVar = 100;
    int256 public intVar = -50;

    // 布尔
    bool public isActive = true;

    // 地址
    address public owner;

    // 固定字节
    bytes1 public singleByte = 0x1f;
    bytes32 public hashValue;

    // 构造函数
    constructor() {
        owner = msg.sender;
        hashValue = keccak256(abi.encodePacked("Hello Solidity"));
    }
}
