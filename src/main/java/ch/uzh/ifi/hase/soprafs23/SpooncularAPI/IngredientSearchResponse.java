package ch.uzh.ifi.hase.soprafs23.SpooncularAPI;

import java.util.List;

public class IngredientSearchResponse {
    private List<IngredientAPI> results;

    public List<IngredientAPI> getResults() {
        return results;
    }

    public void setResults(List<IngredientAPI> results) {
        this.results = results;
    }
}
