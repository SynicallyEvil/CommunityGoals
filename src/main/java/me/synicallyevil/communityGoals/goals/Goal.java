package me.synicallyevil.communityGoals.goals;

import me.synicallyevil.communityGoals.goals.enums.GoalType;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

public class Goal {
    private final String id;
    private final String display;
    private final String description;
    private final String permission;
    private final GoalType type;
    private final int amount;
    private int progress;
    private final List<String> worlds;
    private final List<String> entities;
    private final List<String> blocks;
    private final List<String> tools;
    private final List<String> items;
    private final Instant expiresAt;

    public Goal(String id, String display, String description, String permission, GoalType type, int amount,
                List<String> worlds, List<String> entities, List<String> blocks, List<String> tools, List<String> items, Instant expiresAt) {
        this.id = id;
        this.display = display;
        this.description = description;
        this.permission = permission;
        this.type = type;
        this.amount = amount;
        this.worlds = worlds != null ? worlds : Collections.emptyList();
        this.entities = entities != null ? entities : Collections.emptyList();
        this.blocks = blocks != null ? blocks : Collections.emptyList();
        this.tools = tools != null ? tools : Collections.emptyList();
        this.items = items != null ? items : Collections.emptyList();
        this.expiresAt = expiresAt;
    }

    public boolean isExpired() {
        return expiresAt != null && Instant.now().isAfter(expiresAt);
    }

    public void addProgress(int value) {
        progress = Math.min(progress + value, amount);
    }

    public boolean isComplete() {
        return progress >= amount;
    }

    public String getId() { return id; }
    public String getDisplay() { return display; }
    public String getDescription() { return description; }
    public String getPermission() { return permission; }
    public GoalType getType() { return type; }
    public int getAmount() { return amount; }
    public int getProgress() { return progress; }
    public List<String> getWorlds() { return worlds; }
    public List<String> getEntities() { return entities; }
    public List<String> getBlocks() { return blocks; }
    public List<String> getTools() { return tools; }
    public List<String> getItems() { return items; }
    public Instant getExpiresAt() { return expiresAt; }
    public boolean isTimed() {
        return expiresAt != null;
    }
}
