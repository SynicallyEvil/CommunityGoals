package me.synicallyevil.communityGoals;

import me.synicallyevil.communityGoals.commands.Fund;
import me.synicallyevil.communityGoals.data.FundTop;
import me.synicallyevil.communityGoals.managers.FundManager;
import me.synicallyevil.communityGoals.utils.Utils;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import su.nightexpress.coinsengine.api.CoinsEngineAPI;
import su.nightexpress.coinsengine.api.currency.Currency;

import java.util.*;

public final class CommunityGoals extends JavaPlugin {

    private FundTop fundTop;
    private final Map<Integer, FundManager> fundManager = new HashMap<>();
    private final List<UUID> playersResettingFund = new ArrayList<>();

    private Economy economy;
    private boolean coinEngineEnabled = false;
    private Currency currency;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        reloadConfig();

        registerCommands();
        initializeFundTop();
        reloadFunds();
    }

    @Override
    public void onDisable() {
        getLogger().info("CommunityGoals is shutting down.");
    }

    private void registerCommands() {
        Objects.requireNonNull(getCommand("fund")).setExecutor(new Fund(this));
    }

    private void initializeFundTop() {
        fundTop = new FundTop(this);
    }

    public void reloadFunds() {
        configureEconomy();
        loadFundGoals();
    }

    private void configureEconomy() {
        if (getConfig().getBoolean("funds.coinsengine.enabled")) {
            setupCoinsEngine();
        } else {
            setupVault();
        }
    }

    private void setupCoinsEngine() {
        String currencyName = getConfig().getString("funds.coinsengine.currency", "tokens");
        currency = CoinsEngineAPI.getCurrency(currencyName);

        if (currency != null) {
            coinEngineEnabled = true;
            getLogger().info("Connected to CoinsEngine. Currency: " + currencyName);
        } else {
            getLogger().severe("Invalid CoinsEngine currency: " + currencyName + ". Falling back to Vault.");
            coinEngineEnabled = false;
            setupVault();
        }
    }

    private void setupVault() {
        if (Bukkit.getPluginManager().getPlugin("Vault") != null) {
            RegisteredServiceProvider<Economy> service = Bukkit.getServicesManager().getRegistration(Economy.class);
            economy = (service != null) ? service.getProvider() : null;
        }

        if (economy == null) {
            getLogger().severe("Vault not found. Disabling plugin.");
            getServer().getPluginManager().disablePlugin(this);
        } else {
            getLogger().info("Connected to Vault.");
        }
    }

    private void loadFundGoals() {
        Optional.ofNullable(getConfig().getConfigurationSection("fund.goals"))
                .ifPresent(section -> section.getKeys(false).stream()
                        .filter(Utils::isNumber)
                        .map(Integer::parseInt)
                        .forEach(this::updateFundManager));
    }

    private void updateFundManager(int goal) {
        if (fundManager.containsKey(goal)) {
            FundManager fund = fundManager.get(goal);
            fund.updateFromConfig(getConfig(), goal);
            fund.checkDone();
        } else {
            fundManager.put(goal, FundManager.createFromConfig(this, getConfig(), goal));
        }
    }

    public void withdraw(Player player, double amount) {
        if (coinEngineEnabled) {
            CoinsEngineAPI.removeBalance(player, currency, amount);
        } else {
            economy.withdrawPlayer(player, amount);
        }
    }

    public double getBalance(Player player) {
        return coinEngineEnabled ? CoinsEngineAPI.getBalance(player, currency) : economy.getBalance(player);
    }

    public FundTop getFundTop() {
        return fundTop;
    }

    public List<UUID> getPlayersResettingFund() {
        return Collections.unmodifiableList(playersResettingFund);
    }

    public Map<Integer, FundManager> getFundManager() {
        return fundManager;
    }
}
