package com.fallengod.testament.bounty;

import com.fallengod.testament.TestamentPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class BountyListener implements Listener {

    private final TestamentPlugin plugin;
    private final BountyManager bountyManager;

    public BountyListener(TestamentPlugin plugin) {
        this.plugin = plugin;
        this.bountyManager = plugin.getBountyManager();
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player deceased = event.getEntity();
        Player killer = deceased.getKiller();

        if (killer == null) return; // No killer (e.g., died from environment)

        if (bountyManager.hasBounty(deceased.getUniqueId())) {
            Bounty bounty = bountyManager.getBounty(deceased.getUniqueId());

            // Remove the bounty now that target is dead
            bountyManager.removeBounty(deceased.getUniqueId());

            // Reward the killer - you can customize this reward system
            double amount = bounty.getAmount();
            // For example, you might add currency or items here.
            // For now, let's just notify the killer:

            killer.sendMessage(ChatColor.GOLD + "You have claimed a bounty worth " + amount + " coins for killing " + deceased.getName() + "!");
            Bukkit.getLogger().info("[Bounty] " + killer.getName() + " claimed bounty on " + deceased.getName() + " for " + amount);

            // Broadcast to all players
            Bukkit.broadcastMessage(ChatColor.RED + "[Bounty] " + killer.getName() + " has claimed a bounty on " + deceased.getName() + "!");

            // You can expand this to add actual rewards (money, items) or special effects
        }
    }
}