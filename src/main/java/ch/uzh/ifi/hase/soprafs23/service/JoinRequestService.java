package ch.uzh.ifi.hase.soprafs23.service;

import ch.uzh.ifi.hase.soprafs23.entity.Group;
import ch.uzh.ifi.hase.soprafs23.entity.JoinRequest;
import ch.uzh.ifi.hase.soprafs23.entity.User;
import ch.uzh.ifi.hase.soprafs23.repository.JoinRequestRepository;
import ch.uzh.ifi.hase.soprafs23.rest.dto.JoinRequestPostDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

/**
 * JoinRequest Service
 * This class can be seen as a "working class" for all join requests related methods.
 * It also is responsible for storing/retrieving data from the respective joinRequestRepository.
 */
@Service
@Transactional
public class JoinRequestService {
    private final JoinRequestRepository joinRequestRepository;
    private final GroupService groupService;
    private final UserService userService;

    @Autowired
    public JoinRequestService(JoinRequestRepository joinRequestRepository, GroupService groupService, UserService userService) {
        this.joinRequestRepository = joinRequestRepository;
        this.groupService = groupService;
        this.userService = userService;
    }

    public JoinRequest createJoinRequest(JoinRequestPostDTO joinRequestPostDTO, Long groupId) {
        Long guestId = joinRequestPostDTO.getGuestId();
        Optional<JoinRequest> existingJoinRequest = joinRequestRepository.findByGuestIdAndGroupId(guestId, groupId);
        if (existingJoinRequest.isPresent()) {
            throw new ResponseStatusException(HttpStatus.CREATED);
        }

        if (!groupService.canUserJoinGroup(groupId)) {
            String errorMessage = String.format("You cannot make a join request to Group %d because it is not in the GROUPFORMING state", groupId);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, errorMessage);
        }

        JoinRequest joinRequest = new JoinRequest();
        joinRequest.setGuestId(guestId);
        joinRequest.setGroupId(groupId);

        joinRequestRepository.save(joinRequest);
        return joinRequest;
    }

    public JoinRequest getJoinRequestByGuestIdAndGroupId(Long guestId, Long groupId) {
        Optional<JoinRequest> joinRequest = joinRequestRepository.findByGuestIdAndGroupId(guestId, groupId);
        if (joinRequest.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Join request not found");
        }
        return joinRequest.get();
    }

    public void acceptJoinRequest(Long groupId, Long guestId) {
        JoinRequest joinRequest = getJoinRequestByGuestIdAndGroupId(guestId,groupId);
        joinRequestRepository.delete(joinRequest);

        Group group = groupService.getGroupById(groupId);
        User guest = userService.getUserById(guestId);

        groupService.addGuestToGroupMembers(guest.getId(), group.getId());

        userService.joinGroup(guestId, groupId);
    }

    public void rejectJoinRequest(JoinRequest joinRequest) {
        joinRequestRepository.delete(joinRequest);
    }

    public void deleteOtherJoinRequests(Long guestId, Long acceptedGroupId) {
        List<JoinRequest> joinRequests = joinRequestRepository.findAllByGuestId(guestId);
        for (JoinRequest joinRequest : joinRequests) {
            if (!joinRequest.getGroupId().equals(acceptedGroupId)) {
                joinRequestRepository.delete(joinRequest);
            }
        }
    }
    
    public List<JoinRequest> getOpenJoinRequestsByGroupId(Long groupId) {
        return joinRequestRepository.findAllByGroupId(groupId);
    }

    @Transactional
    public void deleteJoinRequestsByGroupId(Long groupId) {
        joinRequestRepository.deleteAllByGroupId(groupId);
    }

    public void deleteAllJoinRequests(Long guestId) {
        List<JoinRequest> joinRequests = joinRequestRepository.findAllByGuestId(guestId);
        for (JoinRequest joinRequest : joinRequests) {
            joinRequestRepository.delete(joinRequest);
        }
    }

}
