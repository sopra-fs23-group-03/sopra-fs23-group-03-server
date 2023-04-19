package ch.uzh.ifi.hase.soprafs23.service;


import java.util.HashSet;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs23.entity.Invitation;
import ch.uzh.ifi.hase.soprafs23.repository.InvitationRepository;

@Service
@Transactional
public class InvitationService {

    private final InvitationRepository invitationRepository;

    @Autowired
    public InvitationService(@Qualifier("invitationRepository") InvitationRepository invitationRepository) {
        this.invitationRepository = invitationRepository;
    }

    public Invitation getInvitationByGroupIdAndGuestId(Long groupId, Long guestId) {
        List<Invitation> groupInvitations = invitationRepository.findByGroupId(groupId);
        List<Invitation> guestInvitations = invitationRepository.findByGuestId(guestId);

        HashSet<Invitation> intersection = new HashSet<>(groupInvitations);
        intersection.retainAll(new HashSet<>(guestInvitations));

        assert intersection.size() <= 1; // if this is not the case, there is duplicate invitations!!!
        
        if(intersection.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("Group with id %s has no open invitation for user with id %d", groupId, guestId));
        }

        return intersection.iterator().next();
    }

    public void deleteInvitation(Invitation invitation) {
        invitationRepository.delete(invitation);
        invitationRepository.flush();
    }
    
}
