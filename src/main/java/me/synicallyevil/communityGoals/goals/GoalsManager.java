package me.synicallyevil.communityGoals.goals;

import me.synicallyevil.communityGoals.CommunityGoals;
import me.synicallyevil.communityGoals.goals.enums.GoalMode;
import me.synicallyevil.communityGoals.goals.enums.GoalType;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.Instant;
import java.util.*;

public class GoalsManager {
    private final CommunityGoals plugin;
    private final Map<String, Goal> allGoals = new HashMap<>();
    private final List<Goal> activeGoals = new ArrayList<>();
    private final Map<UUID, Long> onlineSince = new HashMap<>();

    private GoalMode goalMode;

    public GoalsManager(CommunityGoals plugin) {
        this.plugin = plugin;
        loadGoals();
        scheduleTickTask();
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

            Goal goal = new Goal(
                    id,
                    goalSec.getString("display"),
                    goalSec.getString("description"),
                    type,
                    goalSec.getInt("amount"),
                    goalSec.getStringList("worlds"),
                    goalSec.getStringList("entities"),
                    goalSec.getStringList("blocks"),
                    goalSec.getStringList("tools"),
                    goalSec.contains("expires_at") ? Instant.ofEpochSecond(goalSec.getLong("expires_at")) : null
            );

            plugin.getLogger().info("Loaded goal: " + id + " (" + goal.getDisplay() + ")" + (goal.isExpired() ? " - EXPIRED" : ""));

            allGoals.put(id, goal);

            plugin.getLogger().info("GoalMode: " + goalMode);

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

            for(UUID uuid : onlineSince.keySet()) {
                if (goal.getType() == GoalType.TIME_PLAYED) {
                    long millis = System.currentTimeMillis() - onlineSince.get(uuid);
                    handleGoalProgress(goal, (int) (millis / 1000L));
                }
            }
        }
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

    public Map<UUID, Long> getOnlineSince() {
        return onlineSince;
    }
}
