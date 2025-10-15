// SPDX-License-Identifier: UNLICENSED
pragma solidity ^0.8.10;

// interface to tanker contract.  we can only resupply
interface ITanker {

    function needResupply() external view returns (bool);

    function resupply() external;
}
