package gruvexp.bbminigames.twtClassic.avatar;

import gruvexp.bbminigames.Main;
import gruvexp.bbminigames.commands.TestCommand;
import gruvexp.bbminigames.twtClassic.BotBows;
import gruvexp.bbminigames.twtClassic.BotBowsPlayer;
import gruvexp.bbminigames.twtClassic.Lobby;
import gruvexp.bbminigames.twtClassic.hazard.HazardType;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class PlayerAvatar implements BotBowsAvatar{

    private final Player player;
    private final BotBowsPlayer bp;
    private final BossBar sneakBar;
    private final Map<HazardType, BossBar> hazardBars = new EnumMap<>(HazardType.class);
    private int visualHp;

    public PlayerAvatar(Player player, BotBowsPlayer bp) {
        this.player = player;
        this.bp = bp;
        sneakBar = BossBar.bossBar(Component.text("Sneaking cooldown"), 0f, BossBar.Color.WHITE, BossBar.Overlay.NOTCHED_10);
        player.setGameMode(GameMode.ADVENTURE);
    }

    @Override
    public void message(Component component) {
        player.sendMessage(component);
    }

    @Override
    public LivingEntity getEntity() {
        return player;
    }

    @Override
    public BotBowsPlayer getBotBowsPlayer() {
        return bp;
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
        visualHp = hp;
        if (hp == 0) { // spilleren dauer(går i spectator) og livene disses resettes
            player.setHealth(1); // kan ikke sette til 0 for da dauer spilleren på ekte og respawner med en gang, spilleren skal isteden settes i spectator mode der spilleren daua
        } else {
            player.setHealth(hp * 2); // halve hjerter
            updateArmor();
        }
    }

    @Override
    public void setMaxHP(int maxHP) {
        getRequiredAttribute(Attribute.MAX_HEALTH).setBaseValue(maxHP * 2);
        setHP(maxHP);
    }

    @Override
    public ArmorSet getArmor() {
        ItemStack[] armor = player.getInventory().getArmorContents();
        return new ArmorSet(armor[0], armor[1], armor[2], armor[3]);
    }

    @Override
    public void remove() {
        eliminate();
        reset();
        player.getInventory().setItem(0, BotBows.MENU_ITEM);
    }

    @Override
    public void reset() {
        player.setScoreboard(bp.lobby.botBowsGame.boardManager.manager.getNewScoreboard());
        player.getInventory().clear();
        player.setGlowing(false);
        player.setInvulnerable(false);
        getRequiredAttribute(Attribute.MAX_HEALTH).setBaseValue(20);
        player.setGameMode(GameMode.SPECTATOR);
        hazardBars.values().forEach(bar -> bar.removeViewer(player));
        sneakBar.removeViewer(player);
    }

    @Override
    public void readyBattle() {
        player.getInventory().remove(Lobby.READY.clone()); // removes ready up item
    }

    @Override
    public void setReady(boolean ready, int itemIndex) {
        player.getInventory().setItem(itemIndex, Lobby.LOADING);
        // venter litt før itemet settes itilfelle noen spammer og bøgger det til
        Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> player.getInventory().setItem(itemIndex, ready ? Lobby.READY : Lobby.NOT_READY), 2L);
    }

    @Override
    public int getNextFreeSlot() {
        PlayerInventory inv = player.getInventory();
        for (int slot = 1; slot < 9; slot++) {
            if (inv.getItem(slot) == null) {
                return slot;
            }
        }
        return -1;
    }

    @Override
    public void damage() {
        player.damage(0.001);
        player.setGlowing(true);
        player.setInvulnerable(true);

        PlayerInventory inv = player.getInventory();
        for (int i = 0; i < 9; i++) { // fyller inventoriet med barrier blocks for å vise at man ikke kan skyte eller bruke abilities og flytter items fra hotbar 1 hakk opp
            if (inv.getItem(i) == null) continue;

            inv.setItem(i + 27, inv.getItem(i));
            inv.setItem(i, new ItemStack(Material.BARRIER));
        }

        Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> {
            player.setGlowing(false);
            player.setInvulnerable(false);
            for (int i = 0; i < 9; i++) { // flytter items tilbake
                ItemStack item = inv.getItem(i + 27);
                inv.setItem(i, item);
            }
        }, BotBows.HIT_DISABLED_ITEM_TICKS);
    }

    @Override
    public void setGlowing(boolean flag) {
        player.setGlowing(flag);
    }

    @Override
    public void addPotionEffect(PotionEffect effect) {
        player.addPotionEffect(effect);
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
        return player.getUniqueId();
    }

    @Override
    public boolean isSneaking() {
        return player.isSneaking();
    }

    @Override
    public void updateSneakStamina(float progress) {
        boolean isExhausted = bp.isSneakingExhausted();
        sneakBar.progress(progress);
        BotBows.debugMessage("Progress: " + progress, TestCommand.test3);
        if (progress > 0) player.showBossBar(sneakBar); else player.hideBossBar(sneakBar);
        BotBows.debugMessage("Show it: " + (progress > 0), TestCommand.test3);
        sneakBar.color(isExhausted ? BossBar.Color.RED : BossBar.Color.YELLOW);
        sneakBar.name(Component.text ("Sneaking", isExhausted ? NamedTextColor.RED : NamedTextColor.YELLOW));
        if (progress >= 1) player.setSneaking(false);
    }

    @Override
    public ItemStack getHeadItem() {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta itemMeta = (SkullMeta) item.getItemMeta();
        itemMeta.displayName(bp.getName().decoration(TextDecoration.ITALIC, false));
        itemMeta.getPersistentDataContainer().set(new NamespacedKey(Main.getPlugin(), "uuid"), PersistentDataType.STRING, player.getUniqueId().toString());
        itemMeta.setOwningPlayer(Bukkit.getPlayer(player.getName()));

        item.setItemMeta(itemMeta);
        return item;
    }

    @Override
    public void setItem(int index, ItemStack item) {
        player.getInventory().setItem(index, item);
    }

    @Override
    public void setInvis(boolean invis) { // will temporarily move armor content out of inventory and
        if (invis) {
            player.getInventory().setArmorContents(new ItemStack[] {null, null, null, null});
        } else {
            updateArmor();
        }
    }

    @Override
    public void showTitle(Title title) {
        player.showTitle(title);
    }

    @Override
    public void playSound(Location location, String sound, float volume, float pitch) {
        player.playSound(location, sound, volume, pitch);
    }

    @Override
    public void initHazardBar(HazardType hazardType, BossBar bar) {
        if (hazardBars.containsKey(hazardType)) throw new IllegalStateException("That hazardbar already exists");
        hazardBars.put(hazardType, bar);
    }

    @Override
    public void setHazardBarProgress(HazardType hazardType, float progress) {
        BossBar bar = hazardBars.get(hazardType);
        if (progress == 0) {
            bar.removeViewer(player);
            return;
        }
        bar.addViewer(player);
        bar.progress(progress);
    }

    private void updateArmor() { // updates the armor pieces of the player
        int maxHP = bp.getMaxHP();
        if (visualHp == maxHP) { // hvis playeren har maxa liv så skal de få fullt ut med armor
            player.getInventory().setArmorContents(new ItemStack[] {
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
                case 0 -> player.getInventory().setBoots(null);
                case 1 -> player.getInventory().setLeggings(null);
                case 2 -> player.getInventory().setChestplate(null);
                case 3 -> player.getInventory().setHelmet(null);
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

    public void spectate(BotBowsAvatar avatar) {
        player.setSpectatorTarget(avatar.getEntity());
        player.sendMessage(Component.text("Now spectating ", NamedTextColor.GRAY)
                .append(avatar.getBotBowsPlayer().getName()));
    }

    private AttributeInstance getRequiredAttribute(Attribute attribute) {
        return Objects.requireNonNull( // it should always exist but if not throw
                player.getAttribute(attribute),
                () -> "Missing attribute " + attribute + " for player " + player.getName()
        );
    }
}
