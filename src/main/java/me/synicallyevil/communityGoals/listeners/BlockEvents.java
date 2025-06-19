package me.synicallyevil.communityGoals.listeners;

import me.synicallyevil.communityGoals.CommunityGoals;
import me.synicallyevil.communityGoals.goals.GoalsManager;
import me.synicallyevil.communityGoals.goals.enums.GoalType;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

public class BlockEvents implements Listener {

    private final CommunityGoals plugin;
    private final GoalsManager manager;

    public BlockEvents(CommunityGoals plugin) {
        this.plugin = plugin;
        this.manager = plugin.getGoalsManager();
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();

        manager.getActiveGoals().forEach(goal -> {
            if(goal.getType() == GoalType.BLOCK_BREAK) {
                if(manager.checkRequirements(goal, player, player.getWorld().getName(), block.getType().name(), player.getInventory().getItemInMainHand().getType().name()))
                    manager.handleGoalProgress(goal, 1);
            }

            if(goal.getType() == GoalType.CROP_HARVEST && block.getBlockData() instanceof Ageable ageable){
                if (ageable.getAge() >= ageable.getMaximumAge()) {
                    if(manager.checkRequirements(goal, player, player.getWorld().getName(), block.getType().name(), player.getInventory().getItemInMainHand().getType().name()))
                        manager.handleGoalProgress(goal, 1);
                }
            }
        });
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();

        manager.getActiveGoals().forEach(goal -> {
            if(goal.getType() == GoalType.BLOCK_PLACE) {
                if(manager.checkRequirements(goal, player, player.getWorld().getName(), event.getBlock().getType().name(), player.getInventory().getItemInMainHand().getType().name()))
                    manager.handleGoalProgress(goal, 1);
            }
        });
    }
}
