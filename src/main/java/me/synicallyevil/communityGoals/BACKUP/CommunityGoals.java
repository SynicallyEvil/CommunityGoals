package me.synicallyevil.communityGoals.BACKUP;

import me.synicallyevil.communityGoals.BACKUP.commands.Goal;
import me.synicallyevil.communityGoals.BACKUP.listeners.EventsListener;
import me.synicallyevil.communityGoals.BACKUP.managers.GoalManager;
import me.synicallyevil.communityGoals.BACKUP.utils.Utils;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import su.nightexpress.coinsengine.api.CoinsEngineAPI;
import su.nightexpress.coinsengine.api.currency.Currency;

import java.util.*;

import static me.synicallyevil.communityGoals.BACKUP.utils.Utils.getColor;

public final class CommunityGoals extends JavaPlugin {

    private final Map<Integer, GoalManager> goalManager = new HashMap<>();
    private final List<UUID> playersResettingGoal = new ArrayList<>();

    private Economy economy;
    private boolean coinEngineEnabled = false;
    private Currency currency;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        reloadConfig();

        registerCommands();
        registerListeners();
        reloadGoals();
    }

    @Override
    public void onDisable() {
        getLogger().info("CommunityGoals is shutting down.");
    }

    private void registerCommands() {
        Objects.requireNonNull(getCommand("goal")).setExecutor(new Goal(this));
    }

    private void registerListeners(){
        getServer().getPluginManager().registerEvents(new EventsListener(this), this);
    }

    public void reloadGoals() {
        configureEconomy();
        loadGoals();
    }

    private void configureEconomy() {
        if (getConfig().getBoolean("goal.coinsengine.enabled")) {
            setupCoinsEngine();
        } else {
            setupVault();
        }
    }

    private void setupCoinsEngine() {
        String currencyName = getConfig().getString("goal.coinsengine.currency", "tokens");
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

    private void loadGoals() {
        Optional.ofNullable(getConfig().getConfigurationSection("goal.goals"))
                .ifPresent(section -> section.getKeys(false).stream()
                        .filter(Utils::isNumber)
                        .map(Integer::parseInt)
                        .forEach(this::updateGoalManager));
    }

    private void updateGoalManager(int goal) {
        if (goalManager.containsKey(goal)) {
            GoalManager goalM = goalManager.get(goal);
            goalM.updateFromConfig(getConfig(), goal);
            goalM.checkDone();
        } else {
            goalManager.put(goal, GoalManager.createFromConfig(this, getConfig(), goal));
        }
    }

    public void withdraw(Player player, double amount) {
        if (coinEngineEnabled) {
            CoinsEngineAPI.removeBalance(player, currency, amount);
        } else {
            economy.withdrawPlayer(player, amount);
        }
    }

    public void deposit(Player player, double amount) {
        if (coinEngineEnabled) {
            CoinsEngineAPI.addBalance(player, currency, amount);
        } else {
            economy.depositPlayer(player, amount);
        }
    }

    public String getSymbol() {
        if (coinEngineEnabled) {
            return "%,d " + currency.getSymbol();
        }

        return "$%,d";
    }

    public double getBalance(Player player) {
        return coinEngineEnabled ? CoinsEngineAPI.getBalance(player, currency) : economy.getBalance(player);
    }

    public List<UUID> getPlayersResettingGoal() {
        return Collections.unmodifiableList(playersResettingGoal);
    }

    public Map<Integer, GoalManager> getGoalManager() {
        return goalManager;
    }

    public void openDepositGui(Player player, GoalManager goal) {
        Inventory depositGui = Bukkit.createInventory(null, 9, getColor("&bDeposit Currency"));

        // Add deposit options (10, 50, 100, 500, 1000)
        int[] amounts = {10, 25, 50, 100, 500, 1000, 2500, 5000, 10000};
        for (int i = 0; i < amounts.length; i++) {
            ItemStack item = new ItemStack(Material.EMERALD);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(getColor("&9Deposit &f" + String.format(getSymbol(), amounts[i])));
                meta.setLore(List.of(getColor("&7Click to deposit &f" + String.format(getSymbol(), amounts[i])), getColor("&7towards the current goal!")));
                item.setItemMeta(meta);
            }
            depositGui.setItem(i, item);
        }

        player.openInventory(depositGui);
    }

}
