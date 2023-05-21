# CollaborEat

[Deployed Server](https://sopra-fs23-group-03-server.oa.r.appspot.com/)
[Deployed Client](https://sopra-fs23-group-03-client.oa.r.appspot.com/)

## Introduction
the project’s goal and motivation

## Technologies
- Java
- JPA (database functionality)
- Spring Boot (RESTful API)
- Google Cloud (doployment)

## High-level components

### [User](https://github.com/sopra-fs23-group-03/sopra-fs23-group-03-server/blob/main/src/main/java/ch/uzh/ifi/hase/soprafs23/entity/User.java)
- Role: The user can save their profile.
- Relation: A user can create a group (be the host) or join a group (be a guest). They can recieve invitations to groups and send out requests to join groups.

### [Group](https://github.com/sopra-fs23-group-03/sopra-fs23-group-03-server/blob/main/src/main/java/ch/uzh/ifi/hase/soprafs23/entity/Group.java)
- Role: Users can form groups in order to plan a meal together.
- Relation: A group has a host and guests. The group keeps track of the ingredients the members contribute and their ratings and finds a recipe for the members at the end of the process.

### [Invitation](https://github.com/sopra-fs23-group-03/sopra-fs23-group-03-server/blob/main/src/main/java/ch/uzh/ifi/hase/soprafs23/entity/Invitation.java) and [JoinRequest](https://github.com/sopra-fs23-group-03/sopra-fs23-group-03-server/blob/main/src/main/java/ch/uzh/ifi/hase/soprafs23/entity/JoinRequest.java)
- Role: Users can join groups through invitations and join requests.
- Relation: The host of a group can send out invitations to their group to other users, who will accept or reject the invitation. Users can send join-requests to groups, which will be accepted or rejected by the host of the group.

### [Ingredient](https://github.com/sopra-fs23-group-03/sopra-fs23-group-03-server/blob/main/src/main/java/ch/uzh/ifi/hase/soprafs23/entity/Ingredient.java)
- Role: Users can contribute ingredients to the group they are part of.
- Relation: The ingredients are added to the group by the members of the group. The members then rate the ingredients and based on them a final recipe is chosen for the group.

### [Recipe](https://github.com/sopra-fs23-group-03/sopra-fs23-group-03-server/blob/main/src/main/java/ch/uzh/ifi/hase/soprafs23/SpooncularAPI/Recipe.java)
- Role: At the end of the process a recipe is found for the group.
- Relation: The final recipe is found based on the ingredients the users contibute, the rating of these ingredients, and the group members' allergies.

## Launch & Deployment
Write down the steps a new developer joining your team would have to take to get started with your application. What commands are required to build and run your project locally? How can they run the tests? Do you have external dependencies or a database that needs to be running? How can they do releases?

### Getting started with Spring Boot
-   Documentation: https://docs.spring.io/spring-boot/docs/current/reference/html/index.html
-   Guides: http://spring.io/guides
    -   Building a RESTful Web Service: http://spring.io/guides/gs/rest-service/
    -   Building REST services with Spring: https://spring.io/guides/tutorials/rest/

### Setup with your IDE of choice
Download your IDE of choice (e.g., [IntelliJ](https://www.jetbrains.com/idea/download/), [Visual Studio Code](https://code.visualstudio.com/), or [Eclipse](http://www.eclipse.org/downloads/)). Make sure Java 17 is installed on your system (for Windows, please make sure your `JAVA_HOME` environment variable is set to the correct version of Java).

#### IntelliJ
1. File -> Open... -> SoPra server template
2. Accept to import the project as a `gradle project`
3. To build right click the `build.gradle` file and choose `Run Build`

#### VS Code
The following extensions can help you get started more easily:
-   `vmware.vscode-spring-boot`
-   `vscjava.vscode-spring-initializr`
-   `vscjava.vscode-spring-boot-dashboard`
-   `vscjava.vscode-java-pack`

**Note:** You'll need to build the project first with Gradle, just click on the `build` command in the _Gradle Tasks_ extension. Then check the _Spring Boot Dashboard_ extension if it already shows `soprafs23` and hit the play button to start the server. If it doesn't show up, restart VS Code and check again.

### Building with Gradle
You can use the local Gradle Wrapper to build the application.
-   macOS: `./gradlew`
-   Linux: `./gradlew`
-   Windows: `./gradlew.bat`

More Information about [Gradle Wrapper](https://docs.gradle.org/current/userguide/gradle_wrapper.html) and [Gradle](https://gradle.org/docs/).

#### **Build** `./gradlew build`

#### **Run** `./gradlew bootRun`
You can verify that the server is running by visiting `localhost:8080` in your browser.

#### **Test** `./gradlew test`

#### **Development Mode**
You can start the backend in development mode, this will automatically trigger a new build and reload the application once the content of a file has been changed.

Start two terminal windows and run: `./gradlew build --continuous`

and in the other one: `./gradlew bootRun`

### API Endpoint Testing with Postman
We recommend using [Postman](https://www.getpostman.com) to test our API Endpoints.

### Debugging
If something is not working and/or you don't know what is going on. We recommend using a debugger and step-through the process step-by-step.

To configure a debugger for SpringBoot's Tomcat servlet (i.e. the process you start with `./gradlew bootRun` command), do the following:

1. Open Tab: **Run**/Edit Configurations
2. Add a new Remote Configuration and password it properly
3. Start the Server in Debug mode: `./gradlew bootRun --debug-jvm`
4. Press `Shift + F9` or the use **Run**/Debug "Name of your task"
5. Set breakpoints in the application where you need it
6. Step through the process one step at a time

## External API
TODO:
In your client repository, briefly describe and illustrate the main user flow(s) of your interface. How does it work (without going into too much detail)? Feel free to include a few screenshots of your application.
--> we here in the server want to say something on the external api

## Roadmap
The top 2-3 features that new developers who want to contribute to your project could add.

## Authors and acknowledgment
- Kalliopi Papadaki - [KallPap](https://github.com/KallPap)
- Orestis Bollano - [OrestisBollano](https://github.com/OrestisBollano)
- Ann-Kathrin Hedwig Sabina Müller - [akkuebler](https://github.com/akkuebler)
- Chiara Letsch - [chiaralentsch](https://github.com/chiaralentsch)
- Lany Dennise Weizenblut Oseguera - [wlany](https://github.com/wlany)

## License
Say how your project is licensed
License guide: https://choosealicense.com/

