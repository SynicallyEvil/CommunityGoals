package me.synicallyevil.communityGoals.utils;

public enum GoalTypes {
    CURRENCY("currency"),
    VILLAGER_TRADE("villager_trade"),
    REPAIR_GOLEMS("repair_golems");

    private final String type;

    GoalTypes(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
