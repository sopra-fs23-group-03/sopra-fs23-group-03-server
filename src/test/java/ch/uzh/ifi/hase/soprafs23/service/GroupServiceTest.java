package ch.uzh.ifi.hase.soprafs23.service;

import ch.uzh.ifi.hase.soprafs23.constant.VotingType;
import ch.uzh.ifi.hase.soprafs23.entity.Group;
import ch.uzh.ifi.hase.soprafs23.entity.User;
import ch.uzh.ifi.hase.soprafs23.repository.GroupRepository;
import javassist.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GroupServiceTest {

    @Mock
    private GroupRepository groupRepository;

    @InjectMocks
    private GroupService groupService;

    private Group group;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);

        // given
        group = new Group();
        group.setId(1L);
        group.setGroupName("firstGroupName");
        group.setHostId(2L);
        group.setVotingType(VotingType.MAJORITYVOTE);

        // mocks the save() method of GroupRepository
        when(groupRepository.save(any())).thenReturn(group);
    }

    @Test
    public void getGroupById_test() {
        // mocks the findById(Long id) method of GroupRepository
        when(groupRepository.findById(group.getId())).thenReturn(Optional.of(group));
        when(groupRepository.findById(2L)).thenReturn(Optional.empty());

        assertEquals(group, groupService.getGroupById(group.getId()));
        assertThrows(ResponseStatusException.class, () -> groupService.getGroupById(2L));
    }

    @Test
    public void countGuests_test() throws NotFoundException {
        Group group = new Group();
        group.setId(1L);
        group.setGroupName("Test Group");
        group.setHostId(12L);
        group.addGuestId(34L);
        group.addGuestId(86L);
        groupRepository.save(group);

        when(groupRepository.findById(group.getId())).thenReturn(Optional.of(group));
        assertEquals(2, groupService.countGuests(group.getId()));

        when(groupRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(ResponseStatusException.class, () -> groupService.countGuests(2L));
    }



    @Test
    public void getAllMemberIdsOfGroup_test() {
        List<Long> expectedMembers = new ArrayList<>();
        expectedMembers.add(group.getHostId());
        assertEquals(expectedMembers, groupService.getAllMemberIdsOfGroup(group));

        Long firstGuestId = 4L;
        group.addGuestId(firstGuestId);
        expectedMembers.add(firstGuestId);
        assertEquals(expectedMembers, groupService.getAllMemberIdsOfGroup(group));

        Long secondGuestId = 7L;
        group.addGuestId(secondGuestId);
        expectedMembers.add(secondGuestId);
        assertEquals(expectedMembers, groupService.getAllMemberIdsOfGroup(group));
    }
    @Test
    void createGroup_success() {
        // Given
        Group newGroup = new Group();
        newGroup.setGroupName("TestGroup");
        User host = new User();
        host.setUsername("JohnDoe");
        when(groupRepository.save(any(Group.class))).thenReturn(newGroup);

        // When
        Group createdGroup = groupService.createGroup(newGroup);

        // Then
        assertEquals(newGroup.getGroupName(), createdGroup.getGroupName());
        assertEquals(host.getId(), createdGroup.getHostId());
        verify(groupRepository, times(1)).save(newGroup);
        verify(groupRepository, times(1)).flush();
    }
}
