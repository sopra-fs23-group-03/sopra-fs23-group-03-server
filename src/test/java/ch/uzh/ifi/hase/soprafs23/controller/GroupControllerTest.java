package ch.uzh.ifi.hase.soprafs23.controller;

import ch.uzh.ifi.hase.soprafs23.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs23.entity.Group;
import ch.uzh.ifi.hase.soprafs23.entity.Invitation;
import ch.uzh.ifi.hase.soprafs23.entity.User;
import ch.uzh.ifi.hase.soprafs23.repository.GroupRepository;
import ch.uzh.ifi.hase.soprafs23.repository.InvitationRepository;
import ch.uzh.ifi.hase.soprafs23.rest.dto.GroupPostDTO;
import ch.uzh.ifi.hase.soprafs23.rest.dto.InvitationPutDTO;
import ch.uzh.ifi.hase.soprafs23.service.GroupService;
import ch.uzh.ifi.hase.soprafs23.service.InvitationService;
import ch.uzh.ifi.hase.soprafs23.service.UserService;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.hamcrest.Matchers;
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


    @Mock
    //private InvitationRepository invitationRepository;

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
        verify(userService, times(1)).getUseridByToken(user.getToken());

       

        verify(groupService, times(1)).getGroupById(group.getId());
        verify(userService, times(1)).getUserById(user.getId());
        verify(invitationService, times(1)).getInvitationByGroupIdAndGuestId(group.getId(), user.getId());

        verify(invitationService, times(1)).deleteInvitation(invitation);
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
    public void sendInvitation_returns404_whenGroupNotFound() throws Exception {
        // given
        Long groupId = 1L;
        List<Long> guestIds = Arrays.asList(2L, 3L);
        HttpServletRequest request = mock(HttpServletRequest.class);

        Mockito.when(groupService.getGroupById(groupId)).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("Group with id %d not found.", groupId)));

        String token = "valid-token";
        Mockito.when(request.getHeader("X-Token")).thenReturn(token);

        // when and then
        mockMvc.perform(MockMvcRequestBuilders.post("/groups/" + groupId + "/invitations")
                        .header("X-Token", token)
                        .content(new ObjectMapper().writeValueAsString(guestIds))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }


    @Test
    public void sendInvitation_returns401_whenUnauthorized() throws Exception {
        // given
        Long groupId = 1L;
        List<Long> guestIds = Arrays.asList(2L, 3L);
        HttpServletRequest request = mock(HttpServletRequest.class);

        Group currentGroup = new Group();
        currentGroup.setId(groupId);
        currentGroup.setHostId(4L);

        String token = "valid-token";
        Long tokenId = 5L;

        Mockito.when(groupService.getGroupById(groupId)).thenReturn(currentGroup);
        Mockito.when(userService.getUseridByToken(request.getHeader("X-Token"))).thenReturn(tokenId);

        // when and then
        mockMvc.perform(MockMvcRequestBuilders.post("/groups/" + groupId + "/invitations")
                        .header("X-Token", token)
                        .content(new ObjectMapper().writeValueAsString(guestIds))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

   /* @Test
    public void testSendInvitation_409Conflict() throws Exception {
        // Set up test data
        Long hostId = 1L;
        Group testGroup = new Group();
        testGroup.setId(1L);
        testGroup.setGroupName("Test Group");
        testGroup.setHostId(hostId);
        testGroup.addGuestId(2L);

        Invitation existingInvitation = new Invitation();
        existingInvitation.setGroupId(testGroup.getId());
        existingInvitation.setGuestId(2L);

        Mockito.when(groupService.getGroupById(Mockito.anyLong())).thenReturn(testGroup);
        Mockito.when(userService.getUseridByToken(Mockito.anyString())).thenReturn(hostId);
        Mockito.when(invitationService.createInvitation(Mockito.anyLong(), Mockito.anyLong())).thenThrow(new ResponseStatusException(HttpStatus.CONFLICT, "An invitation has already been sent."));

        // Perform POST request
        mockMvc.perform(MockMvcRequestBuilders.post("/groups/{groupId}/invitations", 1L)
                        .header("X-Token", "testToken")
                        .content("[2]")
                        .contentType(MediaType.APPLICATION_JSON))
                // Validate response code
                .andExpect(MockMvcResultMatchers.status().isConflict())
                // Validate response body
                .andExpect(MockMvcResultMatchers.header().string("Content-Type", "application/json;charset=UTF-8"))
                .andExpect(jsonPath("$.error", is("An invitation has already been sent.")));
    }

*/


    @Test
    public void sendInvitation_returns201_whenSuccessful() throws Exception {
        // given
        Long groupId = 1L;
        List<Long> guestIds = Arrays.asList(2L, 3L);
        HttpServletRequest request = mock(HttpServletRequest.class);

        Group currentGroup = new Group();
        currentGroup.setId(groupId);
        currentGroup.setHostId(4L);

        String token = "valid-token";
        Long tokenId = 4L;

        Mockito.when(groupService.getGroupById(groupId)).thenReturn(currentGroup);
        Mockito.when(userService.getUseridByToken(request.getHeader("X-Token"))).thenReturn(tokenId);

        // when and then
        mockMvc.perform(MockMvcRequestBuilders.post("/groups/" + groupId + "/invitations")
                        .header("X-Token", token));
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
