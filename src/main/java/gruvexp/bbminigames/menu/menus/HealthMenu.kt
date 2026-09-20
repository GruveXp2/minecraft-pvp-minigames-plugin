package gruvexp.bbminigames.menu.menus

import gruvexp.bbminigames.Main
import gruvexp.bbminigames.menu.MenuSlider
import gruvexp.bbminigames.menu.PlayerListMenu
import gruvexp.bbminigames.menu.PlayerMenuRow
import gruvexp.bbminigames.menu.SettingsMenu
import gruvexp.bbminigames.twtClassic.BotBowsPlayer
import gruvexp.bbminigames.twtClassic.Settings
import gruvexp.bbminigames.twtClassic.settings.HealthUpdateListener
import gruvexp.bbminigames.twtClassic.settings.player.PlayerHealthUpdateListener
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import java.util.*

class HealthMenu(settings: Settings)
    : SettingsMenu(settings, Component.text("Health, Damage, Speed (3/6)"), 36), PlayerListMenu, HealthUpdateListener, PlayerHealthUpdateListener {
    private val healthSlider: MenuSlider
    private val healthRow: PlayerMenuRow
    private val damageRow: PlayerMenuRow
    private val speedSlider: MenuSlider
    private val speedRow: PlayerMenuRow

    init {
        setPageButtons(3, prevMenuButton = true, nextMenuButton = true)
        healthSlider = MenuSlider(
            inventory,
            MenuAction.SET_HEALTH.name,
            2,
            Material.PINK_STAINED_GLASS_PANE,
            NamedTextColor.RED,
            mutableListOf("1", "2", "3", "4", "5"),
            "Health"
        )
        speedSlider = MenuSlider(
            inventory,
            MenuAction.SET_SPEED.name,
            20,
            Material.LIGHT_BLUE_STAINED_GLASS_PANE,
            NamedTextColor.AQUA,
            mutableListOf("1", "2", "3", "4", "5"),
            "Speed"
        )
        healthRow = PlayerMenuRow(inventory, MenuAction.SET_INDIVIDUAL_HEALTH.name, 2, 5)
        damageRow = PlayerMenuRow(inventory, MenuAction.SET_INDIVIDUAL_DAMAGE.name, 11, 7)
        speedRow = PlayerMenuRow(inventory, MenuAction.SET_INDIVIDUAL_SPEED.name, 20, 5)

        onIndividualMaxHealthToggle()
        onMaxHealthChange()
        onCustomDamageToggle()
        onIndividualSpeedToggle()
        onSpeedChange()
    }

    override fun handleMenu(e: InventoryClickEvent) {
        if (e.clickedInventory !== inventory) return
        val clickedItem = e.getCurrentItem() ?: return
        if (handlePageClick(e)) return
        val clicker = e.whoClicked as Player
        val bp = settings.lobby.getBotBowsPlayer(clicker)!!
        if (!settings.checkMod(bp)) return


        val action = MenuAction.valueOf(getActionId(clickedItem) ?: return)
        val healthSettings = settings.healthSettings
        when (action) {
            MenuAction.TOGGLE_INDIVIDUAL_HEALTH -> healthSettings.isIndividualMaxHealth = !healthSettings.isIndividualMaxHealth
            MenuAction.SET_HEALTH -> {
                val component = clickedItem.itemMeta.displayName() ?: return
                val string = PlainTextComponentSerializer.plainText().serialize(component)
                healthSettings.maxHealth = string.toInt()
            }
            MenuAction.SET_INDIVIDUAL_HEALTH -> {
                val bp = getPlayerFromHead(clickedItem) ?: return

                var maxHealth = clickedItem.amount
                if (maxHealth > 9) {
                    maxHealth += 5
                    if (maxHealth > 20) maxHealth = 1
                } else {
                    maxHealth += 1
                }
                bp.settings.maxHealth = maxHealth
            }
            MenuAction.TOGGLE_CUSTOM_DAMAGE -> healthSettings.isCustomDamage = !healthSettings.isCustomDamage
            MenuAction.SET_INDIVIDUAL_DAMAGE -> {
                val bp = getPlayerFromHead(clickedItem) ?: return

                var attackDamage = clickedItem.amount
                attackDamage += 1
                if (attackDamage > 5) attackDamage = 1

                bp.settings.attackDamage = attackDamage
            }
            MenuAction.TOGGLE_INDIVIDUAL_SPEED -> healthSettings.isIndividualSpeed = !healthSettings.isIndividualSpeed
            MenuAction.SET_SPEED -> {
                val component = clickedItem.itemMeta.displayName() ?: return
                val string = PlainTextComponentSerializer.plainText().serialize(component)
                healthSettings.speed = string.toInt()
            }
            MenuAction.SET_INDIVIDUAL_SPEED -> {
                val bp = getPlayerFromHead(clickedItem) ?: return

                var speed = clickedItem.amount
                speed += 1
                if (speed > 5) speed = 1

                bp.settings.speed = speed
            }
        }
    }

    fun getPlayerFromHead(item: ItemStack) : BotBowsPlayer? {
        val key = NamespacedKey(Main.plugin, "uuid")
        val playerIdStr = item.itemMeta.persistentDataContainer.get(key, PersistentDataType.STRING) ?: return null
        val playerId = UUID.fromString(playerIdStr)
        val bp = settings.lobby.getBotBowsPlayer(playerId)
        return bp
    }

    public override fun prevPage(p: Player) = settings.teamsMenu.open(p)
    public override fun nextPage(p: Player) = settings.winConditionMenu.open(p)

    override fun onIndividualMaxHealthToggle() {
        if (settings.healthSettings.isIndividualMaxHealth) {
            inventory.setItem(0, CUSTOM_HEALTH_ENABLED)
            inventory.setItem(1, VOID)
            healthRow.show()
        } else {
            inventory.setItem(0, CUSTOM_HEALTH_DISABLED)
            inventory.setItem(1, VOID)
            inventory.setItem(7, VOID)
            inventory.setItem(8, VOID)
            healthRow.hide()
            healthSlider.setProgressSlots(settings.healthSettings.maxHealth)
        }
    }

    override fun onMaxHealthChange() {
        healthSlider.setProgressSlots(settings.healthSettings.maxHealth)
    }

    override fun onMaxHealthChange(bp: BotBowsPlayer) {
        if (!settings.healthSettings.isIndividualMaxHealth) return

        val headItem = healthRow.getItem(bp) ?: return
        headItem.amount = bp.settings.maxHealth
        healthRow.displayRow()
    }

    override fun onCustomDamageToggle() {
        if (settings.healthSettings.isCustomDamage) {
            inventory.setItem(9, CUSTOM_DAMAGE_ENABLED)
            inventory.setItem(10, VOID)
            damageRow.show()
        } else {
            inventory.setItem(9, CUSTOM_DAMAGE_DISABLED)
            inventory.setItem(10, VOID)
            for (i in 0..6) {
                inventory.setItem(11 + i, DISABLED_SLOT)
            }
            damageRow.hide()
        }
    }

    override fun onIndividualSpeedToggle() {
        if (settings.healthSettings.isIndividualSpeed) {
            inventory.setItem(18, CUSTOM_SPEED_ENABLED)
            inventory.setItem(19, VOID)
            speedRow.show()
        } else {
            inventory.setItem(18, CUSTOM_SPEED_DISABLED)
            inventory.setItem(19, VOID)
            inventory.setItem(25, VOID)
            inventory.setItem(26, VOID)
            speedRow.hide()
            speedSlider.setProgressSlots(settings.healthSettings.speed)
        }
    }

    override fun onAttackDamageChange(bp: BotBowsPlayer) {
        if (!settings.healthSettings.isCustomDamage) return

        val headItem = damageRow.getItem(bp) ?: return
        headItem.amount = bp.settings.attackDamage
        damageRow.displayRow()
    }

    override fun onSpeedChange() {
        speedSlider.setProgressSlots(settings.healthSettings.speed)
    }

    override fun onSpeedChange(bp: BotBowsPlayer) {
        if (!settings.healthSettings.isIndividualSpeed) return

        val headItem = speedRow.getItem(bp) ?: return
        headItem.amount = bp.settings.speed
        speedRow.displayRow()
    }

    override fun addPlayer(bp: BotBowsPlayer) {
        val maxHealthHead = bp.avatar.headItem.apply { amount = bp.settings.maxHealth }
        healthRow.addItem(maxHealthHead)

        val damageHead = bp.avatar.headItem.apply { amount = bp.settings.attackDamage }
        damageRow.addItem(damageHead)

        val speedHead = bp.avatar.headItem.apply { amount = bp.settings.speed }
        speedRow.addItem(speedHead)
    }

    override fun removePlayer(bp: BotBowsPlayer) {
        healthRow.removeItem(bp)
        damageRow.removeItem(bp)
        speedRow.removeItem(bp)
    }

    override fun updatePlayer(bp: BotBowsPlayer) {
        healthRow.editItem(bp) { displayName(bp.name) }
        damageRow.editItem(bp) { displayName(bp.name) }
        speedRow.editItem(bp) { displayName(bp.name) }
    }

    companion object {
        private val CUSTOM_HEALTH_DISABLED = makeItem(
            Material.RED_STAINED_GLASS_PANE, Component.text("Custom player HP", NamedTextColor.RED),
            MenuAction.TOGGLE_INDIVIDUAL_HEALTH.name,
            STATUS_DISABLED,
            Component.text("By enabling this, each player"),
            Component.text("can have a different amount of hp")
        )

        private val CUSTOM_HEALTH_ENABLED = makeItem(
            Material.LIME_STAINED_GLASS_PANE, Component.text("Custom player HP", NamedTextColor.GREEN),
            MenuAction.TOGGLE_INDIVIDUAL_HEALTH.name,
            STATUS_ENABLED,
            Component.text("By enabling this, each player"),
            Component.text("can have a different amount of hp")
        )

        private val CUSTOM_DAMAGE_DISABLED = makeItem(
            Material.RED_STAINED_GLASS_PANE, Component.text("Custom Damage", NamedTextColor.RED),
            MenuAction.TOGGLE_CUSTOM_DAMAGE.name,
            STATUS_DISABLED,
            Component.text("By enabling this, each player"),
            Component.text("ca do different amounts of damage")
        )

        private val CUSTOM_DAMAGE_ENABLED = makeItem(
            Material.LIME_STAINED_GLASS_PANE, Component.text("Custom Damage", NamedTextColor.GREEN),
            MenuAction.TOGGLE_CUSTOM_DAMAGE.name,
            STATUS_ENABLED,
            Component.text("By enabling this, each player"),
            Component.text("can do different amounts of damage")
        )

        private val CUSTOM_SPEED_DISABLED = makeItem(
            Material.RED_STAINED_GLASS_PANE, Component.text("Custom Speed", NamedTextColor.RED),
            MenuAction.TOGGLE_INDIVIDUAL_SPEED.name,
            STATUS_DISABLED,
            Component.text("By enabling this, each player"),
            Component.text("can move at different speeds")
        )

        private val CUSTOM_SPEED_ENABLED = makeItem(
            Material.LIME_STAINED_GLASS_PANE, Component.text("Custom Speed", NamedTextColor.GREEN),
            MenuAction.TOGGLE_INDIVIDUAL_SPEED.name,
            STATUS_ENABLED,
            Component.text("By enabling this, each player"),
            Component.text("can move at different speeds")
        )
    }

    private enum class MenuAction {
        SET_HEALTH,
        SET_SPEED,
        SET_INDIVIDUAL_DAMAGE,
        SET_INDIVIDUAL_HEALTH,
        SET_INDIVIDUAL_SPEED,
        TOGGLE_INDIVIDUAL_HEALTH,
        TOGGLE_CUSTOM_DAMAGE,
        TOGGLE_INDIVIDUAL_SPEED,
    }
}
