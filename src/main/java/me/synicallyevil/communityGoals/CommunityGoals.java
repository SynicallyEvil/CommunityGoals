package me.synicallyevil.communityGoals;

import me.synicallyevil.communityGoals.commands.Fund;
import me.synicallyevil.communityGoals.data.FundTop;
import me.synicallyevil.communityGoals.managers.FundManager;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import su.nightexpress.coinsengine.api.CoinsEngineAPI;
import su.nightexpress.coinsengine.api.currency.Currency;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static me.synicallyevil.communityGoals.utils.Utils.isNumber;

public final class CommunityGoals extends JavaPlugin {

    private FundTop fundTop;
    public HashMap<Integer, FundManager> fundManager = new HashMap<>();
    public List<UUID> playersResettingFund = new ArrayList<>();

    private Economy economy;

    private boolean coinEngineEnabled = false;
    private Currency currency;

    @Override
    public void onEnable() {
        getConfig().options().copyDefaults(true);
        saveConfig();
        reloadConfig();

        getCommand("fund").setExecutor(new Fund(this));

        fundTop = new FundTop(this);
        reload();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public void reload(){
        if(getConfig().getBoolean("funds.coinengine.enabled")){
            coinEngineEnabled = true;
            currency = CoinsEngineAPI.getCurrency(getConfig().getString("funds.coinsengine.currency"));

            if(currency == null){
                coinEngineEnabled = false;
                currency = null;

                setupVault();
            }
        }else{
            setupVault();
        }

        for(String n : getConfig().getConfigurationSection("fund.goals").getKeys(false)){
            if(!isNumber(n))
                continue;

            int goal = Integer.parseInt(n);
            if(fundManager.containsKey(goal)){
                FundManager fund = fundManager.get(goal);
                fund.setCurrent(getConfig().getInt("fund.goals." + goal + ".current", 0));
                fund.setMax(getConfig().getInt("fund.goals." + goal + ".max", 10000000));
                fund.setName(getConfig().getString("fund.goals." + goal + ".name"));
                fund.setCommands(getConfig().getStringList("fund.goals." + goal + ".custom_commands"));
                fund.checkDone();
            }else{
                fundManager.put(goal, new FundManager(this, getConfig().getString("fund.goals." + goal + ".name"), goal, getConfig().getInt("fund.goals." + goal + ".current", 0), getConfig().getInt("fund.goals." + goal + ".max", 10000000), getConfig().getStringList("fund.goals." + goal + ".console_commands")));
            }
        }

    }

    private void setupVault(){
        if(Bukkit.getPluginManager().getPlugin("Vault") != null){
            RegisteredServiceProvider<Economy> service = Bukkit.getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);

            if(service != null)
                economy = service.getProvider();
        }

    }

    public void withdraw(Player player, double amount){
        if(this.coinEngineEnabled){
            CoinsEngineAPI.removeBalance(player, currency, amount);
            return;
        }

        economy.withdrawPlayer(player, amount);
    }

    public double getBalance(Player player){
        if(this.coinEngineEnabled){
            return CoinsEngineAPI.getBalance(player, currency);
        }

        return economy.getBalance(player);
    }

    public FundTop getFundTop(){
        return fundTop;
    }

    public List<UUID> getPlayersResettingFund() {
        return playersResettingFund;
    }


}
