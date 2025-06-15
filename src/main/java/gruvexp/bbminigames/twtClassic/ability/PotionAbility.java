package gruvexp.bbminigames.twtClassic.ability;

import gruvexp.bbminigames.Main;
import gruvexp.bbminigames.twtClassic.BotBows;
import gruvexp.bbminigames.twtClassic.BotBowsPlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

import java.util.Set;
import java.util.stream.Collectors;

public abstract class PotionAbility extends Ability {

    public static final int RADIUS = 4;

    protected PotionAbility(BotBowsPlayer bp, int hotBarSlot, AbilityType type) {
        super(bp, hotBarSlot, type);
    }

    @Override
    public void use() {
        super.use();
        Set<Player> players = Main.WORLD.getNearbyEntities(bp.player.getLocation(), RADIUS, RADIUS, RADIUS, entity -> entity instanceof Player)
                .stream().map(p -> (Player) p)
                .filter(p -> BotBows.getLobby(p) != null)
                .filter(p -> BotBows.getBotBowsPlayer(p).getTeam() == bp.getTeam())
                .collect(Collectors.toSet());
        players.remove(bp.player);
        applyPotionEffect(players);

        players.forEach(p -> p.sendMessage(Component.text("Got ", NamedTextColor.GREEN)
                .append(Component.text(getEffectDuration()))
                .append(Component.text(getEffectName(), NamedTextColor.DARK_GREEN))
                .append(Component.text(" effect from "))
                .append(bp.player.name())));
    }

    protected abstract void applyPotionEffect(Set<Player> players);

    protected abstract String getEffectName();

    protected abstract int getEffectDuration();
}
