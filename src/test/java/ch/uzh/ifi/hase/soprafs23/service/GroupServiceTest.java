package ch.uzh.ifi.hase.soprafs23.service;

import ch.uzh.ifi.hase.soprafs23.SpooncularAPI.Recipe;
import ch.uzh.ifi.hase.soprafs23.constant.GroupState;
import ch.uzh.ifi.hase.soprafs23.constant.VotingType;
import ch.uzh.ifi.hase.soprafs23.entity.Group;
import ch.uzh.ifi.hase.soprafs23.entity.User;
import ch.uzh.ifi.hase.soprafs23.entity.Ingredient;
import ch.uzh.ifi.hase.soprafs23.repository.GroupRepository;
import ch.uzh.ifi.hase.soprafs23.repository.IngredientRepository;
import ch.uzh.ifi.hase.soprafs23.repository.RecipeRepository;
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
import java.util.Collections;
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
    private JoinRequestService joinRequestService;

    @Mock
    private InvitationService invitationService;

    @Mock
    private RecipeRepository recipeRepository;

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
        when(groupRepository.findById(group.getId())).thenReturn(Optional.of(group));
    }

    @Test
    public void test_getGroups() {
        when(groupRepository.findAll()).thenReturn(Collections.singletonList(group));

        assertEquals(Collections.singletonList(group), groupService.getGroups());
    }

    @Test
    public void getGroupById_test() {
        // mocks the findById(Long id) method of GroupRepository
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
    public void createGroup_disallowedName() {
        Group newGroup = new Group();
        newGroup.setGroupName("name123");

        assertThrows(ResponseStatusException.class, () -> {
            groupService.createGroup(newGroup);
        });

        verify(groupRepository, times(0)).save(any());
        verify(groupRepository, times(0)).flush();
    }

    @Test
    public void createGroup_nameTaken() {
        Group newGroup = new Group();
        newGroup.setGroupName("TestGroup");

        when(groupRepository.findByGroupName(any())).thenReturn(new Group());

        assertThrows(ResponseStatusException.class, () -> {
            groupService.createGroup(newGroup);
        });

        verify(groupRepository, times(0)).save(any());
        verify(groupRepository, times(0)).flush();
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

        Set<Ingredient> expectedGoodIngredients = new HashSet<>();
        expectedGoodIngredients.add(carrot);
        expectedGoodIngredients.add(potato);


        assertEquals(expectedGoodIngredients, groupService.getFinalIngredients(group));
    }

    @Test
    public void getBadIngredients_test() {
        Ingredient onion = new Ingredient("onion");
        onion.setCalculatedRating(-1);
        Ingredient meat = new Ingredient("meat");
        meat.setCalculatedRating(-1);
        Ingredient carrot = new Ingredient("carrot");
        carrot.setCalculatedRating(0);

        List<Ingredient> ingredients = new ArrayList<>();
        ingredients.add(onion);
        ingredients.add(carrot);
        ingredients.add(meat);
        group.addIngredient(ingredients);

        Set<Ingredient> expectedBadIngredients = new HashSet<>();
        expectedBadIngredients.add(onion);
        expectedBadIngredients.add(meat);

        assertEquals(expectedBadIngredients, groupService.getBadIngredients(group));
    }

    @Test
    public void removeGuestFromGroup_test() {
        Ingredient apple = new Ingredient("apple");
        apple.setId(9L);
        
        User user = new User();
        user.setId(5L);
        user.addIngredient(Collections.singletonList(apple));
        apple.setUsersSet(Collections.singleton(user));

        group.addGuestId(user.getId());
        group.addIngredient(Collections.singletonList(apple));
        apple.setGroup(group);

        // mocks
        when(userService.getUserById(user.getId())).thenReturn(user);
        when(ingredientRepository.findById(apple.getId())).thenReturn(Optional.of(apple));

        groupService.removeGuestFromGroup(group, user.getId());

        assertEquals(null, user.getGroupId());
        assertTrue(user.getIngredients().isEmpty());
        assertTrue(group.getGuestIds().isEmpty());
        assertTrue(group.getIngredients().isEmpty());

        verify(ingredientRepository, times(1)).delete(apple);
    }

    @Test
    public void test_addGuestToMembers() {
        User guest = new User();
        guest.setId(5L);

        when(groupRepository.findById(group.getId())).thenReturn(Optional.of(group));

        groupService.addGuestToGroupMembers(guest.getId(), group.getId());

        assertEquals(Collections.singleton(guest.getId()), group.getGuestIds());
    }

    @Test
    public void test_deleteGroup() {
        Recipe recipe = new Recipe();

        when(recipeRepository.findAllByGroup(group)).thenReturn(Collections.singletonList(recipe));

        groupService.deleteGroup(group.getId());

        verify(userService, times(1)).leaveGroup(group.getHostId());
        verify(invitationService, times(1)).deleteInvitationsByGroupId(group.getId());
        verify(joinRequestService, times(1)).deleteJoinRequestsByGroupId(group.getId());
        verify(recipeRepository, times(1)).delete(recipe);
        verify(groupRepository, times(1)).delete(group);
    }

    @Test
    public void test_canUserJoinGroup() {
        group.setGroupState(GroupState.GROUPFORMING);
        assertTrue(groupService.canUserJoinGroup(group.getId()));

        group.setGroupState(GroupState.INGREDIENTENTERING);
        assertFalse(groupService.canUserJoinGroup(group.getId()));
        group.setGroupState(GroupState.INGREDIENTVOTING);
        assertFalse(groupService.canUserJoinGroup(group.getId()));
        group.setGroupState(GroupState.FINAL);
        assertFalse(groupService.canUserJoinGroup(group.getId()));
        group.setGroupState(GroupState.RECIPE);
        assertFalse(groupService.canUserJoinGroup(group.getId()));
    }

    @Test
    public void test_changeGroupState() {
        groupService.changeGroupState(group.getId(), GroupState.INGREDIENTENTERING);
        assertEquals(GroupState.INGREDIENTENTERING, group.getGroupState());

        verify(joinRequestService, times(1)).deleteJoinRequestsByGroupId(group.getId());
        verify(invitationService, times(1)).deleteInvitationsByGroupId(group.getId());
    }

    @Test
    public void test_getGroupMemberAllergies() {
        User host = new User();
        host.setId(group.getHostId());
        host.addAllergy("hostAllergy");

        User guest = new User();
        guest.setId(8L);
        guest.addAllergy("guestAllergy");
        group.addGuestId(guest.getId());

        when(userService.getAllergiesById(host.getId())).thenReturn(host.getAllergiesSet());
        when(userService.getAllergiesById(guest.getId())).thenReturn(guest.getAllergiesSet());

        Set<String> expectedAllergies = new HashSet<>();
        expectedAllergies.addAll(host.getAllergiesSet());
        expectedAllergies.addAll(guest.getAllergiesSet());

        assertEquals(expectedAllergies, groupService.getGroupMemberAllergies(group));
    }

    @Test
    public void test_getGroupByHostId() {
        when(groupRepository.findByHostId(group.getHostId())).thenReturn(group);
        assertEquals(group, groupService.getGroupByHostId(group.getHostId()));

        when(groupRepository.findByHostId(5L)).thenReturn(null);
        assertThrows(ResponseStatusException.class, () -> {
            groupService.getGroupByHostId(5L);
        });
    }

    @Test
    public void test_getGroupByUserId() {
        User user = new User();
        user.setId(15L);
        
        when(userService.getUserById(user.getId())).thenReturn(user);

        assertEquals(null, groupService.getGroupByUserId(user.getId()));

        user.setGroupId(group.getId());
        assertEquals(group, groupService.getGroupByUserId(user.getId()));
    }

}
