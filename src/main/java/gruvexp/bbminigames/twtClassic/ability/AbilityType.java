package gruvexp.bbminigames.twtClassic.ability;

import gruvexp.bbminigames.commands.TestCommand;
import gruvexp.bbminigames.menu.Menu;
import gruvexp.bbminigames.twtClassic.BotBows;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionType;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public enum AbilityType {



    ENDER_PEARL(Menu.makeItem(Material.ENDER_PEARL, Component.text("Ender Pearl")),
            15, "CONCRETE"),
    WIND_CHARGE(Menu.makeItem(Material.WIND_CHARGE, Component.text("Wind Charge"), 3),
            15, "DYE"),
    SPEED_POTION(makePotion(PotionType.SWIFTNESS),
            25, "CANDLE"),
    INVIS_POTION(makePotion(PotionType.INVISIBILITY),
            25, "CANDLE"),
    SHRINK(Menu.makeItem(Material.REDSTONE, Component.text("Shrink"),
            Component.text("Makes you shrink to half the size"),
            getDurationComponent(5)),
            20, "BUNDLE"),
    RADAR(Menu.makeItem(Material.BELL, Component.text("Radar"),
            Component.text("Reveals the position of the enemy team by making them glow"),
            getDurationComponent(4)),
            30, "BANNER"),
    SPLASH_BOW(makeSplashBow(),
            15, "CONCRETE_POWDER");

    private final ItemStack abilityItem;
    private final ItemStack[] cooldownItems;
    private final int baseCooldown;

    AbilityType(ItemStack item, int baseCooldown, String cooldownItemType) {
        this.abilityItem = item;
        this.baseCooldown = baseCooldown;
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
}
