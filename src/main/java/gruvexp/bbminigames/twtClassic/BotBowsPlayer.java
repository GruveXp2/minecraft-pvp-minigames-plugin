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
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.function.Consumer;

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
    private boolean toggleAbilityMode = false;
    private final HashMap<AbilityType, Ability> abilities = new HashMap<>();
    private int thrownAbilityAmount;
    private boolean hasKarmaEffect = false;

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

    public TextColor getTeamColor() {
        if (team != null) return team.color;
        return NamedTextColor.WHITE;
    }

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
        new HashSet<>(abilities.keySet()).forEach(p -> unequipAbility(p, true));
        player.getInventory().setItem(0, BotBows.MENU_ITEM);
    }

    public void revive() { // resetter for å gjør klar til en ny runde
        setHP(maxHP);
        isDamaged = false;
        player.setGameMode(GameMode.ADVENTURE);
    }

    public void reset() {
        player.setScoreboard(lobby.botBowsGame.boardManager.manager.getNewScoreboard());
        player.getInventory().setArmorContents(new ItemStack[]{});
        player.setGlowing(false);
        player.setInvulnerable(false);
        player.getAttribute(Attribute.MAX_HEALTH).setBaseValue(20);
        hasKarmaEffect = false;
        player.setGameMode(GameMode.SPECTATOR);
        lobby.botBowsGame.barManager.sneakBars.get(player).setVisible(false);
        if (Cooldowns.sneakRunnables.containsKey(player)) {
            Cooldowns.sneakRunnables.get(player).cancel();
        }
    }

    public void initBattle() {
        abilities.values().forEach(ability -> ability.setCooldownMultiplier(abilityCooldownMultiplier));
        player.getInventory().remove(Lobby.READY.clone()); // removes ready up item
    }

    public void readyAbilities() {
        abilities.values().forEach(Ability::obtain);
        abilities.values().forEach(Ability::reset);
    }

    public void registerUsedAbilityItem(int abilityItemAmount) {
        this.thrownAbilityAmount = abilityItemAmount;
    }

    public int getUsedAbilityItemAmount() {
        return thrownAbilityAmount;
    }

    public void useRadarAbility() {
        BotBowsTeam opponentTeam = team.getOppositeTeam();
        opponentTeam.glow(Radar.DURATION);
        CreeperTrap.glowCreepers(opponentTeam, Radar.DURATION);
    }

    public int getMaxHP() {return maxHP;}

    public void setMaxHP(int maxHP) {
        this.maxHP = maxHP;
        player.getAttribute(Attribute.MAX_HEALTH).setBaseValue(2 * maxHP);
        player.setHealth(2 * maxHP);
    }

    public int getHP() {return hp;}

    private void setHP(int hp) { // heile hjerter
        this.hp = hp;
        if (hp == 0) { // spilleren dauer(går i spectator) og livene disses resettes
            player.setHealth(1); // kan ikke sette til 0 for da dauer spilleren på ekte og respawner med en gang, spilleren skal isteden settes i spectator mode der spilleren daua
        } else {
            player.setHealth(hp * 2); // halve hjerter
            updateArmor();
        }
        lobby.botBowsGame.boardManager.updatePlayerScore(this);
    }

    public boolean isAlive() {
        return hp > 0;
    }

    public void setAttackDamage(int hearts) {
        this.attackDamage = hearts;
    }

    public int getAttackDamage() {
        return attackDamage;
    }

    private AbilityMenu getAbilityMenu() {
        return lobby.settings.abilityMenus.get(this);
    }

    public Collection<Ability> getAbilities() {
        return abilities.values();
    }

    public Ability getAbility(AbilityType type) {
        return abilities.get(type);
    }

    public void setMaxAbilities(int maxAbilities) {
        this.maxAbilities = maxAbilities;
        lobby.settings.abilityMenus.values().forEach(menu -> menu.updateMaxAbilities(this));
        if (getTotalAbilities() > maxAbilities) {
            int excess = getTotalAbilities() - maxAbilities;
            for (int i = 0; i < excess; i++) {
                for (AbilityType type : AbilityType.values()) {
                    if (hasAbilityEquipped(type)) {
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
        lobby.settings.abilityMenus.values().forEach(menu -> menu.updateCooldownMultiplier(this));
    }

    public float getAbilityCooldownMultiplier() {
        return abilityCooldownMultiplier;
    }

    public void disableAbilityToggle() {
        getAbilityMenu().getInventory().setItem(27, AbilityMenu.MOD_TOGGLE_DISABLED);
        toggleAbilityMode = false;
    }

    public void enableAbilityToggle() {
        getAbilityMenu().getInventory().setItem(27, AbilityMenu.MOD_TOGGLE_ENABLED);
        toggleAbilityMode = true;
    }

    public void toggleAbilityToggle() {
        if (toggleAbilityMode) {
            disableAbilityToggle();
        } else {
            enableAbilityToggle();
        }
    }

    public boolean isToggleAbilityMode() {
        return toggleAbilityMode;
    }

    public void equipAbility(int slot, AbilityType type) {
        boolean abilityAlreadyEquipped = hasAbilityEquipped(type);
        switch (type) {
            case ENDER_PEARL -> abilities.put(type, new Ability(this, slot, AbilityType.ENDER_PEARL));
            case INVIS_POTION -> abilities.put(type, new InvisPotion(this, slot));
            case RADAR -> abilities.put(type, new Radar(this, slot));
            case SPLASH_BOW -> abilities.put(type, new SplashBow(this, slot));
            case THUNDER_BOW -> abilities.put(type, new ThunderBow(this, slot));
            case LONG_ARMS -> abilities.put(type, new Ability(this, slot, AbilityType.LONG_ARMS));
            case SALMON_SLAP -> abilities.put(type, new SalmonSlap(this, slot));
            case BUBBLE_JET -> abilities.put(type, new BubbleJet(this, slot));
            case CREEPER_TRAP -> abilities.put(type, new CreeperTrap(this, slot));
            case BABY_POTION -> abilities.put(type, new BabyPotion(this, slot));
            case LINGERING_POTION -> abilities.put(type, new LingeringPotionTrap(this, slot));
            case CHARGE_POTION -> abilities.put(type, new ChargePotion(this, slot));
            case KARMA_POTION -> abilities.put(type, new KarmaPotion(this, slot));
            case LASER_TRAP -> abilities.put(type, new LaserTrap(this, slot));
            default -> throw new IllegalStateException("Error, contact Gruve: he forgot to connect this ability type to a java class");
        }
        if (abilityAlreadyEquipped) return;

        int relativeAbilitySlot = getAbilityMenu().getRelativeAbilitySlot(type);
        if (relativeAbilitySlot > 0) { // slot -1 means cursor
            getAbilityMenu().getInventory().setItem(relativeAbilitySlot + 27, AbilityMenu.ABILITY_EQUIPPED);
            BotBows.debugMessage("Setting equip item at " + relativeAbilitySlot);
        }
        if (type == AbilityType.BUBBLE_JET) lobby.settings.rain++;

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
        ability.unequip();

        int hotBarSlot = ability.getHotBarSlot();
        if (hotBarSlot > 0) {
            inv.setItem(hotBarSlot, null);
        }
        int abilityEquipSlot = getAbilityMenu().getRelativeAbilitySlot(type);
        if (abilityEquipSlot > 0) {
            if (lobby.settings.abilityAllowed(type)) {
                getAbilityMenu().getInventory().setItem(abilityEquipSlot + 27, AbilityMenu.VOID);
            } else {
                getAbilityMenu().getInventory().setItem(abilityEquipSlot + 27, AbilityMenu.ABILITY_DISABLED);
            }
        }
        abilities.remove(type);
        if (type == AbilityType.BUBBLE_JET) lobby.settings.rain--;

        String abilityName = type.name().charAt(0) + type.name().substring(1).toLowerCase().replace('_', ' ');
        if (!hideMessage) {
            player.sendMessage(Component.text("Unequipping ability: ", NamedTextColor.RED).append(Component.text(abilityName, NamedTextColor.LIGHT_PURPLE)));
        }
    }

    public boolean hasAbilityEquipped(AbilityType type) {
        return abilities.containsKey(type);
    }

    public void obtainWeaponAbilities() {
        abilities.values().stream().filter(a -> a.getType().category == AbilityCategory.DAMAGING).forEach(Ability::obtain);
    }

    public void loseWeaponAbilities() {
        abilities.values().stream().filter(a -> a.getType().category == AbilityCategory.DAMAGING).forEach(Ability::lose);
    }

    public int getTotalAbilities() {
        return abilities.size();
    }

    public boolean isDamaged() {
        return isDamaged;
    }

    public void handleHit(TextComponent hitActionMessage, BotBowsPlayer attacker) {
        handleHit(hitActionMessage, attacker, Component.empty());
    }

    public void handleHit(TextComponent hitActionMessage, BotBowsPlayer attacker, TextComponent hitActionMessage2) {
        if (isDamaged) return;
        player.damage(0.001);
        TextColor defenderColor = team.color;
        TextColor attackerColor = attacker.team.color;
        TextColor lightColor = BotBows.lighten(attackerColor, 0.5f);
        if (hp <= attacker.attackDamage) { // spilleren kommer til å daue
            die(player.name().color(defenderColor)
                    .append(hitActionMessage.color(lightColor))
                    .append(attacker.player.name().color(attackerColor))
                    .append(hitActionMessage2.color(lightColor))
                    .append(Component.text(" and got", lightColor))
                    .append(Component.text(" eliminated", NamedTextColor.DARK_RED)));
            if (player.getGameMode() == GameMode.SPECTATOR) {
                player.setSpectatorTarget(attacker.player);
                player.sendMessage(Component.text("Now spectating ", NamedTextColor.GRAY)
                        .append(attacker.player.name().color(attackerColor)));
            }
            return;
        }
        setHP(hp - attacker.attackDamage);
        lobby.messagePlayers(Component.text(player.getName(), defenderColor)
                .append(hitActionMessage.color(lightColor))
                .append(attacker.player.name().color(attackerColor))
                .append(hitActionMessage2.color(lightColor))
                .append(Component.text("; ", NamedTextColor.WHITE)
                        .append(Component.text(hp + "hp left"))));
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
                if (item != null && item.getType() == Material.RED_STAINED_GLASS_PANE) continue;
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
        abilities.values().forEach(a -> a.setTickRate(20));
        hasKarmaEffect = false;
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

    public void setReady(boolean ready, int itemIndex) {
        if (this.ready == ready) return;
        this.ready = ready;
        player.getInventory().setItem(itemIndex, Lobby.LOADING);
        // venter litt før itemet settes itilfelle noen spammer og bøgger det til
        Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> {
            player.getInventory().setItem(itemIndex, ready ? Lobby.READY : Lobby.NOT_READY);
            lobby.handlePlayerReady(this);
        }, 2L);
    }

    public void setAbilityCooldownTickRate(int abilityCooldownTickRate) {
        for (Ability ability : abilities.values()) {
            if (ability.getType() == AbilityType.CHARGE_POTION) continue; // charge potion wont affect itself
            ability.setTickRate(abilityCooldownTickRate);
        }
    }

    public boolean hasKarmaEffect() {
        return hasKarmaEffect;
    }

    public void setKarmaEffect(boolean hasKarmaEffect) {
        this.hasKarmaEffect = hasKarmaEffect;
    }

    public void getKarma() {
        PotionEffectType[] effects = {
            PotionEffectType.SLOWNESS,
            PotionEffectType.LEVITATION,
            PotionEffectType.BLINDNESS
        };

        int effectID = BotBows.RANDOM.nextInt(effects.length + 1);
        if (effectID == effects.length) {
            growSize(10);
            return;
        }
        PotionEffectType randomEffect = effects[effectID];

        player.addPotionEffect(new PotionEffect(randomEffect, 200, 1));
        Bukkit.getScheduler().runTaskTimer(Main.getPlugin(), new Consumer<>() {
            int counter = 40;
            @Override
            public void accept(BukkitTask task) {
                if (counter == 0) {
                    task.cancel();
                    return;
                }
                player.setGlowing(counter % 2 == 0);
                counter--;
            }
        }, 0, 5);

        lobby.messagePlayers(Component.empty()
                .append(player.name().color(team.color))
                .append(Component.text(" got karma! ", NamedTextColor.RED))
                .append(Component.text(randomEffect.getKey().value(), NamedTextColor.DARK_RED)));
    }

    boolean isBig = false;

    public void growSize(int duration) {
        if (isBig) return;
        isBig = true;
        new BukkitRunnable() {
            int i = 1;
            final int totalAniTicks = 20;
            @Override
            public void run() {
                if (i == totalAniTicks) {
                    this.cancel();
                }
                player.getAttribute(Attribute.SCALE).setBaseValue(1.0 + 0.5/totalAniTicks * i);
                i++;
            }
        }.runTaskTimer(Main.getPlugin(), 0L, 1L);
        new BukkitRunnable() {
            int i = 1;
            final int totalAniTicks = 40;
            @Override
            public void run() {
                if (i == totalAniTicks) {
                    this.cancel();
                    isBig = false;
                }
                player.getAttribute(Attribute.SCALE).setBaseValue(1.5 - 0.5/totalAniTicks * i);
                i++;
            }
        }.runTaskTimer(Main.getPlugin(), duration * 20L, 1L);
    }
}
