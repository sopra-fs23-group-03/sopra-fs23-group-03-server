package ch.uzh.ifi.hase.soprafs23.rest.dto;

import java.util.List;


public class RecipeInfoDTO {
        private Long id;
        private String title;
        private String image;
        private String imageType;
        private int usedIngredientCount;
        private int missedIngredientCount;
        private List<IngredientInfoDTO> missedIngredients;
        private List<IngredientInfoDTO> usedIngredients;
        private List<IngredientInfoDTO> unusedIngredients;
        private int likes;
        private String instructions;
        private int readyInMinutes;

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

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getImageType() {
        return imageType;
    }

    public void setImageType(String imageType) {
        this.imageType = imageType;
    }

    public int getUsedIngredientCount() {
        return usedIngredientCount;
    }

    public void setUsedIngredientCount(int usedIngredientCount) {
        this.usedIngredientCount = usedIngredientCount;
    }

    public int getMissedIngredientCount() {
        return missedIngredientCount;
    }

    public void setMissedIngredientCount(int missedIngredientCount) {
        this.missedIngredientCount = missedIngredientCount;
    }

    public List<IngredientInfoDTO> getMissedIngredients() {
        return missedIngredients;
    }

    public void setMissedIngredients(List<IngredientInfoDTO> missedIngredients) {
        this.missedIngredients = missedIngredients;
    }

    public List<IngredientInfoDTO> getUsedIngredients() {
        return usedIngredients;
    }

    public void setUsedIngredients(List<IngredientInfoDTO> usedIngredients) {
        this.usedIngredients = usedIngredients;
    }

    public List<IngredientInfoDTO> getUnusedIngredients() {
        return unusedIngredients;
    }

    public void setUnusedIngredients(List<IngredientInfoDTO> unusedIngredients) {
        this.unusedIngredients = unusedIngredients;
    }

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    public int getReadyInMinutes() {
        return readyInMinutes;
    }

    public void setReadyInMinutes(int readyInMinutes) {
        this.readyInMinutes = readyInMinutes;
    }
}
