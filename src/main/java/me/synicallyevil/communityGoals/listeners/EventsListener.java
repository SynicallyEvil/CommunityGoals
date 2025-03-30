package me.synicallyevil.communityGoals.listeners;

import me.synicallyevil.communityGoals.CommunityGoals;
import me.synicallyevil.communityGoals.managers.GoalManager;
import me.synicallyevil.communityGoals.utils.GoalTypes;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;

import static me.synicallyevil.communityGoals.utils.Utils.getColor;
import static me.synicallyevil.communityGoals.utils.Utils.getCurrentGoal;

public class EventsListener implements Listener {

    private final CommunityGoals cg;

    public EventsListener(CommunityGoals cg){
        this.cg = cg;
    }

    @EventHandler
    public void onVillagerTrade(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof Villager)) return;
        if (!(event.getWhoClicked() instanceof Player player)) return;

        // Slot 2 is the result slot in a villager trade inventory
        if (event.getSlotType() != InventoryType.SlotType.RESULT) return;

        ItemStack clickedItem = event.getCurrentItem();

        // Ensure the player actually clicked a valid trade result
        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

        // Trade was successful, add to goal
        addToGoal(GoalTypes.VILLAGER_TRADE, 1);
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

        // Check if the golem is damaged and not at full health
        if (initialHealth < maxHealth) {
            // Schedule a small delay to check if the health increased
            Bukkit.getScheduler().runTaskLater(cg, () -> {
                if (golem.getHealth() > initialHealth) {
                    addToGoal(GoalTypes.REPAIR_GOLEMS, 1);
                }
            }, 1L); // 1 tick delay to allow the repair action to apply
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
