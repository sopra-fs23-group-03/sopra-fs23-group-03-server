package ch.uzh.ifi.hase.soprafs23.controller;

import ch.uzh.ifi.hase.soprafs23.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs23.entity.Group;
import ch.uzh.ifi.hase.soprafs23.entity.Invitation;
import ch.uzh.ifi.hase.soprafs23.entity.User;
import ch.uzh.ifi.hase.soprafs23.repository.GroupRepository;
import ch.uzh.ifi.hase.soprafs23.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs23.rest.dto.GroupPostDTO;
import ch.uzh.ifi.hase.soprafs23.rest.dto.InvitationPutDTO;
import ch.uzh.ifi.hase.soprafs23.service.GroupService;
import ch.uzh.ifi.hase.soprafs23.service.InvitationService;
import ch.uzh.ifi.hase.soprafs23.service.UserService;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;


import javax.servlet.http.HttpServletRequest;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;

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

    private final ObjectMapper objectMapper = new ObjectMapper();

    private GroupPostDTO groupPostDTO;
    private HttpServletRequest request;

    @InjectMocks
    private GroupController groupController;

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
        user.setStatus(UserStatus.ONLINE);

        // mocks the getUserIdByToken(token) method in UserService
        given(userService.getUseridByToken(user.getToken())).willReturn(user.getId());
    }

    @Test
    public void getGroups_returnsJsonArrayOfExistingGroups_whenAuthenticated() throws Exception {

        // given
        List<Group> allGroups = Collections.singletonList(group);

        // this mocksGroupService
        given(userService.getUseridByToken(user.getToken())).willReturn(user.getId()); // first get User ID by Token
        given(userService.getUserById(user.getId())).willReturn(user); // then get User by ID
        given(groupService.getGroups()).willReturn(allGroups);

        // when
        MockHttpServletRequestBuilder getRequest = get("/groups")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-Token", user.getToken());

        // then
        mockMvc.perform(getRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].groupName", is(group.getGroupName())));
    }


    @Test
    public void getGroups_returnsErrorUNAUTHORIZED_whenNotAuthenticated() throws Exception {
        // given
        List<Group> allGroups = Collections.singletonList(group);

        // mocks the getUserIdByToken(token) method in UserService
        given(userService.getUseridByToken("newToken")).willReturn(0L);
        given(groupService.getGroups()).willReturn(allGroups);

        // when
        MockHttpServletRequestBuilder getRequest = get("/groups")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-Token", "newToken");

        // then
        mockMvc.perform(getRequest).andExpect(status().isUnauthorized());
        verify(userService, times(1)).getUseridByToken("newToken");
    }

    @Test
    public void invitationsReject_valid() throws Exception {
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


        // verify that all calls to services were made
        verify(groupService, times(1)).getGroupById(group.getId());
        verify(userService, times(1)).getUserById(user.getId());
        verify(invitationService, times(1)).getInvitationByGroupIdAndGuestId(group.getId(), user.getId());

        verify(userService, times(1)).getUseridByToken(user.getToken());

        verify(invitationService, times(1)).deleteInvitation(invitation);
    }

    @Test
    public void invitationsReject_groupNotFound() throws Exception {
        Long anotherGroupId = 4L;

        InvitationPutDTO invitationPutDTO = new InvitationPutDTO();
        invitationPutDTO.setGuestId(user.getId());
        
        // mocks
        given(groupService.getGroupById(anotherGroupId)).willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "error message"));

        // when
        MockHttpServletRequestBuilder rejectRequest = put("/groups/{groupId}/invitations/reject", anotherGroupId)
                                                        .contentType(MediaType.APPLICATION_JSON)
                                                        .content(asJsonString(invitationPutDTO))
                                                        .header("X-Token", user.getToken());

        // then
        mockMvc.perform(rejectRequest).andExpect(status().isNotFound());


        // verify the important calls to service
        verify(groupService, times(1)).getGroupById(anotherGroupId);

        verify(invitationService, times(0)).deleteInvitation(any());
    }

    @Test
    public void invitationsReject_guestNotFound() throws Exception {
        Long anotherGuestId = 4L;

        InvitationPutDTO invitationPutDTO = new InvitationPutDTO();
        invitationPutDTO.setGuestId(anotherGuestId);
        
        // mocks
        given(groupService.getGroupById(group.getId())).willReturn(group);
        given(userService.getUserById(anotherGuestId)).willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "error message"));

        // when
        MockHttpServletRequestBuilder rejectRequest = put("/groups/{groupId}/invitations/reject", group.getId())
                                                        .contentType(MediaType.APPLICATION_JSON)
                                                        .content(asJsonString(invitationPutDTO))
                                                        .header("X-Token", user.getToken());

        // then
        mockMvc.perform(rejectRequest).andExpect(status().isNotFound());


        // verify the important calls to service
        verify(userService, times(1)).getUserById(anotherGuestId);

        verify(invitationService, times(0)).deleteInvitation(invitation);
    }

    @Test
    public void invitationsReject_invitationNotFound() throws Exception {
        InvitationPutDTO invitationPutDTO = new InvitationPutDTO();
        invitationPutDTO.setGuestId(user.getId());
        
        // mocks
        given(groupService.getGroupById(group.getId())).willReturn(group);
        given(userService.getUserById(user.getId())).willReturn(user);
        given(invitationService.getInvitationByGroupIdAndGuestId(group.getId(), user.getId())).willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "error message"));

        // when
        MockHttpServletRequestBuilder rejectRequest = put("/groups/{groupId}/invitations/reject", group.getId())
                                                        .contentType(MediaType.APPLICATION_JSON)
                                                        .content(asJsonString(invitationPutDTO))
                                                        .header("X-Token", user.getToken());

        // then
        mockMvc.perform(rejectRequest).andExpect(status().isNotFound());


        // verify the important calls to service
        verify(invitationService, times(1)).getInvitationByGroupIdAndGuestId(group.getId(), user.getId());

        verify(invitationService, times(0)).deleteInvitation(any());
    }

    @Test
    public void invitationsReject_notValidToken() throws Exception {
        String anotherToken = "anotherToken";

        InvitationPutDTO invitationPutDTO = new InvitationPutDTO();
        invitationPutDTO.setGuestId(user.getId());
        
        // mocks
        given(userService.getUseridByToken(anotherToken)).willReturn(0L);

        // when
        MockHttpServletRequestBuilder rejectRequest = put("/groups/{groupId}/invitations/reject", group.getId())
                                                        .contentType(MediaType.APPLICATION_JSON)
                                                        .content(asJsonString(invitationPutDTO))
                                                        .header("X-Token", anotherToken);

        // then
        mockMvc.perform(rejectRequest).andExpect(status().isUnauthorized());


        // verify the important calls to service
        verify(userService, times(1)).getUseridByToken(anotherToken);

        verify(invitationService, times(0)).deleteInvitation(any());
    }

    @Test
    public void invitationsAccept_valid() throws Exception {
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
        MockHttpServletRequestBuilder acceptRequest = put("/groups/{groupId}/invitations/accept", group.getId())
                                                        .contentType(MediaType.APPLICATION_JSON)
                                                        .content(asJsonString(invitationPutDTO))
                                                        .header("X-Token", user.getToken());

        // then
        mockMvc.perform(acceptRequest).andExpect(status().isNoContent());


        // verify that all calls to services were made
        verify(groupService, times(1)).getGroupById(group.getId());
        verify(userService, times(1)).getUserById(user.getId());
        verify(invitationService, times(1)).getInvitationByGroupIdAndGuestId(group.getId(), user.getId());

        verify(userService, times(1)).getUseridByToken(user.getToken());

        verify(groupService, times(1)).addGuestToGroupMembers(user.getId(), group.getId());
        verify(userService, times(1)).joinGroup(user.getId(), group.getId());

        verify(invitationService, times(1)).deleteInvitation(invitation);
    }

    @Test
    public void invitationsAccept_groupNotFound() throws Exception {
        Long anotherGroupId = 4L;

        InvitationPutDTO invitationPutDTO = new InvitationPutDTO();
        invitationPutDTO.setGuestId(user.getId());
        
        // mocks
        given(groupService.getGroupById(anotherGroupId)).willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "error message"));

        // when
        MockHttpServletRequestBuilder acceptRequest = put("/groups/{groupId}/invitations/accept", anotherGroupId)
                                                        .contentType(MediaType.APPLICATION_JSON)
                                                        .content(asJsonString(invitationPutDTO))
                                                        .header("X-Token", user.getToken());

        // then
        mockMvc.perform(acceptRequest).andExpect(status().isNotFound());


        // verify the important calls to service
        verify(groupService, times(1)).getGroupById(anotherGroupId);

        verify(groupService, times(0)).addGuestToGroupMembers(any(), any());
        verify(userService, times(0)).joinGroup(any(), any());

        verify(invitationService, times(0)).deleteInvitation(any());
    }

    @Test
    public void invitationsAccept_guestNotFound() throws Exception {
        Long anotherGuestId = 4L;

        InvitationPutDTO invitationPutDTO = new InvitationPutDTO();
        invitationPutDTO.setGuestId(anotherGuestId);
        
        // mocks
        given(groupService.getGroupById(group.getId())).willReturn(group);
        given(userService.getUserById(anotherGuestId)).willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "error message"));

        // when
        MockHttpServletRequestBuilder acceptRequest = put("/groups/{groupId}/invitations/accept", group.getId())
                                                        .contentType(MediaType.APPLICATION_JSON)
                                                        .content(asJsonString(invitationPutDTO))
                                                        .header("X-Token", user.getToken());

        // then
        mockMvc.perform(acceptRequest).andExpect(status().isNotFound());


        // verify the important calls to service
        verify(userService, times(1)).getUserById(anotherGuestId);

        verify(groupService, times(0)).addGuestToGroupMembers(any(), any());
        verify(userService, times(0)).joinGroup(any(), any());

        verify(invitationService, times(0)).deleteInvitation(invitation);
    }

    @Test
    public void invitationsAccept_invitationNotFound() throws Exception {
        InvitationPutDTO invitationPutDTO = new InvitationPutDTO();
        invitationPutDTO.setGuestId(user.getId());
        
        // mocks
        given(groupService.getGroupById(group.getId())).willReturn(group);
        given(userService.getUserById(user.getId())).willReturn(user);
        given(invitationService.getInvitationByGroupIdAndGuestId(group.getId(), user.getId())).willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "error message"));

        // when
        MockHttpServletRequestBuilder acceptRequest = put("/groups/{groupId}/invitations/accept", group.getId())
                                                        .contentType(MediaType.APPLICATION_JSON)
                                                        .content(asJsonString(invitationPutDTO))
                                                        .header("X-Token", user.getToken());

        // then
        mockMvc.perform(acceptRequest).andExpect(status().isNotFound());


        // verify the important calls to service
        verify(invitationService, times(1)).getInvitationByGroupIdAndGuestId(group.getId(), user.getId());

        verify(groupService, times(0)).addGuestToGroupMembers(any(), any());
        verify(userService, times(0)).joinGroup(any(), any());

        verify(invitationService, times(0)).deleteInvitation(any());
    }

    @Test
    public void invitationsAccept_notValidToken() throws Exception {
        String anotherToken = "anotherToken";

        InvitationPutDTO invitationPutDTO = new InvitationPutDTO();
        invitationPutDTO.setGuestId(user.getId());
        
        // mocks
        given(userService.getUseridByToken(anotherToken)).willReturn(0L);

        // when
        MockHttpServletRequestBuilder acceptRequest = put("/groups/{groupId}/invitations/accept", group.getId())
                                                        .contentType(MediaType.APPLICATION_JSON)
                                                        .content(asJsonString(invitationPutDTO))
                                                        .header("X-Token", anotherToken);

        // then
        mockMvc.perform(acceptRequest).andExpect(status().isUnauthorized());


        // verify the important calls to service
        verify(userService, times(1)).getUseridByToken(anotherToken);

        verify(groupService, times(0)).addGuestToGroupMembers(any(), any());
        verify(userService, times(0)).joinGroup(any(), any());

        verify(invitationService, times(0)).deleteInvitation(any());
    }


    @Test
    public void createGroup_returns201() throws Exception {

        // given
        GroupPostDTO groupPostDTO = new GroupPostDTO();
        groupPostDTO.setGroupName("Test Group");
        groupPostDTO.setHostId(1L);

        Group group = new Group();
        group.setId(1L);
        group.setGroupName("Test Group");
        group.setHostId(1L);

        given(userService.getUseridByToken(any())).willReturn(group.getHostId());
        given(userService.getUserById(group.getHostId())).willReturn(new User());
        given(groupService.createGroup(any(Group.class), any(User.class))).willReturn(group);

        // when
        MockHttpServletRequestBuilder postRequest = post("/groups")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-Token", "valid-token")
                .content("{\"groupName\": \"Test Group\", \"hostId\": 1}");

        // then
        mockMvc.perform(postRequest)
                .andExpect(status().isCreated());
    }

    @Test
    public void testCreateGroupReturns401() throws Exception {
        Long userId = 1L;
        String token = "invalid-token";
        Mockito.when(userService.getUseridByToken(token)).thenReturn(0L);

        GroupPostDTO groupPostDTO = new GroupPostDTO();
        groupPostDTO.setHostId(userId);

        ObjectMapper mapper = new ObjectMapper();
        String requestBody = mapper.writeValueAsString(groupPostDTO);

        mockMvc.perform(MockMvcRequestBuilders.post("/groups")
                        .header("X-Token", token)
                        .content(requestBody)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }
    @Test
    public void testCreateGroupReturns409() throws Exception {
        GroupPostDTO groupPostDTO = new GroupPostDTO();
        groupPostDTO.setGroupName(group.getGroupName());
        groupPostDTO.setHostId(group.getHostId());

        HttpServletRequest mockedRequest = Mockito.mock(HttpServletRequest.class);
        Mockito.when(mockedRequest.getHeader("X-Token")).thenReturn("validToken");

        Mockito.when(userService.getUseridByToken("validToken")).thenReturn(group.getHostId());
        Mockito.when(userService.getUserById(group.getHostId())).thenReturn(Mockito.mock(User.class));

        given(groupService.createGroup(Mockito.any(Group.class), Mockito.any(User.class))).willThrow(new ResponseStatusException(HttpStatus.CONFLICT, "error message"));

        MockHttpServletRequestBuilder postRequest = post("/groups")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(groupPostDTO))
                .with(request -> {
                    request.setMethod("POST");
                    request.setRequestURI("/groups");
                    request.addHeader("X-Token", "validToken");
                    return request;
                });

        mockMvc.perform(postRequest)
                .andExpect(status().isConflict());
    }

    @Test
    public void testCreateGroupReturns404() throws Exception {
        Long userId = 1L;

        GroupPostDTO groupPostDTO = new GroupPostDTO();
        groupPostDTO.setHostId(userId);

        String errorMessage = String.format("User with id %s does not exist.", userId);

        Mockito.when(userService.getUserById(userId))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, errorMessage));

        String token = "valid-token";
        Mockito.when(userService.getUseridByToken(token)).thenReturn(userId);

        ObjectMapper mapper = new ObjectMapper();
        String requestBody = mapper.writeValueAsString(groupPostDTO);

        mockMvc.perform(MockMvcRequestBuilders.post("/groups")
                        .header("X-Token", token)
                        .content(requestBody)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    public void testGetGroupById_valid() throws Exception {
        // mocks
        given(groupService.getGroupById(group.getId())).willReturn(group);

        // when
        MockHttpServletRequestBuilder getRequest = get("/groups/{groupId}", group.getId())
                                                    .contentType(MediaType.APPLICATION_JSON)
                                                    .header("X-Token", user.getToken());

        // then
        mockMvc.perform(getRequest)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id", is(group.getId().intValue())))
            .andExpect(jsonPath("$.groupName", is(group.getGroupName())))
            .andExpect(jsonPath("$.hostId", is(group.getHostId().intValue())));
            
        // verifies
        verify(groupService, times(1)).getGroupById(group.getId());
        verify(userService, times(1)).getUseridByToken(user.getToken());
    }

    @Test
    public void testGetGroupById_groupNotFound() throws Exception {
        Long anotherGroupId = 8L;

        // mocks
        given(groupService.getGroupById(anotherGroupId)).willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "error message"));

        // when
        MockHttpServletRequestBuilder getRequest = get("/groups/{groupId}", anotherGroupId)
                                                    .contentType(MediaType.APPLICATION_JSON)
                                                    .header("X-Token", user.getToken());

        // then
        mockMvc.perform(getRequest).andExpect(status().isNotFound());
            
        // verifies
        verify(groupService, times(1)).getGroupById(anotherGroupId);
        verify(userService, times(0)).getUseridByToken(any());
    }

    @Test
    public void testGetGroupById_notValidToken() throws Exception {
        String anotherToken = "anotherToken";

        // mocks
        given(userService.getUseridByToken(anotherToken)).willReturn(0L);

        // when
        MockHttpServletRequestBuilder getRequest = get("/groups/{groupId}", group.getId())
                                                    .contentType(MediaType.APPLICATION_JSON)
                                                    .header("X-Token", anotherToken);

        // then
        mockMvc.perform(getRequest).andExpect(status().isUnauthorized());
            
        // verifies
        verify(userService, times(1)).getUseridByToken(anotherToken);
    }
    
    @Test
    public void testGetGroupMembersById_valid() throws Exception {
        // mocks
        given(groupService.getGroupById(group.getId())).willReturn(group);
        given(groupService.getAllMemberIdsOfGroup(group)).willReturn(Arrays.asList(group.getHostId()));
        given(userService.getUserById(group.getHostId())).willReturn(user);

        // when
        MockHttpServletRequestBuilder getRequest = get("/groups/{groupId}/members", group.getId())
                                                    .contentType(MediaType.APPLICATION_JSON)
                                                    .header("X-Token", user.getToken());

        // then
        mockMvc.perform(getRequest)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].id", is(group.getHostId().intValue())));
            
        // verifies
        verify(groupService, times(1)).getGroupById(group.getId());
        verify(userService, times(1)).getUseridByToken(user.getToken());
        verify(groupService, times(1)).getAllMemberIdsOfGroup(group);
        verify(userService, times(1)).getUserById(group.getHostId());
    }

    @Test
    public void testGetGroupMembersById_groupNotFound() throws Exception {
        Long anotherGroupId = 8L;

        // mocks
        given(groupService.getGroupById(anotherGroupId)).willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "error message"));

        // when
        MockHttpServletRequestBuilder getRequest = get("/groups/{groupId}/members", anotherGroupId)
                                                    .contentType(MediaType.APPLICATION_JSON)
                                                    .header("X-Token", user.getToken());

        // then
        mockMvc.perform(getRequest).andExpect(status().isNotFound());
            
        // verifies
        verify(groupService, times(1)).getGroupById(anotherGroupId);
        verify(userService, times(0)).getUseridByToken(any());
        verify(groupService, times(0)).getAllMemberIdsOfGroup(any());
        verify(userService, times(0)).getUserById(any());
    }

    @Test
    public void testGetGroupMembersById_notValidToken() throws Exception {
        String anotherToken = "anotherToken";

        // mocks
        given(userService.getUseridByToken(anotherToken)).willReturn(0L);

        // when
        MockHttpServletRequestBuilder getRequest = get("/groups/{groupId}/members", group.getId())
                                                    .contentType(MediaType.APPLICATION_JSON)
                                                    .header("X-Token", anotherToken);

        // then
        mockMvc.perform(getRequest).andExpect(status().isUnauthorized());
            
        // verifies
        verify(groupService, times(1)).getGroupById(group.getId());
        verify(userService, times(1)).getUseridByToken(anotherToken);
        verify(groupService, times(0)).getAllMemberIdsOfGroup(any());
        verify(userService, times(0)).getUserById(any());
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
