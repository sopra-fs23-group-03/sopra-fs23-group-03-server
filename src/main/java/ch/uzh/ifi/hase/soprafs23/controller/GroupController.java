package ch.uzh.ifi.hase.soprafs23.controller;

import ch.uzh.ifi.hase.soprafs23.entity.Group;
import ch.uzh.ifi.hase.soprafs23.entity.Invitation;
import ch.uzh.ifi.hase.soprafs23.entity.User;
import ch.uzh.ifi.hase.soprafs23.rest.dto.GroupGetDTO;
import ch.uzh.ifi.hase.soprafs23.rest.dto.GroupPostDTO;
import ch.uzh.ifi.hase.soprafs23.rest.dto.InvitationPutDTO;
import ch.uzh.ifi.hase.soprafs23.rest.dto.UserGetDTO;
import ch.uzh.ifi.hase.soprafs23.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs23.service.GroupService;
import ch.uzh.ifi.hase.soprafs23.service.InvitationService;
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

//    @PostMapping("/groups")
//    @ResponseStatus(HttpStatus.CREATED)
//    @ResponseBody
//    public GroupGetDTO createGroup(@RequestBody GroupPostDTO groupPostDTO, @RequestParam Long hostId) {
//        // convert API user to internal representation
//        GroupGetDTO groupInput = DTOMapper.INSTANCE.convertGroupPostDTOtoEntity(groupPostDTO);
//
//        // get the user creating the group
//        User host = userService.getUserById(hostId);
//
//        // create group
//        GroupGetDTO createdGroup = groupService.createGroup(groupInput, host);
//
//        // convert internal representation of user back to API
//        GroupGetDTO groupGetDTO = DTOMapper.INSTANCE.convertEntityToGroupGetDTO(createdGroup);
//
//        return groupGetDTO;
//    }

    @PutMapping("/groups/{groupId}/invitations/reject")
    @ResponseStatus(HttpStatus.NO_CONTENT) // 204
    public void rejectInvitation(@PathVariable Long groupId,
                                 @RequestBody InvitationPutDTO invitationPutDTO,
                                 HttpServletRequest request) {
        Long guestId = invitationPutDTO.getGuestId();
        
        // 401 - not authorized
        Long tokenId = userService.getUserByToken(request.getHeader("X-Token"));
        if(tokenId != guestId) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, String.format("You are not authorized to reject this invitation."));
        }
        
        // 404 - group, guest, and/or invitation not found
        groupService.getGroupById(groupId);
        userService.getUserById(guestId);
        Invitation invitation = invitationService.getInvitationByGroupIdAndGuestId(groupId, guestId);

        // delete invitation
        invitationService.deleteInvitation(invitation);
    }

    @PutMapping("/groups/{groupId}/invitations/accept")
    @ResponseStatus(HttpStatus.NO_CONTENT) // 204
    public void acceptInvitation(@PathVariable Long groupId,
                                 @RequestBody InvitationPutDTO invitationPutDTO,
                                 HttpServletRequest request) {
        Long guestId = invitationPutDTO.getGuestId();

        // 401 - not authorized
        Long tokenId = userService.getUserByToken(request.getHeader("X-Token"));
        if(tokenId != guestId) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, String.format("You are not authorized to accept this invitation."));
        }

        // 404 - group, guest, and/or invitation not found
        groupService.getGroupById(groupId);
        userService.getUserById(invitationPutDTO.getGuestId());
        Invitation invitation = invitationService.getInvitationByGroupIdAndGuestId(groupId, guestId);

        // TODO: add guest to group
        

        // delete invitation
        invitationService.deleteInvitation(invitation);
    }
    
}
