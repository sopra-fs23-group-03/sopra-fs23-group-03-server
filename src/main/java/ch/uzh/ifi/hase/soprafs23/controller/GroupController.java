package ch.uzh.ifi.hase.soprafs23.controller;

import ch.uzh.ifi.hase.soprafs23.entity.Group;
//import ch.uzh.ifi.hase.soprafs23.entity.User; // unused
import ch.uzh.ifi.hase.soprafs23.entity.User;
import ch.uzh.ifi.hase.soprafs23.rest.dto.GroupGetDTO;
import ch.uzh.ifi.hase.soprafs23.rest.dto.GroupPostDTO;
//import ch.uzh.ifi.hase.soprafs23.rest.dto.UserGetDTO; // unused
//import ch.uzh.ifi.hase.soprafs23.rest.dto.UserPostDTO; // unused
import ch.uzh.ifi.hase.soprafs23.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs23.service.GroupService;
//import ch.uzh.ifi.hase.soprafs23.service.UserService; // unused
import ch.uzh.ifi.hase.soprafs23.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
public class GroupController {
    private final GroupService groupService;

    private final UserService userService;

    GroupController(GroupService groupService, UserService userService) {
        this.groupService = groupService;
        this.userService = userService;
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
}
