package me.synicallyevil.communityGoals;

import me.synicallyevil.communityGoals.commands.BaseCommand;
import me.synicallyevil.communityGoals.commands.CommandManager;
import me.synicallyevil.communityGoals.commands.GoalReload;
import me.synicallyevil.communityGoals.goals.GoalsManager;
import me.synicallyevil.communityGoals.metrics.Metrics;
import me.synicallyevil.communityGoals.storage.GoalsStorage;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class CommunityGoals extends JavaPlugin {

    private GoalsManager goalsManager;
    private CommandManager commandManager;

    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        this.reloadConfig();
        // Initialize GoalsManager and GoalsStorage
        goalsManager = new GoalsManager(this);
        this.goalsManager.loadGoals();

        this.commandManager = new CommandManager(this);

        // Register commands
        getCommand("goals").setExecutor(commandManager);

        //commandManager.registerCommand(new BaseCommand(this), goalsManager);
        commandManager.registerCommand(new GoalReload(this, goalsManager));
        //commandManager.registerCommand(new GoalSaveCommand(plugin, goalsManager));

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

    public void reloadGoals() {
        goalsManager.loadGoals(); // clears and loads again
        getLogger().info("[CommunityGoals] Goals reloaded from goals.yml.");
    }
}