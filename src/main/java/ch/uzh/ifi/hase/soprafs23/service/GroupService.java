package ch.uzh.ifi.hase.soprafs23.service;

import ch.uzh.ifi.hase.soprafs23.constant.GroupState;
import ch.uzh.ifi.hase.soprafs23.entity.Group;
import ch.uzh.ifi.hase.soprafs23.entity.Ingredient;
import ch.uzh.ifi.hase.soprafs23.entity.User;
import ch.uzh.ifi.hase.soprafs23.repository.GroupRepository;
import ch.uzh.ifi.hase.soprafs23.repository.IngredientRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;


@Service
@Transactional
public class GroupService {

    private final GroupRepository groupRepository;
    private final IngredientRepository ingredientRepository;
    private final JoinRequestService joinRequestService;
    private final InvitationService invitationService;
    private final UserService userService;

    @Autowired
    public GroupService(@Qualifier("groupRepository") GroupRepository groupRepository, IngredientRepository ingredientRepository,
                        @Lazy @Qualifier("userService") UserService userService,
                        @Lazy JoinRequestService joinRequestService, @Lazy InvitationService invitationService) {
        this.groupRepository = groupRepository;
        this.userService = userService;
        this.ingredientRepository = ingredientRepository;
        this.joinRequestService = joinRequestService;
        this.invitationService = invitationService;
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
        memberIds.addAll(getAllGuestIdsOfGroup(group));

        return memberIds;
    }

    public List<Long> getAllGuestIdsOfGroup(Group group) {
        List<Long> guestIds = new ArrayList<>();

        for(Long guestId : group.getGuestIds()) {
            guestIds.add(guestId);
        }

        return guestIds;
    }

    public void deleteGroup(Long groupId) {
        // Check if the group exists
        Group group = getGroupById(groupId);

        // Remove the group from each user's groupId
        List<Long> memberIds = getAllMemberIdsOfGroup(group);
        for (Long memberId : memberIds) {
            userService.leaveGroup(memberId);
        }

        // Delete the group
        groupRepository.delete(group);
    }

    @Transactional
    public void calculateRatingPerGroup(Long groupId) {
        // Fetch all ingredients related to the given groupId
        List<Ingredient> ingredients = ingredientRepository.findByGroupId(groupId);

        // Throw a 422 error if no ingredients are found
        if (ingredients.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "No ingredients found for group " + groupId);
        }

        // Loop through each ingredient and calculate the sum of singleUserRatings --> MAJORITY VOTING LOGIC
        for (Ingredient ingredient : ingredients) {
            List<String> singleUserRatings = ingredient.getSingleUserRatings();
            int sum = 0;
            for (String rating : singleUserRatings) {
                sum += Integer.parseInt(rating);
            }

            // Store the sum in the calculatedRating field for the ingredient and save to the database
            ingredient.setCalculatedRating(sum);
            ingredientRepository.save(ingredient);
        }
    }

    public Set<Ingredient> getFinalIngredients(Group group) {
        Set<Ingredient> ingredients = group.getIngredients();
        Set<Ingredient> finalIngredients = new HashSet<>();

        for (Ingredient ingredient : ingredients) {
            if (ingredient.getCalculatedRating() >= 0) {
                finalIngredients.add(ingredient);
            }
        }

        return finalIngredients;
    }

    public Group removeGuestFromGroup(Group group, Long guestId) {
        group.removeGuestId(guestId);

        removeIngredientsFromUser(group, guestId);

        group = groupRepository.save(group);
        groupRepository.flush();

        return group;
    }

    private void removeIngredientsFromUser(Group group, Long userId) {
        User user = userService.getUserById(userId);

        Set<Ingredient> ingredients = group.getIngredients();
        Set<Long> ingredientToBeDeletedIds = new HashSet<>();
        Set<Ingredient> ingredientsToBeRemoved = new HashSet<>();

        for (Ingredient ingredient : ingredients) {
            Set<User> ingredientUsers = ingredient.getUsersSet();
            if (ingredientUsers.contains(user)) {
                if (ingredientUsers.size() == 1) {
                    ingredientToBeDeletedIds.add(ingredient.getId());
                    ingredientsToBeRemoved.add(ingredient); // Don't remove it yet, just note it for removal
                }
                user.removeIngredient(ingredient);
            }
        }

        // Now it's safe to remove the ingredients from the group
        for (Ingredient ingredient : ingredientsToBeRemoved) {
            group.removeIngredient(ingredient);
        }

        for (Long id : ingredientToBeDeletedIds) {
            Optional<Ingredient> ingredient = ingredientRepository.findById(id);
            if (ingredient.isPresent()) {
                ingredientRepository.delete(ingredient.get());
            }
        }
    }


    public Group addGuestToGroup(Group group, Long guestId) {
        group.addGuestId(guestId);

        group = groupRepository.save(group);
        groupRepository.flush();

        return group;
    }

    public boolean canUserJoinGroup(Long groupId) {
        Group group = getGroupById(groupId);
        return group.getGroupState() == GroupState.GROUPFORMING;
    }

    public Group changeGroupState(Long groupId, GroupState newState) {
        Group group = getGroupById(groupId);
        group.setGroupState(newState);

        // If the new state is not GROUPFORMING, delete pending join requests and invitations
        if (newState != GroupState.GROUPFORMING) {
            joinRequestService.deleteJoinRequestsByGroupId(groupId);
            invitationService.deleteInvitationsByGroupId(groupId);
        }

        return groupRepository.save(group);
    }

    public Set<String> getGroupMemberAllergies(Group group) {
        List<Long> memberIds = getAllMemberIdsOfGroup(group);
        Set<String> allergies = new HashSet<>();

        for (Long userId : memberIds) {
            Set<String> userAllergies = userService.getAllergiesById(userId);
            for (String allergy : userAllergies) {
                String[] allergyArray = allergy.split(",");
                allergies.addAll(Arrays.asList(allergyArray));
            }
        }
        
        return allergies;
    }

    public Group getGroupByHostId(Long hostId) {
        Group group = this.groupRepository.findByHostId(hostId);

        if (group == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("Group with host id %s does not exist", hostId));
        }

        return group;
    }

}
