package ch.uzh.ifi.hase.soprafs23.service;

import ch.uzh.ifi.hase.soprafs23.entity.Invitation;
import ch.uzh.ifi.hase.soprafs23.repository.InvitationRepository;

import org.junit.jupiter.api.BeforeEach;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class InvitationServiceTest {
    
    @Mock
    private InvitationRepository invitationRepository;

    @InjectMocks
    private InvitationService invitationService;

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
        //Mockito.when(InvitationRepository.save(Mockito.any())).thenReturn(invitation);
    }

}
