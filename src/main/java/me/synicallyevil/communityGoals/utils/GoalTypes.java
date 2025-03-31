package me.synicallyevil.communityGoals.utils;

public enum GoalTypes {
    CURRENCY("CURRENCY"),
    VILLAGER_TRADE("VILLAGER_TRADE"),
    REPAIR_GOLEMS("REPAIR_GOLEMS"),
    ORE_MINING("ORE_MINING");

    private final String type;

    GoalTypes(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
