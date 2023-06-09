package ch.uzh.ifi.hase.soprafs23.controller;

import ch.uzh.ifi.hase.soprafs23.constant.GroupState;
import ch.uzh.ifi.hase.soprafs23.entity.Group;
import ch.uzh.ifi.hase.soprafs23.entity.Invitation;
import ch.uzh.ifi.hase.soprafs23.entity.User;
import ch.uzh.ifi.hase.soprafs23.rest.dto.GroupGetDTO;
import ch.uzh.ifi.hase.soprafs23.rest.dto.UserGetDTO;
import ch.uzh.ifi.hase.soprafs23.rest.dto.UserPostDTO;
import ch.uzh.ifi.hase.soprafs23.rest.dto.UserPutDTO;
import ch.uzh.ifi.hase.soprafs23.rest.dto.IngredientPutDTO;
import ch.uzh.ifi.hase.soprafs23.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs23.service.GroupService;
import ch.uzh.ifi.hase.soprafs23.service.InvitationService;
import ch.uzh.ifi.hase.soprafs23.service.JoinRequestService;
import ch.uzh.ifi.hase.soprafs23.service.UserService;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;

import java.util.ArrayList;
import java.util.List;

/**
 * User Controller
 * This class is responsible for handling all REST request that are related to
 * the user.
 * The controller will receive the request and delegate the execution to the
 * UserService and finally return the result.
 */
@RestController
public class UserController {

    private final UserService userService;

    private final GroupService groupService;

    private final InvitationService invitationService;

    private final JoinRequestService joinRequestService;

    public UserController(UserService userService, GroupService groupService, InvitationService invitationService,@Lazy JoinRequestService joinRequestService) {
        this.userService = userService;
        this.groupService = groupService;
        this.invitationService = invitationService;
        this.joinRequestService = joinRequestService;
    }

    @GetMapping("/users")
    @ResponseStatus(HttpStatus.OK) // 200
    @ResponseBody
    public List<UserGetDTO> getAllUsers(HttpServletRequest request) {
        // check validity of token
        String token = request.getHeader("X-Token");
        if(userService.getUseridByToken(token).equals(0L)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, String.format("You are not authorized."));
        }
      
        // fetch all users in the internal representation
        List<User> users = userService.getUsers();
        List<UserGetDTO> userGetDTOs = new ArrayList<>();
      
        // convert each user to the API representation
        for (User user : users) {
            userGetDTOs.add(DTOMapper.INSTANCE.convertEntityToUserGetDTO(user));
        }
        return userGetDTOs;
    }

    @PostMapping("/users")
    @ResponseStatus(HttpStatus.CREATED) //201
    @ResponseBody
    public ResponseEntity<UserGetDTO> createUser(@RequestBody UserPostDTO userPostDTO) {
        // convert API user to internal representation
        User userInput = DTOMapper.INSTANCE.convertUserPostDTOtoEntity(userPostDTO);
      
        // create user
        User createdUser = userService.createUser(userInput);
      
        // convert internal representation of user back to API
        UserGetDTO userGetDTO = DTOMapper.INSTANCE.convertEntityToUserGetDTO(createdUser);
      
        // create HttpHeaders object, add token in response header and make it accessible to the client
        HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.add("X-Token", createdUser.getToken());
        List<String> customHeaders = new ArrayList<String>();
        customHeaders.add("X-Token");
        headers.setAccessControlExposeHeaders(customHeaders);
      
        return ResponseEntity.status(HttpStatus.CREATED).headers(headers).body(userGetDTO);
    }

    @PostMapping("/users/{username}/login")
    @ResponseStatus(HttpStatus.OK) //OK is 200
    @ResponseBody
    public ResponseEntity<UserGetDTO> loginUser(@RequestBody UserPostDTO userPostDTO, @PathVariable String username){
        //get user by username
        User user = userService.getUserByUsername(username);

        //get password which belongs to given username and check if provided password is same
        User userInput = DTOMapper.INSTANCE.convertUserPostDTOtoEntity(userPostDTO); //convert info to internal representation
        userService.correctPassword(username, userInput.getPassword());

        //set online
        userService.login(user);

        // convert internal representation of user back to API
        UserGetDTO userGetDTO = DTOMapper.INSTANCE.convertEntityToUserGetDTO(user);
      
        // create HttpHeaders object, add token in response header and make it accessible to the client
        HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.add("X-Token", user.getToken());
        List<String> customHeaders = new ArrayList<String>();
        customHeaders.add("X-Token");
        headers.setAccessControlExposeHeaders(customHeaders);
      
        return ResponseEntity.status(HttpStatus.OK).headers(headers).body(userGetDTO);
    }

    @PostMapping("/users/{userId}/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT) // NO CONTENT IS 204
    public void logoutUser(HttpServletRequest request, @PathVariable Long userId) {
        // 404
        userService.getUserById(userId);

        // 401
        Long tokenId = userService.getUseridByToken(request.getHeader("X-Token"));
        if(!tokenId.equals(userId)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "You are not authorized.");
        }

        // Check if the user is in a group that is not in the GROUPFORMING state
        Group currentGroup = groupService.getGroupByUserId(userId);
        if(currentGroup != null && currentGroup.getGroupState() != GroupState.GROUPFORMING) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot logout while your group is beyond the forming stage.");
        }

        // delete all join requests of the user
        joinRequestService.deleteAllJoinRequests(userId);

        // Check if the user is a host of a group
        Group hostedGroup = null;
        try {
            hostedGroup = groupService.getGroupByHostId(userId);
            if (hostedGroup != null) {
                // delete all the invitations that were sent out for this group
                invitationService.deleteInvitationsByGroupId(hostedGroup.getId());

                // delete all the join requests to join this group
                joinRequestService.deleteJoinRequestsByGroupId(hostedGroup.getId());

                // delete the group
                groupService.deleteGroup(hostedGroup.getId());
            }
        } catch (ResponseStatusException ex) {
            // If the user is not a host, they might still be a member of a group
            if(userService.isUserInGroup(userId)) {
                userService.leaveGroup(userId);
            }
        }

        userService.logout(userId);
    }

    @PutMapping("/users/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT) //NO CONTENT IS 204
    public void updateUser(@PathVariable Long userId,
                           @RequestBody UserPutDTO userPutDTO,
                           HttpServletRequest request)
    {
        // 404
        userService.getUserById(userId);

        // 401
        Long tokenId = userService.getUseridByToken(request.getHeader("X-Token"));
        if(!tokenId.equals(userId)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, String.format("You are not authorized to make changes to this profile."));
        }

        userService.updateUser(userId, userPutDTO);
    }

    @GetMapping("/users/{userId}")
    @ResponseStatus(HttpStatus.OK) // OK IS 200
    @ResponseBody
    public UserGetDTO getUserById(@PathVariable Long userId, HttpServletRequest request) {
        // check validity of token
        String token = request.getHeader("X-Token");
        if(userService.getUseridByToken(token).equals(0L)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, String.format("You are not authorized."));
        }

        // fetch the user in the internal representation
        User user = userService.getUserById(userId);
        UserGetDTO userGetDTO = DTOMapper.INSTANCE.convertEntityToUserGetDTO(user);

        return userGetDTO;
    }

    @GetMapping("/users/{userId}/groups")
    @ResponseStatus(HttpStatus.OK) // OK IS 200
    @ResponseBody
    public Long getGroupIdOfUser(@PathVariable Long userId, HttpServletRequest request) {
        // 404 - user not found
        User user = userService.getUserById(userId);

        // check validity of token
        Long tokenId = userService.getUseridByToken(request.getHeader("X-Token"));
        if(!tokenId.equals(userId)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, String.format("You are not authorized."));
        }

        // 204 - user is not in a group
        if(user.getGroupId() == null) {
            throw new ResponseStatusException(HttpStatus.NO_CONTENT);
        }

        return user.getGroupId();
    }

    @GetMapping("/users/{userId}/invitations")
    @ResponseStatus(HttpStatus.OK) // 200
    @ResponseBody
    public List<GroupGetDTO> getOpenInvitationsByGuest(@PathVariable Long userId, HttpServletRequest request) {
        // 404 - user not found
        userService.getUserById(userId);
      
        // 401 - not authorized
        Long tokenId = userService.getUseridByToken(request.getHeader("X-Token"));
        if(!tokenId.equals(userId)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, String.format("You are not authorized."));
        }
      
        // retrieve the invitations
        List<Invitation> invitations = invitationService.getInvitationsByGuestId(userId);
      
        // 204 - no open invitations
        if(invitations.size() == 0) {
            throw new ResponseStatusException(HttpStatus.NO_CONTENT);
        }
      
        // convert to GroupGetDTOs and return them
        List<GroupGetDTO> groupGetDTOs = new ArrayList<>();
        for(Invitation invitation : invitations) {
            GroupGetDTO groupGetDTO = DTOMapper.INSTANCE.convertEntityToGroupGetDTO(groupService.getGroupById(invitation.getGroupId()));
            groupGetDTOs.add(groupGetDTO);
        }
      
        return groupGetDTOs;
    }

    @PutMapping("/users/{userId}/ingredients")
    @ResponseStatus(HttpStatus.NO_CONTENT) // 204
    public void updateUserIngredients(@PathVariable Long userId,
                                      @RequestBody List<IngredientPutDTO> ingredientsPutDTOSet, // changing List to Set here
                                      @RequestHeader(name = "X-Token") String xToken) {
        // 404 - group not found
        User user = userService.getUserById(userId);

        // 401 - not authorized
        Long tokenId = userService.getUseridByToken(xToken);
        if (!tokenId.equals(userId)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, String.format("You are not authorized to update ingredients associated with this user.")); //401 - unauthorized
        }

        if (user.getGroupId() == null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "You must be in a group to enter ingredients.");
        }

        // 409 - groupState is not INGREDIENTENTERING
        Group group = groupService.getGroupById(user.getGroupId());
        GroupState groupState = group.getGroupState();
        if(!groupState.equals(GroupState.INGREDIENTENTERING)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "The groupState is not INGREDIENTENTERING"); // 409
        }

        userService.addUserIngredients(userId, ingredientsPutDTOSet); // passing Set here

        // Check if all members of group have entered ingredients, change state if so
        List<Long> memberIds = groupService.getAllMemberIdsOfGroup(group);
        boolean changeState = true;
        for (Long memberId : memberIds) {
            if(!userService.userHasIngredients(memberId)) {
                changeState = false;
                break;
            }
        }
        if (changeState) {
            groupService.changeGroupState(group.getId(), GroupState.INGREDIENTVOTING); // if all members have entered ingredients change state and continue
        }
    }

    @PutMapping("/users/{userId}/{groupId}/ready")
    @ResponseStatus(HttpStatus.NO_CONTENT) // 204
    public ResponseEntity<Void> setReady(@PathVariable Long userId, @PathVariable Long groupId, HttpServletRequest request) {

        //404 - user not found
        userService.getUserById(userId);

        //404 - group not found
        Group group = groupService.getGroupById(groupId);

        // 401 - not authorized if not the same user
        Long tokenId = userService.getUseridByToken(request.getHeader("X-Token"));
        if(!tokenId.equals(userId)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "You are not authorized to update this user's status.");
        }

        // 401 - not authorized if not a member of the group
        List<Long> memberIds = groupService.getAllMemberIdsOfGroup(group);
        if(!memberIds.contains(tokenId)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, String.format("You are not a member of the group with id %d.", groupId));
        }

        // Checking the group state before setting ready state
        GroupState groupState = group.getGroupState();
        if(!(groupState.equals(GroupState.GROUPFORMING) || groupState.equals(GroupState.FINAL) || groupState.equals(GroupState.RECIPE))) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "The groupState must be either GROUPFORMING, FINAL or RECIPE to set ready state"); // 409
        }

        // Set user's isReady to true and save
        userService.setUserReady(userId);

        // Check if all users in the group are ready
        boolean allReady = userService.areAllUsersReady(memberIds);

        if (allReady) {
            // If all users are ready, change the state and set isReady to false for all
            if (groupState == GroupState.GROUPFORMING) {
                groupService.changeGroupState(groupId, GroupState.INGREDIENTENTERING);
            } else if (groupState == GroupState.FINAL) {
                groupService.changeGroupState(groupId, GroupState.RECIPE);
            } else if (groupState == GroupState.RECIPE) {
                userService.deleteGroupAndSetAllUsersNotReady(groupId, memberIds);
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
            userService.setAllUsersNotReady(memberIds);
        }
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
