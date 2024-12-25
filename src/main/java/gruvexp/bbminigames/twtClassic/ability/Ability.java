package gruvexp.bbminigames.twtClassic.ability;

import gruvexp.bbminigames.Main;
import gruvexp.bbminigames.twtClassic.BotBowsPlayer;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public abstract class Ability { // each player has some ability objects.

    protected final BotBowsPlayer player;
    protected AbilityType type;

    protected int maxCooldown; // seconds
    protected float cooldownMultiplier = 1.0f;
    protected int cooldown = 0;

    protected int hotBarSlot;

    protected Ability(BotBowsPlayer player, int hotBarSlot) {
        this.player = player;
        this.hotBarSlot = hotBarSlot;
    }

    public AbilityType getType() {
        return type;
    }

    public int getHotBarSlot() {
        return hotBarSlot;
    }

    public void setCooldownMultiplier(float multiplier) {
        this.cooldownMultiplier = multiplier;
    }

    public void use() {
        Inventory inv = player.player.getInventory();
        ItemStack cooldownItem = type.getCooldownItem().clone();
        cooldown = (int) (maxCooldown * cooldownMultiplier);
        Bukkit.getScheduler().runTaskTimer(Main.getPlugin(), task -> {
            if (cooldown == 0) {
                inv.setItem(hotBarSlot, type.getAbilityItem());
                task.cancel();
                return;
            }
            cooldownItem.setAmount(cooldown);
            inv.setItem(hotBarSlot, cooldownItem);
            cooldown--;
        }, 0L, 20L); // 5 sekunder
    }
}
