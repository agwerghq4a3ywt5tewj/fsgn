package com.fallengod.testament.commands;

import com.fallengod.testament.data.PlayerData;
import com.fallengod.testament.managers.PlayerDataManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BountyCommand implements CommandExecutor {

    private final PlayerDataManager playerDataManager;

    public BountyCommand(PlayerDataManager playerDataManager) {
        this.playerDataManager = playerDataManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;
        PlayerData data = playerDataManager.getPlayerData(player.getUniqueId());
        Map<UUID, List<ItemStack>> rewards = data.getPendingBountyRewards();

        if (rewards.isEmpty()) {
            player.sendMessage(ChatColor.RED + "You have no bounty rewards to claim.");
            return true;
        }

        for (Map.Entry<UUID, List<ItemStack>> entry : rewards.entrySet()) {
            for (ItemStack item : entry.getValue()) {
                player.getInventory().addItem(item);
            }
            data.clearBountyRewards(entry.getKey());
        }

        player.sendMessage(ChatColor.GREEN + "You claimed all your bounty rewards!");
        return true;
    }
}