package gruvexp.bbminigames.twtClassic.ability;

import gruvexp.bbminigames.Main;
import gruvexp.bbminigames.menu.Menu;
import gruvexp.bbminigames.twtClassic.ability.abilities.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public enum AbilityType {

    SPLASH_BOW(makeSplashBow(),
            "CONCRETE_POWDER", AbilityCategory.DAMAGING),
    THUNDER_BOW(Menu.makeItem(Material.BLUE_ICE, Component.text("Thunder Bow"),
            Component.text("Converts your crossbow into a thunder crossbow"),
            Component.text("When hitting an opponent, damage chains to nearby enemies"),
            Component.empty(),
            getDamageInfo("chain", 6, 'r'),
            getDurationInfo(ThunderBowAbility.DURATION)),
            "TERRACOTTA", AbilityCategory.DAMAGING),
    BUBBLE_JET(makeRiptideTrident(),
            "CANDLE", AbilityCategory.DAMAGING),
    LONG_ARMS(makeLongHandsItem(),
            "WOOL", AbilityCategory.DAMAGING),
    SALMON_SLAP(Menu.makeItem(Material.SALMON_BUCKET, Component.text("Salmon Slap"),
            Component.text("Give your opponents a salmon slap"),
            Component.empty(),
            getDamageInfo("punch", 3, 'm'),
            getDurationInfo(SalmonSlapAbility.DURATION)),
            "WOOL", AbilityCategory.DAMAGING),
    RADAR(Menu.makeItem(Material.BELL, Component.text("Radar"),
            Component.text("Reveals the position of opponents by making them glow"),
            Component.empty(),
            getDurationInfo(RadarAbility.DURATION)),
            30, "BANNER", AbilityCategory.UTILITY),
    ENDER_PEARL(Menu.makeItem(Material.ENDER_PEARL, Component.text("Ender Pearl")),
            15, "CONCRETE", AbilityCategory.UTILITY),
    INVIS_POTION(makeInvisPotion(),
            25, "CANDLE", AbilityCategory.POTION),
    BABY_POTION(makeBabyPotion(),
            25, "CANDLE", AbilityCategory.POTION),
    CHARGE_POTION(makeChargePotion(),
            25, "CANDLE", AbilityCategory.POTION),
    KARMA_POTION(makeKarmaPotion(),
            30, "CANDLE", AbilityCategory.POTION),
    CREEPER_TRAP(Menu.makeItem(Material.CREEPER_HEAD, Component.text("Creeper"),
            Component.text("Deploy a creeper mine"),
            Component.text("to surprise your friends!"),
            Component.empty(),
            Component.text("Trigger radius: ", NamedTextColor.YELLOW).append(Component.text(CreeperTrapAbility.BLAST_RADIUS, NamedTextColor.YELLOW))),
            25, "CONCRETE_POWDER", AbilityCategory.TRAP),
    LINGERING_POTION(makeLingeringPotion(),
            LingeringPotionAbility.DURATION + 5, "CANDLE", AbilityCategory.TRAP);

    private final ItemStack abilityItem;
    private final ItemStack[] cooldownItems;
    private final int baseCooldown;
    public final AbilityCategory category;

    AbilityType(ItemStack item, int baseCooldown, String cooldownItemType, AbilityCategory category) {
        appendCooldownInfo(item, category, baseCooldown);

        this.abilityItem = item;
        this.baseCooldown = baseCooldown;
        this.category = category;
        Material red = Material.getMaterial("RED_" + cooldownItemType);
        Material orange = Material.getMaterial("ORANGE_" + cooldownItemType);
        Material yellow = Material.getMaterial("YELLOW_" + cooldownItemType);
        Material green = Material.getMaterial("LIME_" + cooldownItemType);
        this.cooldownItems = new ItemStack[]{new ItemStack(red), new ItemStack(orange), new ItemStack(yellow), new ItemStack(green)};
    }

    private static void appendCooldownInfo(ItemStack item, AbilityCategory category, int baseCooldown) {
        ItemMeta meta = item.getItemMeta();
        Component cooldownComponent = category == AbilityCategory.DAMAGING ? Component.text("Cooldown: ", NamedTextColor.GOLD)
                .append(Component.text("obtain by hitting opponent", NamedTextColor.YELLOW))
                : Component.text("Cooldown: ", NamedTextColor.GOLD)
                .append(Component.text(baseCooldown + "s", NamedTextColor.YELLOW));
        List<Component> lore = meta.hasLore() ? meta.lore() : new ArrayList<>();
        lore.add(cooldownComponent.decoration(TextDecoration.ITALIC, false));
        meta.lore(lore);
        item.setItemMeta(meta);
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

    private static @NotNull TextComponent getDurationInfo(int seconds) {
        return Component.text("Duration: ", NamedTextColor.DARK_GREEN)
                .append(Component.text(seconds + "s", NamedTextColor.GREEN))
                .decoration(TextDecoration.ITALIC, false);
    }

    private static @NotNull TextComponent getDamageInfo(String damageType, int value, char unit) {
        return Component.text("Damage: ", NamedTextColor.DARK_RED)
                .append(Component.text(damageType, NamedTextColor.RED).appendSpace()
                        .append(Component.text(value + "" + unit)))
                .decoration(TextDecoration.ITALIC, false);
    }

    private static @NotNull TextComponent getPotionEffectInfo(String potionEffect) {
        return Component.text("Potion effect: ", NamedTextColor.DARK_AQUA)
                .append(Component.text(potionEffect, NamedTextColor.AQUA))
                .decoration(TextDecoration.ITALIC, false);
    }

    private static ItemStack makeInvisPotion() {
        ItemStack potion = new ItemStack(Material.POTION);
        PotionMeta meta = (PotionMeta) potion.getItemMeta();

        PotionEffect effect = new PotionEffect(PotionEffectType.INVISIBILITY, 5 * 20, 4);
        meta.addCustomEffect(effect, true);
        meta.customName(Component.text("Invisibility potion").decoration(TextDecoration.ITALIC, false));
        meta.lore(List.of(getDurationInfo(5)));
        meta.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP);

        potion.setItemMeta(meta);
        return potion;
    }

    private static ItemStack makeBabyPotion() {
        ItemStack potion = new ItemStack(Material.POTION);
        PotionMeta meta = (PotionMeta) potion.getItemMeta();

        meta.addCustomEffect(new PotionEffect(PotionEffectType.SPEED, BabyPotionAbility.DURATION * 20, 4), true);
        meta.customName(Component.text("Baby Potion").decoration(TextDecoration.ITALIC, false));
        meta.lore(List.of(
                Component.text("Makes you small and fast"),
                Component.empty(),
                getPotionEffectInfo("2x Speed"),
                getPotionEffectInfo("-30% Size"),
                getDurationInfo(BabyPotionAbility.DURATION)
        ));
        meta.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP);

        potion.setItemMeta(meta);
        return potion;
    }

    private static ItemStack makeChargePotion() {
        ItemStack potion = new ItemStack(Material.POTION);
        PotionMeta meta = (PotionMeta) potion.getItemMeta();

        meta.addCustomEffect(new PotionEffect(PotionEffectType.LUCK, ChargePotionAbility.DURATION * 20, 4), true);
        meta.customName(Component.text("Charge Potion").decoration(TextDecoration.ITALIC, false));
        meta.lore(List.of(
                Component.text("Makes your cooldowns go faster"),
                Component.empty(),
                getPotionEffectInfo("2x cooldown speed"),
                getDurationInfo(ChargePotionAbility.DURATION)
        ));
        meta.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP);

        potion.setItemMeta(meta);
        return potion;
    }

    private static ItemStack makeKarmaPotion() {
        ItemStack potion = new ItemStack(Material.POTION);
        PotionMeta meta = (PotionMeta) potion.getItemMeta();

        meta.addCustomEffect(new PotionEffect(PotionEffectType.UNLUCK, KarmaPotion.DURATION * 20, 4), true);
        meta.customName(Component.text("Karma Potion").decoration(TextDecoration.ITALIC, false));
        meta.lore(List.of(
                Component.text("Attacker gets glowing and"),
                Component.text("slowness, levitation, nausea, or blindness"),
                Component.empty(),
                getPotionEffectInfo("karma"),
                getDurationInfo(KarmaPotion.DURATION)
        ));
        meta.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP);

        potion.setItemMeta(meta);
        return potion;
    }

    private static ItemStack makeLingeringPotion() {
        ItemStack potion = new ItemStack(Material.LINGERING_POTION);
        PotionMeta meta = (PotionMeta) potion.getItemMeta();
        meta.customName(Component.text("Lingering potion").decoration(TextDecoration.ITALIC, false));
        meta.lore(List.of(
                Component.text("Contains one of the following at random:"),
                Component.text("Growing", NamedTextColor.LIGHT_PURPLE),
                Component.text("Slowness", NamedTextColor.LIGHT_PURPLE),
                Component.text("Levitation", NamedTextColor.LIGHT_PURPLE),
                Component.text("Blindness", NamedTextColor.LIGHT_PURPLE),
                getDurationInfo(LingeringPotionAbility.DURATION)
        ));
        meta.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
        meta.setColor(Color.fromRGB(100, 62, 46));

        potion.setItemMeta(meta);
        return potion;
    }

    private static ItemStack makeSplashBow() {
        ItemStack splashBow = new ItemStack(Material.BOW);
        ItemMeta meta = splashBow.getItemMeta();
        meta.displayName(Component.text("Splash Bow").decoration(TextDecoration.ITALIC, false));
        meta.lore(List.of(
                Component.text("A bow that shoots arrows exploding on impact"),
                Component.empty(),
                getDamageInfo("splash", 6, 'r')
        ));
        meta.addEnchant(Enchantment.POWER, 10, true);
        meta.addEnchant(Enchantment.PUNCH, 10, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        Damageable damageable = (Damageable) meta;
        damageable.setDamage((short) 384);
        splashBow.setItemMeta(damageable);
        return splashBow;
    }

    private static ItemStack makeLongHandsItem() {
        ItemStack item = Menu.makeItem(Material.FISHING_ROD, Component.text("Cool Rod"),
                Component.text("Punch someone far away, only 1 punch granted"),
                Component.empty(),
                getDamageInfo("punch", 50, 'm'));
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

    private static ItemStack makeRiptideTrident() {
        ItemStack item = new ItemStack(Material.TRIDENT);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("Trident").decoration(TextDecoration.ITALIC, false));
        meta.lore(List.of(
                Component.text("Makes you fly thru the air"),
                Component.text("and damage opponents in a 2m radius"),
                Component.empty(),
                getDamageInfo("aura", 2, 'r')
        ));
        meta.setAttributeModifiers(null);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);

        item.setItemMeta(meta);
        return item;
    }
}
