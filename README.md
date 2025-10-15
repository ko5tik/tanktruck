##  Purpose

Tanktruck refuels your ethereum account when needed. If you are running some blockchain operation  
and require periodic automated maintenance, you can use Tanktruck to keep technical runners funded. 

## Support

If you need support, of maintenance bots, or smart contracts developed—ask me.  If you like to thank me, 
send your thanks to: 0x853CE673EeE6e9FF5EE8144eF291ab31604e6D35


## docker image

### build

````shell
./gradlew bootBuildImage --imageName=tanktruck
````
You should not store real production keys in the docker image or repository.
Add production config after you build the image



### download and ship

````shell
    docker save -o tanktruck.tar tanktruck
    gzip -f tanktruck.tar
    scp tanktruck.tar.gz  root@37.27.2.146:.
````

Ship to new server:
````shell
    docker save -o tanktruck.tar tanktruck
    gzip -f tanktruck.tar
    scp tanktruck.tar.gz  root@135.181.1.93:.
````


### run

````shell
sudo docker run -d -it tanktruck --tanktruck.executorKey=xxxxx-your-private-key-here  --spring.profiles.active=pulse
````


## Contract instance

Contract:  0xc3995B7D1Fd836e578B6F116b50B68f42B1975b8
GUI: https://scgui.xyz/Tanktruck-F7Y48vE7J7

### Set up contracts

- Register the attendant address. This address will be allowed to run the resupply method.  Private key of this address shall be supplied as credential to runner bot
- Register addresses to be supplied with gas.   Attendant address can be suplied but this bot too!
- Send gas money to the contract address.  This amount will be distributed between registered addresses.