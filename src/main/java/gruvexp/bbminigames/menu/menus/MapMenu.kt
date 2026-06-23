package gruvexp.bbminigames.menu.menus

import gruvexp.bbminigames.menu.SettingsMenu
import gruvexp.bbminigames.twtClassic.BotBowsMap
import gruvexp.bbminigames.twtClassic.BotBowsPlayer
import gruvexp.bbminigames.twtClassic.Settings
import gruvexp.bbminigames.twtClassic.avatar.BotBowsAvatar
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

class MapMenu(settings: Settings, val bp: BotBowsPlayer) : SettingsMenu(settings), MapUpdateListener {
    private var isOldMapCategory = false
        set(value) {
            field = value
            updateMenu()
        }

    private var uiMode = UiMode.MAIN
        set(value) {
            field = value
            updateMenu()
        }

    init {
        setPageButtons(1, false, true)
        updateMenu()
    }

    override fun getMenuName(): Component = Component.text("Arena map (1/6)")
    override fun getSlots(): Int = 18

    override fun open(p: Player) {
        super.open(p)
        uiMode = UiMode.MAIN
    }

    override fun handleMenu(e: InventoryClickEvent) {
        if (e.clickedInventory !== inventory) return
        val clickedItem = e.currentItem ?: return
        if (handlePageClick(e)) return
        val mapSettings = settings.mapSettings

        val mapStr = clickedItem.persistentDataContainer.get(BotBowsMap.KEY, PersistentDataType.STRING)
        if (mapStr != null) {
            val map = BotBowsMap.valueOf(mapStr)
            if (map == BotBowsMap.MARS_BASE) {
                bp.avatar.message(Component.text("This map is not added yet", NamedTextColor.RED))
                return
            }
            if (map == BotBowsMap.STEAMPUNK) {
                sendBrokedLockedMessage(bp.avatar)
                return
            }
            if (mapSettings.isVoteMode) {
                mapSettings.mapVotingSession.vote(bp, map)
            } else {
                mapSettings.currentMap = map

                settings.lobby.messagePlayers(
                    Component.text("Map set to ").append(Component.text(map.prettyName(), NamedTextColor.GREEN))
                )
            }
            uiMode = UiMode.MAIN
            return
        }

        val action = MenuAction.valueOf(getActionId(clickedItem) ?: return)
        when (action) {
            MenuAction.VOTE -> uiMode = UiMode.VOTE
            MenuAction.SET -> if (settings.checkMod(bp)) uiMode = UiMode.SET
            MenuAction.TOGGLE_VOTE -> if (settings.checkMod(bp))  mapSettings.isVoteMode = !mapSettings.isVoteMode
            MenuAction.CYCLE_MAP_CATEGORY -> isOldMapCategory = !isOldMapCategory
            MenuAction.BACK -> uiMode = UiMode.MAIN
        }
    }

    override fun nextPage(p: Player) = settings.teamsMenu.open(p)

    private fun sendExperimentalLockedMessage(avatar: BotBowsAvatar) {
        avatar.message(
            Component.text("This map is not fully added yet. To play on it, run ", NamedTextColor.YELLOW)
                .append(Component.text("/test toggle_experimental", NamedTextColor.AQUA, TextDecoration.UNDERLINED))
                .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, ClickEvent.Payload.string("/test toggle_experimental")))
        )
    }

    private fun sendBrokedLockedMessage(avatar: BotBowsAvatar) {
        avatar.message(
            Component.text("This map is currently broken (someone did /kill @e). To play on it, run ", NamedTextColor.YELLOW)
                .append(Component.text("/test toggle_experimental", NamedTextColor.AQUA, TextDecoration.UNDERLINED))
                .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, ClickEvent.Payload.string("/test toggle_experimental")))
        )
    }

    fun updateMenu() {
        when (uiMode) {
            UiMode.MAIN -> {
                inventory.setItem(13, VOID)
                if (settings.mapSettings.isVoteMode) {
                    inventory.setItem(9, VOTE_MODE_ENABLED)
                    inventory.setItem(0, VOTE)
                    displayVotes()
                } else {
                    inventory.setItem(9, VOTE_MODE_DISABLED)
                    if (settings.checkMod(bp)) {
                        inventory.setItem(0, SET_MAP)
                    } else {
                        inventory.setItem(0, DISABLED_SLOT)
                    }
                    for (slot in 3..8) {
                        inventory.setItem(slot, VOID)
                    }
                    displayCurrentMap()
                }
            }
            UiMode.VOTE, UiMode.SET -> {
                inventory.setItem(0, BACK)
                if (isOldMapCategory) {
                    inventory.setItem(1, BotBowsMap.INSIDE_BOTBASE.getMenuItem())
                    inventory.setItem(2, BotBowsMap.OUTSIDE_BOTBASE.getMenuItem())
                    inventory.setItem(3, BotBowsMap.ROCKET_FOREST.getMenuItem())
                    inventory.setItem(4, VOID)
                    inventory.setItem(5, BotBowsMap.ROCKET.getMenuItem())
                    inventory.setItem(6, BotBowsMap.SPACE_STATION.getMenuItem())
                    inventory.setItem(7, BotBowsMap.MARS_BASE.getMenuItem())
                    inventory.setItem(8, VOID)
                    inventory.setItem(13, MAP_CATEGORY_OLD)
                } else {
                    inventory.setItem(1, VOID)
                    inventory.setItem(2, BotBowsMap.CLASSIC_ARENA.getMenuItem())
                    inventory.setItem(3, BotBowsMap.ICY_RAVINE.getMenuItem())
                    inventory.setItem(4, BotBowsMap.ROYAL_CASTLE.getMenuItem())
                    inventory.setItem(5, BotBowsMap.STEAMPUNK.getMenuItem())
                    inventory.setItem(6, BotBowsMap.PIGLIN_HIDEOUT.getMenuItem())
                    inventory.setItem(7, VOID)
                    inventory.setItem(8, BotBowsMap.RANDOM.getMenuItem())
                    inventory.setItem(13, MAP_CATEGORY_MODERN)
                }
            }
        }
    }

    fun displayVotes() {
        for (slot in 2..8) {
            inventory.setItem(slot, null)
        }
        val mapVotingSession = settings.mapSettings.mapVotingSession
        mapVotingSession.getVotedMaps()
            .sortedByDescending { map -> mapVotingSession.getVotes(map) }
            .take(7)
            .forEachIndexed { i, map ->
                val mapItem = map.menuItem.clone()
                mapItem.amount = mapVotingSession.getVotes(map)
                inventory.setItem(2 + i, mapItem)
            }
    }

    fun displayCurrentMap() {
        inventory.setItem(2, settings.mapSettings.currentMap.menuItem)
    }

    override fun onVoteToggle() {
        updateMenu()
    }

    override fun onVote() {
        if (uiMode == UiMode.MAIN && settings.mapSettings.isVoteMode)
            displayVotes()
    }

    override fun onMapSet() {
        if (uiMode == UiMode.MAIN) displayCurrentMap()
    }

    companion object {
        val MAP_CATEGORY_MODERN: ItemStack = makeItem(
            "gear", Component.text("Map category"),
            MenuAction.CYCLE_MAP_CATEGORY.name,
            Component.text("Modern BotBows", NamedTextColor.GREEN),
            Component.text("2023-", NamedTextColor.DARK_GREEN)
        )

        val MAP_CATEGORY_OLD: ItemStack = makeItem(
            "gear", Component.text("Map category"),
            MenuAction.CYCLE_MAP_CATEGORY.name,
            Component.text("Old BotBows", NamedTextColor.YELLOW),
            Component.text("2019-2020", NamedTextColor.GOLD)
        )

        val VOTE: ItemStack = makeItem(Material.PAPER, Component.text("Vote for map"), MenuAction.VOTE.name)
        val SET_MAP: ItemStack = makeItem(Material.MAP, Component.text("Set the map"), MenuAction.SET.name)
        val BACK: ItemStack = makeItem("back", Component.text("Back"), MenuAction.BACK.name)

        val VOTE_MODE_ENABLED: ItemStack = makeItem(
            Material.LIME_STAINED_GLASS_PANE, Component.text("Vote mode"),
            MenuAction.TOGGLE_VOTE.name,
            STATUS_ENABLED,
            Component.text("The map with most votes will be used in the match")
        )

        val VOTE_MODE_DISABLED: ItemStack = makeItem(
            Material.RED_STAINED_GLASS_PANE, Component.text("Vote mode"),
            MenuAction.TOGGLE_VOTE.name,
            STATUS_DISABLED,
            Component.text("The map with most votes will be used in the match")
        )
    }

    private enum class UiMode(menuTitle: TextComponent) {
        MAIN(Component.text("Arena map (1/6)")),
        VOTE(Component.text("Vote for map")),
        SET(Component.text("Set map"));

        val menuTitle: TextComponent

        init {
            this.menuTitle = menuTitle
        }
    }

    private enum class MenuAction {
        VOTE,
        SET,
        TOGGLE_VOTE,
        CYCLE_MAP_CATEGORY,
        BACK
    }
}