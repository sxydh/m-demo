// SPDX-License-Identifier: MIT
pragma solidity ^0.8.20;

import "@openzeppelin/contracts/token/ERC721/ERC721.sol";
import "@openzeppelin/contracts/access/Ownable.sol";
import "@openzeppelin/contracts/utils/Counters.sol";

contract UUIDNFT is ERC721, Ownable {
    using Counters for Counters.Counter;
    Counters.Counter private _tokenIdCounter;

    mapping(string => uint256) private _uuidToTokenId;
    mapping(uint256 => string) private _tokenIdToUUID;

    constructor() ERC721("UUIDNFT", "UNFT") Ownable(msg.sender) {}

    function createUUID(string memory uuid) public {
        require(bytes(uuid).length > 0, "UUID cannot be empty");
        require(_uuidToTokenId[uuid] == 0, "UUID already exists");

        _tokenIdCounter.increment();
        uint256 newTokenId = _tokenIdCounter.current();

        _safeMint(msg.sender, newTokenId);
        _uuidToTokenId[uuid] = newTokenId;
        _tokenIdToUUID[newTokenId] = uuid;
    }

    function getBalance(address owner) public view returns (uint256) {
        return balanceOf(owner);
    }

    function getUUID(uint256 tokenId) public view returns (string memory) {
        require(_ownerOf(tokenId) != address(0), "Token does not exist");
        return _tokenIdToUUID[tokenId];
    }
}