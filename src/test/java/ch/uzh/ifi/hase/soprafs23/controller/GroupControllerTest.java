package ch.uzh.ifi.hase.soprafs23.controller;

import ch.uzh.ifi.hase.soprafs23.constant.GroupState;
import ch.uzh.ifi.hase.soprafs23.constant.UserVotingStatus;
import ch.uzh.ifi.hase.soprafs23.constant.VotingType;
import ch.uzh.ifi.hase.soprafs23.entity.Group;
import ch.uzh.ifi.hase.soprafs23.entity.Ingredient;
import ch.uzh.ifi.hase.soprafs23.entity.Invitation;
import ch.uzh.ifi.hase.soprafs23.entity.JoinRequest;
import ch.uzh.ifi.hase.soprafs23.entity.User;
import ch.uzh.ifi.hase.soprafs23.repository.JoinRequestRepository;
import ch.uzh.ifi.hase.soprafs23.rest.dto.*;
import ch.uzh.ifi.hase.soprafs23.service.GroupService;
import ch.uzh.ifi.hase.soprafs23.service.InvitationService;
import ch.uzh.ifi.hase.soprafs23.service.JoinRequestService;
import ch.uzh.ifi.hase.soprafs23.service.UserService;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.server.ResponseStatusException;


import javax.servlet.http.HttpServletRequest;
import java.util.*;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(GroupController.class)
public class GroupControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GroupService groupService;

    @MockBean
    private UserService userService;

    @MockBean
    private JoinRequestService joinRequestService;

    @MockBean
    private InvitationService invitationService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private JoinRequestRepository joinRequestRepository;

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
        group.setVotingType(VotingType.MAJORITYVOTE);

        user = new User();
        user.setId(group.getHostId());
        user.setToken("testtoken");

        // mocks that are used in most tests
        given(userService.getUseridByToken(user.getToken())).willReturn(user.getId());
        given(userService.getUserById(user.getId())).willReturn(user);
        given(groupService.getGroupById(group.getId())).willReturn(group);
    }

    @Test
    public void getGroups_returnsJsonArrayOfExistingGroups_whenAuthenticated() throws Exception {
        // given
        List<Group> allGroups = Collections.singletonList(group);

        // mocks
        given(groupService.getGroups()).willReturn(allGroups);

        // when
        MockHttpServletRequestBuilder getRequest = get("/groups")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-Token", user.getToken());

        // then
        mockMvc.perform(getRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(group.getId().intValue())))
                .andExpect(jsonPath("$[0].groupName", is(group.getGroupName())))
                .andExpect(jsonPath("$[0].hostId", is(group.getHostId().intValue())))
                .andExpect(jsonPath("$[0].votingType", is(group.getVotingType().toString())));
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
        groupPostDTO.setGroupName(group.getGroupName());
        groupPostDTO.setHostId(group.getHostId());
        groupPostDTO.setVotingType(group.getVotingType().toString());

        // mocks
        given(groupService.createGroup(any(Group.class))).willReturn(group);

        // when
        MockHttpServletRequestBuilder postRequest = post("/groups")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-Token", user.getToken())
                .content(asJsonString(groupPostDTO));

        // then
        mockMvc.perform(postRequest)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(group.getId().intValue())))
                .andExpect(jsonPath("$.groupName", is(group.getGroupName())))
                .andExpect(jsonPath("$.hostId", is(group.getHostId().intValue())))
                .andExpect(jsonPath("$.votingType", is(group.getVotingType().toString())));

        // verifies
        verify(groupService, times(1)).createGroup(any());
    }

    @Test
    public void testCreateGroupReturns401() throws Exception {
        // given
        GroupPostDTO groupPostDTO = new GroupPostDTO();
        groupPostDTO.setGroupName(group.getGroupName());
        groupPostDTO.setHostId(group.getHostId());
        groupPostDTO.setVotingType(group.getVotingType().toString());

        String anotherToken = "invalid-token";

        // mocks
        given(groupService.createGroup(any(Group.class))).willReturn(group);
        given(userService.getUseridByToken(anotherToken)).willReturn(0L);

        // when
        MockHttpServletRequestBuilder postRequest = post("/groups")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-Token", anotherToken)
                .content(asJsonString(groupPostDTO));

        // then
        mockMvc.perform(postRequest)
                .andExpect(status().isUnauthorized());

        // verifies
        verify(groupService, times(0)).createGroup(any());
    }
    
    @Test
    public void testCreateGroupReturns409() throws Exception {
        // given
        GroupPostDTO groupPostDTO = new GroupPostDTO();
        groupPostDTO.setGroupName(group.getGroupName());
        groupPostDTO.setHostId(group.getHostId());
        groupPostDTO.setVotingType(group.getVotingType().toString());

        // mocks
        given(groupService.createGroup(any(Group.class))).willThrow(new ResponseStatusException(HttpStatus.CONFLICT, "error message"));

        // when
        MockHttpServletRequestBuilder postRequest = post("/groups")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-Token", user.getToken())
                .content(asJsonString(groupPostDTO));

        // then
        mockMvc.perform(postRequest)
                .andExpect(status().isConflict());

        // verifies
        verify(groupService, times(1)).createGroup(any());
    }

    @Test
    public void testCreateGroupReturns404() throws Exception {
        // given
        Long anotherUserId = 5L;

        GroupPostDTO groupPostDTO = new GroupPostDTO();
        groupPostDTO.setGroupName(group.getGroupName());
        groupPostDTO.setHostId(anotherUserId);
        groupPostDTO.setVotingType(group.getVotingType().toString());

        // mocks
        given(userService.getUserById(anotherUserId)).willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "error message"));

        // when
        MockHttpServletRequestBuilder postRequest = post("/groups")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-Token", user.getToken())
                .content(asJsonString(groupPostDTO));

        // then
        mockMvc.perform(postRequest)
                .andExpect(status().isNotFound());

        // verifies
        verify(groupService, times(0)).createGroup(any());
    }

    @Test
    public void sendInvitation_returns404_whenGroupNotFound() throws Exception {
        // given
        Long anotherGroupId = 5L;
        List<Long> guestIds = Arrays.asList(2L, 3L);

        // mocks
        given(groupService.getGroupById(anotherGroupId)).willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "error message"));

        // when and then
        MockHttpServletRequestBuilder postRequest = post("/groups/" + anotherGroupId + "/invitations")
                                                    .header("X-Token", user.getToken())
                                                    .content(new ObjectMapper().writeValueAsString(guestIds))
                                                    .contentType(MediaType.APPLICATION_JSON)
                                                    .accept(MediaType.APPLICATION_JSON);

        mockMvc.perform(postRequest)
                .andExpect(status().isNotFound());
    }

    @Test
    public void sendInvitation_returns401_whenUnauthorized() throws Exception {
        // given
        List<Long> guestIds = Arrays.asList(3L, 4L);

        Long anotherUserId = 9L;
        String anotherToken = "another token";

        // mocks
        given(userService.getUseridByToken(anotherToken)).willReturn(anotherUserId);

        // when and then
        MockHttpServletRequestBuilder postRequest = post("/groups/" + group.getId() + "/invitations")
                                                    .header("X-Token", anotherToken)
                                                    .content(new ObjectMapper().writeValueAsString(guestIds))
                                                    .contentType(MediaType.APPLICATION_JSON)
                                                    .accept(MediaType.APPLICATION_JSON);

        mockMvc.perform(postRequest)
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testSendInvitation_409Conflict() throws Exception {
        // given
        Invitation existingInvitation = new Invitation();
        existingInvitation.setGroupId(group.getId());
        existingInvitation.setGuestId(2L);

        // mocks
        given(invitationService.createInvitation(anyLong(), anyLong())).willThrow(new ResponseStatusException(HttpStatus.CONFLICT, "An invitation has already been sent."));

        // when and then
        MockHttpServletRequestBuilder postRequest = post("/groups/{groupId}/invitations", group.getId())
                                                    .header("X-Token", user.getToken())
                                                    .content("[2]")
                                                    .contentType(MediaType.APPLICATION_JSON);
        
        mockMvc.perform(postRequest)
                .andExpect(MockMvcResultMatchers.status().isConflict());
    }

    @Test
    public void sendInvitation_returns201_whenSuccessful() throws Exception {
        // given
        Long firstInviteeId = 3L;
        Long secondInviteeId = 4L;
        List<Long> guestIds = Arrays.asList(firstInviteeId, secondInviteeId);

        // when and then
        MockHttpServletRequestBuilder postRequest = post("/groups/" + group.getId() + "/invitations")
                                                    .header("X-Token", user.getToken())
                                                    .content(new ObjectMapper().writeValueAsString(guestIds))
                                                    .contentType(MediaType.APPLICATION_JSON)
                                                    .accept(MediaType.APPLICATION_JSON);

        mockMvc.perform(postRequest)
                .andExpect(status().isCreated());

        // verify
        verify(invitationService, times(1)).createInvitation(group.getId(), firstInviteeId);
        verify(invitationService, times(1)).createInvitation(group.getId(), secondInviteeId);
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
            .andExpect(jsonPath("$.hostId", is(group.getHostId().intValue())))
            .andExpect(jsonPath("$.votingType", is(group.getVotingType().toString())));
            
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

    @Test
    public void testGetIngredientsOfGroupById_valid_oneIngredient() throws Exception {
        // given
        Ingredient apple = new Ingredient();
        apple.setId(19L);
        apple.setName("apple");

        List<Ingredient> ingredients = new ArrayList<>();
        ingredients.add(apple);
        group.addIngredient(ingredients);

        List<Long> memberIds = new ArrayList<>();
        memberIds.add(group.getHostId());

        // mocks
        given(groupService.getAllMemberIdsOfGroup(group)).willReturn(memberIds);
        given(userService.userHasIngredients(group.getHostId())).willReturn(true);

        // when
        MockHttpServletRequestBuilder getRequest = get("/groups/{groupId}/ingredients", group.getId())
                                                    .contentType(MediaType.APPLICATION_JSON)
                                                    .header("X-Token", user.getToken());

        // then
        mockMvc.perform(getRequest)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].id", is(apple.getId().intValue())))
            .andExpect(jsonPath("$[0].name", is(apple.getName())));
    }

    @Test
    public void testGetIngredientsOfGroupById_valid_multipleIngredients() throws Exception {
        // given
        Ingredient apple = new Ingredient("apple");
        Ingredient pear = new Ingredient("pear");
        Ingredient banana = new Ingredient("banana");

        List<Ingredient> ingredients = new ArrayList<>();
        ingredients.add(apple);
        ingredients.add(pear);
        ingredients.add(banana);
        group.addIngredient(ingredients);

        List<Long> memberIds = new ArrayList<>();
        memberIds.add(group.getHostId());

        // mocks
        given(groupService.getAllMemberIdsOfGroup(group)).willReturn(memberIds);
        given(userService.userHasIngredients(group.getHostId())).willReturn(true);

        // when
        MockHttpServletRequestBuilder getRequest = get("/groups/{groupId}/ingredients", group.getId())
                                                    .contentType(MediaType.APPLICATION_JSON)
                                                    .header("X-Token", user.getToken());

        // then
        mockMvc.perform(getRequest)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(3)));
    }

    @Test
    public void testGetIngredientsOfGroupById_notYetEnteredIngredients() throws Exception {
        List<Long> memberIds = new ArrayList<>();
        memberIds.add(group.getHostId());

        // mocks
        given(groupService.getAllMemberIdsOfGroup(group)).willReturn(memberIds);
        given(userService.userHasIngredients(group.getHostId())).willReturn(false);

        // when
        MockHttpServletRequestBuilder getRequest = get("/groups/{groupId}/ingredients", group.getId())
                                                    .contentType(MediaType.APPLICATION_JSON)
                                                    .header("X-Token", user.getToken());

        // then
        mockMvc.perform(getRequest)
            .andExpect(status().isAccepted());
    }

    @Test
    public void testGetIngredientsOfGroupById_groupNotFound() throws Exception {
        // given
        Long anotherGroupId = 17L;

        // mocks
        given(groupService.getGroupById(anotherGroupId)).willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "error message"));

        // when
        MockHttpServletRequestBuilder getRequest = get("/groups/{groupId}/ingredients", anotherGroupId)
                                                    .contentType(MediaType.APPLICATION_JSON)
                                                    .header("X-Token", user.getToken());

        // then
        mockMvc.perform(getRequest)
            .andExpect(status().isNotFound());
    }

    @Test
    public void testGetIngredientsOfGroupById_notValidToken() throws Exception {
        // given
        String anotherToken = "anotherToken";
        Long anotherUserId = 7L;

        List<Long> memberIds = new ArrayList<>();
        memberIds.add(group.getHostId());

        // mocks
        given(groupService.getAllMemberIdsOfGroup(group)).willReturn(memberIds);
        given(userService.getUseridByToken(anotherToken)).willReturn(anotherUserId);

        // when
        MockHttpServletRequestBuilder getRequest = get("/groups/{groupId}/ingredients", group.getId())
                                                    .contentType(MediaType.APPLICATION_JSON)
                                                    .header("X-Token", anotherToken);

        // then
        mockMvc.perform(getRequest)
            .andExpect(status().isUnauthorized());
    }

    @Test
    public void testGetFinalIngredientsOfGroupById_valid_oneIngredient() throws Exception {
        // given
        user.setVotingStatus(UserVotingStatus.VOTED);

        Ingredient apple = new Ingredient();
        apple.setId(19L);
        apple.setName("apple");
        apple.setCalculatedRating(1);

        Set<Ingredient> ingredients = new HashSet<>();
        ingredients.add(apple);

        List<Long> memberIds = new ArrayList<>();
        memberIds.add(group.getHostId());

        // mocks
        given(groupService.getAllMemberIdsOfGroup(group)).willReturn(memberIds);
        given(groupService.getFinalIngredients(group)).willReturn(ingredients);

        // when
        MockHttpServletRequestBuilder getRequest = get("/groups/{groupId}/ingredients/final", group.getId())
                                                    .contentType(MediaType.APPLICATION_JSON)
                                                    .header("X-Token", user.getToken());

        // then
        mockMvc.perform(getRequest)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].id", is(apple.getId().intValue())))
            .andExpect(jsonPath("$[0].name", is(apple.getName())));

        verify(groupService, times(1)).changeGroupState(group.getId(), GroupState.FINAL);
    }

    @Test
    public void testGetFinalIngredientsOfGroupById_valid_multipleIngredients() throws Exception {
        // given
        group.setGroupState(GroupState.FINAL);
        user.setVotingStatus(UserVotingStatus.VOTED);

        Ingredient apple = new Ingredient("apple");
        Ingredient pear = new Ingredient("pear");
        Ingredient banana = new Ingredient("banana");

        Set<Ingredient> ingredients = new HashSet<>();
        ingredients.add(apple);
        ingredients.add(pear);
        ingredients.add(banana);

        List<Long> memberIds = new ArrayList<>();
        memberIds.add(group.getHostId());

        // mocks
        given(groupService.getAllMemberIdsOfGroup(group)).willReturn(memberIds);
        given(groupService.getFinalIngredients(group)).willReturn(ingredients);

        // when
        MockHttpServletRequestBuilder getRequest = get("/groups/{groupId}/ingredients/final", group.getId())
                                                    .contentType(MediaType.APPLICATION_JSON)
                                                    .header("X-Token", user.getToken());

        // then
        mockMvc.perform(getRequest)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(3)));

        verify(groupService, times(0)).changeGroupState(any(), any());
    }

    @Test
    public void testGetFinalIngredientsOfGroupById_notYetVoted() throws Exception {
        // given
        user.setVotingStatus(UserVotingStatus.NOT_VOTED);

        List<Long> memberIds = new ArrayList<>();
        memberIds.add(group.getHostId());

        // mocks
        given(groupService.getAllMemberIdsOfGroup(group)).willReturn(memberIds);

        // when
        MockHttpServletRequestBuilder getRequest = get("/groups/{groupId}/ingredients/final", group.getId())
                                                    .contentType(MediaType.APPLICATION_JSON)
                                                    .header("X-Token", user.getToken());

        // then
        mockMvc.perform(getRequest)
            .andExpect(status().isAccepted());
    }

    @Test
    public void testGetFinalIngredientsOfGroupById_groupNotFound() throws Exception {
        // given
        Long anotherGroupId = 17L;

        // mocks
        given(groupService.getGroupById(anotherGroupId)).willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "error message"));

        // when
        MockHttpServletRequestBuilder getRequest = get("/groups/{groupId}/ingredients/final", anotherGroupId)
                                                    .contentType(MediaType.APPLICATION_JSON)
                                                    .header("X-Token", user.getToken());

        // then
        mockMvc.perform(getRequest)
            .andExpect(status().isNotFound());
    }

    @Test
    public void testGetFinalIngredientsOfGroupById_notValidToken() throws Exception {
        // given
        String anotherToken = "anotherToken";
        Long anotherUserId = 7L;

        List<Long> memberIds = new ArrayList<>();
        memberIds.add(group.getHostId());

        // mocks
        given(groupService.getAllMemberIdsOfGroup(group)).willReturn(memberIds);
        given(userService.getUseridByToken(anotherToken)).willReturn(anotherUserId);

        // when
        MockHttpServletRequestBuilder getRequest = get("/groups/{groupId}/ingredients/final", group.getId())
                                                    .contentType(MediaType.APPLICATION_JSON)
                                                    .header("X-Token", anotherToken);

        // then
        mockMvc.perform(getRequest)
            .andExpect(status().isUnauthorized());
    }

    @Test
    public void testUpdateRatings_success() throws Exception {
        // given
        Map<Long, String> ingredientRatings = new HashMap<>();
        ingredientRatings.put(1L, "1");
        ingredientRatings.put(2L, "-1");

        String requestBody = asJsonString(ingredientRatings);

        group.setVotingType(VotingType.MAJORITYVOTE);
        List<Long> memberIds = new ArrayList<>();
        memberIds.add(group.getHostId());

        // mocks
        given(groupService.getAllMemberIdsOfGroup(group)).willReturn(memberIds);

        // when
        MockHttpServletRequestBuilder putRequest = put("/groups/{groupId}/ratings/{userId}", group.getId(), user.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-Token", user.getToken())
                .content(requestBody);

        mockMvc.perform(putRequest)
                .andExpect(status().isNoContent());

        // then
        verify(userService, times(1)).updateIngredientRatings(group.getId(), user.getId(), ingredientRatings);
        verify(groupService, times(1)).calculateRatingPerGroup(group.getId());
    }

    @Test
    public void testUpdateRatings_groupNotFound() throws Exception {
        // given
        Long nonExistingGroupId = 99L;
        Map<Long, String> ingredientRatings = new HashMap<>();
        ingredientRatings.put(1L, "1");

        String requestBody = asJsonString(ingredientRatings);

        // mocks
        given(groupService.getGroupById(nonExistingGroupId)).willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));

        // when
        MockHttpServletRequestBuilder putRequest = put("/groups/{groupId}/ratings/{userId}", nonExistingGroupId, user.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-Token", user.getToken())
                .content(requestBody);

        // then
        mockMvc.perform(putRequest)
                .andExpect(status().isNotFound());
    }

    @Test
    public void testUpdateRatings_notAuthorized() throws Exception {
        // given
        Long anotherUserId = 7L;
        List<Long> memberIds = new ArrayList<>();
        memberIds.add(group.getHostId());
        Map<Long, String> ingredientRatings = new HashMap<>();
        ingredientRatings.put(1L, "-1");

        String requestBody = asJsonString(ingredientRatings);

        // mocks
        given(groupService.getAllMemberIdsOfGroup(group)).willReturn(memberIds);
        given(userService.getUseridByToken("anotherToken")).willReturn(anotherUserId);

        // when
        MockHttpServletRequestBuilder putRequest = put("/groups/{groupId}/ratings/{userId}", group.getId(), user.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-Token", "anotherToken")
                .content(requestBody);

        // then
        mockMvc.perform(putRequest)
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testGetGroupGuestsById_valid() throws Exception {
        User guest = new User();
        guest.setId(6L);

        List<Long> guestIds = Arrays.asList(guest.getId());

        // mocks
        given(groupService.getGroupById(group.getId())).willReturn(group);
        given(groupService.getAllGuestIdsOfGroup(group)).willReturn(guestIds);
        given(userService.getUserById(guest.getId())).willReturn(guest);

        // when
        MockHttpServletRequestBuilder getRequest = get("/groups/{groupId}/guests", group.getId())
                                                    .contentType(MediaType.APPLICATION_JSON)
                                                    .header("X-Token", user.getToken());

        // then
        mockMvc.perform(getRequest)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].id", is(guest.getId().intValue())));
            
        // verifies
        verify(groupService, times(1)).getGroupById(group.getId());
        verify(userService, times(1)).getUseridByToken(user.getToken());
        verify(groupService, times(1)).getAllGuestIdsOfGroup(group);
        verify(userService, times(1)).getUserById(guest.getId());
    }

    @Test
    public void testGetGroupGuestsById_noGuestsInGroup() throws Exception {
         // mocks
        given(groupService.getGroupById(group.getId())).willReturn(group);
        given(groupService.getAllGuestIdsOfGroup(group)).willReturn(new ArrayList<>());

        // when
        MockHttpServletRequestBuilder getRequest = get("/groups/{groupId}/guests", group.getId())
                                                    .contentType(MediaType.APPLICATION_JSON)
                                                    .header("X-Token", user.getToken());

        // then
        mockMvc.perform(getRequest)
            .andExpect(status().isNoContent());
          
        // verifies
        verify(groupService, times(1)).getGroupById(group.getId());
        verify(userService, times(1)).getUseridByToken(user.getToken());
        verify(groupService, times(1)).getAllGuestIdsOfGroup(group);
        verify(userService, times(0)).getUserById(any());
    }

    @Test
    public void testGetGroupGuestsById_groupNotFound() throws Exception {
        Long anotherGroupId = 8L;

        // mocks
        given(groupService.getGroupById(anotherGroupId)).willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "error message"));

        // when
        MockHttpServletRequestBuilder getRequest = get("/groups/{groupId}/guests", anotherGroupId)
                                                    .contentType(MediaType.APPLICATION_JSON)
                                                    .header("X-Token", user.getToken());

        // then
        mockMvc.perform(getRequest).andExpect(status().isNotFound());
            
        // verifies
        verify(groupService, times(1)).getGroupById(anotherGroupId);
        verify(userService, times(0)).getUseridByToken(any());
        verify(groupService, times(0)).getAllGuestIdsOfGroup(any());
        verify(userService, times(0)).getUserById(any());
    }

    @Test
    public void testGetGroupGuestsById_notValidToken() throws Exception {
        String anotherToken = "anotherToken";

        // mocks
        given(userService.getUseridByToken(anotherToken)).willReturn(0L);

        // when
        MockHttpServletRequestBuilder getRequest = get("/groups/{groupId}/guests", group.getId())
                                                    .contentType(MediaType.APPLICATION_JSON)
                                                    .header("X-Token", anotherToken);

        // then
        mockMvc.perform(getRequest).andExpect(status().isUnauthorized());
            
        // verifies
        verify(groupService, times(1)).getGroupById(group.getId());
        verify(userService, times(1)).getUseridByToken(anotherToken);
        verify(groupService, times(0)).getAllGuestIdsOfGroup(any());
        verify(userService, times(0)).getUserById(any());
    }

    //Helper Method to convert DTOs into a JSON strings
    private String asJsonString(final Object object) {
        try {
            return new ObjectMapper().writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
            String.format("The request body could not be created.%s", e.toString()));
        }
    }

    @Test
    public void testDeleteGroup204() throws Exception {
        mockMvc.perform(delete("/groups/{groupId}", group.getId())
                        .header("X-Token", user.getToken()))
                .andExpect(status().isNoContent());

        verify(groupService).deleteGroup(group.getId());
    }

    @Test
    public void testDeleteGroup400() throws Exception {
        group.setGroupState(GroupState.INGREDIENTENTERING);

        mockMvc.perform(delete("/groups/{groupId}", group.getId())
                        .header("X-Token", user.getToken()))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testDeleteGroup404() throws Exception {
        ResponseStatusException exception = new ResponseStatusException(HttpStatus.NOT_FOUND, "Group not found");
        given(groupService.getGroupById(group.getId())).willThrow(exception);

        mockMvc.perform(delete("/groups/{groupId}", group.getId())
                        .header("X-Token", user.getToken()))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testDeleteGroup401() throws Exception {
        // Prepare a different user
        User anotherUser = new User();
        anotherUser.setId(3L);
        anotherUser.setToken("anothertoken");

        // Set up mock
        given(userService.getUseridByToken(anotherUser.getToken())).willReturn(anotherUser.getId());
        given(userService.getUserById(anotherUser.getId())).willReturn(anotherUser);

        mockMvc.perform(delete("/groups/{groupId}", group.getId())
                        .header("X-Token", anotherUser.getToken()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void leaveGroup_guestSuccessfullyLeaves_204() throws Exception {
        Long groupId = 1L;
        Long hostId = 2L;
        Long guestId = 3L;

        Group group = new Group();
        group.setId(groupId);
        group.setHostId(hostId);

        User guest = new User();
        guest.setId(guestId);
        guest.setGroupId(groupId);

        when(groupService.getGroupById(groupId)).thenReturn(group);
        when(userService.getUseridByToken("testtoken")).thenReturn(guestId);
        when(userService.getUserById(guestId)).thenReturn(guest);
        when(groupService.getAllMemberIdsOfGroup(group)).thenReturn(Arrays.asList(hostId, guestId));

        mockMvc.perform(put("/groups/{groupId}/leave", groupId)
                        .header("X-Token", "testtoken"))
                .andExpect(status().isNoContent());

        // Verify that userService.leaveGroup was called with the correct guestId
        verify(userService).leaveGroup(guestId);
    }

    @Test
    public void leaveGroup_groupInWrongState_400() throws Exception {
        group.setGroupState(GroupState.INGREDIENTENTERING);

        mockMvc.perform(put("/groups/{groupId}/leave", group.getId())
                        .header("X-Token", user.getToken()))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void leaveGroup_guestNotInGroup_409() throws Exception {
        Long groupId = 1L;
        Long nonMemberId = 3L;

        Group group = new Group();
        group.setId(groupId);
        group.setHostId(2L);

        User user = new User();
        user.setId(nonMemberId);

        when(groupService.getGroupById(groupId)).thenReturn(group);
        when(userService.getUseridByToken("testtoken")).thenReturn(nonMemberId);
        when(userService.getUserById(nonMemberId)).thenReturn(user);
        when(groupService.getAllMemberIdsOfGroup(group)).thenReturn(Arrays.asList(2L, 4L, 5L));

        mockMvc.perform(put("/groups/{groupId}/leave", groupId)
                        .header("X-Token", "testtoken"))
                .andExpect(status().isConflict())
                .andExpect(status().reason("Guest was not in the group"));
    }

    @Test
    public void leaveGroup_hostCannotLeave() throws Exception {
        Long groupId = 1L;
        Long hostId = 2L;

        Group group = new Group();
        group.setId(groupId);
        group.setHostId(hostId);

        User user = new User();
        user.setId(hostId);
        user.setGroupId(groupId);

        when(groupService.getGroupById(groupId)).thenReturn(group);
        when(userService.getUseridByToken("testtoken")).thenReturn(hostId);
        when(userService.getUserById(hostId)).thenReturn(user);
        when(groupService.getAllMemberIdsOfGroup(group)).thenReturn(Arrays.asList(hostId)); // Update the list of memberIds to include only the host

        mockMvc.perform(put("/groups/{groupId}/leave", groupId)
                        .header("X-Token", "testtoken"))
                .andExpect(status().isConflict())
                .andExpect(status().reason("You are the host. If you want to leave, delete the group"));
    }

    @Test
    public void leaveGroup_groupNotFound_404() throws Exception {
        Long groupId = 1L;

        when(groupService.getGroupById(groupId)).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Group not found"));

        mockMvc.perform(put("/groups/{groupId}/leave", groupId)
                        .header("X-Token", "testtoken"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void leaveGroup_unauthorized_401() throws Exception {
        Long groupId = 1L;

        Group group = new Group();
        group.setId(groupId);
        group.setHostId(2L);

        when(groupService.getGroupById(groupId)).thenReturn(group);
        when(userService.getUseridByToken("testtoken")).thenReturn(0L);

        mockMvc.perform(put("/groups/{groupId}/leave", groupId)
                        .header("X-Token", "testtoken"))
                .andExpect(status().isUnauthorized())
                .andExpect(status().reason("Not authorized"));
    }

    @Test
    void testSendRequestToJoinGroupReturns201() throws Exception {
        // Arrange
        Long groupId = 1L;
        Long guestId = 2L;
        JoinRequestPostDTO joinRequestPostDTO = new JoinRequestPostDTO();
        joinRequestPostDTO.setGuestId(guestId);
        String token = "valid-token";
        String url = "/groups/" + groupId + "/requests";
        given(groupService.getGroupById(groupId)).willReturn(new Group());
        given(userService.getUserById(guestId)).willReturn(new User());
        given(userService.getUseridByToken(token)).willReturn(guestId);
        given(joinRequestService.createJoinRequest(joinRequestPostDTO, groupId)).willReturn(new JoinRequest());

        // Act
        MvcResult mvcResult = mockMvc.perform(post(url)
                        .header("X-Token", token)
                        .content(new ObjectMapper().writeValueAsString(joinRequestPostDTO))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andReturn();

        // Assert
        assertEquals(HttpStatus.CREATED.value(), mvcResult.getResponse().getStatus());
    }

    @Test
    public void sendRequestToJoinGroup_forbidden_403() throws Exception {
        // Prepare
        String groupId = "1";
        String guestId = "2";

        String joinRequestPostDTOJson = String.format("{\"guestId\": %s}", guestId);

        when(userService.isUserInGroup(Long.parseLong(guestId))).thenReturn(true);

        // Execute & Assert
        mockMvc.perform(post("/groups/{groupId}/requests", groupId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(joinRequestPostDTOJson)
                        .header("X-Token", guestId))
                .andExpect(status().isForbidden());
    }

    @Test
    public void sendRequestToJoinGroup_unauthorized_401() throws Exception {
        // Prepare
        String groupId = "1";
        String guestId = "2";
        String invalidToken = "3";

        String joinRequestPostDTOJson = String.format("{\"guestId\": %s}", guestId);

        when(groupService.getGroupById(anyLong())).thenReturn(new Group());
        when(userService.getUserById(anyLong())).thenReturn(new User());
        when(userService.getUseridByToken(invalidToken)).thenReturn(Long.parseLong(invalidToken));

        // Execute & Assert
        mockMvc.perform(post("/groups/{groupId}/requests", groupId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(joinRequestPostDTOJson)
                        .header("X-Token", invalidToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void sendRequestToJoinGroup_conflict_409() throws Exception {
        // Prepare
        String groupId = "1";
        String guestId = "2";

        String joinRequestPostDTOJson = String.format("{\"guestId\": %s}", guestId);

        doThrow(new ResponseStatusException(HttpStatus.CONFLICT))
                .when(joinRequestService)
                .createJoinRequest(any(), anyLong());

        // Add this line to mock the getUseridByToken method to return the correct guestId
        when(userService.getUseridByToken("2")).thenReturn(Long.parseLong(guestId));

        // Execute & Assert
        mockMvc.perform(post("/groups/{groupId}/requests", groupId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(joinRequestPostDTOJson)
                        .header("X-Token", guestId))
                .andExpect(status().isConflict());
    }

    @Test
    public void sendRequestToJoinGroup_notFound_404() throws Exception {
        // Prepare
        String groupId = "1";
        String guestId = "2";

        String joinRequestPostDTOJson = String.format("{\"guestId\": %s}", guestId);

        // Mock groupService.getGroupById to throw a ResponseStatusException with 404 status
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND))
                .when(groupService)
                .getGroupById(anyLong());

        when(userService.getUseridByToken("2")).thenReturn(Long.parseLong(guestId));

        // Execute & Assert
        mockMvc.perform(post("/groups/{groupId}/requests", groupId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(joinRequestPostDTOJson)
                        .header("X-Token", guestId))
                .andExpect(status().isNotFound());
    }

    @Test
    public void acceptJoinRequest_204() throws Exception {
        // Prepare
        Long groupId = 1L;
        Long hostId = 1L;
        Long guestId = 2L;
        Group group = new Group();
        group.setHostId(hostId);
        JoinRequestPutDTO joinRequestPutDTO = new JoinRequestPutDTO();
        joinRequestPutDTO.setGuestId(guestId);

        when(groupService.getGroupById(groupId)).thenReturn(group);
        when(userService.getUserById(guestId)).thenReturn(new User());
        when(userService.getUseridByToken("valid-token")).thenReturn(hostId);
        doNothing().when(joinRequestService).acceptJoinRequest(groupId, guestId);
        doNothing().when(joinRequestService).deleteOtherJoinRequests(guestId, groupId);

        // Execute & Assert
        mockMvc.perform(put("/groups/{groupId}/requests/accept", groupId)
                        .header("X-Token", "valid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(joinRequestPutDTO)))
                .andExpect(status().isNoContent());
    }

    @Test
    public void acceptJoinRequest_404() throws Exception {
        // Prepare
        Long groupId = 1L;
        Long guestId = 2L;
        JoinRequestPutDTO joinRequestPutDTO = new JoinRequestPutDTO();
        joinRequestPutDTO.setGuestId(guestId);

        when(groupService.getGroupById(groupId)).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));

        // Execute & Assert
        mockMvc.perform(put("/groups/{groupId}/requests/accept", groupId)
                        .header("X-Token", "valid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(joinRequestPutDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void acceptJoinRequest_401() throws Exception {
        // Prepare
        Long groupId = 1L;
        Long hostId = 1L;
        Long guestId = 2L;
        Group group = new Group();
        group.setHostId(hostId);
        JoinRequestPutDTO joinRequestPutDTO = new JoinRequestPutDTO();
        joinRequestPutDTO.setGuestId(guestId);

        when(groupService.getGroupById(groupId)).thenReturn(group);
        when(userService.getUserById(guestId)).thenReturn(new User());
        when(userService.getUseridByToken("invalid-token")).thenReturn(2L);

        // Execute & Assert
        mockMvc.perform(put("/groups/{groupId}/requests/accept", groupId)
                        .header("X-Token", "invalid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(joinRequestPutDTO)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void rejectJoinRequest_204() throws Exception {
        // Prepare
        Long groupId = 1L;
        Long hostId = 1L;
        Long guestId = 2L;
        Group group = new Group();
        group.setHostId(hostId);
        JoinRequest joinRequest = new JoinRequest();
        JoinRequestPutDTO joinRequestPutDTO = new JoinRequestPutDTO();
        joinRequestPutDTO.setGuestId(guestId);

        when(groupService.getGroupById(groupId)).thenReturn(group);
        when(userService.getUserById(guestId)).thenReturn(new User());
        when(userService.getUseridByToken("valid-token")).thenReturn(hostId);
        when(joinRequestService.getJoinRequestByGuestIdAndGroupId(guestId, groupId)).thenReturn(joinRequest);
        doNothing().when(joinRequestService).rejectJoinRequest(joinRequest);

        // Execute & Assert
        mockMvc.perform(put("/groups/{groupId}/requests/reject", groupId)
                        .header("X-Token", "valid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(joinRequestPutDTO)))
                .andExpect(status().isNoContent());
    }

    @Test
    public void rejectJoinRequest_404() throws Exception {
        // Prepare
        Long groupId = 1L;
        Long guestId = 2L;
        JoinRequestPutDTO joinRequestPutDTO = new JoinRequestPutDTO();
        joinRequestPutDTO.setGuestId(guestId);

        when(groupService.getGroupById(anyLong())).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));

        // Execute & Assert
        mockMvc.perform(put("/groups/{groupId}/requests/reject", groupId)
                        .header("X-Token", "valid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(joinRequestPutDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void rejectJoinRequest_401() throws Exception {
        // Prepare
        Long groupId = 1L;
        Long hostId = 1L;
        Long guestId = 2L;
        JoinRequestPutDTO joinRequestPutDTO = new JoinRequestPutDTO();
        joinRequestPutDTO.setGuestId(guestId);
        Group group = new Group();
        group.setHostId(hostId);

        when(groupService.getGroupById(groupId)).thenReturn(group);
        when(userService.getUserById(guestId)).thenReturn(new User());
        when(userService.getUseridByToken("invalid-token")).thenReturn(2L);

        // Execute & Assert
        mockMvc.perform(put("/groups/{groupId}/requests/reject", groupId)
                        .header("X-Token", "invalid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(joinRequestPutDTO)))
                .andExpect(status().isUnauthorized());
    }
    
    @Test
    void getOpenJoinRequests_returns200_whenOpenRequestsPresent() throws Exception {
        // Arrange
        JoinRequest joinRequest = new JoinRequest();
        joinRequest.setGuestId(3L);
        joinRequest.setGroupId(group.getId());

        List<JoinRequest> joinRequestList = Collections.singletonList(joinRequest);
        given(joinRequestService.getOpenJoinRequestsByGroupId(group.getId())).willReturn(joinRequestList);

        // Act
        mockMvc.perform(get("/groups/{groupId}/requests", group.getId())
                        .header("X-Token", user.getToken()))
                // Assert
                .andExpect(status().isOk());
    }
    
    @Test
    void getOpenJoinRequests_returns204_whenNoOpenRequests() throws Exception {
        // Arrange
        given(joinRequestService.getOpenJoinRequestsByGroupId(group.getId())).willReturn(Collections.emptyList());

        // Act
        mockMvc.perform(get("/groups/{groupId}/requests", group.getId())
                        .header("X-Token", user.getToken()))
                // Assert
                .andExpect(status().isNoContent());
    }
    
    @Test
    void getOpenJoinRequests_returns404_whenGroupNotFound() throws Exception {
        // Arrange
        Long nonExistentGroupId = 99L;
        given(groupService.getGroupById(nonExistentGroupId)).willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Group not found"));

        // Act
        mockMvc.perform(get("/groups/{groupId}/requests", nonExistentGroupId)
                        .header("X-Token", user.getToken()))
                // Assert
                .andExpect(status().isNotFound());
    }
    
    @Test
    void getOpenJoinRequests_returns401_whenUserNotHost() throws Exception {
        // Arrange
        User nonHostUser = new User();
        nonHostUser.setId(3L);
        nonHostUser.setToken("nonhosttoken");

        given(userService.getUseridByToken(nonHostUser.getToken())).willReturn(nonHostUser.getId());

        // Act
        mockMvc.perform(get("/groups/{groupId}/requests", group.getId())
                        .header("X-Token", nonHostUser.getToken()))
                // Assert
                .andExpect(status().isUnauthorized());
    }
    
    @Test
    public void testGetGroupStateNotFound() throws Exception {
        given(groupService.getGroupById(anyLong())).willReturn(null);

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("X-Token")).thenReturn(user.getToken());

        mockMvc.perform(MockMvcRequestBuilders.get("/groups/1/state")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Token", user.getToken()))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testGetGroupStateUnauthorized() throws Exception {
        given(userService.getUseridByToken(anyString())).willReturn(user.getId() + 1L);

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("X-Token")).thenReturn(user.getToken());

        mockMvc.perform(MockMvcRequestBuilders.get("/groups/1/state")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Token", user.getToken()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testGetGroupStateOk() throws Exception {
        given(groupService.getAllMemberIdsOfGroup(group)).willReturn(Collections.singletonList(user.getId()));

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("X-Token")).thenReturn(user.getToken());

        mockMvc.perform(MockMvcRequestBuilders.get("/groups/1/state")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Token", user.getToken()))
                .andExpect(status().isOk());
    }
    
    @Test
    public void changeGroupState_GroupNotFound_404() throws Exception {
        Long nonExistingGroupId = 99L;
        given(groupService.getGroupById(nonExistingGroupId)).willReturn(null);

        mockMvc.perform(put("/groups/{groupId}/state", nonExistingGroupId)
                        .header("X-Token", user.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(GroupState.GROUPFORMING)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void changeGroupState_NotAuthorized_401() throws Exception {
        User anotherUser = new User();
        anotherUser.setId(3L);
        anotherUser.setToken("anothertoken");
        given(userService.getUseridByToken(anotherUser.getToken())).willReturn(anotherUser.getId());

        mockMvc.perform(put("/groups/{groupId}/state", group.getId())
                        .header("X-Token", anotherUser.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(GroupState.GROUPFORMING)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void changeGroupState_Success_200() throws Exception {
        mockMvc.perform(put("/groups/{groupId}/state", group.getId())
                        .header("X-Token", user.getToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(GroupState.GROUPFORMING)))
                .andExpect(status().isNoContent());

        verify(groupService, times(1)).changeGroupState(group.getId(), GroupState.GROUPFORMING);
    }

    @Test
    public void getGroupMemberAllergiesById_returnsAllergies() throws Exception {
        // given
        String allergy = "Peanut";
        Set<String> allergies = Collections.singleton(allergy);

        // mocks
        given(groupService.getAllMemberIdsOfGroup(group)).willReturn(Arrays.asList(group.getHostId()));
        given(groupService.getGroupMemberAllergies(group)).willReturn(allergies);

        // when
        MockHttpServletRequestBuilder getRequest = get("/groups/{groupId}/members/allergies", group.getId())
                                                    .contentType(MediaType.APPLICATION_JSON)
                                                    .header("X-Token", user.getToken());

        // then
        mockMvc.perform(getRequest)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0]", is(allergy)));
    }

    @Test
    public void getGroupMemberAllergiesById_noAllergies() throws Exception {
        // mocks
        given(groupService.getAllMemberIdsOfGroup(group)).willReturn(Arrays.asList(group.getHostId()));
        given(groupService.getGroupMemberAllergies(group)).willReturn(new HashSet<>());

        // when
        MockHttpServletRequestBuilder getRequest = get("/groups/{groupId}/members/allergies", group.getId())
                                                    .contentType(MediaType.APPLICATION_JSON)
                                                    .header("X-Token", user.getToken());

        // then
        mockMvc.perform(getRequest)
            .andExpect(status().isNoContent());
    }

    @Test
    public void getGroupMemberAllergiesById_groupNotFound() throws Exception {
        Long anotherGroupId = 6L;

        // mocks
        given(groupService.getGroupById(anotherGroupId)).willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "error message"));

        // when
        MockHttpServletRequestBuilder getRequest = get("/groups/{groupId}/members/allergies", anotherGroupId)
                                                    .contentType(MediaType.APPLICATION_JSON)
                                                    .header("X-Token", user.getToken());

        // then
        mockMvc.perform(getRequest)
            .andExpect(status().isNotFound());
    }

    @Test
    public void getGroupMemberAllergiesById_notValidToken() throws Exception {
        String anotherToken = "another Token";

        // mocks
        given(userService.getUseridByToken(anotherToken)).willReturn(6L);
        given(groupService.getAllMemberIdsOfGroup(group)).willReturn(Arrays.asList(group.getHostId()));
        // given(groupService.getGroupMemberAllergies(group)).willReturn(new HashSet<>());

        // when
        MockHttpServletRequestBuilder getRequest = get("/groups/{groupId}/members/allergies", group.getId())
                                                    .contentType(MediaType.APPLICATION_JSON)
                                                    .header("X-Token", anotherToken);

        // then
        mockMvc.perform(getRequest)
            .andExpect(status().isUnauthorized());
    }

}
