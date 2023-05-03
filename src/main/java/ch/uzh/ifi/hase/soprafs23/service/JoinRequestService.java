package ch.uzh.ifi.hase.soprafs23.service;

import ch.uzh.ifi.hase.soprafs23.constant.JoinRequestStatus;
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
import java.util.Optional;

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
        // Check if there is already a pending request from the guest to join the group
        Optional<JoinRequest> existingJoinRequest = joinRequestRepository.findByGuestIdAndGroupId(joinRequestPostDTO.getGuestId(), groupId);
        if (existingJoinRequest.isPresent()) {
            JoinRequestStatus status = existingJoinRequest.get().getStatus();
            if (status == JoinRequestStatus.PENDING) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "A request could not be sent");
            } else if (status == JoinRequestStatus.ACCEPTED || status == JoinRequestStatus.REJECTED) {
                joinRequestRepository.delete(existingJoinRequest.get());
            }
        }

        // Create a new JoinRequest
        JoinRequest joinRequest = new JoinRequest();
        joinRequest.setGuestId(joinRequestPostDTO.getGuestId());
        joinRequest.setGroupId(groupId);
        joinRequest.setStatus(JoinRequestStatus.PENDING);

        // Save the JoinRequest in the repository
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

    public void acceptJoinRequest(Long groupId, Long hostId, Long guestId) {
        Optional<JoinRequest> joinRequestOptional = joinRequestRepository.findByGuestIdAndGroupId(guestId, groupId);

        if (!joinRequestOptional.isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Join request not found"); // 404 - join request not found
        }

        JoinRequest joinRequest = joinRequestOptional.get();

        joinRequest.setStatus(JoinRequestStatus.ACCEPTED);
        joinRequestRepository.save(joinRequest);

        // Add the guest to the group and update the group in the database
        Group group = groupService.getGroupById(groupId);
        User guest = userService.getUserById(guestId);

        group.addGuestId(guest.getId());
        groupService.updateGroupToRemoveGuest(group);

        // Create a link between the guest and the group in the User entity
        userService.joinGroup(guestId, groupId);
    }


    public void rejectJoinRequest(JoinRequest joinRequest) {
        if (joinRequest.getStatus() != JoinRequestStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Join request Not Found"); // 404 - Not found
        }

        joinRequest.setStatus(JoinRequestStatus.REJECTED);
        joinRequestRepository.save(joinRequest);
    }

}

