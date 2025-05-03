package gruvexp.bbminigames.twtClassic.ability.abilities;

import gruvexp.bbminigames.Main;
import gruvexp.bbminigames.twtClassic.BotBows;
import gruvexp.bbminigames.twtClassic.BotBowsPlayer;
import gruvexp.bbminigames.twtClassic.ability.AbilityType;
import gruvexp.bbminigames.twtClassic.ability.PotionAbility;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Set;

public class KarmaPotion extends PotionAbility {

    public static final int DURATION = 20;

    public KarmaPotion(BotBowsPlayer bp, int hotBarSlot) {
        super(bp, hotBarSlot, AbilityType.KARMA_POTION);
    }

    @Override
    protected void use(Set<Player> players) {
        bp.setKarmaEffect(true);
        players.stream()
                .map(p -> BotBows.getLobby(p).getBotBowsPlayer(p))
                .forEach(p -> p.setKarmaEffect(true));

        Bukkit.getScheduler().runTaskLater(Main.getPlugin(), bukkitTask -> bp.setKarmaEffect(false), 20L * DURATION);
        Bukkit.getScheduler().runTaskLater(Main.getPlugin(), bukkitTask -> players.stream()
                .map(p -> BotBows.getLobby(p).getBotBowsPlayer(p))
                .forEach(p -> p.setKarmaEffect(false)), 15L * DURATION);
    }
}
