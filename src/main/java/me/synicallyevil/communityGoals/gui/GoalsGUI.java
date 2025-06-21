package me.synicallyevil.communityGoals.gui;

import me.synicallyevil.communityGoals.CommunityGoals;
import me.synicallyevil.communityGoals.goals.Goal;
import me.synicallyevil.communityGoals.goals.GoalsManager;
import me.synicallyevil.communityGoals.goals.enums.GoalType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

public class GoalsGUI implements Listener {
    private final CommunityGoals plugin;
    private final GoalsManager manager;
    private final Map<UUID, GoalFilter> filters = new HashMap<>();

    public GoalsGUI(CommunityGoals plugin) {
        this.plugin = plugin;
        this.manager = plugin.getGoalsManager();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void open(Player player) {
        List<Goal> allGoals = new ArrayList<>(manager.getActiveGoals());
        GoalFilter filter = filters.getOrDefault(player.getUniqueId(), new GoalFilter());

        List<Goal> filteredGoals = allGoals.stream()
                .filter(filter::matches)
                .sorted(Comparator.comparing(Goal::getType))
                .toList();

        ConfigurationSection guiConfig = plugin.getConfig().getConfigurationSection("gui");
        String guiTitle = ChatColor.translateAlternateColorCodes('&', guiConfig.getString("title", "&2Community Goals"));
        ConfigurationSection goalItems = guiConfig.getConfigurationSection("goal_item");

        int maxPerRow = 8;
        int maxRows = 6;
        int totalGoals = filteredGoals.size();
        int goalRows = (int) Math.ceil(totalGoals / (double) maxPerRow);
        int totalRows = Math.min(maxRows, Math.max(goalRows + 2, 3)); // +2 for padding, cap at 6
        int guiSize = totalRows * 9;

        Inventory gui = Bukkit.createInventory(null, guiSize, guiTitle);

        // Filler item
        Material fillerMat = Material.valueOf(guiConfig.getString("filler.material", "BLACK_STAINED_GLASS_PANE"));
        String fillerName = ChatColor.translateAlternateColorCodes('&', guiConfig.getString("filler.name", " "));
        ItemStack filler = new ItemStack(fillerMat);
        ItemMeta fillerMeta = filler.getItemMeta();
        fillerMeta.setDisplayName(fillerName);
        filler.setItemMeta(fillerMeta);

        for (int i = 0; i < gui.getSize(); i++) {
            gui.setItem(i, filler);
        }

        // Place goal items centered
        int index = 0;
        for (int row = 1; row < totalRows - 1 && index < totalGoals; row++) {
            int itemsThisRow = Math.min(maxPerRow, totalGoals - index);
            int startCol = (9 - itemsThisRow) / 2;

            for (int col = 0; col < itemsThisRow && index < totalGoals; col++) {
                int slot = row * 9 + startCol + col;
                if (slot >= gui.getSize()) continue;

                Goal goal = filteredGoals.get(index++);

                ConfigurationSection itemConfig;
                if (goal.isExpired()) {
                    itemConfig = goalItems.getConfigurationSection("expired");
                } else if (goal.getProgress() >= goal.getAmount()) {
                    itemConfig = goalItems.getConfigurationSection("completed");
                } else {
                    itemConfig = goalItems.getConfigurationSection("active");
                }

                Material itemMat = Material.getMaterial(itemConfig.getString("material", "BOOK"));
                ItemStack item = new ItemStack(itemMat);
                ItemMeta meta = item.getItemMeta();
                meta.setDisplayName(ChatColor.translateAlternateColorCodes('&',
                        itemConfig.getString("name", goal.getDisplay()).replace("%display%", goal.getDisplay())));

                List<String> loreTemplate = itemConfig.getStringList("lore");
                List<String> lore = new ArrayList<>();
                for (String line : loreTemplate) {
                    line = ChatColor.translateAlternateColorCodes('&', line)
                            .replace("%type%", goal.getType().getDisplayName())
                            .replace("%progress%", String.valueOf(goal.getProgress()))
                            .replace("%amount%", String.valueOf(goal.getAmount()))
                            .replace("%description%", goal.getDescription())
                            .replace("%time_left%", goal.isTimed() ? formatTimeLeft(goal.getExpiresAt()) : "∞");
                    lore.add(line);
                }

                meta.setLore(lore);
                if (itemConfig.getBoolean("glow", false)) {
                    meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                    item.addUnsafeEnchantment(Enchantment.UNBREAKING, 1);
                }
                if (slot >= gui.getSize()) {
                    plugin.getLogger().warning("Skipping slot " + slot + " (GUI size: " + gui.getSize() + ")");
                    continue;
                }
                item.setItemMeta(meta);
                gui.setItem(slot, item);
            }
        }

        // Filter Button
        ConfigurationSection filterBtn = guiConfig.getConfigurationSection("filter_button");
        Material filterMat = Material.valueOf(filterBtn.getString("material", "COMPARATOR"));
        ItemStack filterButton = new ItemStack(filterMat);
        ItemMeta filterMeta = filterButton.getItemMeta();
        filterMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', filterBtn.getString("name", "&bFilter Options")));
        List<String> lore = filterBtn.getStringList("lore").stream()
                .map(line -> ChatColor.translateAlternateColorCodes('&', line.replace("%current_filter%", filter.getTypeFilterName())))
                .collect(Collectors.toList());
        filterMeta.setLore(lore);
        filterButton.setItemMeta(filterMeta);

        int filterSlot = filterBtn.getInt("slozzt", gui.getSize() - 5);
        //if (filterSlot < gui.getSize()) {
            gui.setItem(filterSlot, filterButton);
        //}

        filters.put(player.getUniqueId(), filter);
        player.openInventory(gui);
    }

    private String formatTimeLeft(Instant expiresAt) {
        long seconds = Duration.between(Instant.now(), expiresAt).getSeconds();
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if (days > 0) return days + "d";
        if (hours > 0) return hours + "h";
        if (minutes > 0) return minutes + "m";
        return seconds + "s";
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;
        Player player = (Player) e.getWhoClicked();

        String expectedTitle = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("gui.title", "&2Community Goals"));
        if (!e.getView().getTitle().equals(expectedTitle)) return;

        e.setCancelled(true);

        if (e.getCurrentItem() == null || e.getCurrentItem().getType() != Material.COMPARATOR) return;

        GoalFilter filter = filters.getOrDefault(player.getUniqueId(), new GoalFilter());
        filter.cycleType();
        filters.put(player.getUniqueId(), filter);

        Bukkit.getScheduler().runTask(plugin, () -> open(player));
    }

    private static class GoalFilter {
        private GoalType typeFilter = null;

        public boolean matches(Goal goal) {
            return typeFilter == null || goal.getType() == typeFilter;
        }

        public String getTypeFilterName() {
            return typeFilter == null ? "All" : typeFilter.getDisplayName();
        }

        public void cycleType() {
            GoalType[] types = GoalType.values();
            if (typeFilter == null) {
                typeFilter = types[0];
            } else {
                int idx = (typeFilter.ordinal() + 1) % types.length;
                typeFilter = types[idx];
                if (idx == 0) typeFilter = null;
            }
        }
    }
}
