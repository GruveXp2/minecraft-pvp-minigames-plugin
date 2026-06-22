package gruvexp.bbminigames.menu.menus

import gruvexp.bbminigames.Main
import gruvexp.bbminigames.menu.AbilityMenuRow
import gruvexp.bbminigames.menu.MenuSlider
import gruvexp.bbminigames.menu.PlayerMenuRow
import gruvexp.bbminigames.menu.SettingsMenu
import gruvexp.bbminigames.twtClassic.BotBowsPlayer
import gruvexp.bbminigames.twtClassic.Settings
import gruvexp.bbminigames.twtClassic.ability.AbilityType
import gruvexp.bbminigames.twtClassic.settings.AbilityUpdateListener
import gruvexp.bbminigames.twtClassic.settings.player.PlayerAbilityUpdateListener
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryAction
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import java.util.*
import kotlin.math.max

class AbilityMenu(settings: Settings, private val bp: BotBowsPlayer) : SettingsMenu(settings), AbilityUpdateListener,
    PlayerAbilityUpdateListener {
    private val maxAbilitiesSlider = MenuSlider(
        inventory,
        MenuAction.SET_MAX_ABILITIES.name,
        2,
        Material.GREEN_STAINED_GLASS_PANE,
        NamedTextColor.GREEN,
        mutableListOf("1", "2", "3"),
        "Max abilities"
    )
    private val cooldownMultiplierSlider = MenuSlider(
        inventory,
        MenuAction.SET_COOLDOWN_MULTIPLIER.name,
        20,
        Material.PURPLE_STAINED_GLASS_PANE,
        NamedTextColor.LIGHT_PURPLE,
        mutableListOf("0.25x", "0.50x", "0.75x", "1.00x", "1.25x", "1.50x", "2.00x"),
        "Cooldown multiplier"
    )

    private val maxAbilitiesRow = PlayerMenuRow(inventory, MenuAction.SET_INDIVIDUAL_MAX_ABILITIES.name, 2, 5)
    private val cooldownMultiplierRow = PlayerMenuRow(inventory, MenuAction.SET_INDIVIDUAL_COOLDOWN_MULTIPLIER.name, 20, 7)
    private val abilityRow = AbilityMenuRow(inventory, MenuAction.CLICK_ABILITY.name, 37, 8, this)

    init {
        setPageButtons(5, true, false)
        updateUIState()
    }

    override fun getMenuName(): Component = Component.text("Abilities (6/6)")

    override fun getSlots(): Int = 54

    override fun handlesEmptySlots(): Boolean = true

    override fun handleMenu(e: InventoryClickEvent) {
        val clickedItem = e.currentItem ?: makeItem(Material.AIR, Component.empty(), MenuAction.CLICK_AIR.name)

        if (handlePageClick(e)) return
        if (maxAbilitiesRow.handleClick(e)) return
        if (cooldownMultiplierRow.handleClick(e)) return
        if (abilityRow.handleClick(e)) return

        val clicker = e.whoClicked as Player
        val bp = settings.lobby.getBotBowsPlayer(clicker)

        if (clickedItem.type == Material.ARROW && e.clickedInventory !== inventory) e.isCancelled = true

        val actionId =
            clickedItem.persistentDataContainer.get(ACTION_KEY, PersistentDataType.STRING) ?: return

        val action = MenuAction.valueOf(actionId)
        if (action in modActions && !settings.checkMod(bp)) return

        val abilitySettings = settings.abilitySettings
        when (action) {
            MenuAction.TOGGLE_ABILITIES -> abilitySettings.maxAbilities = if (abilitySettings.maxAbilities == 0) 2 else 0
            MenuAction.TOGGLE_INDIVIDUAL_MAX_ABILITIES -> with(abilitySettings) { isIndividualMax = !isIndividualMax }
            MenuAction.SET_MAX_ABILITIES -> {
                val component = clickedItem.itemMeta.displayName() ?: return
                val string = PlainTextComponentSerializer.plainText().serialize(component)
                abilitySettings.maxAbilities = string.toInt()
            }
            MenuAction.SET_INDIVIDUAL_MAX_ABILITIES -> {
                val clickedBp = getPlayerFromHead(clickedItem) ?: return
                with(clickedBp.settings) { maxAbilities = (maxAbilities % 3) + 1 }
            }
            MenuAction.TOGGLE_INDIVIDUAL_COOLDOWN -> with(abilitySettings) { isIndividualCooldown = !isIndividualCooldown }
            MenuAction.SET_COOLDOWN_MULTIPLIER -> {
                val component = clickedItem.itemMeta.displayName() ?: return
                val string = PlainTextComponentSerializer.plainText().serialize(component).dropLast(1)
                abilitySettings.cooldownMultiplier = string.toFloat()
            }
            MenuAction.SET_INDIVIDUAL_COOLDOWN_MULTIPLIER -> {
                val clickedBp = getPlayerFromHead(clickedItem) ?: return
                val cooldownMultiplier = clickedBp.settings.abilityCooldownMultiplier // oppdaterer cooldownmultiplier
                val current = "%.2fx".format(Locale.US, cooldownMultiplier)
                val next = cooldownMultiplierSlider.getNext(current).dropLast(1)
                clickedBp.settings.abilityCooldownMultiplier = next.toFloat()
            }
            MenuAction.TOGGLE_BAN_HAMMER -> bp.toggleAbilityToggle()
            MenuAction.RANDOMIZE_ABILITIES -> {
                bp.abilities.toSet().forEach { bp.unequipAbility(it.type, true) }

                AbilityType.entries.shuffled()
                    .filter { !abilitySettings.isBanned(it) }
                    .take(bp.settings.maxAbilities - bp.totalAbilities)
                    .forEach { bp.equipAbility(it) }
            }
            MenuAction.EDIT_PLAYER_ABILITIES -> clicker.sendMessage(Component.text("This feature isnt added yet", NamedTextColor.RED))
            MenuAction.TOGGLE_UNIQUE_MODE -> with(abilitySettings) { isUniqueMode = !isUniqueMode }
            MenuAction.CLICK_ABILITY, MenuAction.CLICK_AIR -> handleAbilityClick(e, clicker, bp, clickedItem)
        }
    }

    fun getPlayerFromHead(item: ItemStack) : BotBowsPlayer? {
        val key = NamespacedKey(Main.getPlugin(), "uuid")
        val playerIdStr = item.itemMeta.persistentDataContainer.get<String, String>(key, PersistentDataType.STRING) ?: return null
        val playerId = UUID.fromString(playerIdStr)
        val bp = settings.lobby.getBotBowsPlayer(playerId)
        return bp
    }

    private fun handleAbilityClick(e: InventoryClickEvent, p: Player, bp: BotBowsPlayer, clickedItem: ItemStack) {
        val cursorItem = e.cursor
        val cursorAbility = AbilityType.fromItem(cursorItem)
        val clickedAbility = AbilityType.fromItem(clickedItem)

        if (cursorAbility == null && clickedAbility == null) return

        val menuInventory: Inventory = inventory
        val abilitySettings = settings.abilitySettings
        if (e.clickedInventory === menuInventory) {
            if (cursorAbility != null) {
                p.setItemOnCursor(null)
                if (bp.hasAbilityEquipped(cursorAbility)) {
                    bp.unequipAbility(cursorAbility)
                }
            } else {
                if (bp.isToggleAbilityMode) {
                    abilitySettings.toggle(clickedAbility!!)
                    return
                }
                if (cursorItem.type != Material.AIR) return

                if (abilitySettings.isBanned(clickedAbility!!)) {
                    p.sendMessage(Component.text("This ability is disabled", NamedTextColor.RED))
                    return
                }

                if (bp.hasAbilityEquipped(clickedAbility)) {
                    bp.unequipAbility(clickedAbility)
                } else { // picking up ability from menu
                    val clickedOnMenuAbilityRow = e.slot in 37..44
                    if (clickedOnMenuAbilityRow) {
                        if (bp.totalAbilities == bp.settings.maxAbilities) {
                            if (bp.settings.maxAbilities == 0) {
                                p.sendMessage(Component.text("The mod has disabled abilities for you", NamedTextColor.RED))
                            } else {
                                p.sendMessage(Component.text("Ability limit reached", NamedTextColor.RED))
                            }
                            return
                        } else if (abilitySettings.isUniqueMode && abilitySettings.isEquippedByTeam(bp, clickedAbility)) {
                            p.sendMessage(Component.text("This ability is already equipped by other team members (unique abilities is on)", NamedTextColor.YELLOW))
                            return
                        }

                        val abilityItem = clickedItem.clone()
                        val cooldownComponent = clickedAbility.getCooldownComponent(bp)

                        abilityItem.editMeta {
                            val lore = it.lore()?.toMutableList() ?: mutableListOf()

                            if (lore.isNotEmpty()) {
                                lore[lore.lastIndex] = cooldownComponent
                            } else {
                                lore.add(cooldownComponent)
                            }
                            it.lore(lore)
                        }

                        p.setItemOnCursor(abilityItem)
                    }
                }
            }
        } else { // clicked in player inventory
            if (cursorItem.type == Material.AIR) {
                if (bp.hasAbilityEquipped(clickedAbility)) { // picks up ability to move it around
                    bp.equipAbility(-1, clickedAbility)
                }
            } else { // places ability down in that slot
                if (e.action == InventoryAction.COLLECT_TO_CURSOR) { // otherwise, players could collect menu items by double clicking similar items in their inventory
                    e.isCancelled = true
                    return
                }
                if (bp.totalAbilities == bp.settings.maxAbilities && !bp.hasAbilityEquipped(cursorAbility)) {
                    p.setItemOnCursor(null)
                    return
                }

                if (e.slot >= 9) { // clicks somewhere else than hotbar
                    p.setItemOnCursor(null)
                    if (bp.hasAbilityEquipped(cursorAbility)) {
                        bp.unequipAbility(cursorAbility)
                    }
                    return
                }
                bp.equipAbility(e.slot, cursorAbility, false)

                clickedAbility?.let { bp.equipAbility(-1, it) }
            }
        }
    }

    public override fun prevPage(p: Player) {
        settings.hazardMenu.open(p)
    }

    fun handleMenuClose(e: InventoryCloseEvent) {
        handleMenuClose(e.player as Player)
    }

    fun handleMenuClose(p: Player) {
        val inv: Inventory = p.inventory
        for (i in 9..17) { // fjerner menu overlay greier
            if (inv.getItem(i)?.type == Material.FIREWORK_STAR) {
                inv.setItem(i, null)
            }
        }
    }

    fun updateUIState() {
        if (settings.abilitySettings.maxAbilities > 0) {
            inventory.setItem(8, ABILITIES_ENABLED)
            updateMaxAbilitiesUIState()
            updateCooldownMultiplierUIState()
            onUniqueModeToggle()
            abilityRow.show()
            inventory.setItem(36, MOD_TOGGLE)
            inventory.setItem(45, RANDOMIZE_ABILITIES)
            inventory.setItem(49, INDIVIDUAL_PLAYER_ABILITIES)
        } else {
            abilityRow.hide()
            maxAbilitiesRow.hide()
            cooldownMultiplierRow.hide()
            inventory.setItem(8, ABILITIES_DISABLED)
            // fyller med gråe glassvinduer der settings var
            updateMaxAbilitiesUIState()
            inventory.setItem(0, DISABLED_SLOT)
            inventory.setItem(53, DISABLED_SLOT)
            inventory.setItem(18, DISABLED_SLOT)
            for (i in 20..26) {
                inventory.setItem(i, DISABLED_SLOT)
            }
            for (i in 27..35) {
                inventory.setItem(i, VOID)
            }
            for (i in 36..44) {
                inventory.setItem(i, DISABLED_SLOT)
            }
        }
    }

    fun updateMaxAbilitiesUIState() {
        val abilitySettings = settings.abilitySettings
        if (abilitySettings.isIndividualMax) {
            inventory.setItem(0, INDIVIDUAL_MAX_ABILITIES_ENABLED)
            maxAbilitiesRow.show()
        } else {
            inventory.setItem(0, INDIVIDUAL_MAX_ABILITIES_DISABLED)
            maxAbilitiesRow.hide()
            maxAbilitiesSlider.setProgressSlots(abilitySettings.maxAbilities)
            inventory.setItem(5, VOID)
            inventory.setItem(6, VOID)
        }
    }

    fun updateCooldownMultiplierUIState() {
        val abilitySettings = settings.getAbilitySettings()
        if (abilitySettings.isIndividualCooldown) {
            inventory.setItem(18, INDIVIDUAL_COOLDOWN_MULTIPLIER_ENABLED)
            cooldownMultiplierRow.show()
        } else {
            inventory.setItem(18, INDIVIDUAL_COOLDOWN_MULTIPLIER_DISABLED)
            cooldownMultiplierRow.hide()
        }
    }

    override fun onAbilitiesToggle() {
        updateUIState()
    }

    override fun onMaxAbilitiesChange() {
        val abilitySettings = settings.abilitySettings
        if (abilitySettings.isIndividualMax) return

        maxAbilitiesSlider.setProgressSlots(abilitySettings.maxAbilities)
    }

    override fun onMaxAbilitiesChange(bp: BotBowsPlayer) {
        if (!settings.abilitySettings.isIndividualMax) return
        val headItem = maxAbilitiesRow.getItem(bp)
        headItem.amount = max(bp.settings.maxAbilities, 1) // oppdaterer head count
        maxAbilitiesRow.displayRow()
    }

    override fun onIndividualMaxToggle() {
        updateMaxAbilitiesUIState()
    }

    override fun onCooldownMultiplierChange() {
        val abilitySettings = settings.abilitySettings
        if (abilitySettings.isIndividualCooldown) return

        cooldownMultiplierSlider.setProgress("%.2fx".format(Locale.US, bp.settings.abilityCooldownMultiplier))
    }

    override fun onCooldownMultiplierChange(bp: BotBowsPlayer) {
        if (!settings.abilitySettings.isIndividualCooldown) return
        val headItem = cooldownMultiplierRow.getItem(bp)
        headItem.editMeta {
            it.lore(
                listOf(
                    Component.text("Cooldown multiplier: ")
                        .append(
                            Component.text(
                                "%.2fx".format(Locale.US, bp.settings.abilityCooldownMultiplier),
                                NamedTextColor.LIGHT_PURPLE
                            )
                        )
                )
            )
        }
        cooldownMultiplierRow.displayRow()
    }

    override fun onIndividualCooldownToggle() {
        updateCooldownMultiplierUIState()
    }

    override fun onUniqueModeToggle() {
        if (settings.abilitySettings.isUniqueMode) {
            inventory.setItem(53, UNIQUE_MODE_ENABLED)

            // removes abilities that other teammates has equipped
            bp.abilities
                .map { it.type }
                .filter { !settings.abilitySettings.attemptEquip(bp, it) }
                .forEach { bp.unequipAbility(it) }
        } else {
            inventory.setItem(53, UNIQUE_MODE_DISABLED)
            updateAbilityStatuses()
        }
    }

    override fun onAbilityStatusChange(type: AbilityType) { // TODO: huskelapp, om non andre banner abilitis som ikke er på riktig page, så bøgger det kankjse
        val slot = abilityRow.getAbilitySlot(type) + abilityRow.getStartSlot()
        if (settings.abilitySettings.isBanned(type)) {
            inventory.setItem(slot - 9, ABILITY_DISABLED)
        } else {
            inventory.setItem(slot - 9, VOID)
        }
    }

    fun updateAbilityStatuses() {
        for (i in 0..<abilityRow.size) {
            val abilitySlot = abilityRow.startSlot + i
            val abilityItem = inventory.getItem(abilitySlot)
            val abilityType = AbilityType.fromItem(abilityItem)
            val statusItem = when {
                abilityType == null -> VOID
                bp.hasAbilityEquipped(abilityType) -> ABILITY_EQUIPPED
                settings.abilitySettings.isBanned(abilityType) -> ABILITY_DISABLED
                settings.abilitySettings.isUniqueMode && settings.abilitySettings.isEquippedByTeam(bp, abilityType) -> ABILITY_TAKEN
                else -> VOID
            }
            inventory.setItem(abilitySlot - 9, statusItem)
        }
    }

    fun getRelativeAbilitySlot(type: AbilityType): Int { // åssen rad det er, 0-9. negative verdier hvis det er på feil side
        val slot = abilityRow.getAbilitySlot(type) + 1
        if (slot > abilityRow.size) return -1
        return slot
    }

    fun addPlayer(bp: BotBowsPlayer) {
        //max abilities
        val abilitiesHead = bp.avatar.getHeadItem()
        abilitiesHead.amount = max(bp.settings.maxAbilities, 1)
        maxAbilitiesRow.addItem(abilitiesHead)
        // cooldown multiplier
        val cooldownHead = bp.avatar.getHeadItem()
        cooldownHead.editMeta {
            it.lore(
                listOf(
                    Component.text("Cooldown multiplier: ").append(
                        Component.text(
                            "%.2fx".format(Locale.US, bp.settings.abilityCooldownMultiplier),
                            NamedTextColor.LIGHT_PURPLE
                        )
                    )
                )
            )
        }
        cooldownMultiplierRow.addItem(cooldownHead)
    }

    fun removePlayer(bp: BotBowsPlayer) {
        removePlayerFromRow(bp, maxAbilitiesRow)
        removePlayerFromRow(bp, cooldownMultiplierRow)
    }

    private fun removePlayerFromRow(bp: BotBowsPlayer, row: PlayerMenuRow) {
        row.removeItem(row.getItem(bp))
    }

    override fun onUniqueAbilityOccupancyChange(type: AbilityType, bp: BotBowsPlayer, equipped: Boolean) {
        if (bp === this.bp || bp.team !== this.bp.team) return
        val slot = abilityRow.getAbilitySlot(type) + abilityRow.getStartSlot()
        inventory.setItem(slot - 9, if (equipped) ABILITY_TAKEN else VOID)
    }

    companion object {
        private val modActions = EnumSet.of(
            MenuAction.TOGGLE_ABILITIES, MenuAction.TOGGLE_INDIVIDUAL_MAX_ABILITIES, MenuAction.SET_MAX_ABILITIES,
            MenuAction.SET_INDIVIDUAL_MAX_ABILITIES, MenuAction.TOGGLE_INDIVIDUAL_COOLDOWN, MenuAction.SET_COOLDOWN_MULTIPLIER,
            MenuAction.SET_INDIVIDUAL_COOLDOWN_MULTIPLIER, MenuAction.TOGGLE_BAN_HAMMER, MenuAction.RANDOMIZE_ABILITIES,
            MenuAction.EDIT_PLAYER_ABILITIES, MenuAction.TOGGLE_UNIQUE_MODE
        )

        private val ABILITIES_DISABLED: ItemStack = makeItem(
            Material.RED_STAINED_GLASS_PANE, Component.text("Abilities", NamedTextColor.RED),
            MenuAction.TOGGLE_ABILITIES.name,
            STATUS_DISABLED,
            Component.text("By enabling this, each player"), Component.text("can have abilities in addition to the bow")
        )

        private val ABILITIES_ENABLED: ItemStack = makeItem(
            Material.LIME_STAINED_GLASS_PANE, Component.text("Abilities", NamedTextColor.GREEN),
            MenuAction.TOGGLE_ABILITIES.name,
            STATUS_ENABLED,
            Component.text("By enabling this, each player"), Component.text("can have abilities in addition to the bow")
        )

        private val INDIVIDUAL_MAX_ABILITIES_DISABLED: ItemStack = makeItem(
            Material.RED_STAINED_GLASS_PANE, Component.text("Individual max abilities", NamedTextColor.RED),
            MenuAction.TOGGLE_INDIVIDUAL_MAX_ABILITIES.name,
            STATUS_DISABLED,
            Component.text("By enabling this, each player"), Component.text("can have a different max ability cap")
        )

        private val INDIVIDUAL_MAX_ABILITIES_ENABLED: ItemStack = makeItem(
            Material.LIME_STAINED_GLASS_PANE, Component.text("Individual max abilities", NamedTextColor.GREEN),
            MenuAction.TOGGLE_INDIVIDUAL_MAX_ABILITIES.name,
            STATUS_ENABLED,
            Component.text("By enabling this, each player"), Component.text("can have a different max ability cap")
        )

        private val INDIVIDUAL_COOLDOWN_MULTIPLIER_DISABLED: ItemStack = makeItem(
            Material.RED_STAINED_GLASS_PANE, Component.text("Individual cooldown multiplier", NamedTextColor.RED),
            MenuAction.TOGGLE_INDIVIDUAL_COOLDOWN.name,
            STATUS_DISABLED,
            Component.text("By enabling this, each player"), Component.text("can have a different cooldown multiplier")
        )

        private val INDIVIDUAL_COOLDOWN_MULTIPLIER_ENABLED: ItemStack = makeItem(
            Material.LIME_STAINED_GLASS_PANE, Component.text("Individual cooldown multiplier", NamedTextColor.GREEN),
            MenuAction.TOGGLE_INDIVIDUAL_COOLDOWN.name,
            STATUS_ENABLED,
            Component.text("By enabling this, each player"), Component.text("can have a different cooldown multiplier")
        )

        private val UNIQUE_MODE_DISABLED: ItemStack = makeItem(
            Material.RED_STAINED_GLASS_PANE,
            MenuAction.TOGGLE_UNIQUE_MODE.name,
            Component.text("Unique mode", NamedTextColor.RED),
            STATUS_DISABLED,
            Component.text("By enabling this, each ability"),
            Component.text("can can only be equipped by max team member")
        )

        private val UNIQUE_MODE_ENABLED: ItemStack = makeItem(
            Material.LIME_STAINED_GLASS_PANE,
            MenuAction.TOGGLE_UNIQUE_MODE.name,
            Component.text("Unique mode", NamedTextColor.GREEN),
            STATUS_ENABLED,
            Component.text("By enabling this, each player"),
            Component.text("can can only be equipped by max team member")
        )

        val MOD_TOGGLE: ItemStack = makeItem(
            Material.MACE,
            MenuAction.TOGGLE_BAN_HAMMER.name,
            Component.text("Mod Toggle"),
            Component.text("When enabled, you can toggle"),
            Component.text("which abilities will be allowed")
        )

        val MOD_TOGGLE_DISABLED: ItemStack = makeItem("inactive_slot_covered", Component.empty())
        val MOD_TOGGLE_ENABLED:  ItemStack = makeItem("active_slot_covered",   Component.empty())
        val ABILITY_DISABLED:    ItemStack = makeItem("disabled_slot_covered", Component.empty())
        val ABILITY_TAKEN:       ItemStack = makeItem("yellow_slot_covered",   Component.empty())
        val ABILITY_EQUIPPED:    ItemStack = makeItem("enabled_slot_covered",  Component.empty())

        private val RANDOMIZE_ABILITIES: ItemStack = makeItem(
            Material.TARGET,
            MenuAction.RANDOMIZE_ABILITIES.name,
            Component.text("Randomize abilities", NamedTextColor.LIGHT_PURPLE),
            Component.text("Click this to randomize your abilities"),
            Component.text("from the allowed abilities")
        )

        private val INDIVIDUAL_PLAYER_ABILITIES: ItemStack = makeItem(
            "gear",
            Component.text("Edit player abilities", NamedTextColor.LIGHT_PURPLE),
            MenuAction.EDIT_PLAYER_ABILITIES.name,
            Component.text("Edit the allowed abilities"),
            Component.text("for each individual player")
        )
    }

    private enum class MenuAction {
        TOGGLE_ABILITIES,
        TOGGLE_INDIVIDUAL_MAX_ABILITIES,
        SET_MAX_ABILITIES,
        SET_INDIVIDUAL_MAX_ABILITIES,

        TOGGLE_INDIVIDUAL_COOLDOWN,
        SET_COOLDOWN_MULTIPLIER,
        SET_INDIVIDUAL_COOLDOWN_MULTIPLIER,

        TOGGLE_BAN_HAMMER,
        CLICK_ABILITY,
        RANDOMIZE_ABILITIES,
        EDIT_PLAYER_ABILITIES,
        TOGGLE_UNIQUE_MODE,
        CLICK_AIR,
    }
}
