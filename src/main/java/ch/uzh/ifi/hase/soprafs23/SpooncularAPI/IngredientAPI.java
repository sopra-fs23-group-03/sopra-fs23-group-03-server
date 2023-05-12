package ch.uzh.ifi.hase.soprafs23.SpooncularAPI;

public class IngredientAPI {
    private int id;
    private String name;
    private String image;
    private double amount;
    private String unit;
    private String unitLong;
    private String unitShort;
    private String aisle;
    private String original;
    private String originalName;
    private List<String> meta;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
