package ch.uzh.ifi.hase.soprafs23.controller;

import ch.uzh.ifi.hase.soprafs23.constant.VotingType;
import ch.uzh.ifi.hase.soprafs23.entity.Group;
import ch.uzh.ifi.hase.soprafs23.entity.Invitation;
import ch.uzh.ifi.hase.soprafs23.entity.User;
import ch.uzh.ifi.hase.soprafs23.rest.dto.GroupPostDTO;
import ch.uzh.ifi.hase.soprafs23.rest.dto.InvitationPutDTO;
import ch.uzh.ifi.hase.soprafs23.service.GroupService;
import ch.uzh.ifi.hase.soprafs23.service.InvitationService;
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
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
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

    @Mock
    private final ObjectMapper objectMapper = new ObjectMapper();

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
        //HttpServletRequest request = mock(HttpServletRequest.class);

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

    //Helper Method to convert DTOs into a JSON strings
    private String asJsonString(final Object object) {
        try {
            return new ObjectMapper().writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
            String.format("The request body could not be created.%s", e.toString()));
        }
    }
    
}
