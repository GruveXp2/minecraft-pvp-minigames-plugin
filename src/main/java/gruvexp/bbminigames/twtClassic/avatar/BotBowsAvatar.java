package gruvexp.bbminigames.twtClassic.avatar;

import gruvexp.bbminigames.twtClassic.BotBowsPlayer;
import gruvexp.bbminigames.twtClassic.ability.Ability;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.entity.Entity;
import org.bukkit.potion.PotionEffect;

public interface BotBowsAvatar {
    void message(Component component);
    Entity getEntity();
    BotBowsPlayer getBotBowsPlayer();
    void eliminate();
    void revive();
    void setHP(int hp);
    void setMaxHP(int maxHP);
    void remove(); // removes the player from the game
    void reset(); // its like removing and recreating this avatar, but reusing the object
    void readyBattle();
    void setReady(boolean ready, int itemIndex);
    int getNextFreeSlot();
    void removeAbility(Ability ability);
    void damage();
    void setGlowing(boolean flag);
    void addPotionEffect(PotionEffect effect);
    void setColor(TextColor color);
    void growSize(double scale, int duration);
}
