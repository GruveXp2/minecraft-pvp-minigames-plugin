package gruvexp.bbminigames.twtClassic.ability;

import gruvexp.bbminigames.twtClassic.BotBowsPlayer;
import org.bukkit.inventory.ItemStack;

public abstract class Ability { // each player has some ability objects.

    protected final BotBowsPlayer player;
    protected ItemStack abilityItem;
    protected ItemStack cooldownItem;

    protected int maxCooldown;
    protected float cooldownMultiplier = 1.0f;
    protected int cooldown = 0;

    protected int hotBarSlot;

    protected Ability(BotBowsPlayer player) {
        this.player = player;
        // find the next free hotbarslot and place it there
    }

    // everything that all abilities should have and all the methods to customize them

}
