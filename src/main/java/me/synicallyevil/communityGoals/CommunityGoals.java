package me.synicallyevil.communityGoals;

import me.synicallyevil.communityGoals.commands.CommandManager;
import me.synicallyevil.communityGoals.goals.GoalsManager;
import me.synicallyevil.communityGoals.metrics.Metrics;
import me.synicallyevil.communityGoals.storage.GoalsStorage;
import org.bukkit.plugin.java.JavaPlugin;

public class CommunityGoals extends JavaPlugin {

    private GoalsManager goalsManager;
    private CommandManager commandManager;
    private GoalsStorage goalsStorage;

    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        this.reloadConfig();
        // Initialize GoalsManager and GoalsStorage
        goalsManager = new GoalsManager(this);
        this.goalsStorage = new GoalsStorage(this, goalsManager);
        this.goalsManager.loadGoals();

        this.commandManager = new CommandManager(this);

        // Register commands
        getCommand("goal").setExecutor(commandManager);

        Metrics metrics = new Metrics(this, 25392);
        //metrics.addCustomChart(new Metrics.)
    }

    @Override
    public void onDisable() {
        // Save the current goals to the file on disable
        this.goalsManager.saveGoals();
    }

    public GoalsManager getGoalsManager() {
        return goalsManager;
    }

    public GoalsStorage getStorage() {
        return goalsStorage;
    }

    public void reloadGoals() {
        goalsManager.loadGoals(); // clears and loads again
        getLogger().info("Goals reloaded from goals.yml.");
    }
}