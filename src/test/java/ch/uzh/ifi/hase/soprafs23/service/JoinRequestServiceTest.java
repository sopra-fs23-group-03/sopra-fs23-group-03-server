package ch.uzh.ifi.hase.soprafs23.service;

import ch.uzh.ifi.hase.soprafs23.constant.VotingType;
import ch.uzh.ifi.hase.soprafs23.entity.Group;
import ch.uzh.ifi.hase.soprafs23.entity.JoinRequest;
import ch.uzh.ifi.hase.soprafs23.entity.User;
import ch.uzh.ifi.hase.soprafs23.service.GroupService;
import ch.uzh.ifi.hase.soprafs23.service.UserService;
import ch.uzh.ifi.hase.soprafs23.rest.dto.JoinRequestPostDTO;
import ch.uzh.ifi.hase.soprafs23.repository.GroupRepository;
import ch.uzh.ifi.hase.soprafs23.repository.JoinRequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;

import static org.junit.jupiter.api.Assertions.assertEquals;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class JoinRequestServiceTest {

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private JoinRequestRepository joinRequestRepository;

    @InjectMocks
    private JoinRequestService joinRequestService;

    @InjectMocks
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

        when(joinRequestRepository.save(joinRequest)).thenReturn(joinRequest);

        // when
        JoinRequest createdJoinRequest = joinRequestService.createJoinRequest(joinRequestPostDTO, group.getId());

        // then
        assertEquals(createdJoinRequest.getGuestId(), joinRequestPostDTO.getGuestId());
        assertEquals(createdJoinRequest.getGroupId(), group.getId());
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



}
