package gruvexp.bbminigames.twtClassic.ability.abilities;

import gruvexp.bbminigames.Main;
import gruvexp.bbminigames.twtClassic.BotBows;
import gruvexp.bbminigames.twtClassic.BotBowsPlayer;
import gruvexp.bbminigames.twtClassic.ability.AbilityType;
import gruvexp.bbminigames.twtClassic.ability.PotionAbility;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Set;

public class ChargePotionAbility extends PotionAbility {

    public static final int DURATION = 20;

    public ChargePotionAbility(BotBowsPlayer bp, int hotBarSlot) {
        super(bp, hotBarSlot, AbilityType.CHARGE_POTION);
        this.type = AbilityType.CHARGE_POTION;
        this.baseCooldown = type.getBaseCooldown();
    }

    @Override
    protected void applyPotionEffect(Set<Player> players) {
        bp.setAbilityCooldownTickRate(10);
        players.stream()
                .map(p -> BotBows.getLobby(p).getBotBowsPlayer(p))
                .forEach(p -> {
                    p.setAbilityCooldownTickRate(13);
                    p.obtainWeaponAbilities();
                });

        Bukkit.getScheduler().runTaskLater(Main.getPlugin(), bukkitTask -> {
            bp.setAbilityCooldownTickRate(20);
            players.stream()
                    .map(BotBows::getBotBowsPlayer)
                    .forEach(p -> p.setAbilityCooldownTickRate(20));
        }, 20L * DURATION);
    }

    @Override
    protected String getEffectName() {
        return "Charge";
    }

    @Override
    protected int getEffectDuration() {
        return (int) (DURATION * 0.75);
    }
}
