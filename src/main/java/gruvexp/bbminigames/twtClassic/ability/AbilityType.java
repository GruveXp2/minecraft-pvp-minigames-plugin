package gruvexp.bbminigames.twtClassic.ability;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public enum AbilityType {
    ENDER_PEARL(new ItemStack(Material.ENDER_PEARL), new ItemStack(Material.CYAN_CONCRETE));

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
}
