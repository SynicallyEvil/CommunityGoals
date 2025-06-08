package me.synicallyevil.communityGoals.listeners;

import me.synicallyevil.communityGoals.CommunityGoals;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerEvents implements Listener {

    private final CommunityGoals plugin;

    public PlayerEvents(CommunityGoals plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        plugin.getGoalsManager().getOnlineSince().put(event.getPlayer().getUniqueId(), System.currentTimeMillis());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        plugin.getGoalsManager().getOnlineSince().remove(event.getPlayer().getUniqueId());
    }
}
