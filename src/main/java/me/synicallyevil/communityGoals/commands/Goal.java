package me.synicallyevil.communityGoals.commands;

import me.synicallyevil.communityGoals.CommunityGoals;
import me.synicallyevil.communityGoals.managers.GoalManager;
import me.synicallyevil.communityGoals.utils.GoalTypes;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import static me.synicallyevil.communityGoals.utils.Utils.*;

public class Goal implements CommandExecutor {

    private final CommunityGoals cg;

    public Goal(CommunityGoals cg) {
        this.cg = cg;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) return true;

        Player player = (Player) sender;
        if (args.length == 0) {
            displayGoalInfo(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "deposit":
                handleDeposit(player, args);
                break;
            case "help":
                displayHelp(player);
                break;
            case "reset":
                handleReset(player, args);
                break;
            default:
                displayHelp(player);
                break;
        }
        return true;
    }

    private void displayGoalInfo(Player player) {
        GoalManager goal = getCurrentGoal(cg.getGoalManager());
        if (goal == null) {
            player.sendMessage(getColor(cg.getConfig().getString("goal.messages.others.all_goals_completed")));
            return;
        }

        cg.getConfig().getStringList("goal.messages.goal").forEach(s -> {
            String message = s.replace("%name%", goal.getName())
                    .replace("%percent%", getPercentage(goal.getCurrent(), goal.getMax()))
                    .replace("%symbol%", getProgressBar(goal.getCurrent(), goal.getMax(),
                            cg.getConfig().getString("goal.symbol"),
                            cg.getConfig().getInt("goal.symbol_amount"),
                            cg.getConfig().getString("goal.color_of_achieved"),
                            cg.getConfig().getString("goal.color_of_remaining")))
                    .replace("%total%", String.format("$%,d", goal.getMax()))
                    .replace("%remaining%", String.format("$%,d", goal.getRemaining()));
            player.sendMessage(getColor(message));
        });
    }

    private void handleDeposit(Player player, String[] args) {
        if (args.length < 2 || !isNumber(args[1])) {
            displayHelp(player);
            return;
        }

        GoalManager goal = getCurrentGoal(cg.getGoalManager());
        if (goal == null) {
            player.sendMessage(getColor(cg.getConfig().getString("goal.messages.others.all_goals_completed")));
            return;
        }

        if(goal.getType() != GoalTypes.CURRENCY){
            player.sendMessage(getColor(cg.getConfig().getString("goal.messages.others.not_currency")));
            return;
        }

        int amount = Integer.parseInt(args[1]);
        double balance = cg.getBalance(player);

        if (balance < amount) {
            player.sendMessage(getColor(cg.getConfig().getString("goal.messages.others.not_enough_money")));
            return;
        }

        amount = Math.min(amount, goal.getRemaining());
        goal.addAmount(amount);
        cg.withdraw(player, amount);

        player.sendMessage(getColor(cg.getConfig().getString("goal.messages.others.paid")
                .replace("%amount%", String.format("%,d", amount))
                .replace("%name%", goal.getName())));

        if (goal.isDone()) {
            goal.getCommands().forEach(cmd -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd));
            GoalManager nextGoal = getCurrentGoal(cg.getGoalManager());
            Bukkit.broadcastMessage(getColor(cg.getConfig().getString("goal.messages.broadcast.goal_finished")
                    .replace("%goal%", goal.getName())
                    .replace("%next_goal%", nextGoal == null ? "NONE" : nextGoal.getName())));
        }
    }

    private void displayHelp(Player player) {
        cg.getConfig().getStringList("goal.messages.help").forEach(s -> player.sendMessage(getColor(s)));
    }

    private void handleReset(Player player, String[] args) {
        if (!player.hasPermission("goal.reset")) {
            player.sendMessage(getColor(cg.getConfig().getString("goal.messages.others.no_perms")));
            return;
        }

        if (args.length > 1 && args[1].equalsIgnoreCase("confirm")) {
            if (!cg.getPlayersResettingGoal().contains(player.getUniqueId())) {
                player.sendMessage(getColor(cg.getConfig().getString("goal.messages.others.not_confirming_anything")));
                return;
            }

            cg.getGoalManager().values().forEach(GoalManager::reset);
            player.sendMessage(getColor(cg.getConfig().getString("goal.messages.others.goal_reset")));
        } else {
            cg.getPlayersResettingGoal().add(player.getUniqueId());
            player.sendMessage(getColor(cg.getConfig().getString("goal.messages.others.resetting_goal")));
            new BukkitRunnable() {
                @Override
                public void run() {
                    cg.getPlayersResettingGoal().remove(player.getUniqueId());
                }
            }.runTaskLaterAsynchronously(cg, 20 * 60);
        }
    }
}