package gruvexp.bbminigames.twtClassic.avatar;

import gruvexp.bbminigames.Main;
import gruvexp.bbminigames.twtClassic.BotBows;
import gruvexp.bbminigames.twtClassic.BotBowsPlayer;
import gruvexp.bbminigames.twtClassic.hazard.HazardType;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mannequin;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class NpcAvatar implements BotBowsAvatar{

    private Mannequin mannequin;
    private final BotBowsPlayer bp;
    private int visualHp = -1;

    public NpcAvatar(Mannequin mannequin, BotBowsPlayer bp) {
        this.mannequin = mannequin;
        this.bp = bp;
    }


    @Override
    public void message(Component component) {

    }

    @Override
    public LivingEntity getEntity() {
        return mannequin;
    }

    @Override
    public BotBowsPlayer getBotBowsPlayer() {
        return bp;
    }

    @Override
    public void eliminate() {
        Mannequin newMannequin = (Mannequin) Main.WORLD.spawnEntity(bp.getTeam().tribunePos, EntityType.MANNEQUIN);
        newMannequin.setProfile(mannequin.getProfile());
        newMannequin.customName(mannequin.customName());
        newMannequin.setCustomNameVisible(true);
        UUID oldId = getUUID();
        UUID newId = newMannequin.getUniqueId();
        BotBows.replacePlayerId(oldId, newId);
        mannequin.setHealth(0);
        mannequin = newMannequin;
        setInvis(true);
    }

    @Override
    public void revive() {
        setInvis(false);
    }

    @Override
    public void setHP(int hp) {
        visualHp = hp;
        if (hp == 0) {
            eliminate();
        } else {
            updateArmor();
        }
    }

    @Override
    public void setMaxHP(int maxHP) {
        setHP(maxHP);
    }

    @Override
    public ArmorSet getArmor() {
        ItemStack[] armor = mannequin.getEquipment().getArmorContents();
        return new ArmorSet(armor[0], armor[1], armor[2], armor[3]);
    }

    @Override
    public void remove() {
        mannequin.remove();
    }

    @Override
    public void reset() {

    }

    @Override
    public void readyBattle() {

    }

    @Override
    public void setReady(boolean ready, int itemIndex) {

    }

    @Override
    public int getNextFreeSlot() {
        return 0;
    }

    @Override
    public void damage() {
        mannequin.damage(0.001);
        mannequin.setGlowing(true);
        mannequin.setInvulnerable(true);

        Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> {
            mannequin.setGlowing(false);
            mannequin.setInvulnerable(false);
        }, BotBows.HIT_DISABLED_ITEM_TICKS);
    }

    @Override
    public void setGlowing(boolean flag) {
        mannequin.setGlowing(flag);
    }

    @Override
    public void addPotionEffect(PotionEffect effect) {
        mannequin.addPotionEffect(effect);
    }

    @Override
    public void setColor(TextColor color) {

    }

    @Override
    public void growSize(double scale, int duration, int delay) {
        new BukkitRunnable() {
            int i = 1;
            final double scale0 = getRequiredAttribute(Attribute.SCALE).getBaseValue();
            @Override
            public void run() {
                if (i == duration) {
                    this.cancel();
                }
                getRequiredAttribute(Attribute.SCALE).setBaseValue(scale0 + (scale - scale0)/duration * i);
                i++;
            }
        }.runTaskTimer(Main.getPlugin(), delay, 1L);
    }

    @Override
    public UUID getUUID() {
        return mannequin.getUniqueId();
    }

    @Override
    public boolean isSneaking() {
        return mannequin.isSneaking();
    }

    @Override
    public void updateSneakStamina(float progress) {
        if (progress >= 1) mannequin.setSneaking(false);
    }

    @Override
    public ItemStack getHeadItem() {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta itemMeta = (SkullMeta) item.getItemMeta();
        itemMeta.displayName(bp.getName().decoration(TextDecoration.ITALIC, false));
        NamespacedKey key = new NamespacedKey(Main.getPlugin(), "uuid");
        itemMeta.getPersistentDataContainer().set(key, PersistentDataType.STRING, mannequin.getUniqueId().toString());
        itemMeta.setOwningPlayer(Bukkit.getOfflinePlayer("Robotagz"));

        item.setItemMeta(itemMeta);
        return item;
    }

    @Override
    public void setItem(int index, ItemStack item) {

    }

    @Override
    public void setInvis(boolean invis) {
        if (invis) {
            mannequin.getEquipment().setArmorContents(new ItemStack[] {null, null, null, null});
        } else {
            updateArmor();
        }
    }

    @Override
    public void showTitle(Title title) {

    }

    @Override
    public void playSound(Location location, String sound, float volume, float pitch) {

    }

    @Override
    public void initHazardBar(HazardType hazardType, BossBar bar) {

    }

    @Override
    public void setHazardBarProgress(HazardType hazardType, float progress) {

    }

    private void updateArmor() { // updates the armor pieces of the player
        int maxHP = bp.getMaxHP();
        if (visualHp == maxHP) { // hvis playeren har maxa liv så skal de få fullt ut med armor
            mannequin.getEquipment().setArmorContents(new ItemStack[] {
                    getArmorPiece(Material.LEATHER_BOOTS),
                    getArmorPiece(Material.LEATHER_LEGGINGS),
                    getArmorPiece(Material.LEATHER_CHESTPLATE),
                    getArmorPiece(Material.LEATHER_HELMET)});
            return;
        }
        Set<Integer> slots;
        if (maxHP > 5) {
            float d = (float) maxHP / 5;
            int i = (int) Math.ceil((maxHP - visualHp) / d);
            slots = BotBowsPlayer.HEALTH_ARMOR.get(3).get(i - 1);
        } else {
            slots = BotBowsPlayer.HEALTH_ARMOR.get(maxHP - 2).get(maxHP - visualHp - 1);
        }

        for (Integer slot : slots) {
            switch (slot) {
                case 0 -> mannequin.getEquipment().setBoots(null);
                case 1 -> mannequin.getEquipment().setLeggings(null);
                case 2 -> mannequin.getEquipment().setChestplate(null);
                case 3 -> mannequin.getEquipment().setHelmet(null);
            }
        }
    }

    private ItemStack getArmorPiece(Material material) { // makes armor pieces
        ItemStack armor = new ItemStack(material);
        LeatherArmorMeta meta = (LeatherArmorMeta) armor.getItemMeta();
        assert meta != null;
        meta.setColor(bp.getTeam().dyeColor.getColor());
        armor.setItemMeta(meta);
        return armor;
    }

    private AttributeInstance getRequiredAttribute(Attribute attribute) {
        return Objects.requireNonNull( // it should always exist but if not throw
                mannequin.getAttribute(attribute),
                () -> "Missing attribute " + attribute + " for mannequin " + mannequin.getName()
        );
    }
}
