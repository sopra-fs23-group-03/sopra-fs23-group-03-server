package ch.uzh.ifi.hase.soprafs23.service;

import ch.uzh.ifi.hase.soprafs23.entity.Group;
import ch.uzh.ifi.hase.soprafs23.entity.Ingredient;
import ch.uzh.ifi.hase.soprafs23.repository.GroupRepository;
import ch.uzh.ifi.hase.soprafs23.repository.IngredientRepository;

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
    private final IngredientRepository ingredientRepository;

    @Autowired
    public GroupService(@Qualifier("groupRepository") GroupRepository groupRepository, IngredientRepository ingredientRepository) {
        this.groupRepository = groupRepository;
        this.ingredientRepository = ingredientRepository;
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

        for(Long guestId : group.getGuestIds()) {
            memberIds.add(guestId);
        }

        return memberIds;
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

}
