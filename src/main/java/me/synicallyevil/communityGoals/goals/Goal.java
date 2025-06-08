package me.synicallyevil.communityGoals.goals;

import me.synicallyevil.communityGoals.goals.enums.GoalType;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

public class Goal {
    private final String id;
    private final String display;
    private final String description;
    private final GoalType type;
    private final int amount;
    private int progress;
    private final List<String> worlds;
    private final List<String> entities;
    private final List<String> blocks;
    private final List<String> tools;
    private final Instant expiresAt;

    public Goal(String id, String display, String description, GoalType type, int amount,
                List<String> worlds, List<String> entities, List<String> blocks, List<String> tools, Instant expiresAt) {
        this.id = id;
        this.display = display;
        this.description = description;
        this.type = type;
        this.amount = amount;
        this.worlds = worlds != null ? worlds : Collections.emptyList();
        this.entities = entities != null ? entities : Collections.emptyList();
        this.blocks = blocks != null ? blocks : Collections.emptyList();
        this.tools = tools != null ? tools : Collections.emptyList();
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
    public GoalType getType() { return type; }
    public int getAmount() { return amount; }
    public int getProgress() { return progress; }
    public List<String> getWorlds() { return worlds; }
    public List<String> getEntities() { return entities; }
    public List<String> getBlocks() { return blocks; }
    public List<String> getTools() { return tools; }
    public Instant getExpiresAt() { return expiresAt; }
}
