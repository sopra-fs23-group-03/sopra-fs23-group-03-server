package ch.uzh.ifi.hase.soprafs23.service;

import ch.uzh.ifi.hase.soprafs23.SpooncularAPI.Recipe;
import ch.uzh.ifi.hase.soprafs23.entity.Group;
import org.springframework.beans.factory.annotation.Autowired;

import ch.uzh.ifi.hase.soprafs23.repository.RecipeRepository;
import org.springframework.stereotype.Service;

import java.util.List;


/**
 * Recipe Service
 * This class can be seen as a "working class" for all recipe related methods.
 * It also is responsible for storing/retrieving data from the respective recipeRepository.
 */

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

    public Recipe findByExternalRecipeIdAndGroupId(Long externalRecipeId, Long groupId) { //we need to check for both externalID AND GroupID
        return recipeRepository.findByExternalRecipeIdAndGroupId(externalRecipeId, groupId);
    }


}
