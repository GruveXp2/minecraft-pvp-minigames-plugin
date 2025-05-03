package gruvexp.bbminigames.twtClassic.ability;

import gruvexp.bbminigames.Main;
import gruvexp.bbminigames.menu.Menu;
import gruvexp.bbminigames.twtClassic.ability.abilities.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public enum AbilityType {

    SPLASH_BOW(makeSplashBow(),
            "CONCRETE_POWDER", AbilityCategory.DAMAGING),
    THUNDER_BOW(Menu.makeItem(Material.BLUE_ICE, Component.text("Thunder Bow"),
            Component.text("Converts your crossbow into a thunder crossbow"),
            Component.text("When hitting an enemy,"),
            Component.text("lightning strikes other enemies within 6 blocks"),
            getDurationComponent(ThunderBowAbility.DURATION)),
            "TERRACOTTA", AbilityCategory.DAMAGING),
    BUBBLE_JET(getRiptideTrident(),
            "CANDLE", AbilityCategory.DAMAGING),
    LONG_ARMS(getLongHandsItem(),
            "WOOL", AbilityCategory.DAMAGING),
    SALMON_SLAP(Menu.makeItem(Material.SALMON_BUCKET, Component.text("Salmon Slap"),
            Component.text("Melee weapon"),
            getDurationComponent(SalmonSlapAbility.DURATION)),
            "WOOL", AbilityCategory.DAMAGING),
    RADAR(Menu.makeItem(Material.BELL, Component.text("Radar"),
            Component.text("Reveals the position of the enemy team by making them glow"),
            getDurationComponent(RadarAbility.DURATION)),
            30, "BANNER", AbilityCategory.UTILITY),
    ENDER_PEARL(Menu.makeItem(Material.ENDER_PEARL, Component.text("Ender Pearl")),
            15, "CONCRETE", AbilityCategory.UTILITY),
    INVIS_POTION(makePotion(PotionType.INVISIBILITY),
            25, "CANDLE", AbilityCategory.POTION),
    BABY_POTION(makeBabyPotion(),
            25, "CANDLE", AbilityCategory.POTION),
    CHARGE_POTION(makeChargePotion(),
            25, "CANDLE", AbilityCategory.POTION),
    KARMA_POTION(makeKarmaPotion(),
            25, "CANDLE", AbilityCategory.POTION),
    CREEPER_TRAP(Menu.makeItem(Material.CREEPER_HEAD, Component.text("Creeper"),
            Component.text("Deploy a creeper mine"),
            Component.text("to surprise your friends!")),
            25, "CONCRETE_POWDER", AbilityCategory.TRAP),
    LINGERING_POTION(makeLingeringPotion(),
            LingeringPotionAbility.DURATION + 5, "CANDLE", AbilityCategory.TRAP),
    FLOAT_SPELL(getFloatSpellItem(),
            10, "BUNDLE", AbilityCategory.UTILITY),
    WIND_CHARGE(Menu.makeItem(Material.WIND_CHARGE, Component.text("Wind Charge"), 3),
            15, "DYE", AbilityCategory.UTILITY),
    SPEED_POTION(makePotion(PotionType.SWIFTNESS),
            25, "CANDLE", AbilityCategory.POTION),
    SHRINK(Menu.makeItem(Material.REDSTONE, Component.text("Shrink"),
            Component.text("Makes you shrink to half the size"),
            getDurationComponent(5)),
            20, "BUNDLE", AbilityCategory.UTILITY);

    private final ItemStack abilityItem;
    private final ItemStack[] cooldownItems;
    private final int baseCooldown;
    public final AbilityCategory category;

    AbilityType(ItemStack item, int baseCooldown, String cooldownItemType, AbilityCategory category) {
        this.abilityItem = item;
        this.baseCooldown = baseCooldown;
        this.category = category;
        Material red = Material.getMaterial("RED_" + cooldownItemType);
        Material orange = Material.getMaterial("ORANGE_" + cooldownItemType);
        Material yellow = Material.getMaterial("YELLOW_" + cooldownItemType);
        Material green = Material.getMaterial("LIME_" + cooldownItemType);
        this.cooldownItems = new ItemStack[]{new ItemStack(red), new ItemStack(orange), new ItemStack(yellow), new ItemStack(green)};
    }

    AbilityType(ItemStack item, String cooldownItemType, AbilityCategory category) {
        this(item, -1, cooldownItemType, category);
    }

    public ItemStack getAbilityItem() {
        return abilityItem;
    }

    public ItemStack[] getCooldownItems() {
        return cooldownItems;
    }

    public int getBaseCooldown() {
        return baseCooldown;
    }

    public static AbilityType fromItem(ItemStack item) {
        if (item == null) return null;
        for (AbilityType ability : values()) {
            ItemStack abilityItem = ability.getAbilityItem();

            if (item.getType() != abilityItem.getType()) continue;

            boolean itemHasMeta = item.hasItemMeta();
            boolean abilityHasMeta = abilityItem.hasItemMeta();

            if (!itemHasMeta || !abilityHasMeta) {
                if (itemHasMeta == abilityHasMeta) return ability;
                continue;
            }

            ItemMeta meta = item.getItemMeta();
            ItemMeta abilityMeta = abilityItem.getItemMeta();

            boolean itemHasDisplayName = meta.hasDisplayName();
            boolean abilityHasDisplayName = abilityMeta.hasDisplayName();

            if (itemHasDisplayName != abilityHasDisplayName) continue;

            if (itemHasDisplayName) {
                Component itemDisplayName = meta.displayName();
                Component abilityDisplayName = abilityMeta.displayName();
                assert itemDisplayName != null;
                assert abilityDisplayName != null;

                if (itemDisplayName.equals(abilityDisplayName)) {
                    return ability;
                }
            } else {
                return ability;
            }
        }
        return null;
    }

    private static ItemStack makePotion(PotionType type) {
        ItemStack potion = new ItemStack(Material.POTION);
        PotionMeta meta = (PotionMeta) potion.getItemMeta();

        PotionEffect effect = new PotionEffect(type.getPotionEffects().getFirst().getType(), 5 * 20, 4);
        meta.addCustomEffect(effect, true);
        String name = switch (type) {
            case SWIFTNESS -> "Speed Potion 5";
            case INVISIBILITY -> "Invisibility Potion";
            default -> "Potion of unnamed 0";
        };
        meta.customName(Component.text(name));
        meta.lore(List.of(getDurationComponent(5)));

        potion.setItemMeta(meta);
        return potion;
    }

    private static ItemStack makeBabyPotion() {
        ItemStack potion = new ItemStack(Material.POTION);
        PotionMeta meta = (PotionMeta) potion.getItemMeta();

        meta.addCustomEffect(new PotionEffect(PotionEffectType.SPEED, BabyPotionAbility.DURATION * 20, 4), true);
        meta.customName(Component.text("Baby Potion"));
        meta.lore(List.of(Component.text("2x Speed", NamedTextColor.BLUE),
                Component.text("-30% Size", NamedTextColor.BLUE),
                getDurationComponent(BabyPotionAbility.DURATION)));

        potion.setItemMeta(meta);
        return potion;
    }

    private static ItemStack makeChargePotion() {
        ItemStack potion = new ItemStack(Material.POTION);
        PotionMeta meta = (PotionMeta) potion.getItemMeta();

        meta.addCustomEffect(new PotionEffect(PotionEffectType.LUCK, ChargePotionAbility.DURATION * 20, 4), true);
        meta.customName(Component.text("Charge Potion"));
        meta.lore(List.of(Component.text("2x cooldown reduction speed", NamedTextColor.BLUE),
                getDurationComponent(ChargePotionAbility.DURATION)));

        potion.setItemMeta(meta);
        return potion;
    }

    private static ItemStack makeKarmaPotion() {
        ItemStack potion = new ItemStack(Material.POTION);
        PotionMeta meta = (PotionMeta) potion.getItemMeta();

        meta.addCustomEffect(new PotionEffect(PotionEffectType.UNLUCK, KarmaPotion.DURATION * 20, 4), true);
        meta.customName(Component.text("Karma Potion"));
        meta.lore(List.of(Component.text("Attacker gets glowing and", NamedTextColor.BLUE),
                Component.text("slowness, levitation, nausea, or blindness", NamedTextColor.BLUE),
                getDurationComponent(KarmaPotion.DURATION)));

        potion.setItemMeta(meta);
        return potion;
    }

    private static ItemStack makeLingeringPotion() {
        ItemStack potion = new ItemStack(Material.LINGERING_POTION);
        PotionMeta meta = (PotionMeta) potion.getItemMeta();
        meta.customName(Component.text("Lingering potion"));
        meta.lore(List.of(Component.text("Contains one of the following at random:"),
                Component.text("Nausea", NamedTextColor.LIGHT_PURPLE),
                Component.text("Slowness", NamedTextColor.LIGHT_PURPLE),
                Component.text("Levitation", NamedTextColor.LIGHT_PURPLE),
                Component.text("Blindness", NamedTextColor.LIGHT_PURPLE),
                getDurationComponent(LingeringPotionAbility.DURATION)));
        meta.setColor(Color.fromRGB(100, 62, 46));
        potion.setItemMeta(meta);
        return potion;
    }

    private static ItemStack makeSplashBow() {
        ItemStack splashBow = new ItemStack(Material.BOW);
        ItemMeta meta = splashBow.getItemMeta();
        meta.displayName(Component.text("Splash Bow"));
        meta.lore(List.of(Component.text("A bow that does splash damage"), Component.text("in 3 blocks radius")));
        meta.addEnchant(Enchantment.POWER, 10, true);
        meta.addEnchant(Enchantment.PUNCH, 10, true);
        Damageable damageable = (Damageable) meta;
        damageable.setDamage((short) 384);
        splashBow.setItemMeta(damageable);
        return splashBow;
    }

    private static @NotNull TextComponent getDurationComponent(int seconds) {
        return Component.text("Duration: ").append(Component.text(seconds + "s", NamedTextColor.GREEN));
    }

    private static ItemStack getFloatSpellItem() {
        ItemStack item = Menu.makeItem(Material.EGG, Component.text("Float Spell"));
        item.lore(List.of(getDurationComponent(FloatSpellAbility.DURATION)));
        return item;
    }

    private static ItemStack getLongHandsItem() {
        ItemStack item = Menu.makeItem(Material.FISHING_ROD, Component.text("Cool Rod"),
                Component.text("Punch someone far away, only 1 punch granted"));
        ItemMeta meta = item.getItemMeta();

        // Create a unique NamespacedKey for this item
        NamespacedKey key = new NamespacedKey(Main.getPlugin(), "extra_range_" + UUID.randomUUID());

        AttributeModifier extraRangeModifier = new AttributeModifier(
                key,
                50,
                AttributeModifier.Operation.ADD_NUMBER
        );
        meta.addAttributeModifier(Attribute.ENTITY_INTERACTION_RANGE, extraRangeModifier);
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack getRiptideTrident() {
        ItemStack item = Menu.makeItem(Material.TRIDENT, Component.text("Trident"),
                Component.text("Makes you fly thru the air"),
                Component.text("and damage enemies in a 2m radius"));
        return item;
    }
}
