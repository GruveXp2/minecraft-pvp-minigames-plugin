package gruvexp.bbminigames.menu.menus

import gruvexp.bbminigames.menu.SettingsMenu
import gruvexp.bbminigames.twtClassic.Settings
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Color
import org.bukkit.DyeColor
import org.bukkit.Material
import org.bukkit.block.banner.Pattern
import org.bukkit.block.banner.PatternType
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.BannerMeta
import org.bukkit.inventory.meta.PotionMeta

class OverviewMenu(settings: Settings): SettingsMenu(settings) {

    init {
        inventory.setItem(1, PRESETS)
        inventory.setItem(2, MAP_SELECTION)
        inventory.setItem(3, TEAM_SELECTION)
        inventory.setItem(4, HEALTH)
        inventory.setItem(5, WIN_CONDITION)
        inventory.setItem(6, HAZARDS)
        inventory.setItem(7, ABILITIES)
    }

    override fun getMenuName(): Component = Component.text("Settings")
    override fun getSlots(): Int = 9

    override fun handleMenu(e: InventoryClickEvent) {
        if (e.clickedInventory !== inventory) return
        val clickedItem = e.currentItem ?: return
        val clicker = e.whoClicked as Player
        val bp = settings.lobby.getBotBowsPlayer(clicker) ?: return

        val action = MenuAction.valueOf(getActionId(clickedItem) ?: return)
        when (action) {
            MenuAction.PRESETS -> clicker.sendMessage(Component.text("Not added yet, will be added soon:tm:", NamedTextColor.YELLOW))
            MenuAction.MAP_SELECTION -> settings.mapMenus[bp]?.open(clicker)
            MenuAction.TEAM_SELECTION -> settings.teamsMenu.open(clicker)
            MenuAction.HEALTH -> settings.healthMenu.open(clicker)
            MenuAction.WIN_CONDITION -> settings.winConditionMenu.open(clicker)
            MenuAction.HAZARDS -> settings.hazardMenu.open(clicker)
            MenuAction.ABILITIES -> settings.abilityMenus[bp]?.open(clicker)
        }
    }

    companion object {
        val PRESETS: ItemStack = makeItem(
            Material.COMMAND_BLOCK,
            Component.text("Presets"),
            MenuAction.PRESETS.name,
            Component.text("Load predefined settings for a match")
        )

        val MAP_SELECTION: ItemStack = makeItem(
            Material.MAP,
            Component.text("Map Selection"),
            MenuAction.MAP_SELECTION.name,
            Component.text("Vote/set the map to be played on")
        )

        val TEAM_SELECTION: ItemStack = makeItem(
            Material.RED_BANNER,
            Component.text("Team Selection"),
            MenuAction.TEAM_SELECTION.name,
            Component.text("Choose which team each player will be on")
        ).apply { editMeta { (it as BannerMeta).addPattern(Pattern(DyeColor.LIGHT_BLUE, PatternType.DIAGONAL_RIGHT)) } }

        val HEALTH: ItemStack = makeItem(
            Material.TIPPED_ARROW,
            Component.text("Health"),
            MenuAction.HEALTH.name,
            Component.text("Select health and damage multiplier")
        ).apply { editMeta { (it as PotionMeta).color = Color.RED } }

        val WIN_CONDITION: ItemStack = makeItem(
            Material.TEST_BLOCK,
            Component.text("Win conditions", NamedTextColor.WHITE),
            MenuAction.WIN_CONDITION.name,
            Component.text("Select conditions for ending match"),
            Component.text("like round timer and point to win")
        )

        val HAZARDS: ItemStack = makeItem(
            Material.LIGHTNING_ROD,
            Component.text("Hazards"),
            MenuAction.HAZARDS.name,
            Component.text("Controls what hazards may occur,"),
            Component.text("like storms, earthquakes, etc")
        )

        val ABILITIES: ItemStack = makeItem(
            Material.ENDER_PEARL,
            Component.text("Abilities"),
            MenuAction.ABILITIES.name,
            Component.text("Settings related to abilities and equipping them")
        )
    }

    private enum class MenuAction {
        PRESETS,
        MAP_SELECTION,
        TEAM_SELECTION,
        HEALTH,
        WIN_CONDITION,
        HAZARDS,
        ABILITIES
    }
}