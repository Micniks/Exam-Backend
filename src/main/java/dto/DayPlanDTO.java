package dto;

import entities.DayPlan;

public class DayPlanDTO {
    
    private int recipeID;
    private int NumberOfServings;

    public DayPlanDTO() {
    }

    public DayPlanDTO(int recipe, int NumberOfServings) {
        this.recipeID = recipe;
        this.NumberOfServings = NumberOfServings;
    }

    public DayPlanDTO(DayPlan dayPlan){
        this.recipeID = dayPlan.getRecipeID();
        this.NumberOfServings = dayPlan.getNumberOfServings();
    }

    public int getRecipeID() {
        return recipeID;
    }

    public void setRecipeID(int recipeID) {
        this.recipeID = recipeID;
    }

    public int getNumberOfServings() {
        return NumberOfServings;
    }

    public void setNumberOfServings(int NumberOfServings) {
        this.NumberOfServings = NumberOfServings;
    }
    
}
