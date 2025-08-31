
## docker image

### build

````shell
./gradlew bootBuildImage --imageName=tanktruck
````
You should not store real production keys in the docker image or repository.
Add production config after you build the image
