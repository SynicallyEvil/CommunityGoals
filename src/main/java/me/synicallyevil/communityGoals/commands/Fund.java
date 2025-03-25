package me.synicallyevil.communityGoals.commands;

import me.synicallyevil.communityGoals.CommunityGoals;
import me.synicallyevil.communityGoals.managers.FundManager;
import me.synicallyevil.communityGoals.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

import static me.synicallyevil.communityGoals.utils.Utils.*;

public class Fund implements CommandExecutor {

    private final CommunityGoals cg;

    public Fund(CommunityGoals cg) {
        this.cg = cg;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) return true;

        Player player = (Player) sender;
        if (args.length == 0) {
            displayFundInfo(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "deposit":
                handleDeposit(player, args);
                break;
            case "top":
                displayTopPlayers(player);
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

    private void displayFundInfo(Player player) {
        FundManager fund = getCurrentFund(cg.getFundManager());
        if (fund == null) {
            player.sendMessage(getColor(cg.getConfig().getString("fund.messages.others.all_funds_completed")));
            return;
        }

        cg.getConfig().getStringList("fund.messages.fund").forEach(s -> {
            String message = s.replace("%name%", fund.getName())
                    .replace("%percent%", getPercentage(fund.getCurrent(), fund.getMax()))
                    .replace("%symbol%", getProgressBar(fund.getCurrent(), fund.getMax(),
                            cg.getConfig().getString("fund.symbol"),
                            cg.getConfig().getInt("fund.symbol_amount"),
                            cg.getConfig().getString("fund.color_of_achieved"),
                            cg.getConfig().getString("fund.color_of_remaining")))
                    .replace("%total%", String.format("$%,d", fund.getMax()))
                    .replace("%remaining%", String.format("$%,d", fund.getRemaining()));
            player.sendMessage(getColor(message));
        });
    }

    private void handleDeposit(Player player, String[] args) {
        if (args.length < 2 || !isNumber(args[1])) {
            displayHelp(player);
            return;
        }

        int amount = Integer.parseInt(args[1]);
        double balance = cg.getBalance(player);

        if (balance < amount) {
            player.sendMessage(getColor(cg.getConfig().getString("fund.messages.others.not_enough_money")));
            return;
        }

        FundManager fund = getCurrentFund(cg.getFundManager());
        if (fund == null) {
            player.sendMessage(getColor(cg.getConfig().getString("fund.messages.others.all_funds_completed")));
            return;
        }

        amount = Math.min(amount, fund.getRemaining());
        fund.addAmount(amount);
        cg.withdraw(player, amount);
        cg.getFundTop().setPaid(player.getUniqueId(), amount);

        player.sendMessage(getColor(cg.getConfig().getString("fund.messages.others.paid")
                .replace("%amount%", String.format("%,d", amount))
                .replace("%name%", fund.getName())));

        if (amount >= cg.getConfig().getInt("fund.amount_of_or_greater_than_to_announce", 5000000)) {
            Bukkit.broadcastMessage(getColor(cg.getConfig().getString("fund.messages.broadcast.message_when_funded_alot")
                    .replace("%player%", player.getName())
                    .replace("%amount%", String.format("%,d", amount))
                    .replace("%name%", fund.getName())));
        }

        if (fund.isDone()) {
            fund.getCommands().forEach(cmd -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd));
            FundManager nextFund = getCurrentFund(cg.getFundManager());
            Bukkit.broadcastMessage(getColor(cg.getConfig().getString("fund.messages.broadcast.fund_finished")
                    .replace("%fund%", fund.getName())
                    .replace("%next_fund%", nextFund == null ? "NONE" : nextFund.getName())));
        }
    }

    private void displayTopPlayers(Player player) {
        FileConfiguration data = cg.getFundTop().getConfig();
        Map<UUID, Integer> players = new HashMap<>();

        data.getKeys(false).forEach(uuid -> players.put(UUID.fromString(uuid), data.getInt(uuid)));
        cg.getConfig().getStringList("fund.messages.fundtop.header").forEach(s -> player.sendMessage(getColor(s)));

        int i = 1;
        for (Map.Entry<UUID, Integer> entry : Utils.entriesSortedByValues(players)) {
            if (i > 10) break;
            OfflinePlayer p = Bukkit.getOfflinePlayer(entry.getKey());
            player.sendMessage(getColor(cg.getConfig().getString("fund.messages.fundtop.playerformat")
                    .replace("%place%", String.valueOf(i++))
                    .replace("%player%", p.getName())
                    .replace("%amount%", "$" + String.format("%,d", entry.getValue()))));
        }
        cg.getConfig().getStringList("fund.messages.fundtop.footer").forEach(s -> player.sendMessage(getColor(s)));
    }

    private void displayHelp(Player player) {
        cg.getConfig().getStringList("fund.messages.help").forEach(s -> player.sendMessage(getColor(s)));
    }

    private void handleReset(Player player, String[] args) {
        if (!player.hasPermission("fund.reset")) {
            player.sendMessage(getColor(cg.getConfig().getString("fund.messages.others.no_perms")));
            return;
        }

        if (args.length > 1 && args[1].equalsIgnoreCase("confirm")) {
            if (!cg.getPlayersResettingFund().contains(player.getUniqueId())) {
                player.sendMessage(getColor(cg.getConfig().getString("fund.messages.others.not_confirming_anything")));
                return;
            }

            cg.getFundManager().values().forEach(FundManager::reset);
            player.sendMessage(getColor(cg.getConfig().getString("fund.messages.others.fund_reset")));
        } else {
            cg.getPlayersResettingFund().add(player.getUniqueId());
            player.sendMessage(getColor(cg.getConfig().getString("fund.messages.others.resetting_fund")));
            new BukkitRunnable() {
                @Override
                public void run() {
                    cg.getPlayersResettingFund().remove(player.getUniqueId());
                }
            }.runTaskLaterAsynchronously(cg, 20 * 60);
        }
    }
}