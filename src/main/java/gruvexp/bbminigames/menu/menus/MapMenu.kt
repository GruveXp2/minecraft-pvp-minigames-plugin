package gruvexp.bbminigames.menu.menus

import gruvexp.bbminigames.menu.SettingsMenu
import gruvexp.bbminigames.twtClassic.BotBowsMap
import gruvexp.bbminigames.twtClassic.BotBowsPlayer
import gruvexp.bbminigames.twtClassic.Settings
import gruvexp.bbminigames.twtClassic.settings.MapUpdateListener
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

internal enum class UiMode(menuTitle: TextComponent) {
    MAIN(Component.text("Arena map (1/6)")),
    VOTE(Component.text("Vote for map")),
    SET(Component.text("Set map"));

    val menuTitle: TextComponent

    init {
        this.menuTitle = menuTitle
    }
}

class MapMenu(settings: Settings?, val bp: BotBowsPlayer) : SettingsMenu(settings), MapUpdateListener {
    private var isOldMapCategory = false
    private var uiMode = UiMode.MAIN
        set(value) {
            field = value
            updateMenu()
        }

    init {
        setPageButtons(1, false, true)
        updateMenu()
    }

    override fun getMenuName(): Component {
        return Component.text("Arena map (1/6)")
    }

    override fun getSlots(): Int {
        return 18
    }

    override fun handleMenu(e: InventoryClickEvent) {
        val p = e.whoClicked as Player
        if (e.clickedInventory !== inventory) return
        val clickedItem = e.getCurrentItem() ?: return

        val mapStr =
            clickedItem.persistentDataContainer.get<String, String>(BotBowsMap.KEY, PersistentDataType.STRING)
        if (mapStr != null) {
            val map = BotBowsMap.valueOf(mapStr)
            if (map == BotBowsMap.MARS_BASE) {
                bp.avatar.message(Component.text("This map is not added yet", NamedTextColor.RED))
                return
            }
            //settings.setMap(map);
            settings.mapSettings.mapVotingSession.vote(bp, map)
            uiMode = UiMode.MAIN
            return
        }
        when(clickedItem.type) {
            Material.FIREWORK_STAR -> {
                if (e.slot == slots - 4) {
                    settings.teamsMenu.open(p)
                } else if (e.slot == slots - 5) {
                    isOldMapCategory = !isOldMapCategory
                    updateMenu()
                }
            }
            Material.PAPER -> uiMode = UiMode.VOTE
            else -> {}
        }
    }

    private fun sendExperimentalLockedMessage(player: Player) {
        player.sendMessage(
            Component.text("This map is not fully added yet. To play on it, run ", NamedTextColor.YELLOW).append(
                Component.text("/test toggle_experimental", NamedTextColor.AQUA, TextDecoration.UNDERLINED)
            )
                .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, ClickEvent.Payload.string("/test toggle_experimental")))
        )
    }

    fun updateMenu() {
        when (uiMode) {
            UiMode.MAIN -> {
                if (settings.mapSettings.isVoteMode) {
                    inventory.setItem(9, VOTE_MODE_ENABLED)
                    inventory.setItem(0, VOTE)
                } else {
                    inventory.setItem(9, VOTE_MODE_DISABLED)
                    inventory.setItem(0, DISABLED_SLOT)
                }
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

    override fun onVoteToggle() {
        updateMenu()
    }

    override fun onVote() {
        TODO("Not yet implemented")
    }

    override fun onMapSet() {
        val map = settings.mapSettings.currentMap
        inventory.setItem(2, map?.menuItem)
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
        val SET_MAP: ItemStack = makeItem(Material.PAPER, Component.text("Set the map"))

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
