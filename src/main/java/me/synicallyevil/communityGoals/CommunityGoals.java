package me.synicallyevil.communityGoals;

import me.synicallyevil.communityGoals.commands.registry.CommandManager;
import me.synicallyevil.communityGoals.goals.GoalsManager;
import me.synicallyevil.communityGoals.gui.GoalsGUI;
import me.synicallyevil.communityGoals.listeners.PlayerEvents;
import me.synicallyevil.communityGoals.metrics.Metrics;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class CommunityGoals extends JavaPlugin {

    private CommandManager commandManager;
    private GoalsManager goalsManager;
    private GoalsGUI gui;

    private FileConfiguration goalsConfig;
    private FileConfiguration messagesConfig;

    private Metrics metrics;

    @Override
    public void onEnable() {
        this.saveDefaultConfig();

        this.commandManager = new CommandManager(this);

        // Register commands
        getCommand("goal").setExecutor(commandManager);

        goalsManager = new GoalsManager(this);
        gui = new GoalsGUI(this);
        //this.metrics = new Metrics(this, 25392);
        //metrics.addCustomChart(new Metrics.)

        goalsConfig = getGoalsConfig();
        messagesConfig = getMessagesConfig();

        getServer().getPluginManager().registerEvents(new PlayerEvents(this), this);
    }

    @Override
    public void onDisable() {
        // Save the current goals to the file on disable
        //this.metrics.shutdown();
    }

    public FileConfiguration getGoalsConfig() {
        if (goalsConfig == null) {
            File file = new File(getDataFolder(), "goals.yml");

            if (!file.exists()) {
                saveResource("goals.yml", false);
            }

            goalsConfig = YamlConfiguration.loadConfiguration(file);
        }
        return goalsConfig;
    }

    public FileConfiguration getMessagesConfig() {
        if (messagesConfig == null) {
            File file = new File(getDataFolder(), "messages.yml");

            if (!file.exists()) {
                saveResource("messages.yml", false);
            }

            messagesConfig = YamlConfiguration.loadConfiguration(file);
        }
        return messagesConfig;
    }

    public GoalsManager getGoalsManager() {
        return goalsManager;
    }

    public GoalsGUI getGui() {
        return gui;
    }
}