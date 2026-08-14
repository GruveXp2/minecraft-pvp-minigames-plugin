package gruvexp.bbminigames.twtClassic.ability.abilities;

import gruvexp.bbminigames.Main;
import gruvexp.bbminigames.twtClassic.BotBowsPlayer;
import gruvexp.bbminigames.twtClassic.ability.AbilityType;
import gruvexp.bbminigames.twtClassic.ability.PotionAbility;
import org.bukkit.Bukkit;

import java.util.Set;

public class ChargePotion extends PotionAbility {

    public static final int DURATION = 20;

    public ChargePotion(BotBowsPlayer bp, int hotBarSlot) {
        super(bp, hotBarSlot, AbilityType.CHARGE_POTION);
        this.baseCooldown = type.baseCooldown;
    }

    @Override
    public void use() {
        super.use();
        bp.obtainWeaponAbilities();
    }

    @Override
    protected void applyPotionEffect(Set<BotBowsPlayer> players) {
        bp.setAbilityCooldownTickRate(10);
        players.forEach(bp -> {
                    bp.setAbilityCooldownTickRate(13);
                    bp.obtainWeaponAbilities();
        });
        registerSuccess();

        Bukkit.getScheduler().runTaskLater(Main.getPlugin(), _ -> {
            bp.setAbilityCooldownTickRate(20);
            players.forEach(bp -> bp.setAbilityCooldownTickRate(20));
            registerSuccess();
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
