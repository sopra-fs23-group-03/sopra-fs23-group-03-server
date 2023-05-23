package ch.uzh.ifi.hase.soprafs23.SpooncularAPI;

import java.util.List;

/** RecipeSearchResult
 * This class is used in the getRandomRecipeUser and helps mapping the API response of the first API call,
 * which includes as a result, an array inside a results field.
*/

public class RecipeSearchResult {
    private List<RecipeInfo> results;

    public List<RecipeInfo> getResults() {
        return results;
    }

}

