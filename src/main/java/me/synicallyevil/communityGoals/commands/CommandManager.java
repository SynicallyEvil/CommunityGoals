package me.synicallyevil.communityGoals.commands;

import me.synicallyevil.communityGoals.CommunityGoals;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class CommandManager implements CommandExecutor {

    private final CommunityGoals cg;
    private final Map<String, CommandInterface> commandMap = new HashMap<>();

    public CommandManager(CommunityGoals cg) {
        this.cg = cg;
    }

    public void registerCommand(CommandInterface command) {
        commandMap.put(command.getName().toLowerCase(), command);
        for (String alias : command.getAliases()) {
            commandMap.put(alias.toLowerCase(), command);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Please specify a subcommand.");
            return true;
        }

        CommandInterface subcommand = commandMap.get(args[0].toLowerCase());
        if (subcommand == null) {
            sender.sendMessage(ChatColor.RED + "Unknown command.");
            return true;
        }

        String[] subArgs = Arrays.copyOfRange(args, 1, args.length);
        return subcommand.execute(sender, subArgs);
    }
}