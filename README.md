# CollaborEat

![CollaborEat logo](/assets/logo.jpg)

[Deployed Server](https://sopra-fs23-group-03-server.oa.r.appspot.com/)

[Deployed Client](https://sopra-fs23-group-03-client.oa.r.appspot.com/)

CollaborEat is an online application where people can get together to plan a meal. Users can create an account and specify their allergies, favorite cuisines, and dietary preferences. They have an option to go solo and get a random recipe based on their profile. They can also opt to host a group and invite other users to plan a meal together. Guests can join groups by accepting an invitation to one, but they can also request to join an existing group in their group-forming stage. Once a group is formed and all the guests are ready, the host can start the meal-finding process. All members of the group must enter the ingredients they want to contribute to the upcoming meal, and once the information is gathered everyone must rate the potential contributions to the meal. Finally, a recipe will be chosen based on the final ingredients everyone has agreed upon and the members' allergies. In case all ingredients are voted out by the members or all their ingredients match the allergies of group members, they will recieve a random recipe based on the allergies of the group members. On the result page, a photo of the chosen meal, the instructions, and the necessary ingredients will be displayed to the members of the group and the rest is up to them! 

### Motivation
Our motivation for this project was to aid fellow students in planning their meals with food they might already have at home. With our application we aim to reduce food waste and help in saving money and time for our users, but we also hope to inspire people to get creative with their meals and to get together to bond and have fun cooking together.

## Technologies
- Java
- JPA (database functionality)
- Spring Boot (RESTful API)
- Google Cloud (deployment)
- Recipe-Food-Nutrition API by spoonacular (external API)

## High-level components

### [User](https://github.com/sopra-fs23-group-03/sopra-fs23-group-03-server/blob/main/src/main/java/ch/uzh/ifi/hase/soprafs23/entity/User.java)
- Role: The user can save their allergies, favorite cuisines and dietary preference to their profile.
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
- Role: At the end of the process or when choosing to go *solo* a recipe is found for the group. 
- Relation: The final recipe is found based on the ingredients the users contibute, the rating of these ingredients, and the group members' allergies. If the *solo* option is chosen, the favorite cuisine and allergies of the user will be taken into account.

## External API
The [spoonacular API](https://spoonacular.com/food-api) is used to retrieve the final recipes based on the given ingredients, intolerances, favourite cuisines and dietary preferences. The ingredients, which the user can choose to contribute to the meal, are also retrieved from the API to avoid the need for natural language processing in the backend. Furthermore, the intolerances (or allergies), favourite cuisines and dietary preferences used to personalize the profiles are retrieved from spoonacular to later match the requests to the API.

We distinguish between the recipe retrieval for a.) groups and b.) for single users (*solo* option). 
For every displayed final recipe, two calls are needed: First, to retrieve the recipe suggestion based on a.) the ingredients with respect to the intolerances b.) intolerances, dietary preferences and favourite cuisines. With the second call, more detailed information such as image, instructions and ready-in-minutes are retrieved. For the first call we use the complex search provided by spoonacular, with the following additions: `ignorePantry=true` (we assume that pantry items are at home already), `type=main course` (we give only recipes for main courses), `sort=max-used-ingredients` (we first maximise the used ingredients, instead of minimising the additionally needed ones). The second call uses the `api.spoonacular.com/recipes/id/information` call to retireve the additional information on the recipe. 

## Disclaimers

### Users don't leave the process
We assume that all group members stay online and continue the meal-planning process together. If one member is not moving along in the process, all other members of the group get stuck at that point and would have to delete their local storage and create a new account in order to use our services.

### Web Browser
Our application was developed and tested mostly using [Google Chrome](https://www.google.com/chrome/). We therefore recommend you download and install Chrome to have the best experience with our application.

### Database
We have kept the in-memory H2 database of the template project provided by the SoPra FS23 Team. This means that whenever the server gets terminated, e.g. in our deployment due to inactivity, the database gets destroyed with it.

### Server Instance
In a first interaction with our deployed application, upon a server instance being created, you might experience our application stating that the server cannot be reached. This will resolve itself once the server has started properly. In order to avoid issues with multiple instances of the server we have also limited the maximum number of instances to one. This means, that our application does not scale for too many clients.

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

## Roadmap
See the [Future Work](https://github.com/orgs/sopra-fs23-group-03/projects/1/views/5?filterQuery=milestone%3A%22Future+Work%22) in our Project Board for the functionality we'd plan to develop further in our application. Here are the three most interesting additions we'd like to implement:

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
Apache License 2.0

