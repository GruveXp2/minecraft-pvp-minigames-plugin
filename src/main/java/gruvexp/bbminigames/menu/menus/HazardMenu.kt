package gruvexp.bbminigames.menu.menus

import gruvexp.bbminigames.menu.MenuSlider
import gruvexp.bbminigames.menu.SettingsMenu
import gruvexp.bbminigames.twtClassic.BotBows
import gruvexp.bbminigames.twtClassic.Settings
import gruvexp.bbminigames.twtClassic.hazard.Hazard
import gruvexp.bbminigames.twtClassic.hazard.HazardChance
import gruvexp.bbminigames.twtClassic.hazard.HazardType
import gruvexp.bbminigames.twtClassic.settings.HazardUpdateListener
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import java.util.*

class HazardMenu(settings: Settings?) : SettingsMenu(settings), HazardUpdateListener {
    private val hazardsSorted = ArrayList<HazardType>()

    private val hazardSliders = HashMap<HazardType?, MenuSlider>()

    init {
        setPageButtons(3, true, true)
    }

    private fun getHazardItem(hazard: Hazard?): ItemStack {
        if (hazard == null) {
            return makeItem(
                Material.BARRIER, Component.text("Unavailable", NamedTextColor.RED),
                Component.text("This hazard is not compatible with this map", NamedTextColor.RED)
            )
        }
        val item: ItemStack
        val loreDesc = hazard.getDescription()
        if (hazard.chance == HazardChance.DISABLED) {
            item = makeItem(
                Material.RED_STAINED_GLASS_PANE,
                Component.text(hazard.getName(), NamedTextColor.RED),
                MenuAction.TOGGLE_HAZARD.name,
                STATUS_DISABLED,
                Component.text("If enabled, x% of rounds " + hazard.getActionDescription() + "."),
                loreDesc[0],
                loreDesc[1],
                loreDesc[2]
            )
        } else {
            val percentage: Component =
                Component.text(hazard.chance.percent.toString() + "%", NamedTextColor.LIGHT_PURPLE)
            item = makeItem(
                Material.LIME_STAINED_GLASS_PANE, Component.text(hazard.getName(), NamedTextColor.GREEN),
                MenuAction.TOGGLE_HAZARD.name,
                STATUS_ENABLED,
                percentage.append(
                    Component.text(
                        " of rounds " + hazard.getActionDescription() + ".",
                        NamedTextColor.DARK_PURPLE
                    )
                ), loreDesc[0], loreDesc[1], loreDesc[2]
            )
        }
        return item
    }

    override fun getMenuName(): Component {
        return Component.text("Hazards (5/6)")
    }

    override fun getSlots(): Int {
        return 36
    }

    override fun handleMenu(e: InventoryClickEvent) {
        if (e.clickedInventory !== inventory) return
        val clickedItem = e.getCurrentItem() ?: return
        if (handlePageClick(e)) return
        val clicker = e.whoClicked as Player
        val bp = settings.lobby.getBotBowsPlayer(clicker)
        if (!settings.checkMod(bp)) return

        val actionId =
            clickedItem.persistentDataContainer.get<String, String>(ACTION_KEY, PersistentDataType.STRING) ?: return

        val action = MenuAction.valueOf(actionId)
        val hazardSettings = settings.hazardSettings
        when (action) {
            MenuAction.TOGGLE_HAZARD -> {
                val row = e.slot / 9
                val type = hazardsSorted[row]
                if (hazardSettings.getChance(type) == HazardChance.DISABLED) {
                    var chance = type.defaultChance
                    if (chance == HazardChance.DISABLED) chance = HazardChance.FIVE
                    hazardSettings.setChance(type, chance)
                } else {
                    hazardSettings.setChance(type, HazardChance.DISABLED)
                }
            }
            MenuAction.SET_HAZARD_CHANCE -> {
                val c: Component = checkNotNull(e.getCurrentItem()!!.itemMeta.displayName())
                val s = PlainTextComponentSerializer.plainText().serialize(c)
                val chance = HazardChance.of(s)
                val row = e.slot / 9
                val type = hazardsSorted[row]
                hazardSettings.setChance(type, chance)
            }
        }
    }

    public override fun prevPage(p: Player) {
        settings.winConditionMenu.open(p)
    }

    public override fun nextPage(p: Player) {
        settings.abilityMenus[BotBows.getBotBowsPlayer(p)]!!.open(p)
    }

    fun updateBar(hazardType: HazardType, row: Int) {
        val hazardSettings = settings.hazardSettings
        inventory.setItem(row * 9, getHazardItem(hazardSettings.getHazard(hazardType)))
        hazardSliders[hazardType]!!.setProgress(hazardSettings.getChance(hazardType).toString())
    }

    override fun onSchemaUpdate() {
        val newHazards: Set<HazardType> = settings.hazardSettings.getHazardTypes()
        val updated = Arrays.stream(HazardType.entries.toTypedArray())
            .filter { hazard: HazardType -> newHazards.contains(hazard) }
            .toList()

        hazardsSorted.stream()
            .filter { hazard: HazardType -> !updated.contains(hazard) }
            .forEach { hazard: HazardType -> hazardSliders.remove(hazard) }

        hazardsSorted.clear()
        hazardsSorted.addAll(updated)

        // Add sliders for newly compatible hazards and recalculate the order they show in
        for (hazardType in hazardsSorted) {
            val slider = hazardSliders.computeIfAbsent(hazardType) { _ ->
                MenuSlider(
                    inventory,
                    MenuAction.SET_HAZARD_CHANCE.name,
                    2 + hazardsSorted.indexOf(hazardType) * 9,
                    hazardType.menuFillItem,
                    hazardType.textColor,
                    PERCENT,
                    hazardType.name + " chance"
                )
            }
            slider.setStartSlot(2 + hazardsSorted.indexOf(hazardType) * 9)
            updateBar(hazardType, hazardsSorted.indexOf(hazardType))
        }

        for (i in hazardsSorted.size * 9..<slots - 9) {
            inventory.setItem(i, VOID)
        }
    }

    override fun onHazardUpdate(type: HazardType) {
        val row = hazardsSorted.indexOf(type)
        updateBar(type, row)
    }

    companion object {
        private val PERCENT: MutableList<String?> = HazardChance.getPercentStrings()
    }

    private enum class MenuAction {
        TOGGLE_HAZARD,
        SET_HAZARD_CHANCE
    }
}