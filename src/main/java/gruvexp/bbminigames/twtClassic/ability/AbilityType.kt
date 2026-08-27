package gruvexp.bbminigames.twtClassic.ability

import gruvexp.bbminigames.Main
import gruvexp.bbminigames.Util
import gruvexp.bbminigames.menu.Menu
import gruvexp.bbminigames.twtClassic.BotBowsPlayer
import gruvexp.bbminigames.twtClassic.ability.abilities.*
import io.papermc.paper.block.BlockPredicate
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.ItemAdventurePredicate
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Color
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable
import org.bukkit.inventory.meta.PotionMeta
import org.bukkit.persistence.PersistentDataType
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import java.util.UUID

val KEY: NamespacedKey = NamespacedKey("botbows", "ability_item")

enum class AbilityType(item: ItemStack, baseCooldown: Int, cooldownItemType: String, category: AbilityCategory, effect: AbilityEffect) {
    SPLASH_BOW(
        makeSplashBow(),
        "CONCRETE_POWDER", AbilityCategory.DAMAGING, AbilityEffect.DAMAGE
    ),
    THUNDER_BOW(
        Menu.makeItem(
            Material.BLUE_ICE, Component.text("Thunder Bow"),
            Component.text("Converts your crossbow into a thunder crossbow"),
            Component.text("When hitting an opponent, damage chains to nearby enemies"),
            Component.empty(),
            getDamageInfo("chain", 6, 'r'),
            getDurationInfo(ThunderBow.DURATION)
        ),
        "TERRACOTTA", AbilityCategory.DAMAGING, AbilityEffect.DAMAGE
    ),
    BUBBLE_JET(
        makeRiptideTrident(),
        "CANDLE", AbilityCategory.DAMAGING, AbilityEffect.DAMAGE
    ),
    LONG_ARMS(
        makeLongHandsItem(),
        "WOOL", AbilityCategory.DAMAGING, AbilityEffect.DAMAGE
    ),
    SALMON_SLAP(
        Menu.makeItem(
            Material.SALMON_BUCKET, Component.text("Salmon Slap"),
            Component.text("Give your opponents a salmon slap"),
            Component.empty(),
            getDamageInfo("punch", 3, 'm'),
            getDurationInfo(SalmonSlap.DURATION)
        ),
        "WOOL", AbilityCategory.DAMAGING, AbilityEffect.DAMAGE
    ),
    RADAR(
        Menu.makeItem(
            Material.BELL, Component.text("Radar"),
            Component.text("Reveals the position of opponents by making them glow"),
            Component.empty(),
            getDurationInfo(Radar.DURATION)
        ),
        30, "BANNER", AbilityCategory.UTILITY, AbilityEffect.DEBUFF
    ),
    ENDER_PEARL(
        Menu.makeItem(Material.ENDER_PEARL, Component.text("Ender Pearl")),
        15, "CONCRETE", AbilityCategory.UTILITY, AbilityEffect.BUFF
    ),
    BABY_POTION(
        makeBabyPotion(),
        25, "CANDLE", AbilityCategory.POTION, AbilityEffect.BUFF
    ),
    CHARGE_POTION(
        makeChargePotion(),
        25, "CANDLE", AbilityCategory.POTION, AbilityEffect.BUFF
    ),
    KARMA_POTION(
        makeKarmaPotion(),
        30, "CANDLE", AbilityCategory.POTION, AbilityEffect.DEBUFF
    ),
    CREEPER_TRAP(
        Menu.makeItem(
            Material.CREEPER_HEAD, Component.text("Creeper"),
            Component.text("Deploy a creeper mine"),
            Component.text("to surprise your friends!"),
            Component.empty(),
            Component.text("Trigger radius: ", NamedTextColor.YELLOW)
                .append(Component.text(CreeperTrap.BLAST_RADIUS, NamedTextColor.YELLOW))
        ),
        5, "CONCRETE_POWDER", AbilityCategory.TRAP, AbilityEffect.DAMAGE
    ),
    LASER_TRAP(
        makeLaser(),
        5, "CONCRETE_POWDER", AbilityCategory.TRAP, AbilityEffect.DAMAGE
    ),
    LINGERING_POTION(
        makeLingeringPotion(),
        LingeringPotionTrap.DURATION + 5, "CANDLE", AbilityCategory.TRAP, AbilityEffect.DEBUFF
    );

    val displayName: String
        get() = "${name[0]}${name.substring(1).lowercase().replace('_', ' ')}"
    @JvmField
    val abilityItem: ItemStack
    val cooldownItems: Array<ItemStack>
    val baseCooldown: Int
    val category: AbilityCategory
    val effect: AbilityEffect

    init {
        appendCooldownInfo(item, category, baseCooldown)
        item.editMeta { it.persistentDataContainer.set(KEY, PersistentDataType.STRING, this.name) }

        abilityItem = item
        this.baseCooldown = baseCooldown
        this.category = category
        this.effect = effect
        cooldownItems = arrayOf(
            ItemStack(Material.getMaterial("RED_$cooldownItemType")!!),
            ItemStack(Material.getMaterial("ORANGE_$cooldownItemType")!!),
            ItemStack(Material.getMaterial("YELLOW_$cooldownItemType")!!),
            ItemStack(Material.getMaterial("LIME_$cooldownItemType")!!),
        )
    }

    constructor(item: ItemStack, cooldownItemType: String, category: AbilityCategory, effect: AbilityEffect) : this(
        item,
        -1,
        cooldownItemType,
        category,
        effect,
    )

    fun getAbilityItem(bp: BotBowsPlayer): ItemStack {
        val abilityItem = abilityItem.clone()
        abilityItem.editMeta {
            val lore = it.lore() ?: mutableListOf()
            lore[lore.lastIndex] = getCooldownComponent(bp)
            it.lore(lore)
        }
        return abilityItem
    }

    fun getCooldownComponent(bp: BotBowsPlayer): Component {
        if (category == AbilityCategory.DAMAGING) {
            return Component.text("Cooldown: ", NamedTextColor.GOLD)
                .append(Component.text("obtain by hitting opponent", NamedTextColor.YELLOW))
        }
        val percentage = ((bp.settings.abilityCooldownMultiplier - 1) * 100).toInt()
        var cooldownComponent = Component.text("Cooldown: ", NamedTextColor.GOLD)
            .append(Component.text(
                "${baseCooldown * bp.settings.abilityCooldownMultiplier.toInt()}s",
                NamedTextColor.YELLOW
            ))
        if (percentage != 0) {
            cooldownComponent = cooldownComponent
                .append(Component.text(
                    " (${if (percentage > 0) "+" else ""}$percentage%",
                    if (percentage < 0) NamedTextColor.GREEN else NamedTextColor.RED
                ))
        }
        return cooldownComponent.decoration(TextDecoration.ITALIC, false)
    }

    companion object {
        @JvmStatic
        fun fromItem(item: ItemStack): AbilityType? {
            val mapStr = item.persistentDataContainer.get(KEY, PersistentDataType.STRING) ?: return null
            return valueOf(mapStr)
        }
    }
}

private fun makeBabyPotion(): ItemStack {
    val potion = ItemStack(Material.POTION)
    potion.editMeta(PotionMeta::class.java) {
        it.addCustomEffect(PotionEffect(PotionEffectType.SPEED, BabyPotion.DURATION * 20, 4), true)
        it.customName(Component.text("Baby Potion").decoration(TextDecoration.ITALIC, false))
        it.lore(listOf(
            Component.text("Makes you small and fast"),
            Component.empty(),
            getPotionEffectInfo("2x Speed"),
            getPotionEffectInfo("-30% Size"),
            getDurationInfo(BabyPotion.DURATION),
        ))
        it.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP)
    }
    return potion
}

private fun makeChargePotion(): ItemStack {
    val potion = ItemStack(Material.POTION)
    potion.editMeta(PotionMeta::class.java) {
        it.addCustomEffect(PotionEffect(PotionEffectType.LUCK, ChargePotion.DURATION * 20, 4), true)
        it.customName(Component.text("Charge Potion").decoration(TextDecoration.ITALIC, false))
        it.lore(listOf(
            Component.text("Makes your cooldowns go faster"),
            Component.empty(),
            getPotionEffectInfo("2x cooldown speed"),
            getDurationInfo(ChargePotion.DURATION),
        ))
        it.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP)
    }
    return potion
}

private fun makeKarmaPotion(): ItemStack {
    val potion = ItemStack(Material.POTION)
    potion.editMeta(PotionMeta::class.java) {
        it.addCustomEffect(PotionEffect(PotionEffectType.UNLUCK, KarmaPotion.DURATION * 20, 4), true)
        it.customName(Component.text("Karma Potion").decoration(TextDecoration.ITALIC, false))
        it.lore(listOf(
            Component.text("Attacker gets glowing and"),
            Component.text("slowness, levitation, nausea, or blindness"),
            Component.empty(),
            getPotionEffectInfo("karma"),
            getDurationInfo(KarmaPotion.DURATION),
        ))
        it.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP)
    }
    return potion
}

private fun makeLingeringPotion(): ItemStack {
    val potion = ItemStack(Material.LINGERING_POTION)
    potion.editMeta(PotionMeta::class.java) {
        it.customName(Component.text("Lingering potion").decoration(TextDecoration.ITALIC, false))
        it.lore(listOf(
            Component.text("Contains one of the following at random:"),
            Component.text("Growing", NamedTextColor.LIGHT_PURPLE),
            Component.text("Slowness", NamedTextColor.LIGHT_PURPLE),
            Component.text("Levitation", NamedTextColor.LIGHT_PURPLE),
            Component.text("Blindness", NamedTextColor.LIGHT_PURPLE),
            getDurationInfo(LingeringPotionTrap.DURATION),
        ))
        it.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP)
        it.color = Color.fromRGB(100, 62, 46)
    }
    return potion
}

private fun makeSplashBow(): ItemStack {
    val splashBow = ItemStack(Material.BOW)
    splashBow.editMeta(Damageable::class.java) {
        it.displayName(Component.text("Splash Bow").decoration(TextDecoration.ITALIC, false))
        it.lore(listOf(
            Component.text("A bow that shoots arrows exploding on impact"),
            Component.empty(),
            getDamageInfo("splash", 6, 'r')
        ))
        it.addEnchant(Enchantment.PUNCH, 10, true)
        it.addItemFlags(ItemFlag.HIDE_ENCHANTS)
        it.damage = 384
    }
    return splashBow
}

private fun makeLongHandsItem(): ItemStack {
    val coolRod = Menu.makeItem(
        Material.FISHING_ROD, Component.text("Cool Rod"),
        Component.text("Punch someone far away, only 1 punch granted"),
        Component.empty(),
        getDamageInfo("punch", 50, 'm')
    )
    coolRod.editMeta {
        val key = NamespacedKey(Main.getPlugin(), "extra_range_${UUID.randomUUID()}")

        val extraRangeModifier = AttributeModifier(
            key,
            50.0,
            AttributeModifier.Operation.ADD_NUMBER
        )
        it.addAttributeModifier(Attribute.ENTITY_INTERACTION_RANGE, extraRangeModifier)
    }
    return coolRod
}

private fun makeRiptideTrident(): ItemStack {
    val trident = ItemStack(Material.TRIDENT)
    trident.editMeta {
        it.displayName(Component.text("Trident").decoration(TextDecoration.ITALIC, false))
        it.lore(listOf(
            Component.text("Makes you fly thru the air"),
            Component.text("and damage opponents in a 2m radius"),
            Component.empty(),
            getDamageInfo("aura", 2, 'r')
        ))
        it.attributeModifiers = null
        it.addItemFlags(ItemFlag.HIDE_ATTRIBUTES)
    }
    return trident
}

private fun makeLaser(): ItemStack {
    val laserHead = Util.playerHead("dispenser")
    laserHead.editMeta {
        it.displayName(Component.text("Laser").decoration(TextDecoration.ITALIC, false))
        it.lore(listOf(
            Component.text("Emits a laser that damages enemies"),
            Component.text("unlimited range"),
            Component.empty(),
            getDamageInfo("laser", 1, 'r'),
        ))
    }
    laserHead.setData<ItemAdventurePredicate>(
        DataComponentTypes.CAN_PLACE_ON,
        ItemAdventurePredicate.itemAdventurePredicate().addPredicate(BlockPredicate.predicate().build())
    )
    return laserHead
}

private fun getDurationInfo(seconds: Int): TextComponent {
    return Component.text("Duration: ", NamedTextColor.DARK_GREEN)
        .append(Component.text("${seconds}s", NamedTextColor.GREEN))
        .decoration(TextDecoration.ITALIC, false)
}

private fun getDamageInfo(damageType: String, value: Int, unit: Char): TextComponent {
    return Component.text("Damage: ", NamedTextColor.DARK_RED)
        .append(
            Component.text(damageType, NamedTextColor.RED).appendSpace()
                .append(Component.text("$value$unit"))
        )
        .decoration(TextDecoration.ITALIC, false)
}

private fun getPotionEffectInfo(potionEffect: String): TextComponent {
    return Component.text("Potion effect: ", NamedTextColor.DARK_AQUA)
        .append(Component.text(potionEffect, NamedTextColor.AQUA))
        .decoration(TextDecoration.ITALIC, false)
}

private fun appendCooldownInfo(item: ItemStack, category: AbilityCategory, baseCooldown: Int) {
    val cooldownComponent: Component = if (category == AbilityCategory.DAMAGING)
        Component.text("Cooldown: ", NamedTextColor.GOLD)
            .append(Component.text("obtain by hitting opponent", NamedTextColor.YELLOW))
    else
        Component.text("Cooldown: ", NamedTextColor.GOLD)
            .append(Component.text("${baseCooldown}s", NamedTextColor.YELLOW))
    item.editMeta {
        val lore = it.lore() ?: mutableListOf()
        lore.add(cooldownComponent.decoration(TextDecoration.ITALIC, false))
        it.lore(lore)
    }
}