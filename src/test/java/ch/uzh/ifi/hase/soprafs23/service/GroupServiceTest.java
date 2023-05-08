package ch.uzh.ifi.hase.soprafs23.service;

import ch.uzh.ifi.hase.soprafs23.constant.VotingType;
import ch.uzh.ifi.hase.soprafs23.entity.Group;
import ch.uzh.ifi.hase.soprafs23.entity.User;
import ch.uzh.ifi.hase.soprafs23.entity.Ingredient;
import ch.uzh.ifi.hase.soprafs23.repository.GroupRepository;
import ch.uzh.ifi.hase.soprafs23.repository.IngredientRepository;
import javassist.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class GroupServiceTest {

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private IngredientRepository ingredientRepository;

    @InjectMocks
    private GroupService groupService;

    @Mock
    private UserService userService;

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
    public void getAllGuestIdsOfGroup_test() {
        List<Long> expectedMembers = new ArrayList<>();
        assertEquals(expectedMembers, groupService.getAllGuestIdsOfGroup(group));

        Long firstGuestId = 4L;
        group.addGuestId(firstGuestId);
        expectedMembers.add(firstGuestId);
        assertEquals(expectedMembers, groupService.getAllGuestIdsOfGroup(group));

        Long secondGuestId = 7L;
        group.addGuestId(secondGuestId);
        expectedMembers.add(secondGuestId);
        assertEquals(expectedMembers, groupService.getAllGuestIdsOfGroup(group));
    }
    
    @Test
    public void createGroup_success() {
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

    @Test
    public void testCalculateRatingPerGroup_success() {
        // given
        Ingredient ingredient1 = new Ingredient();
        ingredient1.setId(1L);
        ingredient1.setName("Ingredient 1");
        ingredient1.setGroup(group);
        List<String> singleUserRatings1 = new ArrayList<>(Arrays.asList("1", "0", "-1"));
        ingredient1.setSingleUserRatings(singleUserRatings1);

        Ingredient ingredient2 = new Ingredient();
        ingredient2.setId(2L);
        ingredient2.setName("Ingredient 2");
        ingredient1.setGroup(group);
        List<String> singleUserRatings2 = new ArrayList<>(Arrays.asList("1", "0", "0"));
        ingredient2.setSingleUserRatings(singleUserRatings2);

        List<Ingredient> ingredients = new ArrayList<>(Arrays.asList(ingredient1, ingredient2));

        when(ingredientRepository.findByGroupId(1L)).thenReturn(ingredients);

        // when
        groupService.calculateRatingPerGroup(1L);

        // then
        verify(ingredientRepository).findByGroupId(1L);

        verify(ingredientRepository).save(ingredient1);
        assertEquals(0, ingredient1.getCalculatedRating());

        verify(ingredientRepository).save(ingredient2);
        assertEquals(1, ingredient2.getCalculatedRating());
    }

    @Test
    public void testCalculateRatingPerGroup_emptyIngredients() { // gives 422 error bc no ingredients are saved for this group
        // given
        List<Ingredient> ingredients = new ArrayList<>();
        when(ingredientRepository.findByGroupId(1L)).thenReturn(ingredients);

        // when, then
        assertThrows(ResponseStatusException.class, () -> groupService.calculateRatingPerGroup(1L));
        verify(ingredientRepository).findByGroupId(1L);
    }

    @Test
    public void getFinalIngredients_test() {
        Ingredient onion = new Ingredient("onion");
        onion.setCalculatedRating(-1);
        Ingredient carrot = new Ingredient("carrot");
        carrot.setCalculatedRating(0);
        Ingredient potato = new Ingredient("potato");
        potato.setCalculatedRating(1);

        List<Ingredient> ingredients = new ArrayList<>();
        ingredients.add(onion);
        ingredients.add(carrot);
        ingredients.add(potato);
        group.addIngredient(ingredients);

        Set<Ingredient> expectedIngredients = new HashSet<>();
        expectedIngredients.add(carrot);
        expectedIngredients.add(potato);

        assertEquals(expectedIngredients, groupService.getFinalIngredients(group));
    }

}
