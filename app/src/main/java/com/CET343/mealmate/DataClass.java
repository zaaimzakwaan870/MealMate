package com.CET343.mealmate;

public class DataClass {

    private String dataRecipe;
    private String dataDesc;
    private String dataIngredients;
    private String dataImage;

    public String getDataRecipe() {
        return dataRecipe;
    }

    public String getDataDesc() {
        return dataDesc;
    }

    public String getDataIngredients() {
        return dataIngredients;
    }

    public String getDataImage() {
        return dataImage;
    }

    public DataClass(String dataRecipe, String dataDesc, String dataIngredients, String dataImage) {
        this.dataRecipe = dataRecipe;
        this.dataDesc = dataDesc;
        this.dataIngredients = dataIngredients;
        this.dataImage = dataImage;
    }

    public DataClass(){

    }
}
