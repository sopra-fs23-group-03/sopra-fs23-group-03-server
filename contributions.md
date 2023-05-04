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
|             | #51    | Create path to be redirected to user profile                                     |      |
|             | #70    | Redirect user to Group Creation page when pressing on Form Group button          |  X   |

</p>

## **Week 3** 

<p>

| Name        | Issues | Description                                                                      | Done |
|-------------|--------|----------------------------------------------------------------------------------|------|
| Lany        | #84    | Create GET /groups/{id} request                                                  |   X  |
|             | #187   | Create GET /groups/{id}/members request                                          |   X  |
| Orestis     |   #57  | Function to randomly select one recipe suggestion from the API                   |      |
|             |   #88  | Reopened and fixed the updating attributes and the tests                         |  X   |
| Kalliopi    |   #43  | Create host and guest view of group forming page                                 |      |
|             |   #57  | Create "Ingredient Page"                                                         |      |      
|             |   #63  | Create a button to submit the final contributions and transition to next page    |      |   
|             |   #58  | Create ingredient input field and a button for adding an ingredient to the meal  |      |  
|             |   #59  | Create a button to remove an ingredient from the meal                            |      |      
|             |   #60  | Display all ingredients added to the meal in a separate section                  |      |   
|             |   #60  | Display all ingredients added to the meal in a separate section                  |      |     
|             |   #61  | Ensure that at least one ingredient is added before allowing the user to submit  |      |    
| Ann-Kathrin |  #106  |  Update fields for allergies/cuisine to List/Set and adjust all tests            |  X   |
|             | #132   | Create POST /groups/{groupId}/invitations                                        |   X  |
|             | #198   | make GET /groups/{groupId}/ingredients call                                      |    X |
|             | #197   | make PUT /user/{userId}/ingredients call                                         |    X |
|             | #104   | Create a new Ingredient class for storing the ingredients contributed by the user|   X  |
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
| Orestis     | #69    | Create the PUT /groups/{id}/leave request                                        |      |
|             | #114   | Create the DELETE /groups/{id} request                                           |      |
|             | #168   | implement PUT /groups/{groupId}/requests/reject call                             |      |
|             | #167   |implement POST /groups/{groupId}/requests call                                    |      |
|             | #169   | implement PUT /groups/{groupId}/requests/accept call                             |      |    
| Kalliopi    |        |                                                                                  |      |
|             | #18    | Implement a function to fetch the recipe suggestion the backend API              |      |
|             | #17    | Display the randomly selected recipe suggestion on the screen                    |      |
| Ann-Kathrin | #105   |  Implement a method for adding the ingredient contributions                      |  X   |
|             | #210   |  Retrieve all ingredients from external API to avoid spelling mistakes           |  X   |
|             | #54    |  Take input from REST API per user for all ingredients and transform it ...      |      |   
|             | #55    |    Implement majority logic                                                      |      | 
| Chiara      | #94    | Edit change button to edit also username/password                                |  X   |
|             | #100   | Imprement button voting types in Group Creation page with default for majority   |  X   |
|             | #112   | Create dropdown for diet preference                                              |  X   |
|             | #78    | Redirect host to Group Forming Page - Host View when clicking on Continue        |      |
|             | #77    | Create Group Forming Page - Host View                                            |      |
    

</p>
