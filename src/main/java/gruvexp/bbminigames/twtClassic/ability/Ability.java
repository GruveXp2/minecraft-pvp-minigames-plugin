package gruvexp.bbminigames.twtClassic.ability;

import gruvexp.bbminigames.Main;
import gruvexp.bbminigames.twtClassic.BotBows;
import gruvexp.bbminigames.twtClassic.BotBowsPlayer;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class Ability {

    protected final BotBowsPlayer bp;
    protected AbilityType type;

    protected int baseCooldown; // seconds
    protected float cooldownMultiplier = 1.0f;
    private int effectiveCooldown;

    private final int hotBarSlot;
    private CooldownTimer cooldownTimer;

    public Ability(BotBowsPlayer bp, int hotBarSlot, AbilityType type) {
        this.bp = bp;
        this.hotBarSlot = hotBarSlot;
        this.type = type;
        if (type.category != AbilityCategory.DAMAGING) {
            this.baseCooldown = type.getBaseCooldown();
        }
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
            cooldownTimer.cancel();
            cooldownTimer = null;
        }
    }

    public void obtain() {
        resetCooldown();
        Inventory inv = bp.player.getInventory();
        inv.setItem(hotBarSlot, type.getAbilityItem());
    }

    public void hit() {
        if (cooldownTimer != null) {
            cooldownTimer.hit();
        }
    }

    public void use() {
        if (!bp.lobby.botBowsGame.canMove) return;
        Inventory inv = bp.player.getInventory();
        if (type.category == AbilityCategory.DAMAGING) {
            inv.setItem(hotBarSlot, type.getCooldownItems()[0].clone());
        } else {
            cooldownTimer = new CooldownTimer(inv);
            cooldownTimer.tickCooldown(20);
        }
    }

    public void setTickRate(int tickRate) {
        if (cooldownTimer != null) {
            cooldownTimer.tickCooldown(tickRate);
        }
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
        private BukkitTask task;

        private CooldownTimer(Inventory inv) {
            this.inv = inv;
        }

        @Override
        public void run() {
            if (currentCooldown <= 0) {
                obtain();
                this.cancel();
                return;
            }

            switch (currentCooldown) {
                case 10 -> cooldownItem = type.getCooldownItems()[1].clone();
                case 5 -> cooldownItem = type.getCooldownItems()[2].clone();
                case 2 -> cooldownItem = type.getCooldownItems()[3].clone();
            }
            cooldownItem.setAmount(currentCooldown);
            inv.setItem(hotBarSlot, cooldownItem);
            currentCooldown--;
        }

        public void hit() { // when someone hits you with a bow, the cooldown wont go down until the damage cooldown is complete (when barrier blocks get removed)
            currentCooldown += BotBows.HIT_DISABLED_ITEM_TICKS / 20;
        }

        public void tickCooldown(int tickRate) {
            if (task != null) task.cancel();
            task = this.runTaskTimer(Main.getPlugin(), 0L, tickRate);
        }
    }
}
