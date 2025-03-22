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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static me.synicallyevil.communityGoals.utils.Utils.*;

public class Fund implements CommandExecutor {

    private final CommunityGoals cg;

    public Fund(CommunityGoals cg){
        this.cg = cg;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(!(sender instanceof Player))
            return true;

        Player player = (Player)sender;
        if(args.length > 0){
            switch(args[0].toLowerCase()){
                case "deposit":
                    if(args.length < 2){
                        help(player);
                        return true;
                    }

                    deposit(player, args[1]);
                    break;
                case "top":
                    top(player);
                    break;
                case "help":
                    help(player);
                    break;
                case "reset":
                    if(args.length < 2){
                        reset(player, false);
                        return true;
                    }

                    reset(player, args[1].equalsIgnoreCase("confirm"));
                    break;
            }
        }else{
            FundManager fund = getCurrentFund(cg.fundManager);

            if(fund == null){
                player.sendMessage(getColor(cg.getConfig().getString("fund.messages.others.all_funds_completed")));
                return true;
            }

            for(String s : cg.getConfig().getStringList("fund.messages.fund")){
                String message = s;
                message = message.replace("%name%", fund.getName());
                message = message.replace("%percent%", getPercentage(fund.getCurrent(), fund.getMax()));
                message = message.replace("%symbol%",  getProgressBar(fund.getCurrent(), fund.getMax(), cg.getConfig().getString("fund.symbol"), cg.getConfig().getInt("fund.symbol_amount"), cg.getConfig().getString("fund.color_of_achieved"), cg.getConfig().getString("fund.color_of_remaining")));
                message = message.replace("%total%", "$" + String.format("%,d", fund.getMax()));
                message = message.replace("%remaining%", "$" + String.format("%,d", fund.getRemaining()));
                player.sendMessage(getColor(message));
            }
        }

        return true;
    }

    private void deposit(Player player, String amount){
        if(!isNumber(amount)){
            help(player);
            return;
        }

        int a = Integer.parseInt(amount);
        double balance = cg.getBalance(player);
        if(balance < a){
            player.sendMessage(getColor(cg.getConfig().getString("fund.messages.others.not_enough_money")));
            return;
        }

        FundManager fund = getCurrentFund(cg.fundManager);

        if(fund == null){
            player.sendMessage(getColor(cg.getConfig().getString("fund.messages.others.all_funds_completed")));
            return;
        }

        if(a > fund.getRemaining())
            a = fund.getRemaining();

        fund.addAmount(a);
        cg.withdraw(player, a);
        cg.getFundTop().setPaid(player.getUniqueId(), a);
        player.sendMessage(getColor(cg.getConfig().getString("fund.messages.others.paid").replace("%amount%", String.format("%,d", a)).replace("%name%", fund.getName())));

        if(a >= cg.getConfig().getInt("fund.amount_of_or_greater_than_to_announce", 5000000)){
            cg.getServer().broadcastMessage(getColor(cg.getConfig().getString("fund.messages.broadcast.message_when_funded_alot").replace("%player%", player.getName()).replace("%amount%", String.format("%,d", a)).replace("%name%", fund.getName())));
        }

        if(fund.isDone()){
            for(String s : fund.getCommands())
                cg.getServer().dispatchCommand(Bukkit.getConsoleSender(), s);


            FundManager nextFund = getCurrentFund(cg.fundManager);
            cg.getServer().broadcastMessage(getColor(cg.getConfig().getString("fund.messages.broadcast.fund_finished").replace("%fund%", fund.getName()).replace("%next_fund%", nextFund == null ? "NONE" : nextFund.getName())));
        }
    }

    private void top(Player player){
        HashMap<UUID, Integer> players = new HashMap<>();
        FileConfiguration z = cg.getFundTop().getConfig();

        for(String uuid : z.getKeys(false)){
            players.put(UUID.fromString(uuid), z.getInt(uuid));
        }

        for(String s : cg.getConfig().getStringList("fund.messages.fundtop.header"))
            player.sendMessage(getColor(s));

        int i = 1;
        for (Map.Entry<UUID, Integer> entry : Utils.entriesSortedByValues(players)) {
            if(i <= 10){
                UUID uuid = entry.getKey();
                int amount = entry.getValue();

                OfflinePlayer p = cg.getServer().getOfflinePlayer(uuid);
                player.sendMessage(getColor(cg.getConfig().getString("fund.messages.fundtop.playerformat")
                        .replace("%place%", ""+i)
                        .replace("%player%", p.getName())
                        .replace("%amount%", "$" + String.format("%,d", amount))));
                i++;
            }
        }

        for(String s : cg.getConfig().getStringList("fund.messages.fundtop.footer"))
            player.sendMessage(getColor(s));
    }

    private void help(Player player){
        for(String s : cg.getConfig().getStringList("fund.messages.help"))
            player.sendMessage(getColor(s));
    }

    private void reset(Player player, boolean isConfirming){
        if(!(player.hasPermission("fund.reset"))){
            player.sendMessage(getColor(cg.getConfig().getString("fund.messages.others.no_perms")));
            return;
        }

        if(isConfirming){
            if(!(cg.getPlayersResettingFund().contains(player.getUniqueId()))) {
                player.sendMessage(getColor(cg.getConfig().getString("fund.messages.others.not_confirming_anything")));
                return;
            }

            for(int i = 0; i <= cg.fundManager.size(); i++){
                FundManager fund = cg.fundManager.get(i);

                if(fund == null)
                    continue;

                fund.reset();
            }

            player.sendMessage(getColor(cg.getConfig().getString("fund.messages.others.fund_reset")));
        }else{
            if(!(cg.getPlayersResettingFund().contains(player.getUniqueId())))
                cg.getPlayersResettingFund().add(player.getUniqueId());

            player.sendMessage(getColor(cg.getConfig().getString("fund.messages.others.resetting_fund")));

            new BukkitRunnable(){

                @Override
                public void run(){
                    cg.getPlayersResettingFund().remove(player.getUniqueId());
                }

            }.runTaskLaterAsynchronously(cg, 20*60);
        }
    }
}
