package gruvexp.bbminigames.menu

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

abstract class Menu : InventoryHolder {
    @JvmField // jvm bc its used in sumo aswell
    protected var inventory: Inventory

    // menu is an InventoryHolder, that holds our inventory menu. Since all inventories have an owner, we can get the Menu object from the inventory when handling inventory events
    init {
        inventory = Bukkit.createInventory(this, slots, menuName)
        setFillerVoid()
    }

    abstract val menuName: Component

    // the amount of slots must be 9n
    abstract val slots: Int

    abstract fun handleMenu(e: InventoryClickEvent)

    open fun handlesEmptySlots(): Boolean {
        return false // By default, menus dont handle empty slots
    }

    open fun open(p: Player) {
        p.openInventory(inventory)
    }

    override fun getInventory(): Inventory {
        return inventory
    }

    fun setFillerVoid() {
        for (i in 0..<slots) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, VOID)
            }
        }
    }

    companion object {
        val ACTION_KEY: NamespacedKey = NamespacedKey("botbows", "menu_action")
        val DISABLED_SLOT = makeItem(Material.GRAY_STAINED_GLASS_PANE, Component.empty())
        val VOID = makeItem("void", Component.empty())

        fun getActionId(item: ItemStack): String? {
            return item.persistentDataContainer.get(ACTION_KEY, PersistentDataType.STRING)
        }

        fun makeItem(
            material: Material,
            displayName: TextComponent,
            actionId: String?,
            vararg lore: Component?
        ): ItemStack {
            return ItemStack(material).apply {
                editMeta {
                    it.displayName(displayName.decoration(TextDecoration.ITALIC, false))
                    it.lore(listOf(*lore))

                    if (actionId != null) {
                        it.persistentDataContainer.set(ACTION_KEY, PersistentDataType.STRING, actionId)
                    }
                }
            }
        }

        @JvmStatic // jvm bc used by sumo
        fun makeItem(material: Material, displayName: TextComponent, vararg lore: Component): ItemStack {
            return makeItem(material, displayName, null, *lore)
        }

        fun makeItem(material: Material, displayName: TextComponent, actionId: String?): ItemStack {
            return makeItem(material, displayName, actionId, *arrayOfNulls<Component>(0))
        }

        fun makeItem(material: Material, displayName: TextComponent, amount: Int): ItemStack {
            return ItemStack(material).apply {
                editMeta { it.displayName(displayName.decoration(TextDecoration.ITALIC, false)) }
                setAmount(amount)
            }
        }

        @JvmStatic
        fun makeItem(customModelData: String, displayName: TextComponent, vararg lore: Component): ItemStack {
            return makeItem(
                Material.FIREWORK_STAR,
                customModelData,
                displayName.decoration(TextDecoration.ITALIC, false),
                null,
                null,
                *lore
            )
        }

        fun makeItem(customModelData: String, displayName: TextComponent, actionId: String?, vararg lore: Component): ItemStack {
            return makeItem(
                Material.FIREWORK_STAR,
                customModelData,
                displayName.decoration(TextDecoration.ITALIC, false),
                null,
                actionId,
                *lore
            )
        }

        fun makeItem(
            customModelData: String,
            displayName: TextComponent,
            actionKey: NamespacedKey?,
            actionId: String?,
            vararg lore: Component
        ): ItemStack {
            return makeItem(
                Material.FIREWORK_STAR,
                customModelData,
                displayName.decoration(TextDecoration.ITALIC, false),
                actionKey,
                actionId,
                *lore
            )
        }

        fun makeItem(
            material: Material,
            customModelData: String,
            displayName: TextComponent,
            vararg lore: Component
        ): ItemStack {
            return makeItem(
                material,
                customModelData,
                displayName.decoration(TextDecoration.ITALIC, false),
                null,
                null,
                *lore
            )
        }

        fun makeItem(
            material: Material,
            customModelData: String,
            displayName: TextComponent,
            actionKey: NamespacedKey?,
            actionId: String?,
            vararg lore: Component
        ): ItemStack {
            return ItemStack(material).apply {
                editMeta {
                    it.displayName(displayName.decoration(TextDecoration.ITALIC, false))
                    it.lore(listOf(*lore))

                    val customModelDataComponent = it.getCustomModelDataComponent()
                    customModelDataComponent.setStrings(listOf(customModelData))
                    it.setCustomModelDataComponent(customModelDataComponent)

                    if (actionId != null) {
                        it.persistentDataContainer.set(
                            actionKey ?: ACTION_KEY,
                            PersistentDataType.STRING,
                            actionId
                        )
                    }
                }
            }
        }
    }
}

