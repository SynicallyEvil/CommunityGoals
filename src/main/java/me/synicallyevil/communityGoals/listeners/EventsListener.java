package me.synicallyevil.communityGoals.listeners;

import me.synicallyevil.communityGoals.CommunityGoals;
import me.synicallyevil.communityGoals.managers.GoalManager;
import me.synicallyevil.communityGoals.utils.GoalTypes;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;

import java.util.EnumSet;
import java.util.Map;
import java.util.Random;

import static me.synicallyevil.communityGoals.utils.Utils.getColor;
import static me.synicallyevil.communityGoals.utils.Utils.getCurrentGoal;

public class EventsListener implements Listener {

    private final CommunityGoals cg;
    private final Random random = new Random();
    // Define Ore Tiers
    private final Map<Material, Integer> ORE_TIERS = Map.ofEntries(
            Map.entry(Material.COAL_ORE, 1),
            Map.entry(Material.DEEPSLATE_COAL_ORE, 1),
            Map.entry(Material.IRON_ORE, 1),
            Map.entry(Material.DEEPSLATE_IRON_ORE, 1),
            Map.entry(Material.COPPER_ORE, 1),
            Map.entry(Material.DEEPSLATE_COPPER_ORE, 1),

            Map.entry(Material.GOLD_ORE, 2),
            Map.entry(Material.DEEPSLATE_GOLD_ORE, 2),
            Map.entry(Material.REDSTONE_ORE, 2),
            Map.entry(Material.DEEPSLATE_REDSTONE_ORE, 2),
            Map.entry(Material.LAPIS_ORE, 2),
            Map.entry(Material.DEEPSLATE_LAPIS_ORE, 2),

            Map.entry(Material.DIAMOND_ORE, 3),
            Map.entry(Material.DEEPSLATE_DIAMOND_ORE, 3),
            Map.entry(Material.EMERALD_ORE, 3),
            Map.entry(Material.DEEPSLATE_EMERALD_ORE, 3)
    );

    public EventsListener(CommunityGoals cg){
        this.cg = cg;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player)event.getWhoClicked();
        String title = event.getView().getTitle();

        if (title.equals(getColor("&bGoal Information"))) {
            event.setCancelled(true);
            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

            GoalManager goal = getCurrentGoal(cg.getGoalManager());
            if (goal != null && goal.getType() == GoalTypes.CURRENCY && clickedItem.getType() == Material.NETHER_STAR) {
                cg.openDepositGui(player, goal);
            }
        }

        if (title.equals(getColor("&bDeposit Currency"))) {
            event.setCancelled(true);
            GoalManager goal = getCurrentGoal(cg.getGoalManager());
            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem == null || clickedItem.getType() != Material.EMERALD || event.getSlot() == -1) return;

            String name = clickedItem.getItemMeta().getDisplayName();
            int[] amounts = {10, 25, 50, 100, 500, 1000, 2500, 5000, 10000};
            int amount = amounts[event.getSlot()];

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

            player.closeInventory();

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

        // Villager trade stuff.
        if (!(event.getInventory().getHolder() instanceof Villager)) return;
        if (!(event.getWhoClicked() instanceof Player)) return;

        if (event.getSlotType() != InventoryType.SlotType.RESULT) return;

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

        addToGoal(GoalTypes.VILLAGER_TRADE);
    }

    @EventHandler
    public void onIronGolemRepair(PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() instanceof IronGolem golem))
            return;

        Player player = event.getPlayer();
        if (player.getInventory().getItemInMainHand().getType() != Material.IRON_INGOT)
            return;

        double initialHealth = golem.getHealth();
        double maxHealth = golem.getAttribute(Attribute.MAX_HEALTH).getValue();

        if (initialHealth < maxHealth) {
            Bukkit.getScheduler().runTaskLater(cg, () -> {
                if (golem.getHealth() > initialHealth) {
                    addToGoal(GoalTypes.REPAIR_GOLEMS);
                }
            }, 1L);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event){
        Player player = event.getPlayer();
        Material blockType = event.getBlock().getType();

        // Check if the block is an ore
        if (ORE_TIERS.containsKey(blockType)) {
            // Check if the player is using a Silk Touch tool
            ItemStack tool = player.getInventory().getItemInMainHand();
            if (tool != null && tool.hasItemMeta() && tool.getItemMeta().hasEnchant(Enchantment.SILK_TOUCH)) {
                return; // Cancel if Silk Touch is detected
            }

            int tier = ORE_TIERS.get(blockType);

            if(cg.getConfig().getBoolean("goal.give_currency_on_ore_break")){
                //int amount = getWeightedRandom();
                cg.deposit(player, tier);
            }

            addToGoal(GoalTypes.ORE_MINING);
        }
    }

    private void addToGoal(GoalTypes type){
        GoalManager goal = getCurrentGoal(cg.getGoalManager());
        if (goal == null || goal.getType() != type)
            return;

        goal.addAmount(1);

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
}
