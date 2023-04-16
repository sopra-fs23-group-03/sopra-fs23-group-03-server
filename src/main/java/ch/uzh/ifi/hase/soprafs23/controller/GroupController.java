package ch.uzh.ifi.hase.soprafs23.controller;

import ch.uzh.ifi.hase.soprafs23.entity.Group;
//import ch.uzh.ifi.hase.soprafs23.entity.User; // unused
import ch.uzh.ifi.hase.soprafs23.entity.User;
import ch.uzh.ifi.hase.soprafs23.rest.dto.GroupGetDTO;
import ch.uzh.ifi.hase.soprafs23.rest.dto.GroupPostDTO;
import ch.uzh.ifi.hase.soprafs23.rest.dto.InvitationPutDTO;
//import ch.uzh.ifi.hase.soprafs23.rest.dto.UserGetDTO; // unused
//import ch.uzh.ifi.hase.soprafs23.rest.dto.UserPostDTO; // unused
import ch.uzh.ifi.hase.soprafs23.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs23.service.GroupService;
import ch.uzh.ifi.hase.soprafs23.service.InvitationService;
//import ch.uzh.ifi.hase.soprafs23.service.UserService; // unused
import ch.uzh.ifi.hase.soprafs23.service.UserService;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;



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
        //TODO: write rejectInvitation

        Long guestId = invitationPutDTO.getGuestId();
        
        // 404 - group, guest, and/or invitation not found
        groupService.getGroupById(groupId);
        userService.getUserById(guestId);
        invitationService.getInvitationByGroupIdAndGuestId(groupId, guestId);

        // 401 - not authorized
    }

    @PutMapping("/groups/{groupId}/invitations/accept")
    @ResponseStatus(HttpStatus.NO_CONTENT) // 204
    public void acceptInvitation(@PathVariable Long groupId,
                                 @RequestBody InvitationPutDTO invitationPutDTO,
                                 HttpServletRequest request) {
        //TODO: write acceptInvitation

        Long guestId = invitationPutDTO.getGuestId();

        // 404 - group, guest, and/or invitation not found
        groupService.getGroupById(groupId);
        userService.getUserById(invitationPutDTO.getGuestId());
        invitationService.getInvitationByGroupIdAndGuestId(groupId, guestId);

        // 401 - not authorized
    }
    
}
