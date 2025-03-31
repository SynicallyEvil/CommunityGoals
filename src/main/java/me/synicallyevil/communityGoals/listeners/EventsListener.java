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
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;

import java.util.EnumSet;
import java.util.Random;

import static me.synicallyevil.communityGoals.utils.Utils.getColor;
import static me.synicallyevil.communityGoals.utils.Utils.getCurrentGoal;

public class EventsListener implements Listener {

    private final CommunityGoals cg;
    private final Random random = new Random();
    private static final EnumSet<Material> ORES = EnumSet.of(
            Material.COAL_ORE, Material.DEEPSLATE_COAL_ORE,
            Material.IRON_ORE, Material.DEEPSLATE_IRON_ORE,
            Material.COPPER_ORE, Material.DEEPSLATE_COPPER_ORE,
            Material.GOLD_ORE, Material.DEEPSLATE_GOLD_ORE,
            Material.REDSTONE_ORE, Material.DEEPSLATE_REDSTONE_ORE,
            Material.LAPIS_ORE, Material.DEEPSLATE_LAPIS_ORE,
            Material.EMERALD_ORE, Material.DEEPSLATE_EMERALD_ORE,
            Material.DIAMOND_ORE, Material.DEEPSLATE_DIAMOND_ORE
    );

    public EventsListener(CommunityGoals cg){
        this.cg = cg;
    }

    @EventHandler
    public void onVillagerTrade(InventoryClickEvent event) {
        if (event.getView().getTitle().equals(getColor("&bGoal Information"))) {
            event.setCancelled(true);
        }

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
        if (ORES.contains(blockType)) {
            if(cg.getConfig().getBoolean("goal.give_currency_on_ore_break")){
                int amount = getWeightedRandom();
                cg.deposit(player, amount);
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

    private int getWeightedRandom() {
        int roll = random.nextInt(100); // Generate a number between 0-99

        if (roll < 40) return 1; // 40% chance
        if (roll < 70) return 2; // 30% chance
        if (roll < 90) return 3; // 20% chance
        if (roll < 97) return 4; // 7% chance
        return 5;               // 3% chance
    }
}
