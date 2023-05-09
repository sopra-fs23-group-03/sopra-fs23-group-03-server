package ch.uzh.ifi.hase.soprafs23.SpooncularAPI;

import ch.uzh.ifi.hase.soprafs23.entity.Group;

import java.util.List;

public class APIGetDTO {
    private Long id;
    private String title;
    private List<String> usedIngredients;
    private List<String> missedIngredients;
    private Long groupId;

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<String> getUsedIngredients() {
        return usedIngredients;
    }

    public void setUsedIngredients(List<String> usedIngredients) {
        this.usedIngredients = usedIngredients;
    }

    public List<String> getMissedIngredients() {
        return missedIngredients;
    }

    public void setMissedIngredients(List<String> missedIngredients) {
        this.missedIngredients = missedIngredients;
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }
}
