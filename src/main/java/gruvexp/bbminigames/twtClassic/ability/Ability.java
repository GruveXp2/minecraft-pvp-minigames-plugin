package gruvexp.bbminigames.twtClassic.ability;

import gruvexp.bbminigames.Main;
import gruvexp.bbminigames.twtClassic.BotBowsPlayer;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public abstract class Ability { // each player has some ability objects.

    protected final BotBowsPlayer player;
    protected AbilityType type;

    protected int baseCooldown; // seconds
    protected float cooldownMultiplier = 1.0f;
    private int effectiveCooldown;

    private final int hotBarSlot;
    private CooldownTimer cooldownTimer;

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

    public void resetCooldown() {
        if (cooldownTimer != null) {
            cooldownTimer.resetCooldown();
        }
    }

    public void use() {
        Inventory inv = player.player.getInventory();
        cooldownTimer = new CooldownTimer(inv);
        cooldownTimer.runTaskTimer(Main.getPlugin(), 0L, 20L);
    }

    private ItemStack getCooldownItem(int cooldown) {
        if (cooldown > 10) {
            return type.getCooldownItems()[0].clone();
        } else if (cooldown > 5) {
            return type.getCooldownItems()[1].clone();
        } else if (cooldown > 2) {
            return type.getCooldownItems()[2].clone();
        } else {
            return type.getCooldownItems()[3].clone();
        }
    }

    private class CooldownTimer extends BukkitRunnable {
        int currentCooldown = effectiveCooldown;
        ItemStack cooldownItem = getCooldownItem(currentCooldown);
        private final Inventory inv;

        private CooldownTimer(Inventory inv) {
            this.inv = inv;
        }

        @Override
        public void run() {
            switch (currentCooldown) {
                case 10 -> cooldownItem = type.getCooldownItems()[1].clone();
                case 5 -> cooldownItem = type.getCooldownItems()[2].clone();
                case 2 -> cooldownItem = type.getCooldownItems()[3].clone();
                case 0 -> {
                    inv.setItem(hotBarSlot, type.getAbilityItem());
                    cancel();
                    return;
                }
            }
            cooldownItem.setAmount(currentCooldown);
            inv.setItem(hotBarSlot, cooldownItem);
            currentCooldown--;
        }

        public void resetCooldown() {
            currentCooldown = 0;
        }
    }
}
