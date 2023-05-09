package ch.uzh.ifi.hase.soprafs23.SpooncularAPI;
import ch.uzh.ifi.hase.soprafs23.SpooncularAPI.IngredientAPI;
import java.util.List;

    public class RecipeInfo {
        private Long id;
        private String title;
        private String image;
        private String imageType;
        private int usedIngredientCount;
        private int missedIngredientCount;
        private List<IngredientAPI> missedIngredients;
        private List<IngredientAPI> usedIngredients;
        private List<IngredientAPI> unusedIngredients;
        private int likes;

        public RecipeInfo(Long id, String title, String image, String imageType, int usedIngredientCount, int missedIngredientCount,
                          List<IngredientAPI> missedIngredients, List<IngredientAPI> usedIngredients, List<IngredientAPI> unusedIngredients, int likes) {
            this.id = id;
            this.title = title;
            this.image = image;
            this.imageType = imageType;
            this.usedIngredientCount = usedIngredientCount;
            this.missedIngredientCount = missedIngredientCount;
            this.missedIngredients = missedIngredients;
            this.usedIngredients = usedIngredients;
            this.unusedIngredients = unusedIngredients;
            this.likes = likes;
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
        public List<IngredientAPI> getMissedIngredients() { return missedIngredients; }
        public void setMissedIngredients(List<IngredientAPI> missedIngredients) { this.missedIngredients = missedIngredients; }
        public List<IngredientAPI> getUsedIngredients() { return usedIngredients; }
        public void setUsedIngredients(List<IngredientAPI> usedIngredients) { this.usedIngredients = usedIngredients; }
        public List<IngredientAPI> getUnusedIngredients() { return unusedIngredients; }
        public void setUnusedIngredients(List<IngredientAPI> unusedIngredients) { this.unusedIngredients = unusedIngredients; }
        public int getLikes() { return likes; }
        public void setLikes(int likes) { this.likes = likes; }

        @Override
        public String toString() {
            return "RecipeInfo{" +
                    "id=" + id +
                    ", title='" + title + '\'' +
                    ", usedIngredients=" + usedIngredients +
                    ", missedIngredients=" + missedIngredients +
                    '}';
        }

}
