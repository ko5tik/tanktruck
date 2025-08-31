
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