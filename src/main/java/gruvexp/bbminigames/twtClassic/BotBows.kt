package gruvexp.bbminigames.twtClassic

import gruvexp.bbminigames.Main
import gruvexp.bbminigames.commands.TestCommand
import gruvexp.bbminigames.menu.Menu.Companion.makeItem
import gruvexp.bbminigames.menu.menus.GameMenu
import gruvexp.bbminigames.menu.menus.LobbyMenu
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.block.data.type.Light
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.CrossbowMeta
import org.bukkit.inventory.meta.Damageable
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.util.Vector
import java.util.UUID

object BotBows {
    val BOTBOW: ItemStack
        get() = botBow.clone()
    @JvmStatic
    val lobbies = arrayOf(Lobby(0), Lobby(1), Lobby(2))
    private val players = mutableMapOf<UUID, Lobby>() // liste med alle players som er i gamet

    val gameMenu = GameMenu()
    val lobbyMenu = LobbyMenu()

    val MENU_ITEM = makeItem(Material.COMPASS, Component.text("Menu", NamedTextColor.LIGHT_PURPLE))
    val SETTINGS_ITEM = makeItem("gear", Component.text("Settings", NamedTextColor.LIGHT_PURPLE))

    const val HIT_DISABLED_ITEM_TICKS: Int = 40

    val GLOBAL_LOBBY_LOCATION = Location(Main.WORLD, -129.0, 39.0, -197.0)

    private val botBow = ItemStack(Material.CROSSBOW).apply {
        editMeta(CrossbowMeta::class.java) {
            it.displayName(
                Component.text("BotBow")
                    .color(NamedTextColor.GREEN)
                    .decorate(TextDecoration.BOLD)
            )
            it.lore(listOf(
                Component.text("The strongest bow"),
                Component.text("ever known to man")
            ))
            it.addEnchant(Enchantment.POWER, 10, true)
            it.addEnchant(Enchantment.PUNCH, 10, true)
            it.addChargedProjectile(ItemStack(Material.ARROW))
        }
        editMeta(Damageable::class.java) { it.damage = 464 }
    }

    fun registerPlayerLobby(playerId: UUID, lobby: Lobby) {
        players[playerId] = lobby
    }

    fun unRegisterPlayerLobby(playerId: UUID) {
        players.remove(playerId)
    }

    @JvmStatic
    fun getLobby(id: Int): Lobby = lobbies[id]
    @JvmStatic
    fun getLobby(p: Player): Lobby? = getLobby(p.uniqueId)
    fun getLobby(playerId: UUID): Lobby? = players[playerId]

    fun getBotBowsPlayer(p: Player): BotBowsPlayer? = getBotBowsPlayer(p.uniqueId) // gets the BotBowsPlayer that is used by the lobby the player is in
    fun getBotBowsPlayer(playerId: UUID): BotBowsPlayer? = getLobby(playerId)?.getBotBowsPlayer(playerId) // gets the BotBowsPlayer that is used by the lobby the player is in

    fun isPlayerJoined(p: Player): Boolean = getLobby(p) != null
    fun isPlayerJoined(playerId: UUID): Boolean = getLobby(playerId) != null

    fun replacePlayerId(oldId: UUID, newId: UUID) {
        val lobby = getLobby(oldId)!!
        players.remove(oldId)
        players[newId] = lobby
        lobby.replacePlayerId(oldId, newId)
    }

    fun debugMessage(message: String, showMessage: Boolean = true) {
        if (!TestCommand.debugging || !showMessage) return
        Bukkit.getOnlinePlayers()
            .forEach { it.sendMessage(Component.text("[DEBUG]: $message", NamedTextColor.GRAY)) }
        Main.getPlugin().logger.info("[DEBUG]: $message")
    }

    fun accessSettings(p: Player) {
        val lobby = getLobby(p) ?: run {
            p.sendMessage(Component.text("You have to join to access the settings", NamedTextColor.RED))
            return
        }
        if (lobby.isGameActive) {
            p.sendMessage(Component.text("Cant change settings, the game is already ongoing!", NamedTextColor.RED))
            return
        }
        lobby.settings.overviewMenu.open(p)
    }

    fun handleMovement(e: PlayerMoveEvent) {
        val p = e.player
        val b = TestCommand.verboseDebugging
        var block = p.location.add(0.0, -0.05, 0.0).block // sjekker rett under, bare 0.05 itilfelle det er teppe

        debugMessage("1: Material: ${block.type.name}", b)
        if (block.type == Material.AIR) {
            block = p.location.add(0.0, -0.9, 0.0).block // hvis man står på kanten av et teppe kan det være en effektblokk under
            debugMessage("2: Material: ${block.type.name}", b)
        }
        if (block.type == Material.AIR) {
            block = p.location.block
            debugMessage("3: Material: ${block.type.name}", b)
        } else {
            debugMessage("Material: ${block.type.name}", b)
        }
        var material = block.type
        if (block.type == Material.LIGHT) {
            val light = block.blockData as Light
            debugMessage("Light level: ${(block.blockData as Light).level}", b)
            when (light.level) {
                0 -> { // sida det ikke går an å sjekke når players står uttafor kanten, så workarounder jeg det ved å sette light bloccs ved sida cyan yeetpads
                    material = Material.CYAN_CARPET
                    debugMessage("yee it works", b)
                }
                1 -> material = Material.YELLOW_CARPET
                2 -> material = Material.AIR
            }
        }
        when (material) {
            Material.YELLOW_CONCRETE, Material.YELLOW_CONCRETE_POWDER, Material.YELLOW_CARPET -> p.addPotionEffect(
                PotionEffect(PotionEffectType.JUMP_BOOST, 1200, 6, true, false)
            )

            Material.CYAN_CONCRETE, Material.CYAN_CONCRETE_POWDER, Material.CYAN_CARPET -> {
                val Δy = e.to.y - e.from.y
                if (Δy <= 0.1) {
                    return
                } // fortsett bare viss man har hoppa (et visst antall upwards momentum)

                val vX = p.location.getDirection().getX()
                val vZ = p.location.getDirection().getZ()

                p.velocity = Vector(vX * 2.5, 0.5, vZ * 2.5)
                p.playSound(p.location, Sound.ITEM_FIRECHARGE_USE, 10f, 2f)
            }

            else -> p.removePotionEffect(PotionEffectType.JUMP_BOOST)
        }
    }
}