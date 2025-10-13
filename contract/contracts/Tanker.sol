// SPDX-License-Identifier: UNLICENSED
pragma solidity ^0.8.10;

import "@openzeppelin/contracts/access/Ownable2Step.sol";

// tanker contract. holds  fuel on own balance, and distributes it
// when called by an attendant
contract Tanker is Ownable2Step {

    struct Client {
        // address to supply
        address client;
        // when this value is reached,  resupply
        uint256 low;
        //  up to this value
        uint256 high;
    }

    Client[]  public clients;

    // attendant initшting refueling. as this address primary is potentially exposed
    // from the conainer, only low value keys shall be used here.  do not keep more than required balnqce
    // gas money on it!
    address public attendant;


    function addClient(address _client, uint256 _low, uint256 _hi) external onlyOwner {
        clients.push(Client(_client, _low, _hi));
    }

    function removeClient(uint256 pos) external onlyOwner {
        require(pos < clients.length, "Tanktruck: out of  bounds");
        clients[pos] = clients[clients.length-1];
        clients.pop();
    }

    function setAttendant(address _attendant) external onlyOwner {
        attendant = _attendant;
    }

    constructor(address _owner)  Ownable(_owner){

    }


    modifier attendantOnly() {
        require(_msgSender() == attendant, "Tanktruck: attendant only");
        _;
    }
}
