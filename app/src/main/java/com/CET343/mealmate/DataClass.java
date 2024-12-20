package com.CET343.mealmate;

import java.io.Serializable;
import java.util.List;

public class DataClass implements Serializable {

    private String dataTitle;
    private String dataDesc;
    private List<String> dataIngredients;
    private List<Boolean> ingredientStates;  // Track the state of each ingredient (true/false)
    private String dataImage;

    // Default constructor (required for Firebase)
    public DataClass() {}

    // Constructor with parameters
    public DataClass(String dataTitle, String dataDesc, List<String> dataIngredients, List<Boolean> ingredientStates, String dataImage) {
        this.dataTitle = dataTitle;
        this.dataDesc = dataDesc;
        this.dataIngredients = dataIngredients;
        this.ingredientStates = ingredientStates;
        this.dataImage = dataImage;
    }

    // Getters and setters
    public String getDataTitle() {
        return dataTitle;
    }

    public void setDataTitle(String dataTitle) {
        this.dataTitle = dataTitle;
    }

    public String getDataDesc() {
        return dataDesc;
    }

    public void setDataDesc(String dataDesc) {
        this.dataDesc = dataDesc;
    }

    public List<String> getDataIngredients() {
        return dataIngredients;
    }

    public void setDataIngredients(List<String> dataIngredients) {
        this.dataIngredients = dataIngredients;
    }

    public List<Boolean> getIngredientStates() {
        return ingredientStates;
    }

    public void setIngredientStates(List<Boolean> ingredientStates) {
        this.ingredientStates = ingredientStates;
    }

    public String getDataImage() {
        return dataImage;
    }

    public void setDataImage(String dataImage) {
        this.dataImage = dataImage;
    }
}
