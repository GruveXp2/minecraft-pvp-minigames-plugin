package gruvexp.bbminigames.twtClassic.avatar;

import gruvexp.bbminigames.twtClassic.BotBows;
import gruvexp.bbminigames.twtClassic.BotBowsPlayer;
import gruvexp.bbminigames.twtClassic.Cooldowns;
import gruvexp.bbminigames.twtClassic.Lobby;
import org.bukkit.GameMode;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class PlayerAvatar implements BotBowsAvatar{

    public final Player player;
    public final BotBowsPlayer bp;

    public PlayerAvatar(Player player, BotBowsPlayer bp) {
        this.player = player;
        this.bp = bp;
    }


    @Override
    public void eliminate() {
        player.setGameMode(GameMode.SPECTATOR);
    }

    @Override
    public void revive() {
        player.setGameMode(GameMode.ADVENTURE);
    }

    @Override
    public void setHP(int hp) {
        if (hp == 0) { // spilleren dauer(går i spectator) og livene disses resettes
            player.setHealth(1); // kan ikke sette til 0 for da dauer spilleren på ekte og respawner med en gang, spilleren skal isteden settes i spectator mode der spilleren daua
        } else {
            player.setHealth(hp * 2); // halve hjerter
            updateArmor();
        }
    }

    @Override
    public void setMaxHP(int maxHP) {
        player.getAttribute(Attribute.MAX_HEALTH).setBaseValue(maxHP * 2);
        player.setHealth(maxHP * 2);
    }

    @Override
    public void remove() {
        eliminate();
        player.getInventory().remove(BotBows.BOTBOW);
        player.getInventory().setItem(0, BotBows.MENU_ITEM);
    }

    @Override
    public void reset() {
        player.setScoreboard(bp.lobby.botBowsGame.boardManager.manager.getNewScoreboard());
        bp.lobby.botBowsGame.barManager.sneakBars.get(player).setVisible(false);
        player.getInventory().setArmorContents(new ItemStack[]{});
        player.setGlowing(false);
        player.setInvulnerable(false);
        player.getAttribute(Attribute.MAX_HEALTH).setBaseValue(20);
        player.setGameMode(GameMode.SPECTATOR);
        if (Cooldowns.sneakRunnables.containsKey(player)) {
            Cooldowns.sneakRunnables.get(player).cancel();
        }
    }

    @Override
    public void readyBattle() {
        player.getInventory().remove(Lobby.READY.clone()); // removes ready up item
    }
}
