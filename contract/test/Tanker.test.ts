import {network} from "hardhat";
import {expect} from "chai";

const {ethers, networkHelpers} = await network.connect();


describe("Tanker", () => {

    async function deployContracts() {
        const [owner] = await ethers.getSigners();

        const tanker = await ethers.deployContract("Tanker", [owner]);
        return {tanker};
    }


    describe('deploymern', () => {
        it("Should deploy", async () => {
            const [owner] = await ethers.getSigners();
            let {tanker} = await networkHelpers.loadFixture(deployContracts);

            expect(await tanker.owner()).to.equal(owner.address);
        })
    })


    describe('security', () => {
        it("only owner methods", async () => {
            const [owner, nonOwner] = await ethers.getSigners();
            let {tanker} = await networkHelpers.loadFixture(deployContracts);
            await expect(tanker.connect(nonOwner).setAttendant(nonOwner)).to.be.revertedWithCustomError(tanker, "OwnableUnauthorizedAccount");
            await expect(tanker.connect(nonOwner).addClient(nonOwner, 100n, 1000n)).to.be.revertedWithCustomError(tanker, "OwnableUnauthorizedAccount");
            await expect(tanker.connect(nonOwner).removeClient(0)).to.be.revertedWithCustomError(tanker, "OwnableUnauthorizedAccount");
            await expect(tanker.connect(nonOwner).drain(nonOwner,  0)).to.be.revertedWithCustomError(tanker, "OwnableUnauthorizedAccount");

        })

        it("only attendant", async () => {
            const [owner, nonOwner] = await ethers.getSigners();
            let {tanker} = await networkHelpers.loadFixture(deployContracts);
            await expect(tanker.resupply()).to.be.revertedWith("Tanktruck: attendant only");
        })
    })

    //  configuration settings
    describe('configuration', () => {
        // shall be able to set the new attendant
        it("Should set attendant", async () => {
            const [owner, nonOwner] = await ethers.getSigners();
            let {tanker} = await networkHelpers.loadFixture(deployContracts);

            await expect(tanker.setAttendant(nonOwner)).to.not.be.revert(ethers);

            expect(await tanker.attendant()).to.equal(nonOwner.address);
        })


        it('shall add new client', async () => {
            const [owner, client] = await ethers.getSigners();
            let {tanker} = await networkHelpers.loadFixture(deployContracts);

            await expect(tanker.addClient(client, 100n, 1000n)).to.not.be.revert(ethers);

            let [adr, lo, hi] = await tanker.clients(0);
            expect(adr).to.equal(client.address);
            expect(lo).to.equal(100n);
            expect(hi).to.equal(1000n);
        })

        it('shall remove client by number', async () => {
            const [owner, first, second] = await ethers.getSigners();
            let {tanker} = await networkHelpers.loadFixture(deployContracts);

            await expect(tanker.addClient(first, 100n, 1000n)).to.not.be.revert(ethers);
            await expect(tanker.addClient(second, 123n, 456n)).to.not.be.revert(ethers);

            await expect(tanker.removeClient(2)).to.be.revertedWith("Tanktruck: out of  bounds");
            await expect(tanker.removeClient(0)).to.not.be.revert(ethers);

            let [adr, lo, hi] = await tanker.clients(0);
            expect(adr).to.equal(second.address);
            expect(lo).to.equal(123n);
            expect(hi).to.equal(456n);
        })
    })


    //  operations
    describe('operations', () => {

        it('shall resupply clients', async () => {
            const [owner] = await ethers.getSigners();
            let {tanker} = await networkHelpers.loadFixture(deployContracts);

            await tanker.setAttendant(owner);

            //  3 addresses
            let first = ethers.Wallet.createRandom();
            let second = ethers.Wallet.createRandom();
            let third = ethers.Wallet.createRandom();

            console.log("balance: ", await ethers.provider.getBalance(first));

            //  supply tanker with gas

            // gove some value to second
            await owner.sendTransaction({
                to: second,
                value: 500, // Sends exactly 1.0 ether
            });


            await expect(tanker.addClient(first, 100n, 12345n)).to.not.be.revert(ethers);
            await expect(tanker.addClient(second, 500n, 45645454n)).to.not.be.revert(ethers);
            await expect(tanker.addClient(third, 123n, 34567n)).to.not.be.revert(ethers);

            //  resupply everybody,  this shall revert
            await expect(tanker.resupply()).to.be.revertedWith( "Tanktruck: out of gas");

            // gove some value to tanker
            await owner.sendTransaction({
                to: tanker,
                value: 100000, // Sends exactly 1.0 ether
            });

            //  shall be successful
            await expect(tanker.resupply()).to.not.be.revert(ethers);


            expect(await ethers.provider.getBalance(first)).to.equal(12345n);
            //  shall send nothing to second, it had enough!
            expect(await ethers.provider.getBalance(second)).to.equal(500n);
            expect(await ethers.provider.getBalance(third)).to.equal(34567n);
        })


        it('shall drain to destination', async () => {
            const [owner] = await ethers.getSigners();
            let {tanker} = await networkHelpers.loadFixture(deployContracts);


            //  3 addresses
            let dest = ethers.Wallet.createRandom();

            // gove some value to tanker
            await owner.sendTransaction({
                to: tanker,
                value: 100000, // Sends exactly 1.0 ether
            });

            await expect(tanker.drain(dest, 100000n)).to.not.be.revert(ethers);
            expect(await ethers.provider.getBalance(dest)).to.equal(100000n);
        })
    })
})