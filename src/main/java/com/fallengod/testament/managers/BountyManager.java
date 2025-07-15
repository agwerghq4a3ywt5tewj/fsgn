package com.fallengod.testament.managers;

import com.fallengod.testament.TestamentPlugin;
import com.fallengod.testament.data.Bounty;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BountyManager {
    private final TestamentPlugin plugin;
    private final Map<UUID, Bounty> activeBounties;
    
    public BountyManager(TestamentPlugin plugin) {
        this.plugin = plugin;
        this.activeBounties = new HashMap<>();
        
        // Start cleanup task for expired bounties
        startCleanupTask();
    }
    
    public void placeBounty(Player target, double amount, String reason) {
        if (!plugin.getConfigManager().isBountySystemEnabled()) {
            return;
        }
        
        UUID targetId = target.getUniqueId();
        long duration = plugin.getConfigManager().getBountyDurationMinutes();
        
        Bounty bounty = new Bounty(targetId, amount, reason, duration);
        activeBounties.put(targetId, bounty);
        
        // Broadcast bounty placement
        String message = plugin.getConfigManager().getBountyPlacedMessage()
            .replace("{PLAYER}", target.getName())
            .replace("{AMOUNT}", String.valueOf((int)amount))
            .replace("{REASON}", reason)
            .replace("{DURATION}", String.valueOf(duration));
        
        plugin.getServer().broadcastMessage(message);
        
        plugin.getLogger().info("Bounty placed on " + target.getName() + " for " + amount + " - " + reason);
    }
    
    public boolean claimBounty(Player killer, Player target) {
        if (!plugin.getConfigManager().isBountySystemEnabled()) {
            return false;
        }
        
        UUID targetId = target.getUniqueId();
        Bounty bounty = activeBounties.get(targetId);
        
        if (bounty == null || bounty.isExpired()) {
            return false;
        }
        
        // Remove bounty
        activeBounties.remove(targetId);
        
        // Reward killer
        rewardPlayer(killer, bounty.getAmount());
        
        // Broadcast bounty claim
        String message = plugin.getConfigManager().getBountyClaimedMessage()
            .replace("{KILLER}", killer.getName())
            .replace("{TARGET}", target.getName())
            .replace("{AMOUNT}", String.valueOf((int)bounty.getAmount()))
            .replace("{REASON}", bounty.getReason());
        
        plugin.getServer().broadcastMessage(message);
        
        plugin.getLogger().info(killer.getName() + " claimed bounty on " + target.getName() + " for " + bounty.getAmount());
        
        return true;
    }
    
    public boolean hasBounty(Player player) {
        Bounty bounty = activeBounties.get(player.getUniqueId());
        return bounty != null && !bounty.isExpired();
    }
    
    public Bounty getBounty(Player player) {
        return activeBounties.get(player.getUniqueId());
    }
    
    public Map<UUID, Bounty> getActiveBounties() {
        // Remove expired bounties before returning
        activeBounties.entrySet().removeIf(entry -> entry.getValue().isExpired());
        return new HashMap<>(activeBounties);
    }
    
    private void rewardPlayer(Player player, double amount) {
        // Give diamond reward (configurable in future)
        int diamonds = (int) amount;
        ItemStack reward = new ItemStack(Material.DIAMOND, diamonds);
        
        if (player.getInventory().firstEmpty() == -1) {
            // Drop at player location if inventory is full
            player.getWorld().dropItemNaturally(player.getLocation(), reward);
            player.sendMessage("§6Bounty reward dropped at your feet! (Inventory full)");
        } else {
            player.getInventory().addItem(reward);
        }
        
        player.sendMessage("§6§lBounty Claimed! §7You received " + diamonds + " diamonds!");
    }
    
    private void startCleanupTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                int removed = 0;
                var iterator = activeBounties.entrySet().iterator();
                
                while (iterator.hasNext()) {
                    var entry = iterator.next();
                    if (entry.getValue().isExpired()) {
                        iterator.remove();
                        removed++;
                        
                        // Notify that bounty expired
                        Player target = plugin.getServer().getPlayer(entry.getKey());
                        if (target != null) {
                            target.sendMessage("§7Your bounty has expired.");
                        }
                    }
                }
                
                if (removed > 0) {
                    plugin.getLogger().info("Cleaned up " + removed + " expired bounties");
                }
            }
        }.runTaskTimer(plugin, 0L, 1200L); // Every minute
    }
}