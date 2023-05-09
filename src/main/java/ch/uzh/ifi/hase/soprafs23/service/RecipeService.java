package ch.uzh.ifi.hase.soprafs23.service;

import ch.uzh.ifi.hase.soprafs23.SpooncularAPI.Recipe;
import ch.uzh.ifi.hase.soprafs23.entity.User;
import ch.uzh.ifi.hase.soprafs23.entity.Group;
import ch.uzh.ifi.hase.soprafs23.service.GroupService;
import ch.uzh.ifi.hase.soprafs23.SpooncularAPI.IngredientAPI;
import org.springframework.beans.factory.annotation.Autowired;

import ch.uzh.ifi.hase.soprafs23.repository.RecipeRepository;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class RecipeService {
    @Autowired
    private RecipeRepository recipeRepository;

    @Autowired
    public RecipeService(RecipeRepository recipeRepository) {
        this.recipeRepository = recipeRepository;
    }

    public List<Recipe> getRecipesByGroup(Group group) {
        return recipeRepository.findAllByGroup(group);
    }

    public void save(Recipe recipe){
        recipeRepository.save(recipe);
        recipeRepository.flush();
    }
}
