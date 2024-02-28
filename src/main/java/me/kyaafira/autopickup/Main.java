package me.kyaafira.autopickup;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Objects;

public class Main extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        saveDefaultConfig();
        getServer().getPluginManager().registerEvents(this, this);

    }

    @EventHandler
    public void onBreak(@NotNull BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        PlayerInventory inventory = player.getInventory();
        @NotNull Collection<ItemStack> drops = block.getDrops(inventory.getItemInMainHand());
        boolean isFull = false;
        ItemStack[] itemsToAdd = new ItemStack[drops.toArray().length];
        int itemCount = 0;

        if (isWorldBlacklisted(player) || event.isCancelled()) return;

        for (ItemStack drop : drops) {
            if (player.getInventory().firstEmpty() == -1) {
                isFull = true;
            }

            itemsToAdd[itemCount] = drop;
            itemCount++;
        }

        if (itemCount > 0) {
            for (int i = 0; i < itemCount; i++) {
                if (!isFull) {
                    player.getInventory().addItem(itemsToAdd[i]);
                    player.giveExp(event.getExpToDrop());
                } else {
                    player.sendMessage(Objects.requireNonNull(Objects.requireNonNull(this.getConfig().getString("full-inventory")).replace("&", "§")));
                    event.setCancelled(true);
                }
            }
        }

        event.setDropItems(false);
        event.setExpToDrop(0);
    }

    public boolean isWorldBlacklisted(Player player) {
        return Objects.requireNonNull(this.getConfig().getList("worlds")).contains(player.getWorld().getName());
    }
}
