package me.synicallyevil.communityGoals.listeners;

import me.synicallyevil.communityGoals.CommunityGoals;
import me.synicallyevil.communityGoals.goals.GoalsManager;
import me.synicallyevil.communityGoals.goals.enums.GoalType;
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

        manager.getActiveGoals().forEach(goal -> {
            if(goal.getType() == GoalType.BLOCK_BREAK || goal.getType() == GoalType.CROP_HARVEST) {
                if(manager.checkRequirements(goal, player, player.getWorld().getName(), event.getBlock().getType().name(), player.getInventory().getItemInMainHand().getType().name()))
                    manager.handleGoalProgress(goal, 1);
            }
        });
    } //public boolean checkRequirements(Goal goal, Player player, @Nullable String blockOrEntity)

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
