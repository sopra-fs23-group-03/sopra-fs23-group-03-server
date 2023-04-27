package ch.uzh.ifi.hase.soprafs23.SpooncularAPI;

import ch.uzh.ifi.hase.soprafs23.entity.Group;
import ch.uzh.ifi.hase.soprafs23.entity.User;
import ch.uzh.ifi.hase.soprafs23.service.GroupService;
import ch.uzh.ifi.hase.soprafs23.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Objects;
import java.util.Random;

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

    @GetMapping("/groups/{groupId}/result")
    @ResponseStatus(HttpStatus.OK) // 200
    @ResponseBody
    public String getRandomRecipe(@PathVariable Long groupsId, HttpServletRequest request) {
        // 404 - group not found
        Group group = groupService.getGroupById(groupsId);

        // 401 - not authorized
        Long tokenId = userService.getUseridByToken(request.getHeader("X-Token"));
        if (tokenId == 0L) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "You are not authorized.");
        }

        Long hostId = group.getHostId();
        User host = userService.getUserById(hostId); // Assuming there's a method to get user by Id in userService
        Recipe detailedRecipe = apiService.getHostRecipe(host);

        return String.format("ID: %d%nTitle: %s%nReady in minutes: %d%nPrice per serving: %.2f",
                detailedRecipe.getId(),
                detailedRecipe.getTitle(),
                detailedRecipe.getReadyInMinutes(),
                detailedRecipe.getPricePerServing());
    }

}
