package com.CET343.mealmate;

public class DataClass {

    private String dataTitle;
    private String dataDesc;
    private String dataIngredients;
    private String dataImage;

    public String getDataTitle() {
        return dataTitle;
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

    public DataClass(String dataTitle, String dataDesc, String dataIngredients, String dataImage) {
        this.dataTitle = dataTitle;
        this.dataDesc = dataDesc;
        this.dataIngredients = dataIngredients;
        this.dataImage = dataImage;
    }
}
