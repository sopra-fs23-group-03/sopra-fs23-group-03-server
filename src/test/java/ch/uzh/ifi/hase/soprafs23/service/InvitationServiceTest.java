package ch.uzh.ifi.hase.soprafs23.service;

import ch.uzh.ifi.hase.soprafs23.entity.Invitation;
import ch.uzh.ifi.hase.soprafs23.repository.InvitationRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class InvitationServiceTest {
    
    @Mock
    private InvitationRepository invitationRepository;

    @InjectMocks
    private InvitationService invitationService;
    @Mock
    private GroupService groupService;

    private Invitation invitation;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);

        // given
        invitation = new Invitation();
        invitation.setId(1L);
        invitation.setGroupId(2L);
        invitation.setGuestId(3L);

        // mocks the save() method of InvitationRepository
        when(invitationRepository.save(any())).thenReturn(invitation);
    }

    @Test
    public void createInvitation_validInputs_success() {
        // given
        Long groupId = 2L;
        Long guestId = 3L;
        Invitation newInvite = new Invitation();
        newInvite.setGroupId(groupId);
        newInvite.setGuestId(guestId);
        List<Invitation> existingInvites = new ArrayList<>();

        // mocks the findByGroupIdAndGuestId() method of InvitationRepository
        when(invitationRepository.findByGroupIdAndGuestId(anyLong(), anyLong())).thenReturn(existingInvites);

        // mocks the canUserJoinGroup() method of GroupService
        when(groupService.canUserJoinGroup(anyLong())).thenReturn(true);

        // when
        Invitation createdInvite = invitationService.createInvitation(groupId, guestId);

        // then
        verify(invitationRepository, times(1)).save(any());
        verify(invitationRepository, times(1)).flush();

        assertEquals(createdInvite.getId(), invitation.getId());
        assertEquals(createdInvite.getGroupId(), invitation.getGroupId());
        assertEquals(createdInvite.getGuestId(), invitation.getGuestId());
    }

    @Test
    public void createInvitation_groupNotInGroupFormingState_forbidden() {
        // given
        Long groupId = 2L;
        Long guestId = 3L;

        // mocks the findByGroupIdAndGuestId() method of InvitationRepository
        when(invitationRepository.findByGroupIdAndGuestId(anyLong(), anyLong())).thenReturn(new ArrayList<>());

        // mocks the canUserJoinGroup() method of GroupService to return false (not in the GROUPFORMING state)
        when(groupService.canUserJoinGroup(anyLong())).thenReturn(false);

        // when
        Throwable exception = assertThrows(ResponseStatusException.class, () -> invitationService.createInvitation(groupId, guestId));

        // then
        assertEquals(HttpStatus.FORBIDDEN, ((ResponseStatusException) exception).getStatus());
        assertTrue(exception.getMessage().contains("Group"));
        assertTrue(exception.getMessage().contains("is not in the GROUPFORMING state"));
    }


    @Test
    public void createInvitation_existingInvitation_conflictException() {
        // given
        Long groupId = 2L;
        Long guestId = 3L;
        List<Invitation> existingInvites = Arrays.asList(invitation);

        // mocks the findByGroupIdAndGuestId() method of InvitationRepository
        when(invitationRepository.findByGroupIdAndGuestId(anyLong(), anyLong())).thenReturn(existingInvites);

        // when
        assertThrows(ResponseStatusException.class, () -> {
            invitationService.createInvitation(groupId, guestId);
        });

        // then
        verify(invitationRepository, never()).save(any());
        verify(invitationRepository, never()).flush();
    }

}


