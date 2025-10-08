// SPDX-License-Identifier: UNLICENSED
pragma solidity ^0.8.10;

import "@openzeppelin/contracts/access/Ownable2Step.sol";


// tanker contract.  holds  fuel on own balance, and distributes it
// when called by an attendant
contract Tanker is Ownable2Step {

    // attendant initating refueling. as this address primary is potentially exposed
    // from conatiner, only low value keys shall be used here.  do not keep more than required balaqce
    // gas money on it!
    address public attendant;

    constructor(address _owner)  Ownable(_owner){

    }
}
