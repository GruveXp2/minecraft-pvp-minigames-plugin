package gruvexp.bbminigames.menu.menus

import gruvexp.bbminigames.menu.SettingsMenu
import gruvexp.bbminigames.twtClassic.BotBowsMap
import gruvexp.bbminigames.twtClassic.Settings
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack

internal enum class UiMode(menuTitle: TextComponent) {
    MAIN(Component.text("Arena map (1/6)")),
    VOTE(Component.text("Vote for map")),
    SET(Component.text("Set map"));

    val menuTitle: TextComponent?

    init {
        this.menuTitle = menuTitle
    }
}

class MapMenu(settings: Settings?) : SettingsMenu(settings) {
    private var isOldMapCategory = false
    private val uiMode = UiMode.MAIN

    override fun getMenuName(): Component {
        return Component.text("Arena map (1/6)")
    }

    override fun getSlots(): Int {
        return 18
    }

    override fun handleMenu(e: InventoryClickEvent) {
        val clicker = e.getWhoClicked() as Player
        if (e.getClickedInventory() !== inventory) return
        if (!clickedOnBottomButtons(e) && !settings.playerIsMod(settings.lobby.getBotBowsPlayer(clicker))) return
        val clickedItem = e.getCurrentItem()
        if (clickedItem == null) return

        when (clickedItem.getType()) {
            Material.SLIME_BALL -> settings.setMap(BotBowsMap.CLASSIC_ARENA)
            Material.SPRUCE_SAPLING -> {
                if (isOldMapCategory) settings.setMap(BotBowsMap.ROCKET_FOREST)
                else settings.setMap(BotBowsMap.ICY_RAVINE)
            }

            Material.MAGMA_BLOCK             -> settings.setMap(BotBowsMap.PIGLIN_HIDEOUT)
            Material.COPPER_BULB             -> settings.setMap(BotBowsMap.STEAMPUNK)
            Material.STONE_BRICK_STAIRS      -> settings.setMap(BotBowsMap.ROYAL_CASTLE)
            Material.RED_SAND                -> clicker.sendMessage(Component.text("This map is not added yet", NamedTextColor.RED))
            Material.GREEN_GLAZED_TERRACOTTA -> settings.setMap(BotBowsMap.INSIDE_BOTBASE)
            Material.GRASS_BLOCK             -> settings.setMap(BotBowsMap.OUTSIDE_BOTBASE)
            Material.CRAFTER                 -> settings.setMap(BotBowsMap.ROCKET)
            Material.GLASS                   -> settings.setMap(BotBowsMap.SPACE_STATION)
            Material.FIREWORK_STAR           -> {
                if (e.slot == slots - 4) {
                    settings.teamsMenu.open(clicker)
                } else if (e.slot == slots - 5) {
                    isOldMapCategory = !isOldMapCategory
                    updateMenu()
                }
            }

            else -> {}
        }
    }

    private fun sendExperimentalLockedMessage(player: Player) {
        player.sendMessage(
            Component.text("This map is not fully added yet. To play on it, run ", NamedTextColor.YELLOW).append(
                Component.text("/test toggle_experimental", NamedTextColor.AQUA, TextDecoration.UNDERLINED)
            )
                .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/test toggle_experimental"))
        )
    }

    fun updateMenu() {
        when (uiMode) {
            UiMode.MAIN -> {
                inventory.setItem(0, BotBowsMap.INSIDE_BOTBASE.getMenuItem())
            }
            UiMode.VOTE, UiMode.SET -> {
                if (isOldMapCategory) {
                    inventory.setItem(1, BotBowsMap.INSIDE_BOTBASE.getMenuItem())
                    inventory.setItem(2, BotBowsMap.OUTSIDE_BOTBASE.getMenuItem())
                    inventory.setItem(3, BotBowsMap.ROCKET_FOREST.getMenuItem())
                    inventory.setItem(4, VOID)
                    inventory.setItem(5, BotBowsMap.ROCKET.getMenuItem())
                    inventory.setItem(6, BotBowsMap.SPACE_STATION.getMenuItem())
                    inventory.setItem(7, BotBowsMap.MARS_BASE.getMenuItem())
                    inventory.setItem(13, MAP_CATEGORY_OLD)
                } else {
                    inventory.setItem(1, VOID)
                    inventory.setItem(2, BotBowsMap.CLASSIC_ARENA.getMenuItem())
                    inventory.setItem(3, BotBowsMap.ICY_RAVINE.getMenuItem())
                    inventory.setItem(4, BotBowsMap.ROYAL_CASTLE.getMenuItem())
                    inventory.setItem(5, BotBowsMap.STEAMPUNK.getMenuItem())
                    inventory.setItem(6, BotBowsMap.PIGLIN_HIDEOUT.getMenuItem())
                    inventory.setItem(7, VOID)
                    inventory.setItem(13, MAP_CATEGORY_MODERN)
                }
            }
        }
    }

    override fun setMenuItems() {
        setPageButtons(1, false, true)
        updateMenu()
        setFillerVoid()
    }

    companion object {
        val MAP_CATEGORY_MODERN: ItemStack = makeItem(
            "gear", Component.text("Map category"),
            Component.text("Modern BotBows", NamedTextColor.GREEN),
            Component.text("2023-", NamedTextColor.DARK_GREEN)
        )

        val MAP_CATEGORY_OLD: ItemStack = makeItem(
            "gear", Component.text("Map category"),
            Component.text("Old BotBows", NamedTextColor.YELLOW),
            Component.text("2019-2020", NamedTextColor.GOLD)
        )

        val VOTE: ItemStack = makeItem(Material.PAPER, Component.text("Vote for map"))

        val VOTE_MODE_ENABLED: ItemStack = makeItem(
            Material.LIME_STAINED_GLASS_PANE, Component.text("Vote mode"),
            STATUS_ENABLED,
            Component.text("The map with most votes will be used in the match")
        )

        val VOTE_MODE_DISABLED: ItemStack = makeItem(
            Material.RED_STAINED_GLASS_PANE, Component.text("Vote mode"),
            STATUS_DISABLED,
            Component.text("The map with most votes will be used in the match")
        )
    }
}
