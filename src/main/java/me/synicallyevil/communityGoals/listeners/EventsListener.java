package me.synicallyevil.communityGoals.listeners;

import me.synicallyevil.communityGoals.CommunityGoals;
import me.synicallyevil.communityGoals.managers.GoalManager;
import me.synicallyevil.communityGoals.utils.GoalTypes;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;

import static me.synicallyevil.communityGoals.utils.Utils.getColor;
import static me.synicallyevil.communityGoals.utils.Utils.getCurrentGoal;

public class EventsListener implements Listener {

    private final CommunityGoals cg;

    public EventsListener(CommunityGoals cg){
        this.cg = cg;
    }

    @EventHandler
    public void onVillagerTrade(InventoryCloseEvent event) {
        if (event.getInventory().getHolder() instanceof Villager) {
            addToGoal(GoalTypes.VILLAGER_TRADE, 1);
        }
    }

    @EventHandler
    public void onIronGolemRepair(PlayerInteractEntityEvent event) {
        if (event.getRightClicked() instanceof IronGolem) {
            Player player = event.getPlayer();
            if (player.getInventory().getItemInMainHand().getType() == Material.IRON_INGOT) {
                addToGoal(GoalTypes.REPAIR_GOLEMS, 1);
            }
        }
    }

    private void addToGoal(GoalTypes type, int amount){
        GoalManager goal = getCurrentGoal(cg.getGoalManager());
        if (goal == null || goal.getType() != type)
            return;

        goal.addAmount(amount);

        if (goal.isDone()) {
            goal.getCommands().forEach(cmd -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd));
            GoalManager nextGoal = getCurrentGoal(cg.getGoalManager());
            Bukkit.broadcastMessage(getColor(cg.getConfig().getString("goal.messages.broadcast.goal_finished")
                    .replace("%goal%", goal.getName())
                    .replace("%next_goal%", nextGoal == null ? "NONE" : nextGoal.getName())));
        }
    }
}
