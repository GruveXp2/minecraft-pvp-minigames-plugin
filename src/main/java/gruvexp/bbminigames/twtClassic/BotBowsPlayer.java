package gruvexp.bbminigames.twtClassic;

import gruvexp.bbminigames.Main;
import gruvexp.bbminigames.menu.menus.AbilityMenu;
import gruvexp.bbminigames.twtClassic.ability.Ability;
import gruvexp.bbminigames.twtClassic.ability.AbilityCategory;
import gruvexp.bbminigames.twtClassic.ability.AbilityType;
import gruvexp.bbminigames.twtClassic.ability.abilities.*;
import gruvexp.bbminigames.twtClassic.botbowsTeams.BotBowsTeam;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import java.util.*;

public class BotBowsPlayer {

    public final Player player;
    public final Lobby lobby;
    private BotBowsTeam team;
    private int hp;
    private int maxHP;
    private int attackDamage;
    private boolean isDamaged = false; // cooldown når playeren er hitta
    private boolean ready = false; // om playeren er klar for å spille
    private static final List<List<Set<Integer>>> PLAYER_HEALTH_ARMOR = new ArrayList<>(); // Når man tar damag så kan man gette em liste med hvilke armor pieces som skal fjernes

    private int maxAbilities;
    private float abilityCooldownMultiplier;
    private boolean canToggleAbilities = false;
    private final HashMap<AbilityType, Ability> abilities = new HashMap<>();
    private int thrownAbilityAmount;

    public BotBowsPlayer(Player player, Settings settings) {
        this.player = player;
        lobby = settings.lobby;
        maxHP = settings.getMaxHP();
        hp = maxHP;
        attackDamage = 1;
        maxAbilities = settings.getMaxAbilities();
        abilityCooldownMultiplier = settings.getAbilityCooldownMultiplier();
    }

    public BotBowsTeam getTeam() {return team;}

    public void joinTeam(BotBowsTeam team) {
        if (this.team == null) {
            player.getAttribute(Attribute.MAX_HEALTH).setBaseValue(maxHP * 2);
            player.setHealth(maxHP * 2);
            player.setGameMode(GameMode.ADVENTURE);
        } else {
            this.team.leave(this);
        }
        this.team = team;
    }

    public void updateTeam(BotBowsTeam team) {
        this.team = team;
    }

    public void leaveTeam() {
        this.team = null;
    }

    public void leaveGame() {
        team.leave(this);
        player.setGameMode(GameMode.SPECTATOR);
        player.getInventory().remove(BotBows.BOTBOW);
        abilities.keySet().forEach(p -> unequipAbility(p, true));
        player.getInventory().setItem(0, BotBows.MENU_ITEM);
    }

    public void revive() { // resetter for å gjør klar til en ny runde
        setHP(maxHP);
        isDamaged = false;
        player.setGameMode(GameMode.ADVENTURE);
    }

    public void reset() {
        player.setScoreboard(lobby.botBowsGame.boardManager.manager.getNewScoreboard());
        player.getInventory().setArmorContents(null);
        player.setGlowing(false);
        player.setInvulnerable(false);
        player.getAttribute(Attribute.MAX_HEALTH).setBaseValue(20);
        player.setGameMode(GameMode.SPECTATOR);
        lobby.botBowsGame.barManager.sneakBars.get(player).setVisible(false);
        if (Cooldowns.sneakRunnables.containsKey(player)) {
            Cooldowns.sneakRunnables.get(player).cancel();
        }
    }

    public void initBattle() {
        abilities.values().forEach(ability -> ability.setCooldownMultiplier(abilityCooldownMultiplier));
        player.getInventory().setItem(4, null); // fjerner ready item
    }

    public void readyAbilities() {
        abilities.values().forEach(Ability::resetCooldown);
    }

    public void registerUsedAbilityItem(int abilityItemAmount) {
        this.thrownAbilityAmount = abilityItemAmount;
    }

    public int getUsedAbilityItemAmount() {
        return thrownAbilityAmount;
    }

    public void useRadarAbility() {
        team.getOppositeTeam().glow(RadarAbility.DURATION);
    }

    public int getMaxHP() {return maxHP;}

    public void setMaxHP(int maxHP) {
        this.maxHP = maxHP;
        player.getAttribute(Attribute.MAX_HEALTH).setBaseValue(2 * maxHP);
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
        lobby.botBowsGame.boardManager.updatePlayerScore(this);
    }

    public void setAttackDamage(int hearts) {
        this.attackDamage = hearts;
    }

    public int getAttackDamage() {
        return attackDamage;
    }

    public Collection<Ability> getAbilities() {
        return abilities.values();
    }

    public Ability getAbility(AbilityType type) {
        return abilities.get(type);
    }

    public void setMaxAbilities(int maxAbilities) {
        this.maxAbilities = maxAbilities;
        lobby.settings.abilityMenu.updateMaxAbilities(this);
        if (getTotalAbilities() > maxAbilities) {
            int excess = getTotalAbilities() - maxAbilities;
            for (int i = 0; i < excess; i++) {
                for (AbilityType type : AbilityType.values()) {
                    if (isAbilityEquipped(type)) {
                        unequipAbility(type);
                        break;
                    }
                }
            }
        }
    }

    public int getMaxAbilities() {
        return maxAbilities;
    }

    public void setAbilityCooldownMultiplier(float cooldownMultiplier) {
        this.abilityCooldownMultiplier = cooldownMultiplier;
        lobby.settings.abilityMenu.updateCooldownMultiplier(this);
    }

    public float getAbilityCooldownMultiplier() {
        return abilityCooldownMultiplier;
    }

    public void disableAbilityToggle() {
        player.getInventory().setItem(9, AbilityMenu.MOD_TOGGLE_DISABLED);
        canToggleAbilities = false;
    }

    public void enableAbilityToggle() {
        player.getInventory().setItem(9, AbilityMenu.MOD_TOGGLE_ENABLED);
        canToggleAbilities = true;
    }

    public void toggleAbilityToggle() {
        if (canToggleAbilities) {
            disableAbilityToggle();
        } else {
            enableAbilityToggle();
        }
    }

    public boolean canToggleAbilities() {
        return canToggleAbilities;
    }

    public void equipAbility(int slot, AbilityType type) {
        switch (type) {
            case ENDER_PEARL -> abilities.put(type, new EnderPearlAbility(this, slot));
            case WIND_CHARGE -> abilities.put(type, new WindChargeAbility(this, slot));
            case SPEED_POTION -> abilities.put(type, new SpeedPotionAbility(this, slot));
            case INVIS_POTION -> abilities.put(type, new InvisPotionAbility(this, slot));
            case SHRINK -> abilities.put(type, new ShrinkAbility(this, slot));
            case RADAR -> abilities.put(type, new RadarAbility(this, slot));
            case SPLASH_BOW -> abilities.put(type, new SplashBowAbility(this, slot));
            case FLOAT_SPELL -> abilities.put(type, new FloatSpellAbility(this, slot));
            case LONG_ARMS -> abilities.put(type, new LongArmsAbility(this, slot));
        }
        player.getInventory().setItem(lobby.settings.abilityMenu.getRelativeAbilitySlot(type) + 9, AbilityMenu.ABILITY_EQUIPPED);

        String abilityName = type.name().charAt(0) + type.name().substring(1).toLowerCase().replace('_', ' ');
        player.sendMessage(Component.text("Equipping ability: ", NamedTextColor.GREEN).append(Component.text(abilityName, NamedTextColor.LIGHT_PURPLE)));
    }

    public void unequipAbility(AbilityType type) {
        unequipAbility(type, false);
    }

    public void unequipAbility(AbilityType type, boolean hideMessage) {
        if (!abilities.containsKey(type)) return;

        Inventory inv = player.getInventory();
        Ability ability = abilities.get(type);
        ability.resetCooldown();
        inv.setItem(ability.getHotBarSlot(), null);
        inv.setItem(lobby.settings.abilityMenu.getRelativeAbilitySlot(type) + 9, null);
        abilities.remove(type);
        String abilityName = type.name().charAt(0) + type.name().substring(1).toLowerCase().replace('_', ' ');
        if (!hideMessage) {
            player.sendMessage(Component.text("Unequipping ability: ", NamedTextColor.RED).append(Component.text(abilityName, NamedTextColor.LIGHT_PURPLE)));
        }
    }

    public boolean isAbilityEquipped(AbilityType type) {
        return abilities.containsKey(type);
    }

    public int getTotalAbilities() {
        return abilities.size();
    }

    public boolean isDamaged() {
        return isDamaged;
    }

    public void handleHit(BotBowsPlayer attacker, TextComponent hitActionMessage) {
        if (hp <= attacker.attackDamage) { // spilleren kommer til å daue
            die(player.name().color(team.color)
                    .append(hitActionMessage)
                    .append(attacker.player.name().color(attacker.team.color))
                    .append(Component.text(" and got"))
                    .append(Component.text(" eliminated", NamedTextColor.DARK_RED)));
            if (player.getGameMode() == GameMode.SPECTATOR) {
                player.setSpectatorTarget(attacker.player);
                player.sendMessage(Component.text("Now spectating ", NamedTextColor.GRAY)
                        .append(attacker.player.name().color(attacker.team.color)));
            }
            return;
        }
        setHP(hp - attacker.attackDamage);
        lobby.messagePlayers(Component.text(player.getName(), team.color)
                .append(hitActionMessage)
                .append(Component.text(attacker.player.getName(), attacker.team.color))
                .append(Component.text(";"))
                .append(Component.text(hp + "hp left", team.color)));
        // defender effects
        player.setGlowing(true);
        player.setInvulnerable(true);
        isDamaged = true;
        PlayerInventory inv = player.getInventory();
        for (int i = 0; i < 9; i++) { // fyller inventoriet med barrier blocks for å vise at man ikke kan skyte eller bruke abilities og flytter items fra hotbar 1 hakk opp
            if (inv.getItem(i) == null) continue;
            AbilityType abilityType = AbilityType.fromItem(inv.getItem(i));
            if (abilityType != null && abilityType.category != AbilityCategory.DAMAGING) continue;

            inv.setItem(i + 27, inv.getItem(i));
            inv.setItem(i, new ItemStack(Material.BARRIER));
        }
        abilities.values().forEach(Ability::hit);
        Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> {
            player.setGlowing(false);
            player.setInvulnerable(false);
            isDamaged = false;
            for (int i = 0; i < 9; i++) { // flytter items tilbake
                ItemStack item = inv.getItem(i + 27);
                if (item != null && item.getType() == Material.RED_STAINED_GLASS) continue;
                inv.setItem(i, item);
                inv.setItem(i + 27, new ItemStack(Material.RED_STAINED_GLASS_PANE));
            }
        }, BotBows.HIT_DISABLED_ITEM_TICKS);
    }

    public void die(Component deathMessage) { // gjør at spilleren dauer
        setHP(0);
        lobby.botBowsGame.boardManager.updatePlayerScore(this);
        lobby.messagePlayers(deathMessage);
        player.setGameMode(GameMode.SPECTATOR);
        lobby.check4Elimination(this);
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
        meta.setColor(team.dyeColor.getColor());
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

    public boolean isReady() {
        return ready;
    }

    public void setReady(boolean ready) {
        if (this.ready == ready) return;
        this.ready = ready;
        player.getInventory().setItem(4, Lobby.LOADING);
        // venter litt før itemet settes itilfelle noen spammer og bøgger det til
        Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> player.getInventory().setItem(4, ready ? Lobby.READY : Lobby.NOT_READY), 2L);
        lobby.handlePlayerReady(this);
    }
}
