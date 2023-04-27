package ch.uzh.ifi.hase.soprafs23.controller;

import ch.uzh.ifi.hase.soprafs23.entity.Group;
import ch.uzh.ifi.hase.soprafs23.entity.Invitation;
import ch.uzh.ifi.hase.soprafs23.entity.User;
import ch.uzh.ifi.hase.soprafs23.rest.dto.*;
import ch.uzh.ifi.hase.soprafs23.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs23.service.GroupService;
import ch.uzh.ifi.hase.soprafs23.service.InvitationService;
import ch.uzh.ifi.hase.soprafs23.service.UserService;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;



import java.util.ArrayList;
import java.util.List;


@RestController
public class GroupController {
    private final GroupService groupService;

    private final UserService userService;

    private final InvitationService invitationService;

    GroupController(GroupService groupService, UserService userService, InvitationService invitationService) {
        this.groupService = groupService;
        this.userService = userService;
        this.invitationService = invitationService;
    }

    @GetMapping("/groups")
    @ResponseStatus(HttpStatus.OK) // 200
    @ResponseBody
    public List<GroupGetDTO> getAllGroups(HttpServletRequest request) {

        // check validity of token
        String token = request.getHeader("X-Token");
        if(userService.getUseridByToken(token) == 0) {
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

    @PostMapping("/groups")
    @ResponseStatus(HttpStatus.CREATED) // 201
    @ResponseBody
    public GroupGetDTO createGroup(@RequestBody GroupPostDTO groupPostDTO, HttpServletRequest request) {
        // 404
        User host = userService.getUserById(groupPostDTO.getHostId());

        // check validity of token
        Long userId = userService.getUseridByToken(request.getHeader("X-Token"));
        if(userId != groupPostDTO.getHostId()) {
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
        if(tokenId != currentGroupHostId) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, String.format("You are not authorized.")); // 401 - not authorized
        }

        // Loop through each guest id and create an invitation for them: idea--> create InvitationPostDTO object for each guest id, set guest id in it, then use DTOMapper to convert it to an Invit entity
        for (Long guestId : ListGuestIds) {
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
        if(tokenId != guestId) {
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
        if(tokenId != guestId) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, String.format("You are not authorized to accept this invitation."));
        }

        // create link between guest and group
        groupService.addGuestToGroupMembers(guestId, groupId);
        userService.joinGroup(guestId, groupId); 

        // delete invitation
        invitationService.deleteInvitation(invitation);
    }

    @GetMapping("/groups/{groupId}")
    @ResponseStatus(HttpStatus.OK) // 200
    @ResponseBody
    public GroupGetDTO getGroupById(@PathVariable Long groupId, HttpServletRequest request) {
        // 404 - group not found
        Group group = groupService.getGroupById(groupId);

        // 401 - not authorized
        Long tokenId = userService.getUseridByToken(request.getHeader("X-Token"));
        if(tokenId == 0L) {
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
        if(tokenId == 0L) {
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
    
}
