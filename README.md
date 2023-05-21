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
- Recipe-Food-Nutrition API by spoonacular (external API)

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

### Getting started with Spring Boot
-   Documentation: https://docs.spring.io/spring-boot/docs/current/reference/html/index.html
-   Guides: http://spring.io/guides
    -   Building a RESTful Web Service: http://spring.io/guides/gs/rest-service/
    -   Building REST services with Spring: https://spring.io/guides/tutorials/rest/

### Setup with your IDE of choice
Download your IDE of choice (e.g., [IntelliJ](https://www.jetbrains.com/idea/download/), [Visual Studio Code](https://code.visualstudio.com/), or [Eclipse](http://www.eclipse.org/downloads/)). Make sure Java 17 is installed on your system (for Windows, please make sure your `JAVA_HOME` environment variable is set to the correct version of Java).

#### **IntelliJ**
1. File -> Open... -> SoPra server template
2. Accept to import the project as a `gradle project`
3. To build right click the `build.gradle` file and choose `Run Build`

#### **VS Code**
The following extensions can help you get started more easily:
-   `vmware.vscode-spring-boot`
-   `vscjava.vscode-spring-initializr`
-   `vscjava.vscode-spring-boot-dashboard`
-   `vscjava.vscode-java-pack`

*Note*: You'll need to build the project first with Gradle, just click on the `build` command in the _Gradle Tasks_ extension. Then check the _Spring Boot Dashboard_ extension if it already shows `soprafs23` and hit the play button to start the server. If it doesn't show up, restart VS Code and check again.

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

### Disclaimers
We assume that all group members stay online and continue the meal-planning process together. If one member is not moving along in the process, all other members of the group get stuck at that point and would have to delete their local storage and create a new account in order to user our services.

Further our application was developed and tested mostly on [Google Chrome](https://www.google.com/chrome/). We therefore recomend you download and install Chrome to have the best experience with our application.

## External API
TODO: anki
--> we here in the server want to say something on the external api

## Roadmap

- **The distribution of missing ingredients**: While we maximize the ingredients from the final list of ingredients to be used in the recipe, it can happen, that the recipe requires ingredients, that are missing. In a next step, we'd implement a way to decide/plan which of the members of the group will bring which of the missing ingredients. Related user story: [#24](https://github.com/sopra-fs23-group-03/sopra-fs23-group-03-server/issues/24)

- **Additional decision logic**: The rating of the ingredients is implemented with a majority-vote-logic. In a next step, we'd add additional decision logics to this step, e.g. a point-distribution logic. Related user story: [#22](https://github.com/sopra-fs23-group-03/sopra-fs23-group-03-server/issues/22)

- **Choose from multiple recipes**: A group will get one final recipe at the end of the process, if the spoonacular API had returned multiple recipes, we choose one at random for the group. In a next step we'd give the group all options that the spoonacular API provides and let the host choose one of them as the final recipe. Related user story: [#25](https://github.com/sopra-fs23-group-03/sopra-fs23-group-03-server/issues/25)


## Authors and acknowledgment
- Kalliopi Papadaki - [KallPap](https://github.com/KallPap)
- Orestis Bollano - [OrestisBollano](https://github.com/OrestisBollano)
- Ann-Kathrin Hedwig Sabina Kübler - [akkuebler](https://github.com/akkuebler)
- Chiara Letsch - [chiaralentsch](https://github.com/chiaralentsch)
- Lany Dennise Weizenblut Oseguera - [wlany](https://github.com/wlany)

## License
Say how your project is licensed
License guide: https://choosealicense.com/

