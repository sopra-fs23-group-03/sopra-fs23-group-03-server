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


/**
 * Invitation Service
 * This class can be seen as a "working class" for all invitation related methods.
 * It also is responsible for storing/retrieving data from the respective invitationRepository.
 */

@Service
@Transactional
public class InvitationService {

    private final InvitationRepository invitationRepository;
    private final GroupService groupService;

    @Autowired
    public InvitationService(@Qualifier("invitationRepository") InvitationRepository invitationRepository, GroupService groupService) {
        this.invitationRepository = invitationRepository;
        this.groupService = groupService;
    }


    public Invitation createInvitation(Long groupId, Long guestId) {
        List<Invitation> existingInvites = invitationRepository.findByGroupIdAndGuestId(groupId, guestId);
        if (!existingInvites.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, String.format("Invitation already exists for the given group and guest.")); // 409-error
        }

        if (!groupService.canUserJoinGroup(groupId)) {
            String errorMessage = String.format("You cannot create an invitation for Group %d because it is not in the GROUPFORMING state", groupId);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, errorMessage);
        }

        // assign guestId and groupId to invite
        Invitation newInvite = new Invitation();
        newInvite.setGuestId(guestId);
        newInvite.setGroupId(groupId);

        // save the invite
        newInvite = invitationRepository.save(newInvite);
        invitationRepository.flush();

        return newInvite;
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

    public List<Invitation> getInvitationsByGuestId(Long guestId) {
        return invitationRepository.findByGuestId(guestId);
    }

    public void deleteInvitation(Invitation invitation) {
        invitationRepository.delete(invitation);
        invitationRepository.flush();
    }
    
    @Transactional
    public void deleteInvitationsByGroupId(Long groupId) {
        invitationRepository.deleteAllByGroupId(groupId);
    }
    
}
