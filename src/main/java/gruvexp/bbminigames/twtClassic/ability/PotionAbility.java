package gruvexp.bbminigames.twtClassic.ability;

import gruvexp.bbminigames.Main;
import gruvexp.bbminigames.twtClassic.BotBows;
import gruvexp.bbminigames.twtClassic.BotBowsPlayer;
import org.bukkit.entity.Player;

import java.util.Set;
import java.util.stream.Collectors;

public abstract class PotionAbility extends Ability {

    protected static final int RADIUS = 4;

    protected PotionAbility(BotBowsPlayer bp, int hotBarSlot, AbilityType type) {
        super(bp, hotBarSlot, type);
    }

    @Override
    public void use() {
        super.use();
        Set<Player> players = Main.WORLD.getNearbyEntities(bp.player.getLocation(), RADIUS, RADIUS, RADIUS, entity -> entity instanceof Player)
                .stream().map(p -> (Player) p)
                .filter(p -> BotBows.getLobby(p) != null)
                .filter(p -> BotBows.getLobby(p).getBotBowsPlayer(p).getTeam() == bp.getTeam())
                .collect(Collectors.toSet());
        players.remove(bp.player);
        use(players);
    }

    protected abstract void use(Set<Player> players);
}
