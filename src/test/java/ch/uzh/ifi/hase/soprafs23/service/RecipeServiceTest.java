package ch.uzh.ifi.hase.soprafs23.service;

import ch.uzh.ifi.hase.soprafs23.SpooncularAPI.Recipe;
import ch.uzh.ifi.hase.soprafs23.entity.Group;
import ch.uzh.ifi.hase.soprafs23.repository.RecipeRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class RecipeServiceTest {

    @Mock
    private RecipeRepository recipeRepository;

    @InjectMocks
    private RecipeService recipeService;

    private Recipe recipe;
    private Group group;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);

        // given
        group = new Group();
        group.setId(1L);

        recipe = new Recipe();
        recipe.setId(1L);
        recipe.setTitle("Test Recipe");
        recipe.setGroup(group);

        // mocks the save() method of RecipeRepository
        when(recipeRepository.save(any())).thenReturn(recipe);
    }

    @Test
    public void getRecipesByGroup_validInputs_success() {
        // given
        List<Recipe> recipes = Arrays.asList(recipe);

        when(recipeRepository.findAllByGroup(any())).thenReturn(recipes);

        List<Recipe> returnedRecipes = recipeService.getRecipesByGroup(group);

        verify(recipeRepository, times(1)).findAllByGroup(any());
        assertEquals(returnedRecipes.size(), 1);
        assertEquals(returnedRecipes.get(0).getTitle(), "Test Recipe");
    }

    @Test
    public void saveRecipe_validInputs_success() {
        // when
        recipeService.save(recipe);

        // then
        verify(recipeRepository, times(1)).save(any());
        verify(recipeRepository, times(1)).flush();
    }

    @Test
    public void findByExternalRecipeIdAndGroupId_validInputs_success() {
        // given
        Long externalRecipeId = 1L;
        Long groupId = 1L;

        when(recipeRepository.findByExternalRecipeIdAndGroupId(anyLong(), anyLong())).thenReturn(recipe);

        Recipe foundRecipe = recipeService.findByExternalRecipeIdAndGroupId(externalRecipeId, groupId);

        verify(recipeRepository, times(1)).findByExternalRecipeIdAndGroupId(anyLong(), anyLong());
        assertEquals(foundRecipe.getTitle(), "Test Recipe");
    }
}
