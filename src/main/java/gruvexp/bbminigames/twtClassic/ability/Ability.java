package gruvexp.bbminigames.twtClassic.ability;

import gruvexp.bbminigames.Main;
import gruvexp.bbminigames.twtClassic.BotBows;
import gruvexp.bbminigames.twtClassic.BotBowsPlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class Ability {

    protected final BotBowsPlayer bp;
    protected AbilityType type;

    protected int baseCooldown; // seconds
    protected float cooldownMultiplier = 1.0f;
    private int effectiveCooldown;

    private final int hotBarSlot;
    private CooldownTimer cooldownTimer;
    private int cooldownTickRate = 20;

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
        bp.avatar.setItem(hotBarSlot, type.getAbilityItem());
    }

    public void lose() {
        if (type.category == AbilityCategory.DAMAGING) {
            bp.avatar.setItem(hotBarSlot, type.getCooldownItems()[0].clone());
        } else {
            cooldownTimer = new CooldownTimer(bp, effectiveCooldown);
            cooldownTimer.runTaskTimer(Main.getPlugin(), 0L, cooldownTickRate);
        }
    }

    public void hit() {
        if (cooldownTimer != null) {
            cooldownTimer.hit();
        }
    }

    public void use() {
        if (bp.lobby.botBowsGame != null && !bp.lobby.botBowsGame.canMove) return; // null check used when testing ability outside of match

        if (type.category == AbilityCategory.DAMAGING) {
            bp.loseWeaponAbilities();
        } else {
            cooldownTimer = new CooldownTimer(bp, effectiveCooldown);
            cooldownTimer.runTaskTimer(Main.getPlugin(), 0L, cooldownTickRate);
        }
    }

    public void setTickRate(int tickRate) {
        cooldownTickRate = tickRate;
        if (cooldownTimer != null) {
            cooldownTimer.cancel();
            int cooldownLeft = cooldownTimer.currentCooldown;

            cooldownTimer = new CooldownTimer(bp, cooldownLeft);
            cooldownTimer.runTaskTimer(Main.getPlugin(), 0L, cooldownTickRate);
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

    public void unequip() {

    }

    public void reset() {

    }

    private class CooldownTimer extends BukkitRunnable {
        int currentCooldown;
        ItemStack cooldownItem = getCooldownItem(currentCooldown);
        private final BotBowsPlayer bp;

        private CooldownTimer(BotBowsPlayer bp, int effectiveCooldown) {
            this.bp = bp;
            currentCooldown = effectiveCooldown;
        }

        @Override
        public void run() {
            if (currentCooldown <= 0) {
                obtain();
                return;
            }

            switch (currentCooldown) {
                case 10 -> cooldownItem = type.getCooldownItems()[1].clone();
                case 5 -> cooldownItem = type.getCooldownItems()[2].clone();
                case 2 -> cooldownItem = type.getCooldownItems()[3].clone();
            }
            cooldownItem.setAmount(currentCooldown);
            bp.avatar.setItem(hotBarSlot, cooldownItem);
            currentCooldown--;
        }

        public void hit() { // when someone hits you with a bow, the cooldown wont go down until the damage cooldown is complete (when barrier blocks get removed)
            currentCooldown += BotBows.HIT_DISABLED_ITEM_TICKS / 20;
        }
    }
}
