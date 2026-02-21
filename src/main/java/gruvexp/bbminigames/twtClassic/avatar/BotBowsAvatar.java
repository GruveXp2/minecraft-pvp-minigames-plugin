package gruvexp.bbminigames.twtClassic.avatar;

import gruvexp.bbminigames.twtClassic.BotBowsPlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.util.UUID;

public interface BotBowsAvatar {
    void message(Component component);
    LivingEntity getEntity();
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
    void damage();
    void setGlowing(boolean flag);
    void addPotionEffect(PotionEffect effect);
    void setColor(TextColor color);
    void growSize(double scale, int duration, int delay);
    default void growSize(double scale, int duration) {growSize(scale, duration, 0);}
    UUID getUUID();
    boolean isSneaking();
    void updateSneakStamina(float progress);
    ItemStack getHeadItem();
    void setItem(int index, ItemStack item);
    void setInvis(boolean invis);
    default Location getLocation() {return getEntity().getLocation();}
}
