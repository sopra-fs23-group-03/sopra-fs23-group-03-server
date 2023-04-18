package ch.uzh.ifi.hase.soprafs23.service;

import ch.uzh.ifi.hase.soprafs23.entity.Group;
import ch.uzh.ifi.hase.soprafs23.repository.GroupRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;

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

        // mocks the save() method of GroupRepository
        Mockito.when(groupRepository.save(Mockito.any())).thenReturn(group);
    }

    @Test
    public void getGroupById_test() {
        // mocks the findById(Long id) method of GroupRepository
        Mockito.when(groupRepository.findById(group.getId())).thenReturn(Optional.of(group));
        Mockito.when(groupRepository.findById(2L)).thenReturn(Optional.empty());

        assertEquals(group, groupService.getGroupById(group.getId()));
        assertThrows(ResponseStatusException.class, () -> groupService.getGroupById(2L));
    }

    
}
