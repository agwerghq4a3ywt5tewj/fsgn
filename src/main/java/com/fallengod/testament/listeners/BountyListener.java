package com.fallengod.testament.listeners;

import com.fallengod.testament.TestamentPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class BountyListener implements Listener {
    
    private final TestamentPlugin plugin;
    
    public BountyListener(TestamentPlugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player target = event.getEntity();
        Player killer = target.getKiller();
        
        // Check if target has a bounty and was killed by another player
        if (killer != null && plugin.getBountyManager().hasBounty(target)) {
            boolean claimed = plugin.getBountyManager().claimBounty(killer, target);
            
            if (claimed) {
                // Add dramatic effects
                killer.getWorld().spawnParticle(org.bukkit.Particle.FIREWORK, killer.getLocation(), 50, 2, 2, 2, 0.1);
                killer.getWorld().playSound(killer.getLocation(), org.bukkit.Sound.UI_TOAST_CHALLENGE_COMPLETE, 2.0f, 1.0f);
                
                // Special death message
                event.setDeathMessage("§c" + target.getName() + " §7was slain by §6" + killer.getName() + " §7and their bounty was claimed!");
            }
        }
    }
}