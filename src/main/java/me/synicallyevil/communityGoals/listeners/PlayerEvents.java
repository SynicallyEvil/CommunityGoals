package me.synicallyevil.communityGoals.listeners;

import me.synicallyevil.communityGoals.CommunityGoals;
import me.synicallyevil.communityGoals.goals.enums.GoalType;
import org.bukkit.Material;
import org.bukkit.Raid;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.*;
import org.bukkit.event.raid.RaidEvent;
import org.bukkit.event.raid.RaidFinishEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerEvents implements Listener {

    private final CommunityGoals plugin;

    public PlayerEvents(CommunityGoals plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        plugin.getGoalsManager().getOnlineSince().put(event.getPlayer().getUniqueId(), System.currentTimeMillis());

        plugin.getGoalsManager().getActiveGoals().forEach(goal -> {
            if (goal.getType() == GoalType.PLAYER_JOIN) {
                if(plugin.getGoalsManager().checkRequirements(goal, event.getPlayer(), event.getPlayer().getWorld().getName(), null, null))
                    plugin.getGoalsManager().handleGoalProgress(goal, 1);
            }
        });
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        plugin.getGoalsManager().getOnlineSince().remove(event.getPlayer().getUniqueId());

        plugin.getGoalsManager().getActiveGoals().forEach(goal -> {
            if (goal.getType() == GoalType.PLAYER_QUIT) {
                if(plugin.getGoalsManager().checkRequirements(goal, event.getPlayer(), event.getPlayer().getWorld().getName(), null, null))
                    plugin.getGoalsManager().handleGoalProgress(goal, 1);
            }
        });
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event){
        if (!(event.getInventory().getHolder() instanceof Villager)) return;
        if (!(event.getWhoClicked() instanceof Player)) return;

        if (event.getSlotType() != InventoryType.SlotType.RESULT) return;

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

        Player player = (Player) event.getWhoClicked();
        plugin.getGoalsManager().getActiveGoals().forEach(goal -> {
            if (goal.getType() == GoalType.VILLAGER_TRADE) {
                if (plugin.getGoalsManager().checkRequirements(goal, player, player.getWorld().getName(), clickedItem.getType().name(), null)) {
                    plugin.getGoalsManager().handleGoalProgress(goal, 1);
                }
            }
        });
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();

        if (event.isCancelled())
            return;

        plugin.getGoalsManager().getActiveGoals().forEach(goal -> {
            if (goal.getType() == GoalType.CHAT_MESSAGE) {
                if (plugin.getGoalsManager().checkRequirements(goal, player, player.getWorld().getName(), null, null)) {
                    plugin.getGoalsManager().handleGoalProgress(goal, 1);
                }
            }
        });
    }

    @EventHandler
    public void onAdvancementCompleted(PlayerAdvancementDoneEvent event){
        Player player = event.getPlayer();
        String advancementKey = event.getAdvancement().getKey().toString();

        plugin.getGoalsManager().getActiveGoals().forEach(goal -> {
            if (goal.getType() == GoalType.ADVANCEMENT_COMPLETE) {
                if (plugin.getGoalsManager().checkRequirements(goal, player, player.getWorld().getName(), advancementKey, null)) {
                    plugin.getGoalsManager().handleGoalProgress(goal, 1);
                }
            }
        });
    }

    @EventHandler
    public void onFishCaught(PlayerFishEvent event) {
        Player player = event.getPlayer();

        if (event.getCaught() == null || !(event.getCaught() instanceof Item)) return;

        Item caughtItem = (Item) event.getCaught();

        plugin.getGoalsManager().getActiveGoals().forEach(goal -> {
            if (goal.getType() == GoalType.FISH_CAUGHT) {
                if (plugin.getGoalsManager().checkRequirements(goal, player, player.getWorld().getName(), caughtItem.getItemStack().getType().name(), null)) {
                    plugin.getGoalsManager().handleGoalProgress(goal, 1);
                }
            }
        });
    }

    @EventHandler
    public void onPlayerExperienceChange(PlayerExpChangeEvent event) {
        Player player = event.getPlayer();
        int amount = event.getAmount();

        plugin.getGoalsManager().getActiveGoals().forEach(goal -> {
            if (goal.getType() == GoalType.EXPERIENCE_GAINED) {
                if (plugin.getGoalsManager().checkRequirements(goal, player, player.getWorld().getName(), null, null)) {
                    plugin.getGoalsManager().handleGoalProgress(goal, amount);
                }
            }
        });
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        if(event.getFrom().getBlockX() == event.getTo().getBlockX() &&
           event.getFrom().getBlockY() == event.getTo().getBlockY() &&
           event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }

        plugin.getGoalsManager().getActiveGoals().forEach(goal -> {
            if (goal.getType() == GoalType.DISTANCE_TRAVELED) {
                if (plugin.getGoalsManager().checkRequirements(goal, player, player.getWorld().getName(), null, null)) {
                    plugin.getGoalsManager().handleGoalProgress(goal, 1);
                }
            }
        });
    }

    @EventHandler
    public void onRaidWin(RaidFinishEvent event) {
        if(event.getRaid().getStatus() != Raid.RaidStatus.VICTORY) {
            return;
        }

        plugin.getGoalsManager().getActiveGoals().forEach(goal -> {
            if (goal.getType() == GoalType.RAID_WIN) {
                if (plugin.getGoalsManager().checkRequirements(goal, null, event.getWorld().getName(), null, null)) {
                    plugin.getGoalsManager().handleGoalProgress(goal, 1);
                }
            }
        });
    }
}
