package ch.uzh.ifi.hase.soprafs23.service;

import ch.uzh.ifi.hase.soprafs23.entity.Group;
import ch.uzh.ifi.hase.soprafs23.repository.GroupRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;


@Service
@Transactional
public class GroupService {

    private final GroupRepository groupRepository;

    @Autowired
    public GroupService(@Qualifier("groupRepository") GroupRepository groupRepository) {
        this.groupRepository = groupRepository;
    }

    public List<Group> getGroups() {
        return this.groupRepository.findAll();
    }

    public Group createGroup(Group newGroup) {
        // check that the username is still free
        checkIfGroupNameExists(newGroup.getGroupName());

        // check if username is only latin letters
        if (!newGroup.getGroupName().matches("^[a-zA-Z]+$")) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "The Group Name should only contain Latin letters (a-z, A-Z)!");
        }

        // save the group
        newGroup = groupRepository.save(newGroup);
        groupRepository.flush();

        return newGroup;
    }

    private void checkIfGroupNameExists(String groupName) {
        Group groupByGroupName = groupRepository.findByGroupName(groupName);

        if (groupByGroupName != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, String.format("The Group Name %s is already taken!", groupName));
        }
    }

    public Group getGroupById(Long id) {
        Optional<Group> group = this.groupRepository.findById(id);

        if (!group.isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("Group with id %s does not exist", id));
        }

        return group.get();
    }

    // TODO: why does this exist? it doesn't flush the repo, so it wouldn't work, but it's also never used
    public Group updateGroup(Long groupId, String newGroupName) {
        Group groupToUpdate = getGroupById(groupId);
        groupToUpdate.setGroupName(newGroupName);
        return groupRepository.save(groupToUpdate);
    }

    // TODO: why does this exist? it's even tested! but only used where it's tested nowhere else
    public int countGuests(Long groupId) {
        Group group = getGroupById(groupId);
        
        return group.getGuestIds().size();
    }

    public void addGuestToGroupMembers(Long guestId, Long groupId) {
        Group group = getGroupById(groupId);
        group.addGuestId(guestId);

        group = groupRepository.save(group);
        groupRepository.flush();
    }

    public List<Long> getAllMemberIdsOfGroup(Group group) {
        List<Long> memberIds = new ArrayList<>();

        memberIds.add(group.getHostId());

        for(Long guestId : group.getGuestIds()) {
            memberIds.add(guestId);
        }

        return memberIds;
    }
}
