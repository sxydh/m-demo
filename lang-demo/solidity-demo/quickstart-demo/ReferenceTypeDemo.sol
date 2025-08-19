// SPDX-License-Identifier: MIT
pragma solidity ^0.8.20;

contract ReferenceTypeDemo {

    // 动态数组
    uint256[] public numbers;

    // 固定长度数组
    uint256[3] public fixedNumbers = [1, 2, 3];

    // 结构体
    struct Person {
        string name;
        uint256 age;
    }

    Person public person;

    // 映射
    mapping(address => uint256) public balances;

}
