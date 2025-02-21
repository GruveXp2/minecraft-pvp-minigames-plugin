package gruvexp.bbminigames.twtClassic.ability;

import gruvexp.bbminigames.Main;
import gruvexp.bbminigames.commands.TestCommand;
import gruvexp.bbminigames.menu.Menu;
import gruvexp.bbminigames.twtClassic.BotBows;
import gruvexp.bbminigames.twtClassic.ability.abilities.BabyPotionAbility;
import gruvexp.bbminigames.twtClassic.ability.abilities.ChargePotionAbility;
import gruvexp.bbminigames.twtClassic.ability.abilities.FloatSpellAbility;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
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

public enum AbilityType {

    ENDER_PEARL(Menu.makeItem(Material.ENDER_PEARL, Component.text("Ender Pearl")),
            15, "CONCRETE", AbilityCategory.UTILITY),
    WIND_CHARGE(Menu.makeItem(Material.WIND_CHARGE, Component.text("Wind Charge"), 3),
            15, "DYE", AbilityCategory.UTILITY),
    SPEED_POTION(makePotion(PotionType.SWIFTNESS),
            25, "CANDLE", AbilityCategory.POTION),
    INVIS_POTION(makePotion(PotionType.INVISIBILITY),
            25, "CANDLE", AbilityCategory.POTION),
    BABY_POTION(makeBabyPotion(),
            25, "CANDLE", AbilityCategory.POTION),
    CHARGE_POTION(makeChargePotion(),
            25, "CANDLE", AbilityCategory.POTION),
    SHRINK(Menu.makeItem(Material.REDSTONE, Component.text("Shrink"),
            Component.text("Makes you shrink to half the size"),
            getDurationComponent(5)),
            20, "BUNDLE", AbilityCategory.UTILITY),
    RADAR(Menu.makeItem(Material.BELL, Component.text("Radar"),
            Component.text("Reveals the position of the enemy team by making them glow"),
            getDurationComponent(4)),
            30, "BANNER", AbilityCategory.UTILITY),
    SPLASH_BOW(makeSplashBow(),
            0, "CONCRETE_POWDER", AbilityCategory.DAMAGING),
    FLOAT_SPELL(getFloatSpellItem(),
            10, "BUNDLE", AbilityCategory.UTILITY),
    LONG_ARMS(Menu.makeItem(Material.BARRIER, Component.text("Long arms"),
            Component.text("Not implemented yet")),
            15, "WOOL", AbilityCategory.DAMAGING);

    private final ItemStack abilityItem;
    private final ItemStack[] cooldownItems;
    private final int baseCooldown;
    public final AbilityCategory category;

    private static final AttributeModifier extraRangeModifier = new AttributeModifier(
            new NamespacedKey(Main.getPlugin(), "extra_range"),
            50,
            AttributeModifier.Operation.ADD_NUMBER
    );

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
            BotBows.debugMessage("\nability: " + ability.name(), TestCommand.test2);
            ItemStack abilityItem = ability.getAbilityItem();
            BotBows.debugMessage("Type: " + item.getType().name() + " == " + abilityItem.getType().name(), TestCommand.test2);

            if (item.getType() != abilityItem.getType()) continue;

            boolean itemHasMeta = item.hasItemMeta();
            boolean abilityHasMeta = abilityItem.hasItemMeta();
            BotBows.debugMessage("HasMeta: " + itemHasMeta + " == " + abilityHasMeta, TestCommand.test2);

            if (!itemHasMeta || !abilityHasMeta) {
                if (itemHasMeta == abilityHasMeta) return ability;
                continue;
            }

            ItemMeta meta = item.getItemMeta();
            ItemMeta abilityMeta = abilityItem.getItemMeta();

            boolean itemHasDisplayName = meta.hasDisplayName();
            boolean abilityHasDisplayName = abilityMeta.hasDisplayName();
            BotBows.debugMessage("HasDisplayName: " + itemHasDisplayName + " == " + abilityHasDisplayName, TestCommand.test2);

            if (itemHasDisplayName != abilityHasDisplayName) continue;

            if (itemHasDisplayName) {
                Component itemDisplayName = meta.displayName();
                Component abilityDisplayName = abilityMeta.displayName();
                assert itemDisplayName != null;
                assert abilityDisplayName != null;
                BotBows.debugMessage("DisplayName: " + PlainTextComponentSerializer.plainText().serialize(itemDisplayName) + " == " + PlainTextComponentSerializer.plainText().serialize(abilityDisplayName), TestCommand.test2);

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
        ItemStack item = Menu.makeItem(Material.FISHING_ROD, Component.text("Long arms"),
                Component.text("Has a very long range"));
        ItemMeta meta = item.getItemMeta();
        meta.addAttributeModifier(Attribute.ENTITY_INTERACTION_RANGE, extraRangeModifier);
        item.setItemMeta(meta);
        return item;
    }
}
