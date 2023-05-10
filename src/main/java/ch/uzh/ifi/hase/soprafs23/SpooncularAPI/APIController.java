package ch.uzh.ifi.hase.soprafs23.SpooncularAPI;

import ch.uzh.ifi.hase.soprafs23.entity.Group;
import ch.uzh.ifi.hase.soprafs23.entity.Ingredient;
import ch.uzh.ifi.hase.soprafs23.entity.User;
import ch.uzh.ifi.hase.soprafs23.repository.IngredientRepository;
import ch.uzh.ifi.hase.soprafs23.service.GroupService;
import ch.uzh.ifi.hase.soprafs23.service.UserService;
import ch.uzh.ifi.hase.soprafs23.SpooncularAPI.APIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import javax.persistence.criteria.CriteriaBuilder;
import javax.servlet.http.HttpServletRequest;
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
    private IngredientRepository ingredientRepository;

    @GetMapping("/groups/{groupId}/result")
    @ResponseStatus(HttpStatus.OK) // 200
    @ResponseBody
    public APIGetDTO getRandomRecipe(@PathVariable Long groupId, HttpServletRequest request) {
        // 404 - group not found
        Group group = groupService.getGroupById(groupId);

        // 401 - not authorized
        Long tokenId = userService.getUseridByToken(request.getHeader("X-Token"));
        if (tokenId.equals(0L)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "You are not authorized.");
        }

        Long hostId = group.getHostId();
        User host = userService.getUserById(hostId);
        Recipe detailedRecipe = apiService.getHostRecipe(host);

        APIGetDTO apiGetDTO = new APIGetDTO();
        apiGetDTO.setId(detailedRecipe.getId());
        apiGetDTO.setTitle(detailedRecipe.getTitle());
        apiGetDTO.setReadyInMinutes(detailedRecipe.getReadyInMinutes());
        apiGetDTO.setPricePerServing(detailedRecipe.getPricePerServing());

        return apiGetDTO;
    }

    @GetMapping("/ingredients")
    @ResponseStatus(HttpStatus.OK) // 200
    @ResponseBody
    public List<String> getAllIngredients(HttpServletRequest request, @RequestParam String initialString) {
        // check validity of token
        String token = request.getHeader("X-Token"); // 401 - not authorized
        if (userService.getUseridByToken(token).equals(0L)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, String.format("You are not authorized."));
        }

        if (initialString.length() == 1) {
            List<Ingredient> ingredients = ingredientRepository.findByNameContainingIgnoreCase(initialString);
            List<String> ingredientNames = ingredients.stream().map(Ingredient::getName).collect(Collectors.toList());

            // If no ingredients were found in the database, fetch them from the API
            if (ingredientNames.isEmpty()) {
                ingredientNames = apiService.getListOfIngredients(initialString);
            }

            if (ingredientNames.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("No ingredients found for initial string '%s'.", initialString));
            }
            return ingredientNames;
        } else {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, String.format("The provided string has to be exactly length 1, you provided '%s'.", initialString)); // 422 - unprocessable entity
        }
    }

}
