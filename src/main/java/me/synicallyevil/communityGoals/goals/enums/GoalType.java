package me.synicallyevil.communityGoals.goals.enums;

public enum GoalType {
    BLOCK_BREAK("Block Break"), //
    BLOCK_PLACE("Block Place"), //
    MOB_KILL("Mob Kill"), //
    PLAYER_KILL("Player Kill"), //
    PLAYER_DEATH("Player Death"), //
    IRON_GOLEM_REPAIR("Iron Golem Repair"), //
    VILLAGER_TRADE("Villager Trade"), //
    ITEM_CRAFT("Item Craft"), //
    ITEM_SMELT("Item Smelt"), //
    ITEM_REPAIR("Item Repair"), // //
    FISH_CAUGHT("Fish Caught"), //
    DISTANCE_TRAVELED("Distance Traveled"), //
    POTION_BREW("Potion Brew"), //
    ITEM_ENCHANT("Item Enchant"), //
    ANIMAL_BREED("Animal Breed"), //
    CROP_HARVEST("Crop Harvest"), //
    EXPERIENCE_GAINED("Experience Gained"), //
    DAMAGE_DEALT("Damage Dealt"), //
    DAMAGE_TAKEN("Damage Taken"), //
    RAID_WIN("Raid Win"), //
    ADVANCEMENT_COMPLETE("Advancement Complete"), //
    CHAT_MESSAGE("Chat Message"), //
    TIME_PLAYED("Time Played"), //
    MONEY_SPENT("Money Spent"),
    MONEY_EARNED("Money Earned"),
    PLAYER_JOIN("Player Join"), //
    PLAYER_QUIT("Player Quit"); //

    private final String displayName;

    GoalType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}