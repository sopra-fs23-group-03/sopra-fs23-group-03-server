package ch.uzh.ifi.hase.soprafs23.SpooncularAPI;

import java.util.List;

public class ComplexSearchResponse {

    private List<Recipe> results;

    public List<Recipe> getResults() {
        return results;
    }

    public void setResults(List<Recipe> results) {
        this.results = results;
    }
}

