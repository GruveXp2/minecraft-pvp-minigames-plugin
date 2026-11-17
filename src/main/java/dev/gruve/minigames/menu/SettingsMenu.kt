package dev.gruve.minigames.menu

import dev.gruve.minigames.botbows.Settings
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor

abstract class SettingsMenu protected constructor(protected val settings: Settings, menuName: Component, slots: Int): PaginatedMenu(menuName, slots) {
    companion object {
        internal val STATUS_ENABLED = Component.text("Enabled", NamedTextColor.GREEN)
        internal val STATUS_DISABLED = Component.text("Disabled", NamedTextColor.RED)
    }
}
