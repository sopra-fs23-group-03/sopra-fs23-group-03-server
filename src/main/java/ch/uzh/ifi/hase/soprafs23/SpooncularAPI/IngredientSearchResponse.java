package ch.uzh.ifi.hase.soprafs23.SpooncularAPI;

import java.util.List;

/** IngredientSearchResponse
 * represent the JSON response from the API call to search for ingredients
 */

public class IngredientSearchResponse {
    private List<IngredientAPI> results;

    public List<IngredientAPI> getResults() {
        return results;
    }

    public void setResults(List<IngredientAPI> results) {
        this.results = results;
    }
}
