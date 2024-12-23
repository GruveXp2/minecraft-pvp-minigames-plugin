package gruvexp.bbminigames.twtClassic.ability;

import gruvexp.bbminigames.twtClassic.BotBowsPlayer;

public abstract class Ability { // each player has some ability objects.

    protected final BotBowsPlayer player;
    protected AbilityType type;

    protected int maxCooldown;
    protected float cooldownMultiplier = 1.0f;
    protected int cooldown = 0;

    protected int hotBarSlot;

    protected Ability(BotBowsPlayer player, int hotBarSlot) {
        this.player = player;
        this.hotBarSlot = hotBarSlot;
    }

    public abstract AbilityType getType();

    public int getHotBarSlot() {
        return hotBarSlot;
    }

    // everything that all abilities should have and all the methods to customize them

}
