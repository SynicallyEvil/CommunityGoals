package me.synicallyevil.communityGoals.commands.subcommands;

import me.synicallyevil.communityGoals.CommunityGoals;
import me.synicallyevil.communityGoals.commands.registry.CommandInterface;
import me.synicallyevil.communityGoals.goals.GoalsManager;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class GoalReload implements CommandInterface {

    private final CommunityGoals plugin;
    private final GoalsManager goalsManager;

    public GoalReload(CommunityGoals plugin) {
        this.plugin = plugin;
        this.goalsManager = plugin.getGoalsManager();
    }

    @Override
    public String getName() {
        return "reload";
    }

    @Override
    public List<String> getAliases() {
        return List.of();
    }

    @Override
    public String getPermission() {
        return "goals.admin";
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        //goalsManager.loadGoals();
        //plugin.reloadConfig();
        plugin.getGui().open((Player) sender);
        sender.sendMessage(ChatColor.GREEN + "Goals reloaded from file.");
        return true;
    }
}
