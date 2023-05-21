package ch.uzh.ifi.hase.soprafs23.SpooncularAPI;

import ch.uzh.ifi.hase.soprafs23.entity.FullIngredient;
import ch.uzh.ifi.hase.soprafs23.constant.GroupState;
import ch.uzh.ifi.hase.soprafs23.entity.Group;
import ch.uzh.ifi.hase.soprafs23.entity.User;
import ch.uzh.ifi.hase.soprafs23.repository.FullIngredientRepository;
import ch.uzh.ifi.hase.soprafs23.service.GroupService;
import ch.uzh.ifi.hase.soprafs23.service.UserService;
import ch.uzh.ifi.hase.soprafs23.service.RecipeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
public class APIController {

    @Autowired
    @Qualifier("spoonacularRestTemplate")
    private RestTemplate restTemplate;

    @Autowired
    private APIService apiService;

    @Autowired
    private GroupService groupService;

    @Autowired
    private UserService userService;

    private final String apiKey = "56638b96d69d409cab5a0cdf9a8a1f5d";

    @Autowired
    private FullIngredientRepository fullIngredientRepository;


    @Autowired
    private RecipeService recipeService;

    @GetMapping("/groups/{groupId}/result")
    @ResponseStatus(HttpStatus.OK) // 200
    @ResponseBody
    public ResponseEntity<List<APIGetDTO>> getGroupRecipe(@PathVariable Long groupId, HttpServletRequest request) {
        // 404 - group not found
        Group group = groupService.getGroupById(groupId);

        // 401 - not authorized
        Long tokenId = userService.getUseridByToken(request.getHeader("X-Token"));
        if (tokenId.equals(0L)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "You are not authorized.");
        }

        // 409 - groupState is not RECIPE
        GroupState groupState = group.getGroupState();
        if(!groupState.equals(GroupState.RECIPE)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "The groupState is not RECIPE"); // 409
        }

        List<APIGetDTO> apiGetDTOS = new ArrayList<>();

        // Check if there is already a recipe associated with the group
        List<Recipe> existingRecipes = recipeService.getRecipesByGroup(group);


        // If recipes exist, convert them to APIGetDTO and return
        if (!existingRecipes.isEmpty()) {
            for (Recipe recipe : existingRecipes) {
                APIGetDTO apiGetDTO = convertRecipeToAPIGetDTO(recipe);
                apiGetDTOS.add(apiGetDTO);
            }
        } else {
            // Else get new recipes from the Spoonacular API
            List<RecipeInfo> recipeInfos = apiService.getRecipe(group);

            if (recipeInfos.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No recipes found");
            }

            for (RecipeInfo recipeInfo : recipeInfos) {
                // Try to find recipe in db
                Recipe recipe = recipeService.findByExternalRecipeIdAndGroupId(recipeInfo.getId(), groupId);

                // If recipe does not exist in db, create new one
                if (recipe == null) {
                    recipe = new Recipe();
                    recipe.setExternalRecipeId(recipeInfo.getId());
                }

                recipe.setTitle(recipeInfo.getTitle());
                recipe.setUsedIngredients(recipeInfo.getUsedIngredients().stream().map(IngredientInfo::getName).collect(Collectors.toList()));
                recipe.setMissedIngredients(recipeInfo.getMissedIngredients().stream().map(IngredientInfo::getName).collect(Collectors.toList()));
                recipe.setIsRandomBasedOnIntolerances(recipeInfo.getIsRandomBasedOnIntolerances());
                recipe.setGroup(group);

                // Fetch additional details --> makes call to Spoonacular API and map response to RecipeDetailInfo object
                RecipeDetailInfo detailInfo = apiService.getRecipeDetails(recipe.getExternalRecipeId());
                if (detailInfo != null) {
                    recipe.setTitle(detailInfo.getTitle());
                    recipe.setReadyInMinutes(detailInfo.getReadyInMinutes());
                    recipe.setImage(detailInfo.getImage() != null ? detailInfo.getImage() : "Default image URL");
                    recipe.setInstructions(detailInfo.getInstructions() != null ? detailInfo.getInstructions() : "No instructions provided");

                }
                recipe.setGroup(group);

                // Save/update the recipe in db
                recipeService.save(recipe);

                APIGetDTO apiGetDTO = convertRecipeToAPIGetDTO(recipe);
                apiGetDTOS.add(apiGetDTO);
            }
        }
        return new ResponseEntity<>(apiGetDTOS, HttpStatus.OK);
    }

    //TODO: do we need our own DTOMapperAPI Class?
    private APIGetDTO convertRecipeToAPIGetDTO(Recipe recipe) {
        APIGetDTO apiGetDTO = new APIGetDTO();
        apiGetDTO.setId(recipe.getId());
        apiGetDTO.setTitle(recipe.getTitle());
        apiGetDTO.setUsedIngredients(recipe.getUsedIngredients());
        apiGetDTO.setMissedIngredients(recipe.getMissedIngredients());
        apiGetDTO.setInstructions(recipe.getInstructions());
        apiGetDTO.setImage(recipe.getImage());
        apiGetDTO.setReadyInMinutes(recipe.getReadyInMinutes());
        apiGetDTO.setGroupId(recipe.getGroup().getId());
        apiGetDTO.setIsRandomBasedOnIntolerances(recipe.getIsRandomBasedOnIntolerances());
        return apiGetDTO;
    }

    @GetMapping("/users/{userId}/solo/result")
    @ResponseStatus(HttpStatus.OK) // 200
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getSoloRecipe(@PathVariable Long userId, HttpServletRequest request) {

        // 401 - not authorized
        Long tokenId = userService.getUseridByToken(request.getHeader("X-Token"));
        if (tokenId.equals(0L)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "You are not authorized.");
        }

        User user = userService.getUserById(userId);
        Map<String, Object> recipeInfo;
        try {
            recipeInfo = apiService.getRandomRecipeUser(user);
        } catch (RestClientException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error occurred while fetching recipe from Spoonacular API: " + e.getMessage());
        }

        if (recipeInfo.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No recipe found for this user."); // 404 - not found
        }

        if (user.getRecipe() != null) {
            Recipe recipe = user.getRecipe();
            recipeInfo.put("id", recipe.getId());
            recipeInfo.put("title", recipe.getTitle());
            recipeInfo.put("readyInMinutes", recipe.getReadyInMinutes());
            recipeInfo.put("image", recipe.getImage());
            recipeInfo.put("instructions", recipe.getInstructions());
            recipeInfo.put("missedIngredients", recipe.getMissedIngredients());
        }
        return ResponseEntity.ok(recipeInfo);
    }

    @GetMapping("/ingredients")
    @ResponseStatus(HttpStatus.OK) // 200
    @ResponseBody
    public List<String> getAllIngredients(HttpServletRequest request, @RequestParam String initialString) {
        // 401 - not authorized
        Long tokenId = userService.getUseridByToken(request.getHeader("X-Token"));
        if (tokenId.equals(0L)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, String.format("You are not authorized."));
        }

        // check that user is in a group
        User user = userService.getUserById(tokenId);
        if (user.getGroupId() == null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "You need to be part of a group.");
        }

        // check that the group is at INGREDIENTENTERING state
        Group group = groupService.getGroupById(user.getGroupId());
        if(!group.getGroupState().equals(GroupState.INGREDIENTENTERING)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "The groupState is not INGREDIENTENTERING");
        }

        // Fetch ingredients from the API if not already in the database
        for (char ch : initialString.toCharArray()) {
            String apiUrl = "https://api.spoonacular.com/food/ingredients/search?apiKey=" + apiKey + "&number=1000";
            apiService.fetchAndStoreIngredients(apiUrl, String.valueOf(ch));
        }

        List<FullIngredient> fullIngredients = fullIngredientRepository.findByNameContainingIgnoreCase(initialString);
        List<String> ingredientNames = fullIngredients.stream().map(FullIngredient::getName).collect(Collectors.toList());

        if (ingredientNames.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("No ingredients found with the name: '%s'.", initialString));
        }
        return ingredientNames;
    }
}
