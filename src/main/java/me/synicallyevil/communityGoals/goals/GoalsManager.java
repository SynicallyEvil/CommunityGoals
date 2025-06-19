package me.synicallyevil.communityGoals.goals;

import me.synicallyevil.communityGoals.CommunityGoals;
import me.synicallyevil.communityGoals.goals.enums.GoalMode;
import me.synicallyevil.communityGoals.goals.enums.GoalType;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import javax.annotation.Nullable;
import java.time.Instant;
import java.util.*;

public class GoalsManager {
    private final CommunityGoals plugin;
    private final Map<String, Goal> allGoals = new HashMap<>();
    private final List<Goal> activeGoals = new ArrayList<>();

    private final boolean cancelTask = false;

    private GoalMode goalMode;

    public GoalsManager(CommunityGoals plugin) {
        this.plugin = plugin;
        loadGoals();
        scheduleTickTask();
        saveAllProgress();
    }

    public void reloadGoals(){
        loadGoals();
    }

    public void loadGoals() {
        FileConfiguration config = plugin.getGoalsConfig();

        activeGoals.clear();
        allGoals.clear();

        List<String> goalIds = config.getStringList("active_goals");
        goalMode = GoalMode.valueOf(config.getString("goal_mode", "SINGLE"));

        ConfigurationSection section = config.getConfigurationSection("goals");
        if (section == null || goalIds.isEmpty()) {
            plugin.getLogger().warning("No goals found in the configuration file or active_goals is empty.");
            return;
        }

        for (String id : section.getKeys(false)) {
            ConfigurationSection goalSec = section.getConfigurationSection(id);
            GoalType type = GoalType.valueOf(goalSec.getString("type", ""));

            if(activeGoals.stream().anyMatch(g -> g.getId().equals(id))) {
                plugin.getLogger().warning("Goal with ID " + id + " is already active. Skipping duplicate.");
                continue;
            }

            Goal goal = new Goal(
                    id,
                    goalSec.getString("display"),
                    goalSec.getString("description"),
                    goalSec.getString("permission"),
                    type,
                    goalSec.getInt("amount"),
                    goalSec.getInt("progress", 0),
                    goalSec.getStringList("worlds"),
                    goalSec.getStringList("entities"),
                    goalSec.getStringList("blocks"),
                    goalSec.getStringList("tools"),
                    goalSec.getStringList("items"),
                    goalSec.contains("expires_at") ? Instant.ofEpochSecond(goalSec.getLong("expires_at")) : null
            );

            plugin.getLogger().info("Loaded goal: " + id + " (" + goal.getDisplay() + ")" + (goal.isExpired() ? " - EXPIRED" : ""));

            allGoals.putIfAbsent(id, goal);

            if (goal.isExpired() || !goalIds.contains(id))
                continue;

            if (goalMode.equals(GoalMode.SINGLE)) {
                if (activeGoals.isEmpty()) {
                    activeGoals.add(goal);
                }
            } else if (goalMode.equals(GoalMode.MULTI)) {
                activeGoals.add(goal);
            }
        }
    }

    public void scheduleTickTask() {
        new BukkitRunnable() {
            public void run() {
                if(cancelTask) {
                    this.cancel();

                    return;
                }

                tick();
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }

    public void tick() {
        for(Goal goal : activeGoals) {
            if(goal.isComplete()) continue;

            if (goal.isExpired()) {
                plugin.getLogger().info("Goal " + goal.getId() + " has expired.");
                activeGoals.remove(goal);
                continue;
            }

            if(goal.getType() == GoalType.TIME_PLAYED) {
                plugin.getServer().getOnlinePlayers().forEach(p -> {
                    if(checkRequirements(goal, p, p.getWorld().getName(), null, null))
                        handleGoalProgress(goal, 1);
                });
            }
        }
    }

    private void saveAllProgress(){
        new BukkitRunnable() {
            public void run() {
                FileConfiguration config = plugin.getGoalsConfig();
                List<String> goalIds = new ArrayList<>();

                for (Goal goal : activeGoals) {
                    goalIds.add(goal.getId());
                    config.set("goals." + goal.getId() + ".progress", goal.getProgress());
                }

                config.set("active_goals", goalIds);
                plugin.saveGoalsConfig();
            }
        }.runTaskTimerAsynchronously(plugin, 0L, 6000L);
    }

    public boolean checkRequirements(Goal goal, @Nullable Player player, @Nullable String world, @Nullable String target, @Nullable String tool) {
        return checkWorld(goal, world) &&
               checkEntity(goal, target) &&
               checkBlock(goal, target) &&
               checkTool(goal, tool) &&
               checkItem(goal, target) &&
               checkPermission(goal, player);
    }

    private boolean checkWorld(Goal goal, @Nullable String world) {
        plugin.getLogger().info("Checking world for goal " + goal.getId() + ": " + (world != null ? world : "null"));

        if (goal.getWorlds().isEmpty())
            return true;

        return world != null && goal.getWorlds().contains(world);
    }

    private boolean checkEntity(Goal goal, @Nullable String target) {
        plugin.getLogger().info("Checking entity for goal " + goal.getId() + ": " + (target != null ? target : "null"));

        if (goal.getEntities().isEmpty())
            return true;

        return target != null && goal.getEntities().contains(target);
    }

    private boolean checkBlock(Goal goal, @Nullable String target) {
        plugin.getLogger().info("Checking block for goal " + goal.getId() + ": " + (target != null ? target : "null"));

        if (goal.getBlocks().isEmpty())
            return true;

        return target != null && goal.getBlocks().contains(target);
    }

    private boolean checkTool(Goal goal, @Nullable String tool) {
        plugin.getLogger().info("Checking tool for goal " + goal.getId() + ": " + (tool != null ? tool : "null"));

        if (goal.getTools().isEmpty())
            return true;

        return tool != null && goal.getTools().contains(tool);
    }

    private boolean checkItem(Goal goal, @Nullable String item) {
        plugin.getLogger().info("Checking item for goal " + goal.getId() + ": " + (item != null ? item : "null"));

        if (goal.getItems().isEmpty())
            return true;

        return item != null && goal.getItems().contains(item);
    }

    private boolean checkPermission(Goal goal, @Nullable Player player) {
        plugin.getLogger().info("Checking permission for goal " + goal.getId() + ": " + (player != null ? player.getName() : "null"));

        if (goal.getPermission() == null || goal.getPermission().isEmpty())
            return true;

        return player != null && player.hasPermission(goal.getPermission());
    }

    public void handleGoalProgress(Goal goal, int amount) {
        goal.addProgress(amount);

        if (goal.isComplete()) {
            plugin.getLogger().info("Goal completed: " + goal.getId());

            // Remove completed goal
            activeGoals.remove(goal);

            if (goalMode == GoalMode.SINGLE) {
                List<String> configuredGoalIds = plugin.getGoalsConfig().getStringList("active_goals");

                // Find current goal index in config list
                int index = configuredGoalIds.indexOf(goal.getId());

                // Attempt to activate the next goal if it exists
                if (index != -1 && index + 1 < configuredGoalIds.size()) {
                    String nextGoalId = configuredGoalIds.get(index + 1);
                    Goal nextGoal = allGoals.get(nextGoalId);

                    if (nextGoal != null && !nextGoal.isExpired()) {
                        activeGoals.add(nextGoal);
                        plugin.getLogger().info("Activated next goal: " + nextGoal.getId());
                    }
                }
            }
        }
    }

    public List<Goal> getActiveGoals() {
        return activeGoals;
    }

    public Map<String, Goal> getAllGoals() {
        return allGoals;
    }

}
