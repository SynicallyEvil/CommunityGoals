package me.synicallyevil.communityGoals.commands.registry;

import org.bukkit.command.CommandSender;

import java.util.List;

public interface CommandInterface {
    String getName();
    List<String> getAliases();
    String getPermission();
    boolean execute(CommandSender sender, String[] args);
}