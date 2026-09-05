package gruvexp.bbminigames.listeners

import gruvexp.bbminigames.twtClassic.BotBows
import gruvexp.bbminigames.twtClassic.Lobby
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent

class MovementListener : Listener {
    @EventHandler
    fun onMove(e: PlayerMoveEvent) {
        val p = e.getPlayer()
        if (!BotBows.isPlayerJoined(p)) {
            BotBows.handleMovement(e)
            return
        }
        val lobby = BotBows.getLobby(p)
        if (!lobby.isGameActive) return

        if (lobby.botBowsGame!!.canMove) {
            lobby.botBowsGame!!.handleMovement(e)
        } else {
            freeze(lobby, p)
        }
    }

    private fun freeze(lobby: Lobby, p: Player) {
        val bp = lobby.getBotBowsPlayer(p)
        val spawnPos = bp!!.team.getSpawnPos(bp)
        if (p.location.x == spawnPos.x && p.location.z == spawnPos.z) {
            return
        }
        // hvis det er countdown (!canMove), playeren er joina og playeren har gått vekk fra spawn blir man telportert tebake
        p.teleport(spawnPos)
    }
}
