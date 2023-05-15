package ch.uzh.ifi.hase.soprafs23.SpooncularAPI;

import ch.uzh.ifi.hase.soprafs23.entity.Group;
import ch.uzh.ifi.hase.soprafs23.service.GroupService;
import ch.uzh.ifi.hase.soprafs23.service.UserService;
import ch.uzh.ifi.hase.soprafs23.service.RecipeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
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


        List<RecipeInfo> recipeInfos = apiService.getRecipe(group);

        if (recipeInfos.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No recipes found");
        }

        List<APIGetDTO> apiGetDTOS = new ArrayList<>();

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
            recipe.setGroup(group);

            // Fetch additional details
            RecipeDetailInfo detailInfo = apiService.getRecipeDetails(recipe.getExternalRecipeId());
            if (detailInfo != null) {
                recipe.setReadyInMinutes(detailInfo.getReadyInMinutes() != 0 ? detailInfo.getReadyInMinutes() : 0);
                recipe.setImage(detailInfo.getImage() != null ? detailInfo.getImage() : "Default image URL");
                recipe.setInstructions(detailInfo.getInstructions() != null ? detailInfo.getInstructions() : "No instructions provided");
            }

            // Save/update the recipe in db
            recipeService.save(recipe);

            APIGetDTO apiGetDTO = new APIGetDTO();
            apiGetDTO.setId(recipe.getId());
            apiGetDTO.setTitle(recipe.getTitle());
            apiGetDTO.setUsedIngredients(recipe.getUsedIngredients());
            apiGetDTO.setMissedIngredients(recipe.getMissedIngredients());
            apiGetDTO.setInstructions(recipe.getInstructions());
            apiGetDTO.setImage(recipe.getImage());
            apiGetDTO.setReadyInMinutes(recipe.getReadyInMinutes());
            apiGetDTO.setGroupId(groupId);

            apiGetDTOS.add(apiGetDTO);
        }

        return new ResponseEntity<>(apiGetDTOS, HttpStatus.OK);
    }


    @GetMapping("/ingredients")
    @ResponseStatus(HttpStatus.OK) // 200
    @ResponseBody
    public List<String> getAllIngredients(HttpServletRequest request, @RequestParam String initialString){
        // check validity of token
        String token = request.getHeader("X-Token"); // 401 - not authorized
        if(userService.getUseridByToken(token).equals(0L)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, String.format("You are not authorized."));
        }

        if (initialString.length() == 3) {

            List<String> ingredientNames = apiService.getListOfIngredients(initialString);

            // when no ingredients were found - 404
            if (ingredientNames.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("No ingredients found for initial string '%s'.", initialString));
            }
            return ingredientNames;

            }
        else{
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, String.format("The provided string has to be exactly length 3, you provided '%s'.", initialString)); // 422 - unprocessable entity
        }


    }
}
