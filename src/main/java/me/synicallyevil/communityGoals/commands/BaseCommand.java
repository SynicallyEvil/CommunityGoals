package me.synicallyevil.communityGoals.commands;

import me.synicallyevil.communityGoals.CommunityGoals;
import me.synicallyevil.communityGoals.goals.GoalsManager;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public abstract class BaseCommand implements CommandInterface {

    protected final CommunityGoals plugin;
    protected final GoalsManager goalsManager;

    public BaseCommand(CommunityGoals plugin) {
        this.plugin = plugin;
        this.goalsManager = plugin.getGoalsManager();
    }

    @Override
    public List<String> getAliases() {
        return Collections.emptyList();
    }

    @Override
    public String getPermission() {
        return null; // By default, no permission required
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (getPermission() != null && !sender.hasPermission(getPermission())) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }
        return handle(sender, args);
    }

    public abstract boolean handle(CommandSender sender, String[] args);
}
