package ch.uzh.ifi.hase.soprafs23.SpooncularAPI;

import java.util.List;

// API response contains now a results array inside a results field --> need this class to map response
public class RecipeSearchResult {
    private List<RecipeInfo> results;

    public List<RecipeInfo> getResults() {
        return results;
    }

    public void setResults(List<RecipeInfo> results) {
        this.results = results;
    }
}

