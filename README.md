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



### run

````shell
sudo docker run -d -it tanktruck --tanktruck.executorKey=xxxxx-your-private-key-here  --spring.profiles.active=pulse
````