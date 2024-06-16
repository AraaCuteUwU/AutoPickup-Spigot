package me.kyaafira.autopickup;

import org.bukkit.GameMode;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Objects;

public final class Main extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        saveDefaultConfig();
        getServer().getPluginManager().registerEvents(this, this);
    }

    public boolean hasMending(Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();
        return item.containsEnchantment(Enchantment.MENDING);
    }

    public void repairItem(Player player, int exp) {
        ItemStack item = player.getInventory().getItemInMainHand();
        final short maxDurability = item.getType().getMaxDurability();
        if (maxDurability != 0) {
            final ItemMeta meta = item.getItemMeta();
            if ((meta instanceof Damageable damageable)) {
                if (damageable.hasDamage()) {
                    final int damage = damageable.getDamage();
                    if (damage > 0) {
                        if (hasMending(player)) {
                            final int newDamage = damage - (exp * 2);
                            damageable.setDamage(Math.max(newDamage, 0));
                            item.setItemMeta(damageable);
                            player.getInventory().setItemInMainHand(item);
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onBreak(@NotNull BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        int exp = event.getExpToDrop();
        @NotNull Collection<ItemStack> drops = block.getDrops();
        boolean isFull = false;
        ItemStack[] itemsToAdd = new ItemStack[drops.toArray().length];
        int itemCount = 0;

        if (isWorldBlacklisted(player) || event.isCancelled() || !player.getGameMode().equals(GameMode.SURVIVAL)) return;

        for (ItemStack drop : drops) {
            if (player.getInventory().firstEmpty() == -1) {
                isFull = true;
            }
            itemsToAdd[itemCount] = drop;
            itemCount++;
        }

        if (itemCount > 0) {
            for (int i = 0; i < itemCount; i++) {
                player.getInventory().addItem(itemsToAdd[i]);
                player.giveExp(exp);
                repairItem(player, exp);
            }
        }

        event.setExpToDrop(0);
        event.setDropItems(false);

        if (isFull) {
            player.sendMessage(Objects.requireNonNull(Objects.requireNonNull(this.getConfig().getString("full-inventory")).replace("&", "§")));
            event.setCancelled(true);
        }

    }

    public boolean isWorldBlacklisted(Player player) {
        return Objects.requireNonNull(this.getConfig().getList("worlds")).contains(player.getWorld().getName());
    }
}
