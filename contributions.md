# SOPRA Group 3 - Contributions

## **Week 1**

<p>

| Name        | Issues | Description                                                                      | Done |
|-------------|--------|----------------------------------------------------------------------------------|------|
| Lany        | #32    | Create new class user with username and password                                 |  X   |
|             | #30    | Check for unique username                                                        |  X   |
|             | #29    | Implement RegEx to check valid username                                          |  X   |
|             | #144   | Create new registration method                                                   |  X   |
| Orestis     | #106   | Create new fields for allergies, favorite cuisine and special diet in user class |  X   |
|             | #124   | Make sure there's only one host per group                                        |  X   |
|             | #125   | Make a function to assign name to group                                          |  X   |
|             | #127   | Create method checking if group name already exists                              |  X   |
|             | #126   | Create POST /groups for the creation of a new group                              |  X   |
|             | #123   | Assign user as host                                                              |  X   |
| Kalliopi    | #4     | Create Login page                                                                |  X   |
|             | #6     | Create path to be directed to Landing page after login                           |  X   |
|             | #7     | Create API call with body for backend communication                              |  X   |
|             | #8     | Create path to be redirected to login page in case of error                      |  X   |
| Ann-Kathrin | #36    | Create LogIn Method                                                              |  X   |
|             | #40    | Change Status                                                                    |  X   |
|             | #39    | Check for valid password                                                         |  X   |
|             | #37    | Check if user exists                                                             |  X   |
| Chiara      | #1     | Create Register page                                                             |  X   |
|             | #4     | Create Login page                                                                |  X   |
|             | #2     | Write error messages                                                             |  X   |
|             | #3     | Create path to direct to personal profile                                        |  X   |

</p>

## **Week 2**

<p>

| Name        | Issues | Description                                                                      | Done |
|-------------|--------|----------------------------------------------------------------------------------|------|
| Lany        | #115   | Create put call /groups/{groupId}/invitations/reject                             |  X   |
|             | #117   | Create put call /groups/{groupId}/invitations/accept                             |  X   |
|             | #118   | Check if user is authorized to accept/reject invitation else return error        |  X   |
|             | #119   | Check if user exists when accepting/rejecting the invitation else return error   |  X   |
|             | #157   | create GET /users/{userId}/invitations request                                   |  X   |
| Orestis     | #47    | Create Logout Method which sets User to offline                                  |  X   |
|             | #88    | Create method for updating the user profile                                      |  X   |
|             | #87    | Create method for returning the user profile                                     |  X   |
| Kalliopi    | #80    | Create navigation bar                                                            |  X   |
|             | #52    | Create a landing page with sections for displaying the users and active groups.  |  X   |
|             | #9     | Display user as logged in                                                        |  X   |
|             | #11    | Set user to offline and delete localStorage                                      |      |
|             | #10    | Make Logout Button accessible                                                    |  X   |
|             | #79    | Implement method to sort the users based on their online/offline status          |  X   |
|             | #68    | Create the Form Group button in the Landing Page                                 |  X   |
|             | #56    | Νavigation to the respective user profile page when a username is clicked        |      |
|             | #53    | API calls to retrieve registered users and active groups from the backend        |      |
|             | #54    | Display all users with online users at the beginning of the list                 |      |
|             | #55    | Display all active planning groups with their group name, host and guests        |      |
| Ann-Kathrin | #101/102| Create method to retrieve active groups / active users                          |  X   |
|             | #135   | Create function to check that one user is only in one group                      |  X   |
|             | #159   | create GET calls to retrieve group information                                   |  X   |
|             | #134   | Create function to check if group has at least one guest                         |  X   |
|             | #131   | Create a method that checks that person is not already invited to another group  |  X   |
| Chiara      | #45    | Create profile page with fields for allergies, favourite cuisine and special diet|  X   |
|             | #48    | Create edit button to allow user to edit fields of his/her own profile           |  X   |
|             | #50    | Create cancel button to cancel the edits                                         |  X   |
|             | #70    | Redirect user to Group Creation page when pressing on Form Group button          |  X   |

</p>

## **Week 3**

<p>

| Name        | Issues | Description                                                                      | Done |
|-------------|--------|----------------------------------------------------------------------------------|------|
| Lany        | #84    | Create GET /groups/{id} request                                                  |  X   |
|             | #187   | Create GET /groups/{id}/members request                                          |  X   |
| Orestis     |   #57  | Function to randomly select one recipe suggestion from the API                   |  X   |
|             |   #88  | Reopened and fixed the updating attributes and the tests                         |  X   |
| Kalliopi    |   #42  | Create host view of group forming page                                           |  X   |
|             |   #43  | Create guest view of group forming page                                          |  X   |
|             |   #57  | Create "Ingredient Page"                                                         |  X   |
|             |   #63  | Create a button to submit the final contributions and transition to next page    |  X   |
|             |   #58  | Create ingredient input field and a button for adding an ingredient to the meal  |  X   |
|             |   #59  | Create a button to remove an ingredient from the meal                            |  X   |
| Ann-Kathrin |  #106  |  Update fields for allergies/cuisine to List/Set and adjust all tests            |  X   |
|             | #132   | Create POST /groups/{groupId}/invitations                                        |  X   |
|             | #198   | make GET /groups/{groupId}/ingredients call                                      |  X   |
|             | #197   | make PUT /user/{userId}/ingredients call                                         |  X   |
|             | #104/#58| Create a new Ingredient class for storing the ingredients contributed by the user|  X   |
| Chiara      | #69    | Create Group Creation page                                                       |  X   |
|             | #16    | Create a button to enable the user to return to the Landing Page                 |  X   |
|             | #76    | Redirect host to landing page when pressing Delete Group button                  |  X   |
|             | #51    | Create path to be directed to the profile page                                   |  X   |
|             | #72    | Add for each user a button to invite                                             |  X   |
|             | #71    | Create section for online users                                                  |  X   |
|             | #75    | Create buttons for both creating and deleting group                              |  X   |
|             | #74    | Move guests into the guests section                                              |  X   |
|             | #73    | Create a guests section                                                          |  X   |
|             | #51    | Create path to be redirected to user profile                                     |  X   |
|             | #46    | Create API calls with body for backend communication                             |  X   |
|             | #47    | Display username, allergies, favorite cuisine, and special diet on user profile  |  X   |
|             | #49    | Create save button to save the edited fields                                     |  X   |

<p>

## **Week 4**

<p>

| Name        | Issues | Description                                                                      | Done |
|-------------|--------|----------------------------------------------------------------------------------|------|
| Lany        | #88 & #126 | Reopened and fixed                                                           | X     |
|             | #64    | Create an endpoint to retrieve and return the list of ingredients and guest contributions |  X   |
|             | #65    | Create an endpoint to store and update the ingredient ratings for each guest     | X    |
| Orestis     | #69    | Create the PUT /groups/{id}/leave request                                        | X    |
|             | #114   | Create the DELETE /groups/{id} request                                           | X    |
|             | #168   | implement PUT /groups/{groupId}/requests/reject call                             | X    |
|             | #167   |implement POST /groups/{groupId}/requests call                                    | X    |
|             | #169   | implement PUT /groups/{groupId}/requests/accept call                             | X    |
| Kalliopi    | #18    | Implement a function to fetch the recipe suggestion the backend API              |      |
|             | #17    | Display the randomly selected recipe suggestion on the screen                    |  X   |
|             | #55    | Display all active planning groups with their group name, host and guests        |  X   |
|             | #67    | Redirect user to landing page if invitation is rejected                          |  X   |
|             | #65    | Add red notification signal when user gets a new notification                    |  X   |
|             | #66    | Path to be redirected to Group Forming Page - Group Overview if user accepts invitation |  X   |
| Ann-Kathrin | #105   |  Implement a method for adding the ingredient contributions                      |  X   |
|             | #210   |  Retrieve all ingredients from external API to avoid spelling mistakes           |  X   |
|             | #54    |  Take input from REST API per user for all ingredients and transform it ...      |  X   |
| Chiara      | #94    | Edit change button to edit also username/password                                |  X   |
|             | #100   | Imprement button voting types in Group Creation page with default for majority   |  X   |
|             | #112   | Create dropdown for diet preference                                              |  X   |

</p>

## **Week 5** 

<p>

| Name        | Issues | Description                                                                      | Done |
|-------------|--------|----------------------------------------------------------------------------------|------|
| Lany        | #60    | Develop an endpoint to retrieve and return the list of final ingredients.        | X    |
|             | #61    | Develop an endpoint to remove the ingredient contributions of the leaving guest. | X    |
|             | #62    | Implement a function to check if the group is left with only the host, and disband the group if necessary. | X    |
| Orestis     | #171   | Create Method to include new guest to group                                      | X    |
|             | #218   | add GET groups/{groupid}/requests call                                           | X    |
|             | #77    | Create PUT /groups/{id}/state request                                            | X    |
|             | #170   | Create Method to check if user is allowed to request (group not entered certain stange in planning process). |  X   |
| Kalliopi    | #58    | Ingredient input-field + button for adding ingredients to the meal(Improvements) |   X  |
|             | #59    | Create a button to remove an ingredient from the meal (Improvements)             |   X  |
|             | #60    | Display all ingredients added to the meal in a separate section                  |   X  |
|             | #61    | Ensure that at least one ingredient is added before allowing the user to submit  |   X  |
|             | #62    | Implement API call to submit the ingredient contributions                        |   X  |
|             | #63    | Button to submit final contributions and transition to next page  (Improvements) |  X   |
|             |   #78  | Redirect host toGroup Forming Page - Host when clicking on continue              |   X  |
| Ann-Kathrin |#172/#55|    Implement default majority logic                                              |    X |
|             |    #59 |    Develop endpoint to store and retrieve guest and ingredient contributions     |   X  |
|             |   #57  | select one recipe suggestion from the API based on the given ingredients         |    X |
| Chiara      |   #99  | Add description text in group creation for describing the default logic          |   X  |
|             |   #101 | Send the voting type to backend when creating the group                          |   X  |
|             |   #13  | Make yes/no/indifferent buttons available                                        |   X  |
|             |   #126 | Create a voting page that displays all typed in ingredients to be voted          |   X  |
|             |   #125 | Display an overview of all typed-in ingredients so that they can rate them       |   X  |
|             |   #78  | Redirect host toGroup Forming Page - Host when clicking on continue              |   X  |
|             |   #127 | Create a final ingredients page with the ingredients that have been selected after the voting  |   X  |

</p>

## **Week 6** 

<p>

| Name        | Issues | Description                                                                      | Done |
|-------------|--------|----------------------------------------------------------------------------------|------|
| Lany        | #234   | independent: create GET /users/{userId}/groups                                   | X    |
|             | #235   | independent: create GET /groups/{groupId}/members/allergies                      | X    |
|             | #69 & #114 | Edited for added functionality                                               | X    |
|             | #67 & #173 | Implemented previously, connected to corresponding commits.                  | X    |
| Orestis     | #210   | Updated the logic of retrieval of all ingredients from external API to avoid spelling mistakes. | X    |
|             | #47    | Re-opened Create Logout Method which sets User to offline to add some extra functionality to it. | X    |
|             | #77    | Implement a new Ready/Not Ready logic to transition from some states to other states. |      |    
| Kalliopi    |        |                                                                                  |      |
|             |        |                                                                                  |      |
| Ann-Kathrin |  #248  |  implement check that frontend send really just 1,-1,0 for ingredient ratings    |    X |
|             |   #257 |   take allergies of all group members into account                               |      |
|             |   #253 | make sure "No" ingredients are not used in the final recipe                      |      |
|             |   #255 |    in case of an empty final ingredients list: provide RandomRecipe              |      |
|             |        |                                                                                  |      |
| Chiara      |  #15   |  Send list of all ratings of all ingredients to backend through REST API         |  X   |
|             |  #25   |  Implement a function to enable guests and host to vote all ingredients          |  X   |
|             |  #28   |  Implement a submit button to save the ratings                                   |  X   |
|             |  #26   |  Implement a decision logic for the rating system (default: majority)            |  X   |
|             |  #14   |  Make submit button clickable only if all ingredients have been rated            |  X   |
|             |  #27   |  Display a message indicating that all ingredients must be rated before submit   |  X   |
|             |  #19   |  Display the list of final ingredients in IngredientsFinal view (after voting)   |  X   |
|             |  #133  |  Display the allregies of the group to help guests in the selection of ingredients to add |  X   |


</p>

