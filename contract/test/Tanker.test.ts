import {network} from "hardhat";
import {expect} from "chai";

const {ethers, networkHelpers} = await network.connect();


describe("Tanker", () => {

    async function deployContracts() {
        const [owner] = await ethers.getSigners();

        const tanker = await ethers.deployContract("Tanker", [owner]);
        return {tanker};
    }


    it("Should deploy", async () => {
        const [owner] = await ethers.getSigners();
        let {tanker} = await networkHelpers.loadFixture(deployContracts);

        expect(await tanker.owner()).to.equal(owner.address);
    })
})