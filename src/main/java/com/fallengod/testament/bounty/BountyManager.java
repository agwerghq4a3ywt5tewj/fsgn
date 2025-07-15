package com.fallengod.testament.bounty;

import com.fallengod.testament.data.PlayerData;
import com.fallengod.testament.managers.PlayerDataManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class BountyManager {

    private final Map<UUID, Bounty> activeBounties = new ConcurrentHashMap<>();
    private final PlayerDataManager playerDataManager;

    public BountyManager(PlayerDataManager playerDataManager) {
        this.playerDataManager = playerDataManager;
    }

    public void placeBounty(Player target, String reason) {
        if (target == null) return;

        Bounty bounty = new Bounty(target.getUniqueId(), reason);
        activeBounties.put(target.getUniqueId(), bounty);

        Bukkit.getLogger().info("[BountyManager] Bounty placed on "
                + target.getName() + " (" + reason + ")");
    }

    public Collection<Bounty> getActiveBounties() {
        return Collections.unmodifiableCollection(activeBounties.values());
    }

    public Bounty getBounty(UUID targetUUID) {
        return activeBounties.get(targetUUID);
    }

    public void removeBounty(UUID targetUUID) {
        activeBounties.remove(targetUUID);
    }

    public boolean hasBounty(UUID targetUUID) {
        return activeBounties.containsKey(targetUUID);
    }

    public void clearAllBounties() {
        activeBounties.clear();
        Bukkit.getLogger().info("[BountyManager] All bounties cleared.");
    }

    // === Handle Bounty Kill ===
    public void handleBountyKill(Player killer, Player victim) {
        Bounty bounty = activeBounties.remove(victim.getUniqueId());
        if (bounty == null) return;

        ItemStack reward = generateScaledReward(victim);

        PlayerData killerData = playerDataManager.getPlayerData(killer.getUniqueId());
        killerData.addBountyReward(victim.getUniqueId(), reward);

        killer.sendMessage("§6You earned a bounty reward for killing " +
                victim.getName() + "! Use §e/bounty claim §6to collect it.");
    }

    // === Scaled Rewards ===
    private ItemStack generateScaledReward(Player victim) {
        long serverDays = getServerUptimeDays();
        int bountyCount = activeBounties.size();
        int victimLevel = victim.getLevel();

        if (serverDays < 10) {
            return new ItemStack(Material.IRON_INGOT, 3 + victimLevel / 5);
        } else if (serverDays < 30) {
            return new ItemStack(Material.DIAMOND, 1 + bountyCount / 3);
        } else {
            return new ItemStack(Material.NETHERITE_SCRAP, 1 + bountyCount / 5);
        }
    }

    private long getServerUptimeDays() {
        long uptime = System.currentTimeMillis() - Bukkit.getServer().getWorlds().get(0).getFullTime();
        return uptime / (1000L * 60 * 60 * 24);
    }
}