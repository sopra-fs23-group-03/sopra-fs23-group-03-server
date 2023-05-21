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

## Illustrations
In your client repository, briefly describe and illustrate the main user flow(s) of your interface. How does it work (without going into too much detail)? Feel free to include a few screenshots of your application.

## Roadmap
The top 2-3 features that new developers who want to contribute to your project could add.

## Authors and acknowledgment

## License
Say how your project is licensed
License guide: https://choosealicense.com/

