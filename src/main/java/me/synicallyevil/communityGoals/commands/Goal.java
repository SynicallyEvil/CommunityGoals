package me.synicallyevil.communityGoals.commands;

import me.synicallyevil.communityGoals.CommunityGoals;
import me.synicallyevil.communityGoals.managers.GoalManager;
import me.synicallyevil.communityGoals.utils.GoalTypes;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
        if(cg.getConfig().getBoolean("goal.use_gui_for_goals")){
            openGui(player);
            return;
        }

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
                    .replace("%total%", (goal.getType() == GoalTypes.CURRENCY ? String.format(cg.getSymbol(), goal.getMax()) : String.valueOf(goal.getMax())))
                    .replace("%remaining%", (goal.getType() == GoalTypes.CURRENCY ? String.format(cg.getSymbol(), goal.getRemaining()) : String.valueOf(goal.getRemaining())));
            player.sendMessage(getColor(message));
        });
    }

    public void openGui(Player player) {
        Inventory gui = Bukkit.createInventory(null, 9, getColor("&bGoal Information"));

        // Create Items
        ItemStack goalItem = new ItemStack(Material.NETHER_STAR);
        ItemStack hiddenGoalItem = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemStack finishedGoalItem = new ItemStack(Material.GREEN_STAINED_GLASS_PANE);

        ItemMeta gMeta = goalItem.getItemMeta();
        ItemMeta hMeta = hiddenGoalItem.getItemMeta();
        ItemMeta fMeta = finishedGoalItem.getItemMeta();

        // Configure Hidden and Finished Goal Items
        if (hMeta != null) {
            hMeta.setDisplayName(getColor("&7Hidden Goal"));
            hMeta.setLore(List.of(getColor("&fFinish the current task to unlock me")));
            hiddenGoalItem.setItemMeta(hMeta);
        }
        if (fMeta != null) {
            fMeta.setDisplayName(getColor("&aCompleted Goal"));
            finishedGoalItem.setItemMeta(fMeta);
        }

        Map<Integer, GoalManager> goals = cg.getGoalManager();
        int slot = 0;

        for (int i = 0; i < goals.size(); i++) {
            GoalManager goal = goals.get(i);
            if (goal == null) continue;

            if (goal.isDone()) {
                gui.setItem(slot, finishedGoalItem);
            } else {
                if (gMeta != null) {
                    gMeta.setDisplayName(getColor("&9Goal&7: &a" + goal.getName()));
                    List<String> lore = new ArrayList<>();

                    // Add goal name and progress
                    lore.add(getColor("&9Remaining&7: &f" +
                            (goal.getType() == GoalTypes.CURRENCY ? String.format(cg.getSymbol(), goal.getRemaining()) : String.valueOf(goal.getRemaining())) +
                            "&7 / &f" +
                            (goal.getType() == GoalTypes.CURRENCY ? String.format(cg.getSymbol(), goal.getMax()) : String.valueOf(goal.getMax()))
                    ));

                    lore.add(getColor(getProgressBar(goal.getCurrent(), goal.getMax(),
                            cg.getConfig().getString("goal.symbol"),
                            cg.getConfig().getInt("goal.symbol_amount"),
                            cg.getConfig().getString("goal.color_of_achieved"),
                            cg.getConfig().getString("goal.color_of_remaining")) +
                            " &7[&f" + getPercentage(goal.getCurrent(), goal.getMax()) + "&7]"));
                    lore.add(" ");


                    for (String line : goal.getDescription()) {
                        lore.add(getColor(line));
                    }


                    gMeta.setLore(lore);
                    goalItem.setItemMeta(gMeta);
                }
                gui.setItem(slot, goalItem); // Display current goal
                slot++;

                // Show the next hidden goal if it exists
                if (i + 1 < goals.size()) {
                    gui.setItem(slot, hiddenGoalItem);
                }
                break;
            }
            slot++;
        }

        // Open the GUI for the player
        player.openInventory(gui);
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
                .replace("%amount%", String.format(cg.getSymbol(), amount))
                .replace("%name%", goal.getName())));

        if (goal.isDone()) {
            for(String command : goal.getCommands()){
                if(command.equalsIgnoreCase("none"))
                    continue;

                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
            }

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