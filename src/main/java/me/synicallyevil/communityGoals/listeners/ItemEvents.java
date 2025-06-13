package me.synicallyevil.communityGoals.listeners;

import me.synicallyevil.communityGoals.CommunityGoals;
import me.synicallyevil.communityGoals.goals.GoalsManager;
import me.synicallyevil.communityGoals.goals.enums.GoalType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.FurnaceSmeltEvent;

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
    public void onItemEnchant(CraftItemEvent event) {
        if (event.getWhoClicked() instanceof Player player) {
            manager.getActiveGoals().forEach(goal -> {
                if (goal.getType() == GoalType.ITEM_ENCHANT) {
                    if (manager.checkRequirements(goal, player, player.getWorld().getName(), event.getRecipe().getResult().getType().name(), null)) {
                        manager.handleGoalProgress(goal, 1);
                    }
                }
            });
        }
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
    public void onPotionBrewed(CraftItemEvent event) {
        if (event.getWhoClicked() instanceof Player player) {
            manager.getActiveGoals().forEach(goal -> {
                if (goal.getType() == GoalType.POTION_BREW) {
                    if (manager.checkRequirements(goal, player, player.getWorld().getName(), event.getRecipe().getResult().getType().name(), null)) {
                        manager.handleGoalProgress(goal, 1);
                    }
                }
            });
        }
    }
}
