package me.synicallyevil.communityGoals.listeners;

import me.synicallyevil.communityGoals.CommunityGoals;
import me.synicallyevil.communityGoals.goals.GoalsManager;
import me.synicallyevil.communityGoals.goals.enums.GoalType;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerInteractEntityEvent;

public class EntityEvents implements Listener {

    private final CommunityGoals plugin;
    private final GoalsManager manager;

    public EntityEvents(CommunityGoals plugin) {
        this.plugin = plugin;
        this.manager = plugin.getGoalsManager();
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity().getKiller() == null || (event.getEntity() instanceof Player)) return;

        Player player = event.getEntity().getKiller();

        manager.getActiveGoals().forEach(goal -> {
            if (goal.getType() == GoalType.MOB_KILL) {
                if (manager.checkRequirements(goal, player, player.getWorld().getName(), event.getEntity().getType().getName(), player.getInventory().getItemInMainHand().getType().name())) {
                    manager.handleGoalProgress(goal, 1);
                }
            }
        });
    } // Goal goal, @Nullable Player player, @Nullable String world, @Nullable String target, @Nullable String tool

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event){
        if (event.getEntity().getKiller() == null) return;

        Player player = event.getEntity().getKiller();
        Player target = event.getEntity();

        manager.getActiveGoals().forEach(goal -> {
            if (goal.getType() == GoalType.PLAYER_KILL) {
                if (manager.checkRequirements(goal, player, player.getWorld().getName(), null, player.getInventory().getItemInMainHand().getType().name())) {
                    manager.handleGoalProgress(goal, 1);
                }
            }
        });

        manager.getActiveGoals().forEach(goal -> {
            if (goal.getType() == GoalType.PLAYER_DEATH) {
                if (manager.checkRequirements(goal, target, player.getWorld().getName(), null, player.getInventory().getItemInMainHand().getType().name())) {
                    manager.handleGoalProgress(goal, 1);
                }
            }
        });
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event){
        if(event.getDamager() instanceof Player player && event.getEntity() instanceof LivingEntity entity) {
            manager.getActiveGoals().forEach(goal -> {
                if (goal.getType() == GoalType.DAMAGE_DEALT) {
                    if (manager.checkRequirements(goal, player, player.getWorld().getName(), entity.getType().name(), player.getInventory().getItemInMainHand().getType().name())) {
                        manager.handleGoalProgress(goal, (int)event.getDamage());
                    }
                }
            });
        }
    }

    @EventHandler
    public void onDamageTaken(EntityDamageEvent event){
        if(event.getEntity() instanceof Player player) {
            manager.getActiveGoals().forEach(goal -> {
                if (goal.getType() == GoalType.DAMAGE_TAKEN) {
                    if (manager.checkRequirements(goal, player, player.getWorld().getName(), null, player.getInventory().getItemInMainHand().getType().name())) {
                        manager.handleGoalProgress(goal, (int)event.getDamage());
                    }
                }
            });
        }
    }

    @EventHandler
    public void onIronGolemRepair(PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() instanceof IronGolem golem))
            return;

        Player player = event.getPlayer();
        if (player.getInventory().getItemInMainHand().getType() != Material.IRON_INGOT) {
            return;
        }

        AttributeInstance maxHealthAttr = golem.getAttribute(Attribute.MAX_HEALTH);
        if (maxHealthAttr == null) {
            return;
        }
        double maxHealth = maxHealthAttr.getBaseValue();
        double initialHealth = golem.getHealth();

        if (initialHealth < maxHealth) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                double currentHealth = golem.getHealth();
                if (currentHealth > initialHealth) {
                    manager.getActiveGoals().forEach(goal -> {
                        if (goal.getType() == GoalType.IRON_GOLEM_REPAIR) {
                            if (manager.checkRequirements(goal, player, player.getWorld().getName(), null, null)) {
                                manager.handleGoalProgress(goal, 1);
                            }
                        }
                    });
                }
            }, 1L);
        }
    }

    @EventHandler
    public void onEntityBreed(EntityBreedEvent event){
        if(event.getBreeder() instanceof Player player) {
            manager.getActiveGoals().forEach(goal -> {
                if (goal.getType() == GoalType.ANIMAL_BREED) {
                    if (manager.checkRequirements(goal, player, player.getWorld().getName(), event.getEntity().getType().name(), null)) {
                        manager.handleGoalProgress(goal, 1);
                    }
                }
            });
        }
    }
}