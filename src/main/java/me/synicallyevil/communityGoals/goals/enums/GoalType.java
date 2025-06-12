package me.synicallyevil.communityGoals.goals.enums;

public enum GoalType {
    BLOCK_BREAK("Block Break"),
    BLOCK_PLACE("Block Place"),
    MOB_KILL("Mob Kill"),
    PLAYER_KILL("Player Kill"),
    IRON_GOLEM_REPAIR("Iron Golem Repair"),
    VILLAGER_TRADE("Villager Trade"),
    ITEM_CRAFT("Item Craft"),
    ITEM_SMELT("Item Smelt"),
    FISH_CAUGHT("Fish Caught"),
    DISTANCE_TRAVELED("Distance Traveled"),
    POTION_BREW("Potion Brew"),
    ITEM_ENCHANT("Item Enchant"),
    ANIMAL_BREED("Animal Breed"),
    CROP_HARVEST("Crop Harvest"),
    EXPERIENCE_GAINED("Experience Gained"),
    DAMAGE_DEALT("Damage Dealt"),
    DAMAGE_TAKEN("Damage Taken"),
    RAID_WIN("Raid Win"),
    ADVANCEMENT_COMPLETE("Advancement Complete"),
    CHAT_MESSAGE("Chat Message"),
    TIME_PLAYED("Time Played"),
    CUSTOM_EVENT("Custom Event"),
    MONEY_SPENT("Money Spent"),
    MONEY_EARNED("Money Earned");

    private final String displayName;

    GoalType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
