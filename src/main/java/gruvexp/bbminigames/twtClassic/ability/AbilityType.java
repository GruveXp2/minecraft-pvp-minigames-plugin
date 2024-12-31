package gruvexp.bbminigames.twtClassic.ability;

import gruvexp.bbminigames.commands.TestCommand;
import gruvexp.bbminigames.menu.Menu;
import gruvexp.bbminigames.twtClassic.BotBows;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionType;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public enum AbilityType {



    ENDER_PEARL(Menu.makeItem(Material.ENDER_PEARL, Component.text("Ender Pearl")),
            new ItemStack(Material.CYAN_CONCRETE), 15),
    WIND_CHARGE(Menu.makeItem(Material.WIND_CHARGE, Component.text("Wind Charge"), 3),
            new ItemStack(Material.WHITE_WOOL), 15),
    SPEED_POTION(makePotion(PotionType.SWIFTNESS),
            new ItemStack(Material.LIGHT_BLUE_CANDLE), 25),
    INVIS_POTION(makePotion(PotionType.INVISIBILITY),
            new ItemStack(Material.LIGHT_GRAY_CANDLE), 25),
    SHRINK(Menu.makeItem(Material.REDSTONE, Component.text("Shrink"),
            Component.text("Makes you shrink to half the size"),
            getDurationComponent(5)),
            new ItemStack(Material.GUNPOWDER), 20);

    private final ItemStack abilityItem;
    private final ItemStack cooldownItem;
    private final int baseCooldown;

    AbilityType(ItemStack item, ItemStack cooldownItem, int baseCooldown) {
        this.abilityItem = item;
        this.cooldownItem = cooldownItem;
        this.baseCooldown = baseCooldown;
    }

    public ItemStack getAbilityItem() {
        return abilityItem;
    }

    public ItemStack getCooldownItem() {
        return cooldownItem;
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

    private static @NotNull TextComponent getDurationComponent(int seconds) {
        return Component.text("Duration: ").append(Component.text(seconds + "s", NamedTextColor.GREEN));
    }
}
