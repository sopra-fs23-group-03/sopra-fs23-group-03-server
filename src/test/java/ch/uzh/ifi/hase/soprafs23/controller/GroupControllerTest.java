package ch.uzh.ifi.hase.soprafs23.controller;

import ch.uzh.ifi.hase.soprafs23.entity.Group;
import ch.uzh.ifi.hase.soprafs23.entity.Invitation;
import ch.uzh.ifi.hase.soprafs23.entity.User;
import ch.uzh.ifi.hase.soprafs23.rest.dto.InvitationPutDTO;
import ch.uzh.ifi.hase.soprafs23.service.GroupService;
import ch.uzh.ifi.hase.soprafs23.service.InvitationService;
import ch.uzh.ifi.hase.soprafs23.service.UserService;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.server.ResponseStatusException;


import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GroupController.class)
public class GroupControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GroupService groupService;

    @MockBean
    private UserService userService;

    @MockBean
    private InvitationService invitationService;

    private Group group;

    private User user;

    private Invitation invitation;

    @BeforeEach
    private void setup() {
        group = new Group();
        group.setId(1L);
        group.setGroupName("firstGroupName");
        group.setHostId(2L);

        user = new User();
        user.setId(group.getHostId());
        user.setToken("testtoken");

        // mocks the getUserIdByToken(token) method in UserService
        given(userService.getUserByToken(user.getToken())).willReturn(user.getId());
    }

    @Test
    public void invitationReject_validInput() throws Exception {
        invitation = new Invitation();
        invitation.setId(15L);
        invitation.setGroupId(group.getId());
        invitation.setGuestId(user.getId());

        InvitationPutDTO invitationPutDTO = new InvitationPutDTO();
        invitationPutDTO.setGuestId(user.getId());
        
        // mocks
        given(groupService.getGroupById(group.getId())).willReturn(group);
        given(userService.getUserById(user.getId())).willReturn(user);
        given(invitationService.getInvitationByGroupIdAndGuestId(group.getId(), user.getId())).willReturn(invitation);

        // when
        MockHttpServletRequestBuilder rejectRequest = put("/groups/{groupId}/invitations/reject", group.getId())
                                                        .contentType(MediaType.APPLICATION_JSON)
                                                        .content(asJsonString(invitationPutDTO))
                                                        .header("X-Token", user.getToken());

        // then
        mockMvc.perform(rejectRequest).andExpect(status().isNoContent());

        // verify that the correct calls toservices were made
        verify(userService, times(1)).getUserByToken(user.getToken());
        verify(groupService, times(1)).getGroupById(group.getId());
        verify(userService, times(1)).getUserById(user.getId());
        verify(invitationService, times(1)).getInvitationByGroupIdAndGuestId(group.getId(), user.getId());

        verify(invitationService, times(1)).deleteInvitation(invitation);
    }

    /**
   * Helper Method to convert userPostDTO into a JSON string such that the input
   * can be processed
   * Input will look like this: {"name": "Test User", "username": "testUsername"}
   * 
   * @param object
   * @return string
   */
    private String asJsonString(final Object object) {
        try {
            return new ObjectMapper().writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
            String.format("The request body could not be created.%s", e.toString()));
        }
  }
    
}
