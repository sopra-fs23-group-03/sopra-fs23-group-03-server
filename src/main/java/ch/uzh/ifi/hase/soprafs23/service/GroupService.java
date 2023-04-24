package ch.uzh.ifi.hase.soprafs23.service;

import ch.uzh.ifi.hase.soprafs23.entity.Group;
import ch.uzh.ifi.hase.soprafs23.entity.User;
import ch.uzh.ifi.hase.soprafs23.repository.GroupRepository;

import java.util.List;
import java.util.Optional;

import javassist.NotFoundException;
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

    // creates a new user, throws CONFLICT (409) if something goes wrong
    public Group createGroup(Group newGroup, User host) {
        // check that the username is still free
        checkIfGroupNameExists(newGroup.getGroupName());

        // check if username is only latin letters
        if (!newGroup.getGroupName().matches("^[a-zA-Z]+$")) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "The Group Name should only contain Latin letters (a-z, A-Z)!");
        }

        // assign the user creating the group as the host
        newGroup.setHostId(host.getId());

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

    public Group updateGroup(Long groupId, String newGroupName) {
        Group groupToUpdate = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Group not found"));
        groupToUpdate.setGroupName(newGroupName);
        return groupRepository.save(groupToUpdate);
    }


    public int countGuests(Long groupId) {
        Group group = groupRepository.findById(groupId).orElse(null);
        if (group == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Group not found");
        }
        return group.getGuestIds().size();
    }






}
