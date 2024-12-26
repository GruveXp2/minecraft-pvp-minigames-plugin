package gruvexp.bbminigames.twtClassic.ability;

import gruvexp.bbminigames.Main;
import gruvexp.bbminigames.twtClassic.BotBowsPlayer;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public abstract class Ability { // each player has some ability objects.

    protected final BotBowsPlayer player;
    protected AbilityType type;

    protected int baseCooldown; // seconds
    protected float cooldownMultiplier = 1.0f;
    private int currentCooldown;
    private int effectiveCooldown;

    private final int hotBarSlot;

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
        effectiveCooldown = (int) (baseCooldown * cooldownMultiplier);
    }

    public int getEffectiveCooldown() {
        return effectiveCooldown;
    }

    public void use() {
        Inventory inv = player.player.getInventory();
        ItemStack cooldownItem = type.getCooldownItem().clone();
        currentCooldown = effectiveCooldown;
        Bukkit.getScheduler().runTaskTimer(Main.getPlugin(), task -> {
            if (currentCooldown == 0) {
                inv.setItem(hotBarSlot, type.getAbilityItem());
                task.cancel();
                return;
            }
            cooldownItem.setAmount(currentCooldown);
            inv.setItem(hotBarSlot, cooldownItem);
            currentCooldown--;
        }, 0L, 20L); // 5 sekunder
    }
}
