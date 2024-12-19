package gruvexp.bbminigames.twtClassic;

import gruvexp.bbminigames.Main;
import gruvexp.bbminigames.twtClassic.botbowsTeams.BotBowsTeam;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BotBowsPlayer {

    public final Player player;
    private BotBowsTeam team;
    private int hp;
    private int maxHP;
    private boolean isDamaged = false; // cooldown når playeren er hitta
    private static final List<List<Set<Integer>>> PLAYER_HEALTH_ARMOR = new ArrayList<>(); // Når man tar damag så kan man gette em liste med hvilke armor pieces som skal fjernes

    private int maxAbilities;
    private float abilityCooldownMultiplier;

    public BotBowsPlayer(Player player, Settings settings) {
        this.player = player;
        maxHP = settings.getMaxHP();
        hp = maxHP;
        maxAbilities = settings.getMaxAbilities();
        abilityCooldownMultiplier = settings.getAbilityCooldownMultiplier();
    }

    public BotBowsTeam getTeam() {return team;}

    public void joinTeam(BotBowsTeam team) {
        if (this.team == null) {
            player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(maxHP * 2);
            player.setHealth(maxHP * 2);
            player.setGameMode(GameMode.ADVENTURE);
        } else {
            this.team.leave(this);
        }
        this.team = team;
    }

    public void leaveTeam() {
        this.team = null;
    }

    public void leaveGame() {
        team.leave(this);
        player.sendMessage(Component.text("You left BotBows Classic", NamedTextColor.YELLOW));
        player.setGameMode(GameMode.SPECTATOR);
        player.getInventory().remove(BotBows.BOTBOW);
    }

    public void revive() { // resetter for å gjør klar til en ny runde
        setHP(maxHP);
        isDamaged = false;
        player.setGameMode(GameMode.ADVENTURE);
    }

    public void reset() {
        player.setScoreboard(Board.manager.getNewScoreboard());
        player.getInventory().setArmorContents(null);
        player.setGlowing(false);
        player.setInvulnerable(false);
        player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(20);
        player.setGameMode(GameMode.SPECTATOR);
        Bar.sneakBars.get(player).setVisible(false);
        if (Cooldowns.sneakRunnables.containsKey(player)) {
            Cooldowns.sneakRunnables.get(player).cancel();
        }
    }

    public int getMaxHP() {return maxHP;}

    public void setMaxHP(int maxHP) {
        this.maxHP = maxHP;
        player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(2 * maxHP);
        player.setHealth(2 * maxHP);
    }

    public int getHP() {return hp;}

    private void setHP(int hp) { // heile hjerter
        if (this.hp == hp) return;
        this.hp = hp;
        if (hp == 0) { // spilleren dauer(går i spectator) og livene disses resettes
            player.setHealth(1); // kan ikke sette til 0 for da dauer spilleren på ekte og respawner med en gang, spilleren skal isteden settes i spectator mode der spilleren daua
        } else {
            player.setHealth(hp * 2); // halve hjerter
            updateArmor();
        }
        Board.updatePlayerScore(this);
    }

    public void setMaxAbilities(int maxAbilities) {
        this.maxAbilities = maxAbilities;
        BotBows.settings.abilityMenu.updateMaxAbilities(this);
    }

    public int getMaxAbilities() {
        return maxAbilities;
    }

    public void setAbilityCooldownMultiplier(float cooldownMultiplier) {
        BotBows.debugMessage(String.format("Setting cooldown of %s to %.2f (was %.2f)", player.getName(), cooldownMultiplier, abilityCooldownMultiplier));
        this.abilityCooldownMultiplier = cooldownMultiplier;
        BotBows.settings.abilityMenu.updateCooldownMultiplier(this);
    }

    public float getAbilityCooldownMultiplier() {
        return abilityCooldownMultiplier;
    }

    public boolean isDamaged() {
        return isDamaged;
    }

    public void handleHit(BotBowsPlayer attacker) {
        if (hp == 1) { // spilleren kommer til å daue
            die(player.name().color(team.COLOR)
                    .append(Component.text(" was sniped by "))
                    .append(attacker.player.name().color(attacker.team.COLOR))
                    .append(Component.text(" and got"))
                    .append(Component.text(" eliminated", NamedTextColor.DARK_RED)));
            player.setSpectatorTarget(attacker.player);
            player.sendMessage(Component.text("Now spectating ", NamedTextColor.GRAY)
                    .append(attacker.player.name().color(attacker.team.COLOR)));
            return;
        }
        setHP(hp - 1);
        BotBows.messagePlayers(Component.text(player.getName(), team.COLOR)
                .append(Component.text(" was sniped by "))
                .append(Component.text(attacker.player.getName(), attacker.team.COLOR))
                .append(Component.text(";"))
                .append(Component.text(hp + "hp left", team.COLOR)));
        // defender effects
        player.setGlowing(true);
        player.setInvulnerable(true);
        isDamaged = true;
        PlayerInventory inv = player.getInventory();
        for (int i = 0; i < 9; i++) { // fyller inventoriet med barrier blocks for å vise at man ikke kan skyte eller bruke abilities og flytter items fra hotbar 1 hakk opp
            inv.setItem(i + 27, inv.getItem(i));
            inv.setItem(i, new ItemStack(Material.BARRIER));
        }
        Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> {
            player.setGlowing(false);
            player.setInvulnerable(false);
            isDamaged = false;
            for (int i = 0; i < 9; i++) { // flytter items tilbake
                inv.setItem(i, inv.getItem(i + 27));
                inv.setItem(i + 27, new ItemStack(Material.RED_STAINED_GLASS_PANE));
            }
        }, 40L); // 2 sekunder
    }

    public void die(Component deathMessage) { // gjør at spilleren dauer
        setHP(0);
        Board.updatePlayerScore(this);
        BotBows.messagePlayers(deathMessage);
        player.setGameMode(GameMode.SPECTATOR);
        BotBows.check4Victory(this);
    }

    public void updateArmor() { // updates the armor pieces of the player
        if (hp == maxHP) { // hvis playeren har maxa liv så skal de få fullt ut med armor
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
            int i = (int) Math.ceil((maxHP - hp) / d);
            slots = PLAYER_HEALTH_ARMOR.get(3).get(i - 1);
        } else {
            slots = PLAYER_HEALTH_ARMOR.get(maxHP - 2).get(maxHP - hp - 1);
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

    public ItemStack getArmorPiece(Material material) { // makes armor pieces
        ItemStack armor = new ItemStack(material);
        LeatherArmorMeta meta = (LeatherArmorMeta) armor.getItemMeta();
        assert meta != null;
        meta.setColor(team.DYECOLOR.getColor());
        armor.setItemMeta(meta);
        return armor;
    }

    public static void armorInit() { // definerer hvilke armor som skal mistes når playeren har x antall liv. 0=boots, 1=leggings, 2=chestplate, 3=helmet
        List<Set<Integer>> hp2 = new ArrayList<>();
        List<Set<Integer>> hp3 = new ArrayList<>();
        List<Set<Integer>> hp4 = new ArrayList<>();
        List<Set<Integer>> hp5 = new ArrayList<>();
        hp2.add(new HashSet<>(List.of(0, 1, 2, 3)));
        hp3.add(new HashSet<>(List.of(0, 2)));
        hp3.add(new HashSet<>(List.of(1, 3)));
        hp4.add(new HashSet<>(List.of(2)));
        hp4.add(new HashSet<>(List.of(0, 1)));
        hp4.add(new HashSet<>(List.of(3)));
        hp5.add(new HashSet<>(List.of(2)));
        hp5.add(new HashSet<>(List.of(1)));
        hp5.add(new HashSet<>(List.of(0)));
        hp5.add(new HashSet<>(List.of(3)));
        hp5.add(new HashSet<>()); // hvis man har fler liv enn 5 så blir denne calla, men da skal det ikke skje noe
        PLAYER_HEALTH_ARMOR.add(hp2);
        PLAYER_HEALTH_ARMOR.add(hp3);
        PLAYER_HEALTH_ARMOR.add(hp4);
        PLAYER_HEALTH_ARMOR.add(hp5);
    }
}
