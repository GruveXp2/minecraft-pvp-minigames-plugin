package gruvexp.bbminigames.twtClassic;

import gruvexp.bbminigames.Main;
import gruvexp.bbminigames.menu.menus.AbilityMenu;
import gruvexp.bbminigames.twtClassic.ability.Ability;
import gruvexp.bbminigames.twtClassic.ability.AbilityCategory;
import gruvexp.bbminigames.twtClassic.ability.AbilityType;
import gruvexp.bbminigames.twtClassic.ability.abilities.*;
import gruvexp.bbminigames.twtClassic.avatar.BotBowsAvatar;
import gruvexp.bbminigames.twtClassic.avatar.PlayerAvatar;
import gruvexp.bbminigames.twtClassic.botbowsTeams.BotBowsTeam;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class BotBowsPlayer {

    public final Player player;
    public BotBowsAvatar avatar;
    private final Component name;

    public final Lobby lobby;
    private BotBowsTeam team;
    private int hp;
    private int maxHP;
    private int attackDamage;
    private boolean isDamaged = false; // cooldown når playeren er hitta
    private boolean ready = false; // om playeren er klar for å spille
    public static final List<List<Set<Integer>>> HEALTH_ARMOR = new ArrayList<>(); // Når man tar damag så kan man gette em liste med hvilke armor pieces som skal fjernes
    private SneakManager sneakManager;

    private int maxAbilities;
    private float abilityCooldownMultiplier;
    private boolean toggleAbilityMode = false;
    private final HashMap<AbilityType, Ability> abilities = new HashMap<>();
    private int thrownAbilityAmount;
    private boolean hasKarmaEffect = false;

    public BotBowsPlayer(Player player, Settings settings) {
        this.player = player;
        avatar = new PlayerAvatar(player, this);
        name = player.name();
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

    public Component getName() {
        return name.color(getTeamColor());
    }

    public String getPlainName() {
        return PlainTextComponentSerializer.plainText().serialize(getName());
    }

    public void joinTeam(BotBowsTeam team) {
        if (this.team == null) {
            avatar.setMaxHP(maxHP);

            avatar.revive();
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
        avatar.remove();
        new HashSet<>(abilities.keySet()).forEach(p -> unequipAbility(p, true));
    }

    public void start() {
        sneakManager = new SneakManager(avatar);
    }

    public void revive() { // resetter for å gjør klar til en ny runde
        setHP(maxHP);
        avatar.revive();
        isDamaged = false;
    }

    public void reset() {
        sneakManager.destroy();
        avatar.reset();
        hasKarmaEffect = false;
    }

    public void initBattle() {
        avatar.readyBattle();
        abilities.values().forEach(ability -> ability.setCooldownMultiplier(abilityCooldownMultiplier));
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
        avatar.setMaxHP(maxHP);
    }

    public int getHP() {return hp;}

    private void setHP(int hp) { // heile hjerter
        this.hp = hp;
        avatar.setHP(hp);
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

    public void equipAbility(AbilityType type) {
        int slot = avatar.getNextFreeSlot();
        equipAbility(slot, type);
    }

    public void equipAbility(int slot, AbilityType type) {
        boolean abilityAlreadyEquipped = hasAbilityEquipped(type);
        switch (type) {
            case ENDER_PEARL -> abilities.put(type, new Ability(this, slot, AbilityType.ENDER_PEARL));
            case INVIS_POTION -> abilities.put(type, new InvisPotion(this, slot));
            case RADAR -> abilities.put(type, new Radar(this, slot));
            case SPLASH_BOW -> abilities.put(type, new SplashBow(this, slot));
            case THUNDER_BOW -> abilities.put(type, new ThunderBow(this, slot));
            case LONG_ARMS -> abilities.put(type, new LongArms(this, slot));
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
        avatar.message(Component.text("Equipping ability: ", NamedTextColor.GREEN).append(Component.text(abilityName, NamedTextColor.LIGHT_PURPLE)));
    }

    public void unequipAbility(AbilityType type) {
        unequipAbility(type, false);
    }

    public void unequipAbility(AbilityType type, boolean hideMessage) {
        if (!abilities.containsKey(type)) return;

        Ability ability = abilities.get(type);
        ability.resetCooldown();
        ability.unequip();
        int slot = ability.getHotBarSlot();
        if (slot > 0) {
            avatar.setItem(slot, null);
        }

        int abilityEquipSlot = getAbilityMenu().getRelativeAbilitySlot(type);
        if (abilityEquipSlot > 0) {
            if (lobby.settings.isAbilityAllowed(type)) {
                getAbilityMenu().getInventory().setItem(abilityEquipSlot + 27, AbilityMenu.VOID);
            } else {
                getAbilityMenu().getInventory().setItem(abilityEquipSlot + 27, AbilityMenu.ABILITY_DISABLED);
            }
        }
        abilities.remove(type);
        if (type == AbilityType.BUBBLE_JET) lobby.settings.rain--;

        String abilityName = type.name().charAt(0) + type.name().substring(1).toLowerCase().replace('_', ' ');
        if (!hideMessage) {
            avatar.message(Component.text("Unequipping ability: ", NamedTextColor.RED).append(Component.text(abilityName, NamedTextColor.LIGHT_PURPLE)));
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
        if (!isAlive()) return;
        avatar.damage();
        TextColor attackerColor = attacker.team.color;
        TextColor lightColor = BotBows.lighten(attackerColor, 0.5f);
        if (hp <= attacker.attackDamage) { // spilleren kommer til å daue
            die(getName()
                    .append(hitActionMessage.color(lightColor))
                    .append(attacker.getName())
                    .append(hitActionMessage2.color(lightColor))
                    .append(Component.text(" and got", lightColor))
                    .append(Component.text(" eliminated", NamedTextColor.DARK_RED)));
            if (!isAlive() && avatar instanceof PlayerAvatar playerAvatar) {
                playerAvatar.spectate(attacker.avatar);
            }
            return;
        }
        setHP(hp - attacker.attackDamage);
        lobby.messagePlayers(getName()
                .append(hitActionMessage.color(lightColor))
                .append(attacker.getName())
                .append(hitActionMessage2.color(lightColor))
                .append(Component.text("; ", NamedTextColor.WHITE)
                        .append(Component.text(hp + "hp left"))));
        // defender effects
        abilities.values().forEach(Ability::hit);
        isDamaged = true;
        Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> isDamaged = false, BotBows.HIT_DISABLED_ITEM_TICKS);
    }

    public void die(Component deathMessage) { // gjør at spilleren dauer
        setHP(0);
        lobby.botBowsGame.boardManager.updatePlayerScore(this);
        lobby.messagePlayers(deathMessage);
        avatar.eliminate();
        abilities.values().forEach(a -> a.setTickRate(20));
        hasKarmaEffect = false;
        lobby.check4Elimination(this);
    }

    @Deprecated(since = "DungeonGhoster will soon use avatars")
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
        HEALTH_ARMOR.add(hp2);
        HEALTH_ARMOR.add(hp3);
        HEALTH_ARMOR.add(hp4);
        HEALTH_ARMOR.add(hp5);
    }

    public boolean isReady() {
        return ready;
    }

    public void setReady(boolean ready, int itemIndex) {
        if (this.ready == ready) return;
        this.ready = ready;
        avatar.setReady(ready, itemIndex);
        // venter litt før itemet settes itilfelle noen spammer og bøgger det til
        Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> lobby.handlePlayerReady(this), 3L);
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

        avatar.addPotionEffect(new PotionEffect(randomEffect, 200, 1));
        Bukkit.getScheduler().runTaskTimer(Main.getPlugin(), new Consumer<>() {
            int counter = 40;
            @Override
            public void accept(BukkitTask task) {
                if (counter == 0) {
                    task.cancel();
                    avatar.setColor(getTeamColor());
                    return;
                }
                avatar.setGlowing(counter % 2 == 0);
                counter--;
            }
        }, 0, 5);

        lobby.messagePlayers(Component.empty()
                .append(getName())
                .append(Component.text(" got karma! ", NamedTextColor.RED))
                .append(Component.text(randomEffect.getKey().value(), NamedTextColor.DARK_RED)));
    }

    boolean isBig = false;

    public void growSize(int duration) {
        if (isBig) return;
        isBig = true;
        avatar.growSize(1.5, 20);
        Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> {
            avatar.growSize(1, 40);
            isBig = false;
        }, duration * 20L);
    }

    public boolean isSneakingExhausted() {
        return sneakManager.isSneakingExhausted();
    }

    public void reloadBotBow() {
        avatar.setItem(0, BotBows.BOTBOW);
    }

    public void setInvis(int ticks) {
        avatar.setInvis(true);
        Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> avatar.setInvis(false), ticks);
    }

    public Set<BotBowsPlayer> getNearbyPlayers(double radius) {
        return avatar.getLocation().getWorld().getNearbyEntities(avatar.getLocation(), radius, radius, radius, entity -> entity instanceof Player)
                .stream().map(p -> (Player) p)
                .map(BotBows::getBotBowsPlayer).filter(Objects::nonNull)
                .filter(BotBowsPlayer::isAlive)
                .collect(Collectors.toSet());
    }

    public Location getLocation() {
        return avatar.getLocation();
    }
}
