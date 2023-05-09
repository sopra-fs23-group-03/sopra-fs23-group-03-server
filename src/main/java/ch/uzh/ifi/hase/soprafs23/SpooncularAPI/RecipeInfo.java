package ch.uzh.ifi.hase.soprafs23.SpooncularAPI;
import ch.uzh.ifi.hase.soprafs23.SpooncularAPI.IngredientInfo;
import java.util.List;

    public class RecipeInfo { // maps exactly response from spoonacular API
        private Long id;
        private String title;
        private String image;
        private String imageType;
        private int usedIngredientCount;
        private int missedIngredientCount;
        private List<IngredientInfo> missedIngredients;
        private List<IngredientInfo> usedIngredients;
        private List<IngredientInfo> unusedIngredients;
        private int likes;

        public RecipeInfo() {
        }


        // getters and setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getImage() { return image; }
        public void setImage(String image) { this.image = image; }
        public String getImageType() { return imageType; }
        public void setImageType(String imageType) { this.imageType = imageType; }
        public int getUsedIngredientCount() { return usedIngredientCount; }
        public void setUsedIngredientCount(int usedIngredientCount) { this.usedIngredientCount = usedIngredientCount; }
        public int getMissedIngredientCount() { return missedIngredientCount; }
        public void setMissedIngredientCount(int missedIngredientCount) { this.missedIngredientCount = missedIngredientCount; }
        public int getLikes() { return likes; }
        public void setLikes(int likes) { this.likes = likes; }

        public List<IngredientInfo> getMissedIngredients() {
            return missedIngredients;
        }

        public void setMissedIngredients(List<IngredientInfo> missedIngredients) {
            this.missedIngredients = missedIngredients;
        }

        public List<IngredientInfo> getUsedIngredients() {
            return usedIngredients;
        }

        public void setUsedIngredients(List<IngredientInfo> usedIngredients) {
            this.usedIngredients = usedIngredients;
        }

        public List<IngredientInfo> getUnusedIngredients() {
            return unusedIngredients;
        }

        public void setUnusedIngredients(List<IngredientInfo> unusedIngredients) {
            this.unusedIngredients = unusedIngredients;
        }
    }
