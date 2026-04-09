##  Purpose

Tanktruck refuels your ethereum account when needed. If you are running some blockchain operation  
and require periodic automated maintenance, you can use Tanktruck to keep technical runners funded. 

## Support

If you need support, of maintenance bots, or smart contracts developed, ask me.  If you like to thank me, 
send your thanks to: 0x853CE673EeE6e9FF5EE8144eF291ab31604e6D35


## Docker image

 See the readme file in the go subdirectory.

### download and Ship


Ship to server:

````shell
    docker save -o tanktruck.tar tanktruck
    gzip -f tanktruck.tar
    scp tanktruck.tar.gz  root@135.181.1.93:.
````


## Contract instance

After you have deployed smart contract in a chain of your choice, set up GUI for easier configuration:

https://scgui.xyz/

You will need ABI and deployed contract address. 

### Set up contracts

- Register the attendant address. This address will be allowed to run the resupply method.  Private key of this address shall be supplied as a credential to runner bot
- Register addresses to be supplied with gas. Attendant address can be suplied but this bot too!
- bulk address registration is available.
- Send gas money to the contract address. This amount will be distributed between registered addresses.