package me.synicallyevil.communityGoals.storage;

import me.synicallyevil.communityGoals.CommunityGoals;
import me.synicallyevil.communityGoals.goals.Goal;
import me.synicallyevil.communityGoals.goals.GoalType;
import me.synicallyevil.communityGoals.goals.GoalsManager;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GoalsStorage {

    private final CommunityGoals cg;
    private final GoalsManager manager;
    private FileConfiguration config;

    public GoalsStorage(CommunityGoals cg, GoalsManager manager) {
        this.cg = cg;
        this.manager = manager;
        loadGoals();
    }

    public void loadGoals() {
        manager.getGoals().clear();

        File file = new File(cg.getDataFolder(), "goals.yml");
        if (!file.exists()) {
            cg.saveResource("goals.yml", false);
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        ConfigurationSection goalSection = config.getConfigurationSection("goals");
        if (goalSection == null) return;

        for (String key : goalSection.getKeys(false)) {
            try {
                int id = Integer.parseInt(key);
                ConfigurationSection section = goalSection.getConfigurationSection(key);
                if (section == null) continue;

                String name = section.getString("name");
                GoalType type = GoalType.valueOf(section.getString("type").toUpperCase());
                int current = section.getInt("current", 0);
                int max = section.getInt("max", 0);
                List<String> commands = section.getStringList("console_commands");

                Goal goal = new Goal(id, name, current, max, type, commands);
                manager.getGoals().put(id, goal);
                manager.setNextGoalID(Math.max(manager.getNextGoalID(), id + 1));
            } catch (Exception e) {
                System.out.println("Failed to load goal: " + key + " - " + e.getMessage());
            }
        }

        this.config = config;
    }

    public void saveGoals() {
        FileConfiguration config = new YamlConfiguration();

        for (Goal goal : manager.getGoals().values()) {
            String path = "goals." + goal.getId();
            config.set(path + ".name", goal.getName());
            config.set(path + ".type", goal.getType().name());
            config.set(path + ".current", goal.getCurrent());
            config.set(path + ".target", goal.getMax());
            config.set(path + ".commands", goal.getCompletionCommands());
        }

        try {
            config.save(new File(cg.getDataFolder(), "goals.yml"));
            Bukkit.getLogger().info("[CommunityGoals] goals.yml saved.");
        } catch (IOException e) {
            Bukkit.getLogger().severe("Failed to save goals.yml: " + e.getMessage());
        }
    }

    public FileConfiguration getGoalsConfig(){
        return this.config;
    }
}
