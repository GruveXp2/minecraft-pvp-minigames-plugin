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

    protected KarmaPotion(BotBowsPlayer player, int hotBarSlot) {
        super(player, hotBarSlot);
        this.type = AbilityType.KARMA_POTION;
        this.baseCooldown = type.getBaseCooldown();
    }

    @Override
    protected void use(Set<Player> players) {
        player.setKarmaEffect(true);
        players.stream()
                .map(p -> BotBows.getLobby(p).getBotBowsPlayer(p))
                .forEach(p -> p.setKarmaEffect(true));

        Bukkit.getScheduler().runTaskLater(Main.getPlugin(), bukkitTask -> player.setKarmaEffect(false), 20L * DURATION);
        Bukkit.getScheduler().runTaskLater(Main.getPlugin(), bukkitTask -> players.stream()
                .map(p -> BotBows.getLobby(p).getBotBowsPlayer(p))
                .forEach(p -> p.setKarmaEffect(false)), 15L * DURATION);
    }
}
