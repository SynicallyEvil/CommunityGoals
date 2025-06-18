package me.synicallyevil.communityGoals.listeners;

import me.synicallyevil.communityGoals.CommunityGoals;
import me.synicallyevil.communityGoals.goals.GoalsManager;
import me.synicallyevil.communityGoals.goals.enums.GoalType;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.inventory.BrewEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ItemEvents implements Listener {

    private final CommunityGoals plugin;
    private final GoalsManager manager;

    public ItemEvents(CommunityGoals plugin) {
        this.plugin = plugin;
        this.manager = plugin.getGoalsManager();
    }

    @EventHandler
    public void onItemCrafted(CraftItemEvent event) {
        if (event.getWhoClicked() instanceof Player player) {
            manager.getActiveGoals().forEach(goal -> {
                if (goal.getType() == GoalType.ITEM_CRAFT) {
                    if (manager.checkRequirements(goal, player, player.getWorld().getName(), event.getRecipe().getResult().getType().name(), null)) {
                        manager.handleGoalProgress(goal, 1);
                    }
                }
            });
        }
    }

    @EventHandler
    public void onItemSmelted(FurnaceSmeltEvent event) {
        manager.getActiveGoals().forEach(goal -> {
            if (goal.getType() == GoalType.ITEM_SMELT) {
                assert event.getRecipe() != null;
                if (manager.checkRequirements(goal, null, event.getBlock().getWorld().getName(), event.getRecipe().getResult().getType().name(), null)) {
                    manager.handleGoalProgress(goal, 1);
                }
            }
        });
    }

    @EventHandler
    public void onItemEnchant(EnchantItemEvent event) {
        Player player = event.getEnchanter();
        manager.getActiveGoals().forEach(goal -> {
            if (goal.getType() == GoalType.ITEM_ENCHANT) {
                if (manager.checkRequirements(goal, player, player.getWorld().getName(), event.getItem().getType().name(), null)) {
                    manager.handleGoalProgress(goal, 1);
                }
            }
        });
    }

    @EventHandler
    public void onItemRepair(CraftItemEvent event) {
        if (event.getWhoClicked() instanceof Player player) {
            manager.getActiveGoals().forEach(goal -> {
                if (goal.getType() == GoalType.ITEM_REPAIR) {
                    if (manager.checkRequirements(goal, player, player.getWorld().getName(), event.getRecipe().getResult().getType().name(), null)) {
                        manager.handleGoalProgress(goal, 1);
                    }
                }
            });
        }
    }

    @EventHandler
    public void onPotionBrewed(BrewEvent event) {
        if(event.getContents().getViewers().isEmpty()) return;

        // Get the resulting potions
        ItemStack[] results = event.getContents().getContents();
        List<String> potionTargets = new ArrayList<>();

        // Slots 0–2 are the brewing result slots
        for (int i = 0; i < 3; i++) {
            ItemStack result = results[i];
            if (result != null && result.getType().name().contains("POTION")) {
                potionTargets.add(result.getType().name());
            }
        }

        for(HumanEntity viewer : event.getContents().getViewers()){
            if(viewer instanceof Player player){
                manager.getActiveGoals().forEach(goal -> {
                    if (goal.getType() == GoalType.POTION_BREW) {
                        for(String potion : potionTargets){
                            if (manager.checkRequirements(goal, player, player.getWorld().getName(), potion, null)) {
                                manager.handleGoalProgress(goal, 1);
                            }
                        }
                    }
                });
            }
        }
    }
}
