package ch.uzh.ifi.hase.soprafs23.service;

import ch.uzh.ifi.hase.soprafs23.constant.VotingType;
import ch.uzh.ifi.hase.soprafs23.entity.Group;
import ch.uzh.ifi.hase.soprafs23.entity.JoinRequest;
import ch.uzh.ifi.hase.soprafs23.entity.User;
import ch.uzh.ifi.hase.soprafs23.rest.dto.JoinRequestPostDTO;
import ch.uzh.ifi.hase.soprafs23.repository.GroupRepository;
import ch.uzh.ifi.hase.soprafs23.repository.JoinRequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class JoinRequestServiceTest {

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private JoinRequestRepository joinRequestRepository;

    @InjectMocks
    private JoinRequestService joinRequestService;

    @Mock
    private GroupService groupService;

    @Mock
    private UserService userService;

    private Group group;
    private User guest;
    private JoinRequestPostDTO joinRequestPostDTO;
    private JoinRequest existingJoinRequest;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);

        // given
        group = new Group();
        group.setId(1L);
        group.setGroupName("firstGroupName");
        group.setHostId(2L);
        group.setVotingType(VotingType.MAJORITYVOTE);

        guest = new User();
        guest.setId(1L);

        joinRequestPostDTO = new JoinRequestPostDTO();
        joinRequestPostDTO.setGuestId(1L);

        existingJoinRequest = new JoinRequest();
        existingJoinRequest.setId(1L);
        existingJoinRequest.setGuestId(guest.getId());
        existingJoinRequest.setGroupId(group.getId());

        // mocks the save() method of GroupRepository
        when(groupRepository.save(any())).thenReturn(group);

        // mock the findByGuestIdAndGroupId() method of JoinRequestRepository
        when(joinRequestRepository.findByGuestIdAndGroupId(anyLong(), anyLong())).thenReturn(Optional.empty());

        // mocks the save() method of GroupRepository
        when(groupRepository.save(any())).thenReturn(group);
    }

    @Test
    public void testCreateJoinRequest_withValidRequest_returnsJoinRequest() {
        // given
        JoinRequest joinRequest = new JoinRequest();
        joinRequest.setGuestId(joinRequestPostDTO.getGuestId());
        joinRequest.setGroupId(group.getId());

        when(joinRequestRepository.findByGuestIdAndGroupId(joinRequestPostDTO.getGuestId(), group.getId())).thenReturn(Optional.empty());
        when(groupService.canUserJoinGroup(group.getId())).thenReturn(true);
        when(joinRequestRepository.save(any(JoinRequest.class))).thenReturn(joinRequest);

        // when
        JoinRequest createdJoinRequest = joinRequestService.createJoinRequest(joinRequestPostDTO, group.getId());

        // then
        assertEquals(createdJoinRequest.getGuestId(), joinRequestPostDTO.getGuestId());
        assertEquals(createdJoinRequest.getGroupId(), group.getId());
    }

    @Test
    public void testCreateJoinRequest_withGroupNotInGroupFormingState_throwsForbiddenException() {
        // given
        when(joinRequestRepository.findByGuestIdAndGroupId(joinRequestPostDTO.getGuestId(), group.getId())).thenReturn(Optional.empty());
        when(groupService.canUserJoinGroup(group.getId())).thenReturn(false);

        // when
        Throwable exception = assertThrows(ResponseStatusException.class, () -> joinRequestService.createJoinRequest(joinRequestPostDTO, group.getId()));

        // then
        assertEquals(HttpStatus.FORBIDDEN, ((ResponseStatusException) exception).getStatus());
        assertTrue(exception.getMessage().contains("You cannot make a join request to Group " + group.getId() + " because it is not in the GROUPFORMING state"));
    }


    @Test
    public void testCreateJoinRequest_withExistingJoinRequest_throwsConflictException() {
        // given
        joinRequestPostDTO.setGuestId(1L);

        JoinRequest existingJoinRequest = new JoinRequest();
        existingJoinRequest.setId(1L);
        existingJoinRequest.setGuestId(guest.getId());
        existingJoinRequest.setGroupId(group.getId());

        // mock the findByGuestIdAndGroupId() method of JoinRequestRepository
        when(joinRequestRepository.findByGuestIdAndGroupId(anyLong(), anyLong())).thenReturn(Optional.of(existingJoinRequest));

        // when
        try {
            joinRequestService.createJoinRequest(joinRequestPostDTO, group.getId());
        } catch (ResponseStatusException ex) {
            // then
            assertEquals(HttpStatus.CONFLICT, ex.getStatus());
            assertEquals(String.format("Guest %d already has an open request to join Group %d", joinRequestPostDTO.getGuestId(), group.getId()), ex.getReason());
        }
    }

    @Test
    public void getJoinRequestByGuestIdAndGroupId_existingJoinRequest_returnJoinRequest() {
        // given
        JoinRequest existingJoinRequest = new JoinRequest();
        existingJoinRequest.setId(1L);
        existingJoinRequest.setGroupId(group.getId());

        // mock the findByGuestIdAndGroupId() method of JoinRequestRepository
        when(joinRequestRepository.findByGuestIdAndGroupId(anyLong(), anyLong())).thenReturn(Optional.of(existingJoinRequest));

        // when
        JoinRequest result = joinRequestService.getJoinRequestByGuestIdAndGroupId(guest.getId(), group.getId());

        // then
        verify(joinRequestRepository, times(1)).findByGuestIdAndGroupId(guest.getId(), group.getId());
        assertEquals(existingJoinRequest, result);
    }

    @Test
    public void getJoinRequestByGuestIdAndGroupId_nonexistentJoinRequest_throwNotFoundException() {
        // given

        // mock the findByGuestIdAndGroupId() method of JoinRequestRepository to return an empty Optional
        when(joinRequestRepository.findByGuestIdAndGroupId(anyLong(), anyLong())).thenReturn(Optional.empty());

        // when and then
        assertThrows(ResponseStatusException.class, () -> {
            joinRequestService.getJoinRequestByGuestIdAndGroupId(guest.getId(), group.getId());
        });

        // verify that findByGuestIdAndGroupId() was called exactly once with the given arguments
        verify(joinRequestRepository, times(1)).findByGuestIdAndGroupId(guest.getId(), group.getId());
    }
    @Test
    public void testAcceptJoinRequest_validRequest_joinRequestIsDeletedAndUserAddedToGroup() {
        // Arrange
        Long guestId = 1L;
        Long groupId = 2L;

        JoinRequest joinRequest = new JoinRequest();
        joinRequest.setGuestId(guestId);
        joinRequest.setGroupId(groupId);

        Group group = new Group();
        group.setId(groupId);

        User guest = new User();
        guest.setId(guestId);

        when(joinRequestRepository.findByGuestIdAndGroupId(guestId, groupId)).thenReturn(Optional.of(joinRequest));
        when(groupService.getGroupById(groupId)).thenReturn((group));
        when(userService.getUserById(guestId)).thenReturn(guest);

        // Act
        joinRequestService.acceptJoinRequest(groupId, guestId);

        // Assert
        verify(joinRequestRepository).delete(joinRequest);
        verify(groupService).addGuestToGroup(group, guestId);
        verify(userService).joinGroup(guestId, groupId);
    }



    @Test
    public void testRejectJoinRequest_validRequest_joinRequestIsDeleted() {
        // given
        JoinRequest existingJoinRequest = new JoinRequest();
        existingJoinRequest.setId(1L);
        existingJoinRequest.setGuestId(guest.getId());
        existingJoinRequest.setGroupId(group.getId());

        // when
        joinRequestService.rejectJoinRequest(existingJoinRequest);

        // then
        verify(joinRequestRepository, times(1)).delete(existingJoinRequest);
    }

    @Test
    public void testDeleteOtherJoinRequests_existingRequests_deletesOtherJoinRequests() {
        // given
        Long acceptedGroupId = 2L;

        JoinRequest joinRequest1 = new JoinRequest();
        joinRequest1.setId(1L);
        joinRequest1.setGuestId(guest.getId());
        joinRequest1.setGroupId(3L);

        JoinRequest joinRequest2 = new JoinRequest();
        joinRequest2.setId(2L);
        joinRequest2.setGuestId(guest.getId());
        joinRequest2.setGroupId(acceptedGroupId);

        JoinRequest joinRequest3 = new JoinRequest();
        joinRequest3.setId(3L);
        joinRequest3.setGuestId(guest.getId());
        joinRequest3.setGroupId(4L);

        List<JoinRequest> joinRequests = Arrays.asList(joinRequest1, joinRequest2, joinRequest3);

        // mock the findAllByGuestId() method of JoinRequestRepository
        when(joinRequestRepository.findAllByGuestId(guest.getId())).thenReturn(joinRequests);

        // when
        joinRequestService.deleteOtherJoinRequests(guest.getId(), acceptedGroupId);

        // then
        verify(joinRequestRepository, times(1)).delete(joinRequest1);
        verify(joinRequestRepository, times(0)).delete(joinRequest2);
        verify(joinRequestRepository, times(1)).delete(joinRequest3);
    }
}
