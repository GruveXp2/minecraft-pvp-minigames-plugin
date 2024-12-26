package gruvexp.bbminigames.twtClassic.ability;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionType;

import java.util.List;

public enum AbilityType {



    ENDER_PEARL(new ItemStack(Material.ENDER_PEARL), new ItemStack(Material.CYAN_CONCRETE)),
    WIND_CHARGE(new ItemStack(Material.WIND_CHARGE, 3), new ItemStack(Material.WHITE_WOOL)),
    SPEED_POTION(makePotion(PotionType.SWIFTNESS), new ItemStack(Material.LIGHT_BLUE_CANDLE)),
    INVIS_POTION(makePotion(PotionType.INVISIBILITY), new ItemStack(Material.LIGHT_GRAY_CANDLE)),
    SHRINK(new ItemStack(Material.REDSTONE), new ItemStack(Material.GUNPOWDER));

    private final ItemStack abilityItem;
    private final ItemStack cooldownItem;

    AbilityType(ItemStack item, ItemStack cooldownItem) {
        this.abilityItem = item;
        this.cooldownItem = cooldownItem;
    }

    public ItemStack getAbilityItem() {
        return abilityItem;
    }

    public ItemStack getCooldownItem() {
        return cooldownItem;
    }

    // Metode for å finne en ability basert på item
    public static AbilityType fromItem(ItemStack item) {
        for (AbilityType ability : values()) {
            if (ability.getAbilityItem().isSimilar(item)) {
                return ability;
            }
        }
        return null; // Returner null hvis ingen match
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
        meta.lore(List.of(Component.text("Duration: ").append(Component.text("5s", NamedTextColor.GREEN))));

        potion.setItemMeta(meta);
        return potion;
    }
}
