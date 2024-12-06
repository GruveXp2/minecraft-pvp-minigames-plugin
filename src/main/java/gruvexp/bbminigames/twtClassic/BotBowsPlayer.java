package gruvexp.bbminigames.twtClassic;

import gruvexp.bbminigames.Main;
import gruvexp.bbminigames.twtClassic.botbowsTeams.BotBowsTeam;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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

    public final Player PLAYER;
    private BotBowsTeam team;
    private int hp;
    private int maxHP;
    private boolean isDamaged = false; // cooldown når playeren er hitta
    private static final List<List<Set<Integer>>> PLAYER_HEALTH_ARMOR = new ArrayList<>(); // Når man tar damag så kan man gette em liste med hvilke armor pieces som skal fjernes

    private int maxAbilities;
    private int abilityCooldownMultiplier;

    public BotBowsPlayer(Player player, Settings settings) {
        PLAYER = player;
        maxHP = settings.getMaxHP();
        hp = maxHP;
        maxAbilities = settings.getMaxAbilities();
    }

    public BotBowsTeam getTeam() {return team;}

    public void joinTeam(BotBowsTeam team) {
        if (this.team == null) {
            PLAYER.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(maxHP * 2);
            PLAYER.setHealth(maxHP * 2);
            PLAYER.setGameMode(GameMode.ADVENTURE);
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
        PLAYER.sendMessage(ChatColor.YELLOW + "You left BotBows Classic");
        PLAYER.setGameMode(GameMode.SPECTATOR);
        PLAYER.getInventory().remove(BotBows.BOTBOW);
    }

    public void revive() { // resetter for å gjør klar til en ny runde
        setHP(maxHP);
        isDamaged = false;
        PLAYER.setGameMode(GameMode.ADVENTURE);
    }

    public void reset() {
        PLAYER.setScoreboard(Board.manager.getNewScoreboard());
        PLAYER.getInventory().setArmorContents(null);
        PLAYER.setGlowing(false);
        PLAYER.setInvulnerable(false);
        PLAYER.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(20);
        PLAYER.setGameMode(GameMode.SPECTATOR);
        Bar.sneakBars.get(PLAYER).setVisible(false);
        if (Cooldowns.sneakRunnables.containsKey(PLAYER)) {
            Cooldowns.sneakRunnables.get(PLAYER).cancel();
        }
    }

    public int getMaxHP() {return maxHP;}

    public void setMaxHP(int maxHP) {
        this.maxHP = maxHP;
        PLAYER.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(2 * maxHP);
        PLAYER.setHealth(2 * maxHP);
    }

    public int getHP() {return hp;}

    private void setHP(int hp) { // heile hjerter
        if (this.hp == hp) return;
        this.hp = hp;
        if (hp == 0) { // spilleren dauer(går i spectator) og livene disses resettes
            PLAYER.setHealth(1); // kan ikke sette til 0 for da dauer spilleren på ekte og respawner med en gang, spilleren skal isteden settes i spectator mode der spilleren daua
        } else {
            PLAYER.setHealth(hp * 2); // halve hjerter
            updateArmor();
        }
        Board.updatePlayerScore(this);
    }

    public void setMaxAbilities(int maxAbilities) {
        this.maxAbilities = maxAbilities;
    }

    public int getMaxAbilities() {
        return maxAbilities;
    }

    public boolean isDamaged() {
        return isDamaged;
    }

    public void handleHit(BotBowsPlayer attacker) {
        if (hp == 1) { // spilleren kommer til å daue
            die(team.COLOR + PLAYER.getPlayerListName() + " was sniped by " + attacker.team.COLOR + attacker.PLAYER.getPlayerListName() + " and got " + ChatColor.DARK_RED + "eliminated");
            PLAYER.setSpectatorTarget(attacker.PLAYER);
            PLAYER.sendMessage(ChatColor.GRAY + "Now spectating " + attacker.PLAYER.getPlayerListName());
            return;
        }
        setHP(hp - 1);

        BotBows.messagePlayers(team.COLOR + PLAYER.getPlayerListName() + " was sniped by " + attacker.team.COLOR + attacker.PLAYER.getPlayerListName() + "; " + team.COLOR + hp + " hp left.");
        // defender effects
        PLAYER.setGlowing(true);
        PLAYER.setInvulnerable(true);
        isDamaged = true;
        PlayerInventory inv = PLAYER.getInventory();
        for (int i = 0; i < 9; i++) { // fyller inventoriet med barrier blocks for å vise at man ikke kan skyte eller bruke abilities og flytter items fra hotbar 1 hakk opp
            inv.setItem(i + 27, inv.getItem(i));
            inv.setItem(i, new ItemStack(Material.BARRIER));
        }
        Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> {
            PLAYER.setGlowing(false);
            PLAYER.setInvulnerable(false);
            isDamaged = false;
            for (int i = 0; i < 9; i++) { // flytter items tilbake
                inv.setItem(i, inv.getItem(i + 27));
                inv.setItem(i + 27, new ItemStack(Material.RED_STAINED_GLASS_PANE));
            }
        }, 40L); // 2 sekunder
    }

    public void die(String deathMessage) { // gjør at spilleren dauer
        setHP(0);
        Board.updatePlayerScore(this);
        BotBows.messagePlayers(deathMessage);
        PLAYER.setGameMode(GameMode.SPECTATOR);
        BotBows.check4Victory(this);
    }

    public void updateArmor() { // updates the armor pieces of the player
        if (hp == maxHP) { // hvis playeren har maxa liv så skal de få fullt ut med armor
            PLAYER.getInventory().setArmorContents(new ItemStack[] {
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
                case 0 -> PLAYER.getInventory().setBoots(null);
                case 1 -> PLAYER.getInventory().setLeggings(null);
                case 2 -> PLAYER.getInventory().setChestplate(null);
                case 3 -> PLAYER.getInventory().setHelmet(null);
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
