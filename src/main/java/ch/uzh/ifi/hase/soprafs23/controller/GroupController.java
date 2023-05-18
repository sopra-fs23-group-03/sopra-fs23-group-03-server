package ch.uzh.ifi.hase.soprafs23.controller;

import ch.uzh.ifi.hase.soprafs23.constant.GroupState;
import ch.uzh.ifi.hase.soprafs23.constant.UserVotingStatus;
import ch.uzh.ifi.hase.soprafs23.constant.VotingType;
import ch.uzh.ifi.hase.soprafs23.entity.Group;
import ch.uzh.ifi.hase.soprafs23.entity.Ingredient;
import ch.uzh.ifi.hase.soprafs23.entity.Invitation;
import ch.uzh.ifi.hase.soprafs23.entity.JoinRequest;
import ch.uzh.ifi.hase.soprafs23.entity.User;
import ch.uzh.ifi.hase.soprafs23.rest.dto.*;
import ch.uzh.ifi.hase.soprafs23.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs23.service.GroupService;
import ch.uzh.ifi.hase.soprafs23.service.InvitationService;
import ch.uzh.ifi.hase.soprafs23.service.JoinRequestService;
import ch.uzh.ifi.hase.soprafs23.service.UserService;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
public class GroupController {
    private final GroupService groupService;

    private final UserService userService;

    private final InvitationService invitationService;
    private final JoinRequestService joinRequestService;

    GroupController(GroupService groupService, UserService userService, InvitationService invitationService, JoinRequestService joinRequestService) {
        this.groupService = groupService;
        this.userService = userService;
        this.invitationService = invitationService;
        this.joinRequestService = joinRequestService;
    }

    @GetMapping("/groups")
    @ResponseStatus(HttpStatus.OK) // 200
    @ResponseBody
    public List<GroupGetDTO> getAllGroups(HttpServletRequest request) {
        // check validity of token
        String token = request.getHeader("X-Token");
        if(userService.getUseridByToken(token).equals(0L)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, String.format("You are not authorized.")); // 401 - not authorized
        }

        // fetch all groups in the internal representation
        List<Group> groups = groupService.getGroups();
        List<GroupGetDTO> groupGetDTOs = new ArrayList<>();

        // convert each group to the API representation
        for (Group group : groups) {
            groupGetDTOs.add(DTOMapper.INSTANCE.convertEntityToGroupGetDTO(group));
        }
        return groupGetDTOs;
    }

    // TODO: change VotingType check when POINTDISTRIBUTION is implemented
    @PostMapping("/groups")
    @ResponseStatus(HttpStatus.CREATED) // 201
    @ResponseBody
    public GroupGetDTO createGroup(@RequestBody GroupPostDTO groupPostDTO, HttpServletRequest request) {
        // 404 - host not found
        User host = userService.getUserById(groupPostDTO.getHostId());

        // check validity of token
        Long tokenId = userService.getUseridByToken(request.getHeader("X-Token"));
        if(!tokenId.equals(groupPostDTO.getHostId())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, String.format("You are not authorized.")); // 401 - not authorized
        }

        if(groupPostDTO.getVotingType() == null || !groupPostDTO.getVotingType().equals("MAJORITYVOTE")) {// at the moment only MAJORITYVOTE is accepted, later a && !groupPostDTO.getVotingType().equals("POINTDISTRIBUTION") will be needed
            groupPostDTO.setVotingType("MAJORITYVOTE"); // standard voting type
        }

        if(host.getGroupId() != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, String.format("User with id %s is already in the group with id %d.", host.getId(), host.getGroupId()));
        }

        // convert API user to internal representation
        Group groupInput = DTOMapper.INSTANCE.convertGroupPostDTOtoEntity(groupPostDTO);
        groupInput.setHostName(host.getUsername());

        // create group
        Group createdGroup = groupService.createGroup(groupInput);
        userService.joinGroup(host.getId(), createdGroup.getId());

        // convert internal representation of user back to API
        GroupGetDTO groupGetDTO = DTOMapper.INSTANCE.convertEntityToGroupGetDTO(createdGroup);

        return groupGetDTO;
    }

    @PostMapping("/groups/{groupId}/invitations")
    @ResponseStatus(HttpStatus.CREATED) // 201
    public void sendInvitation(@PathVariable Long groupId, @RequestBody List<Long> ListGuestIds, HttpServletRequest request) {
        // only HOST can send invitation: check host token
        Group currentGroup = groupService.getGroupById(groupId);  // 404 - group not found
        Long currentGroupHostId = currentGroup.getHostId(); // get host of the group in db

        Long tokenId = userService.getUseridByToken(request.getHeader("X-Token"));  // check if its the same one sending the invites
        if(!tokenId.equals(currentGroupHostId)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, String.format("You are not authorized.")); // 401 - not authorized
        }

        // 409 - groupState is not GROUPFORMING
        GroupState groupState = currentGroup.getGroupState();
        if(!groupState.equals(GroupState.GROUPFORMING)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "The groupState is not GROUPFORMING"); // 409
        }

        // Loop through each guest id and create an invitation for them:
        // idea--> create InvitationPostDTO object for each guest id, set guest id in it, then use DTOMapper to convert it to an Invit entity
        for (Long guestId : ListGuestIds) {
            // check if users to be invited exist
            userService.getUserById(guestId); // 404 - user not found

            // Convert guest id to invitation entity
            InvitationPostDTO invitationPostDTO = new InvitationPostDTO();
            invitationPostDTO.setGuestId(guestId);
            Invitation invitation = DTOMapper.INSTANCE.convertInvitationPostDTOtoEntity(invitationPostDTO);
            invitation.setGroupId(groupId);

            try {
                // Create invitation using invitation service
                invitationService.createInvitation(groupId, guestId);
            } catch (ResponseStatusException e) {
                if (e.getStatus() == HttpStatus.CONFLICT) {
                    String errorMessage = String.format("An invitation has already been sent to guest with id %d.", guestId); // 409 - conflict
                    throw new ResponseStatusException(HttpStatus.CONFLICT, errorMessage, e);
                } else {
                    throw e;
                }
            }
        }
    }

    @PutMapping("/groups/{groupId}/invitations/reject")
    @ResponseStatus(HttpStatus.NO_CONTENT) // 204
    public void rejectInvitation(@PathVariable Long groupId,
                                 @RequestBody InvitationPutDTO invitationPutDTO,
                                 HttpServletRequest request) {
        Long guestId = invitationPutDTO.getGuestId();

        // 404 - group, guest, and/or invitation not found
        groupService.getGroupById(groupId);
        userService.getUserById(guestId);
        Invitation invitation = invitationService.getInvitationByGroupIdAndGuestId(groupId, guestId);
        
        // 401 - not authorized
        Long tokenId = userService.getUseridByToken(request.getHeader("X-Token"));
        if(!tokenId.equals(guestId)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, String.format("You are not authorized to reject this invitation."));
        }

        // delete invitation
        invitationService.deleteInvitation(invitation);
    }

    @PutMapping("/groups/{groupId}/invitations/accept")
    @ResponseStatus(HttpStatus.NO_CONTENT) // 204
    public void acceptInvitation(@PathVariable Long groupId,
                                 @RequestBody InvitationPutDTO invitationPutDTO,
                                 HttpServletRequest request) {
        Long guestId = invitationPutDTO.getGuestId();

        // 404 - group, guest, and/or invitation not found
        groupService.getGroupById(groupId);
        userService.getUserById(invitationPutDTO.getGuestId());
        Invitation invitation = invitationService.getInvitationByGroupIdAndGuestId(groupId, guestId);

        // 401 - not authorized
        Long tokenId = userService.getUseridByToken(request.getHeader("X-Token"));
        if(!tokenId.equals(guestId)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, String.format("You are not authorized to accept this invitation."));
        }

        // create link between guest and group
        groupService.addGuestToGroupMembers(guestId, groupId);
        userService.joinGroup(guestId, groupId); 

        // delete invitation
        invitationService.deleteInvitation(invitation);
        joinRequestService.deleteAllJoinRequests(guestId);
    }

    @GetMapping("/groups/{groupId}")
    @ResponseStatus(HttpStatus.OK) // 200
    @ResponseBody
    public GroupGetDTO getGroupById(@PathVariable Long groupId, HttpServletRequest request) {
        // 404 - group not found
        Group group = groupService.getGroupById(groupId);

        // 401 - not authorized
        Long tokenId = userService.getUseridByToken(request.getHeader("X-Token"));
        if(tokenId.equals(0L)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, String.format("You are not authorized."));
        }

        return DTOMapper.INSTANCE.convertEntityToGroupGetDTO(group);
    }

    @GetMapping("/groups/{groupId}/members")
    @ResponseStatus(HttpStatus.OK) // 200
    @ResponseBody
    public List<UserGetDTO> getGroupMembersById(@PathVariable Long groupId, HttpServletRequest request) {
        // 404 - group not found
        Group group = groupService.getGroupById(groupId);

        // 401 - not authorized
        Long tokenId = userService.getUseridByToken(request.getHeader("X-Token"));
        if(tokenId.equals(0L)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, String.format("You are not authorized."));
        }

        // get the members of the group
        List<Long> memberIds = groupService.getAllMemberIdsOfGroup(group);
        List<UserGetDTO> userGetDTOs = new ArrayList<>();

        // convert each user to the API representation
        for (Long userId : memberIds) {
            User user = userService.getUserById(userId);
            userGetDTOs.add(DTOMapper.INSTANCE.convertEntityToUserGetDTO(user));
        }

        return userGetDTOs;
    }

    @GetMapping("/groups/{groupId}/members/allergies")
    @ResponseStatus(HttpStatus.OK) // 200
    @ResponseBody
    public Set<String> getGroupMemberAllergiesById(@PathVariable Long groupId, HttpServletRequest request) {
        // 404 - group not found
        Group group = groupService.getGroupById(groupId);

        // 401 - not authorized if not a member of the group
        Long tokenId = userService.getUseridByToken(request.getHeader("X-Token"));
        List<Long> memberIds = groupService.getAllMemberIdsOfGroup(group);
        if(!memberIds.contains(tokenId)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, String.format("You are not a member of the group with id %d.", groupId));
        }

        // get the allergies of the members of the group
        Set<String> allergies = groupService.getGroupMemberAllergies(group);

        // 204 - none of the members have allergies 
        if (allergies.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NO_CONTENT);
        }

        return allergies;
    }

    @GetMapping("/groups/{groupId}/guests")
    @ResponseStatus(HttpStatus.OK) // 200
    @ResponseBody
    public List<UserGetDTO> getGroupGuestsById(@PathVariable Long groupId, HttpServletRequest request) {
        // 404 - group not found
        Group group = groupService.getGroupById(groupId);

        // 401 - not authorized
        Long tokenId = userService.getUseridByToken(request.getHeader("X-Token"));
        if(tokenId.equals(0L)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, String.format("You are not authorized."));
        }

        // get the guests of the group
        List<Long> guestIds = groupService.getAllGuestIdsOfGroup(group);

        // 204 - no guests in group
        if(guestIds.size() == 0) {
            throw new ResponseStatusException(HttpStatus.NO_CONTENT);
        }

        // convert each user to the API representation
        List<UserGetDTO> userGetDTOs = new ArrayList<>();
        for (Long userId : guestIds) {
            User user = userService.getUserById(userId);
            userGetDTOs.add(DTOMapper.INSTANCE.convertEntityToUserGetDTO(user));
        }

        return userGetDTOs;
    }

    @GetMapping("/groups/{groupId}/ingredients")
    @ResponseStatus(HttpStatus.OK) // 200
    @ResponseBody
    public List<IngredientGetDTO> getIngredientsOfGroupById(@PathVariable Long groupId, HttpServletRequest request) {
        // 404 - group not found
        Group group = groupService.getGroupById(groupId);
        List<Long> memberIds = groupService.getAllMemberIdsOfGroup(group);

        // 401 - not authorized if not a member of the group
        Long tokenId = userService.getUseridByToken(request.getHeader("X-Token"));
        if(!memberIds.contains(tokenId)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, String.format("You are not a member of the group with id %d.", groupId));
        }

        // 409 - groupState is not INGREDIENTVOTING
        GroupState groupState = group.getGroupState();
        if(!groupState.equals(GroupState.INGREDIENTVOTING)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "The groupState is not INGREDIENTVOTING"); // 409
        }
        
        // retrieve all ingredients available from the members of the group
        Set<Ingredient> groupIngredients = group.getIngredients();

        // convert to API representation
        List<IngredientGetDTO> ingredientGetDTOs = new ArrayList<>();
        for(Ingredient ingredient : groupIngredients) {
            ingredientGetDTOs.add(DTOMapper.INSTANCE.convertEntityToIngredientGetDTO(ingredient));
        }

        return ingredientGetDTOs;
    }

    @GetMapping("/groups/{groupId}/ingredients/final")
    @ResponseStatus(HttpStatus.OK) // 200
    @ResponseBody
    public List<IngredientGetDTO> getFinalIngredientsOfGroupById(@PathVariable Long groupId, HttpServletRequest request) {
        // 404 - group not found
        Group group = groupService.getGroupById(groupId);
        List<Long> memberIds = groupService.getAllMemberIdsOfGroup(group);

        // 401 - not authorized if not a member of the group
        Long tokenId = userService.getUseridByToken(request.getHeader("X-Token"));
        if(!memberIds.contains(tokenId)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, String.format("You are not a member of the group with id %d.", groupId));
        }

        // 409 - groupState is not FINAL
        GroupState groupState = group.getGroupState();
        if(!groupState.equals(GroupState.FINAL)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "The groupState is not FINAL"); // 409
        }

        // retrieve all ingredients available from the members of the group
        Set<Ingredient> groupFinalIngredients = groupService.getFinalIngredients(group);

        // convert to API representation
        List<IngredientGetDTO> ingredientGetDTOs = new ArrayList<>();
        for(Ingredient ingredient : groupFinalIngredients) {
            ingredientGetDTOs.add(DTOMapper.INSTANCE.convertEntityToIngredientGetDTO(ingredient));
        }

        return ingredientGetDTOs;
    }

    @DeleteMapping("/groups/{groupId}")
    @ResponseStatus(HttpStatus.NO_CONTENT) // 204
    public void deleteGroup(@PathVariable Long groupId, HttpServletRequest request) {
        // Check if the group exists
        Group group = groupService.getGroupById(groupId); // 404 - group not found

        // 409 - groupState is not GROUPFORMING
        GroupState groupState = group.getGroupState();
        if(!groupState.equals(GroupState.GROUPFORMING)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "The groupState is not GROUPFORMING"); // 409
        }

        // Check the validity of the token
        Long tokenId = userService.getUseridByToken(request.getHeader("X-Token"));
        if (!tokenId.equals(group.getHostId())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Only the Host can delete the Group"); // 401 - not authorized
        }

        // Check if any member of the group is ready
        List<Long> memberIds = groupService.getAllMemberIdsOfGroup(group);
        boolean anyMemberReady = memberIds.stream()
                .map(id -> userService.getUserById(id))
                .anyMatch(User::isReady);

        if (anyMemberReady) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cannot delete the group while a member is ready"); // 409
        }

        // Delete the group
        groupService.deleteGroup(groupId);
    }


    @PutMapping("/groups/{groupId}/leave")
    @ResponseStatus(HttpStatus.NO_CONTENT) // 204
    public void leaveGroup(@PathVariable Long groupId, HttpServletRequest request) {
        // Check if the group exists
        Group group = groupService.getGroupById(groupId); // 404 - group not found

        // 409 - groupState is not GROUPFORMING
        GroupState groupState = group.getGroupState();
        if(!groupState.equals(GroupState.GROUPFORMING)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "The groupState is not GROUPFORMING"); // 409
        }

        // Check the validity of the token
        Long guestId = userService.getUseridByToken(request.getHeader("X-Token"));
        if (guestId.equals(0L)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authorized"); // 401 - not authorized
        }

        // Check if the user is a member of the group
        List<Long> memberIds = groupService.getAllMemberIdsOfGroup(group);
        if (!memberIds.contains(guestId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Guest was not in the group"); // 409 - conflict
        }

        // Check if the user is the host of the group
        if (group.getHostId().equals(guestId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "You are the host. If you want to leave, delete the group"); // 409 - conflict
        }

        // Remove the guest and their ingredients from the group
        userService.leaveGroup(guestId);

        // Check if host is now alone & group state was FINAL -> delete group
        memberIds = groupService.getAllMemberIdsOfGroup(group);
        if(groupState.equals(GroupState.FINAL) && memberIds.size() == 1) {
            groupService.deleteGroup(groupId);
        }
    }

    @PostMapping("/groups/{groupId}/requests")
    @ResponseStatus(HttpStatus.CREATED) // 201
    public void sendRequestToJoinGroup(@PathVariable Long groupId, @RequestBody JoinRequestPostDTO joinRequestPostDTO, HttpServletRequest request) {
        if (userService.isUserInGroup(joinRequestPostDTO.getGuestId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are already in a group");
        }

        Group group = groupService.getGroupById(groupId);
        userService.getUserById(joinRequestPostDTO.getGuestId());

        Long tokenId = userService.getUseridByToken(request.getHeader("X-Token"));
        if (!tokenId.equals(joinRequestPostDTO.getGuestId())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authorized");
        }

        // 409 - groupState is not GROUPFORMING
        GroupState groupState = group.getGroupState();
        if(!groupState.equals(GroupState.GROUPFORMING)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "The groupState is not GROUPFORMING"); // 409
        }

        joinRequestService.createJoinRequest(joinRequestPostDTO, groupId);
    }

    @PutMapping("/groups/{groupId}/requests/accept")
    @ResponseStatus(HttpStatus.NO_CONTENT) // 204
    public void acceptJoinRequest(@PathVariable Long groupId, @RequestBody JoinRequestPutDTO joinRequestPutDTO, HttpServletRequest request) {
        Group currentGroup = groupService.getGroupById(groupId);
        Long hostId = currentGroup.getHostId();

        userService.getUserById(joinRequestPutDTO.getGuestId());

        Long tokenId = userService.getUseridByToken(request.getHeader("X-Token"));
        if (!tokenId.equals(hostId)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "You are not authorized to accept this join request.");
        }

        joinRequestService.acceptJoinRequest(groupId, joinRequestPutDTO.getGuestId());

        // Delete other join requests of the user
        joinRequestService.deleteOtherJoinRequests(joinRequestPutDTO.getGuestId(), groupId);
    }

    @PutMapping("/groups/{groupId}/requests/reject")
    @ResponseStatus(HttpStatus.NO_CONTENT) // 204
    public void rejectJoinRequest(@PathVariable Long groupId, @RequestBody JoinRequestPutDTO joinRequestPutDTO, HttpServletRequest request) {
        Group currentGroup = groupService.getGroupById(groupId);
        Long hostId = currentGroup.getHostId();
        userService.getUserById(joinRequestPutDTO.getGuestId());
        JoinRequest joinRequest = joinRequestService.getJoinRequestByGuestIdAndGroupId(joinRequestPutDTO.getGuestId(), groupId);

        Long tokenId = userService.getUseridByToken(request.getHeader("X-Token"));
        if (!tokenId.equals(hostId)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "You are not authorized to reject this join request.");
        }

        joinRequestService.rejectJoinRequest(joinRequest);
    }

    @GetMapping("/groups/{groupId}/requests")
    @ResponseStatus(HttpStatus.OK) // 200
    @ResponseBody
    public List<UserGetDTO> getOpenJoinRequests(@PathVariable Long groupId, HttpServletRequest request) {
        // Check if the group exists
        Group currentGroup = groupService.getGroupById(groupId);

        // Check if the user is the host of the group
        Long tokenId = userService.getUseridByToken(request.getHeader("X-Token"));
        if (!tokenId.equals(currentGroup.getHostId())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "You are not authorized");
        }

        // Get a list of open join requests
        List<JoinRequest> joinRequests = joinRequestService.getOpenJoinRequestsByGroupId(groupId);

        if (joinRequests.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NO_CONTENT, "There are no open requests for the group");
        }

        // Convert JoinRequest objects to UserGetDTOs and return them
        List<UserGetDTO> userGetDTOs = new ArrayList<>();
        for (JoinRequest joinRequest : joinRequests) {
            User guest = userService.getUserById(joinRequest.getGuestId());
            UserGetDTO userGetDTO = DTOMapper.INSTANCE.convertEntityToUserGetDTO(guest);
            userGetDTOs.add(userGetDTO);
        }

        return userGetDTOs;
    }

    @PutMapping("/groups/{groupId}/ratings/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateRatings(@PathVariable Long groupId, @PathVariable Long userId,
                                        @RequestBody Map<Long, String> ingredientRatings,
                                        HttpServletRequest request) {

        Group group = groupService.getGroupById(groupId); // 404 - group not found

        // check for authorization with request
        List<Long> memberIds = groupService.getAllMemberIdsOfGroup(group);
        Long tokenId = userService.getUseridByToken(request.getHeader("X-Token")); // 401 - not authorized
        if(!memberIds.contains(tokenId)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, String.format("You are not a member of the group with id %d.", groupId));
        }

        if(!tokenId.equals(userId)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "You are not authorized");
        }

        // 409 - groupState is not INGREDIENTVOTING
        GroupState groupState = group.getGroupState();
        if(!groupState.equals(GroupState.INGREDIENTVOTING)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "The groupState is not INGREDIENTVOTING"); // 409
        }

        // Validate ingredientRatings - 400 bad request
        for (String rating : ingredientRatings.values()) {
            if (!rating.equals("-1") && !rating.equals("0") && !rating.equals("1")) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid ingredient rating. Ratings should be -1, 0, or 1.");
            }
        }

        // Pass the map of ingredient ratings to the service method
        userService.updateIngredientRatings(groupId, userId, ingredientRatings);

        if(group.getVotingType().equals(VotingType.MAJORITYVOTE)) {
            // As last step calculate the final ingredientRating per Group and store it in ingredientRepo
            groupService.calculateRatingPerGroup(groupId);
        }

        /// Check if all members of group have voted, change state if so
        boolean changeState = true;
        for (Long memberId : memberIds) {
            User member = userService.getUserById(memberId);
            if (member.getVotingStatus() != UserVotingStatus.VOTED) {
                changeState = false;
            }
        }
        if(changeState) {
            groupService.changeGroupState(groupId, GroupState.FINAL); // if all members have voted change state to FINAL and continue
        }
    }

    @PutMapping("/groups/{groupId}/state")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changeGroupState(@PathVariable Long groupId, @RequestBody GroupState newState, HttpServletRequest request) {
        Group group = groupService.getGroupById(groupId);

        if (group == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Group not found");
        }

        Long tokenId = userService.getUseridByToken(request.getHeader("X-Token"));
        if (!tokenId.equals(group.getHostId())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "You are not authorized");
        }

        groupService.changeGroupState(groupId, newState);
    }

    @GetMapping("/groups/{groupId}/state")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public GroupState getGroupState(@PathVariable Long groupId, HttpServletRequest request) {
        Group group = groupService.getGroupById(groupId);

        if (group == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Group not found");
        }

        Long tokenId = userService.getUseridByToken(request.getHeader("X-Token"));
        List<Long> memberIds = groupService.getAllMemberIdsOfGroup(group);
        if (!memberIds.contains(tokenId)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "You are not authorized");
        }

        return group.getGroupState();
    }

}
