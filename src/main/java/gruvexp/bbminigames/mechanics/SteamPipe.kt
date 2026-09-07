package gruvexp.bbminigames.mechanics

import gruvexp.bbminigames.Main
import gruvexp.bbminigames.Util
import gruvexp.bbminigames.twtClassic.BotBows
import gruvexp.bbminigames.twtClassic.effect.PlayerEffectManager
import gruvexp.bbminigames.util.editData
import org.bukkit.Axis
import org.bukkit.Bukkit
import org.bukkit.Chunk
import org.bukkit.Location
import org.bukkit.block.Block
import org.bukkit.block.data.type.CopperBulb
import org.bukkit.entity.Player
import org.bukkit.util.Vector
import kotlin.math.abs

class SteamPipe(val isDualWay: Boolean, private val nodes: List<Location>, entryAxis: Axis, exitAxis: Axis) {
    private var pipeStatus = PipeStatus.INACTIVE
    private var shuttingDown = false
    private val playerEdge = mutableMapOf<Player, Int>()

    private var tick = 0
    private val firstBulbs: List<Block> = Util.getOrthogonalLocations(nodes.first(), entryAxis)
        .map { it.block }
        .filter { it.type.data == CopperBulb::class.java }
    private val secondBulbs: List<Block> = Util.getOrthogonalLocations(nodes.last(), exitAxis)
        .map { it.block }
        .filter { it.type.data == CopperBulb::class.java }

    val tickedChunks: Set<Chunk>
        // the chunks that has the entry and exit. only check if players are near entry/exit if theyre in these chunks, to save performance
        get() {
            val chunks = Util.getChunksAround(nodes.first(), 3)
            if (isDualWay) chunks.addAll(Util.getChunksAround(nodes.last(), 3))
            return chunks
        }

    private fun setPipeStatus(status: PipeStatus) {
        if (pipeStatus == status) return
        when (status) {
            PipeStatus.ACTIVE -> {
                pipeStatus = status
                firstBulbs.forEach { it.editData<CopperBulb> { isLit = true } }
                secondBulbs.forEach { it.editData<CopperBulb> { isPowered = true } }
            }

            PipeStatus.ACTIVE_REVERSED -> {
                pipeStatus = status
                firstBulbs.forEach { it.editData<CopperBulb> { isPowered = true } }
                secondBulbs.forEach { it.editData<CopperBulb> { isLit = true } }
            }

            PipeStatus.INACTIVE -> {
                shuttingDown = true

                Bukkit.getScheduler().runTaskLater(Main.getPlugin(), Runnable {
                    if (playerEdge.isNotEmpty()) return@Runnable
                    pipeStatus = status
                    firstBulbs.forEach { it.editData<CopperBulb> { isPowered = false; isLit = false } }

                    secondBulbs.forEach { it.editData<CopperBulb> { isPowered = false; isLit = false } }
                    shuttingDown = false
                }, 20L)
            }
        }
    }

    fun checkProximity(p: Player) {
        if (p in playerEdge) return
        if (isDualWay) {
            if (pipeStatus != PipeStatus.ACTIVE_REVERSED && checkProximity(true, p)) return
            if (pipeStatus != PipeStatus.ACTIVE && checkProximity(false, p)) return
            return
        }
        checkProximity(true, p)
    }

    private fun checkProximity(firstEntry: Boolean, p: Player): Boolean {
        val enterLocation = if (firstEntry) nodes.first() else nodes.last()
        if (enterLocation.distanceSquared(p.location) < 9) {
            val playerEntryDir = enterLocation.clone().subtract(p.location).toVector()
            val nextNode = if (firstEntry) nodes[1] else nodes[nodes.lastIndex - 1]
            val pipeEntryDir = nextNode.clone().subtract(enterLocation).toVector()
            if (playerEntryDir.normalize().dot(pipeEntryDir.normalize()) < 0.6) return false // if youre close to the entry point, but not standing at the front


            // you wont get sucked in. this is to fix getting stuck at the back of the entry
            // 0.6 here means around 55 deg, so if angle between player->entry and entry->nextnode is more than that its not gonna suck the player in
            playerEdge[p] = if (firstEntry) -1 else nodes.size

            //Bukkit.broadcast(Component.text("Registerd enter"));
            setPipeStatus(if (firstEntry) PipeStatus.ACTIVE else PipeStatus.ACTIVE_REVERSED)

            return true
        }
        return false
    }

    fun tick() {
        if (pipeStatus == PipeStatus.INACTIVE) return
        tick++

        playerEdge.forEach { (p: Player, currentNode: Int) ->
            val nextNode = if (pipeStatus == PipeStatus.ACTIVE) currentNode + 1 else currentNode - 1

            val currentNodeLoc = if (currentNode < 0 || currentNode == nodes.size) p.location else nodes[currentNode]
            val nextNodeLoc = nodes[nextNode]

            val pLoc = p.location
            val nodeX = currentNodeLoc.blockX
            val nodeY = currentNodeLoc.blockY
            val nodeZ = currentNodeLoc.blockZ
            if (tick % 5 == 0) { // if the player goes outside of the pipe bounds, they exited
                if (nodeX == nextNodeLoc.blockX && abs(pLoc.blockX - nodeX) > 1
                    || nodeY == nextNodeLoc.blockY && abs(pLoc.blockY - nodeY) > 1
                    || nodeZ == nextNodeLoc.blockZ && abs(pLoc.blockZ - nodeZ) > 1
                ) {
                    exitPlayer(p)
                    return@forEach
                }
            }

            val isEntering = if (pipeStatus == PipeStatus.ACTIVE) currentNode == -1 else currentNode == nodes.size
            val onFirstNode = if (pipeStatus == PipeStatus.ACTIVE) currentNode == 0 else currentNode == nodes.size - 1
            val onLastNode = if (pipeStatus == PipeStatus.ACTIVE) nextNode == nodes.size - 1 else nextNode == 0

            val bp = BotBows.getBotBowsPlayer(p)
            if (isEntering) {
                if (nextNodeLoc.clone().distanceSquared(pLoc) > 12) {
                    playerEdge[p] = -2 // the player exited and will be removed
                }
                bp.effectManager.applyScale(
                    PlayerEffectManager.ScaleSource.STEAM_PIPE,
                    0.4,
                    PlayerEffectManager.ScalePriority.OVERRIDE,
                    3L
                )
            } else if (onFirstNode) {
                bp.effectManager.applyScale(
                    PlayerEffectManager.ScaleSource.STEAM_PIPE,
                    0.4,
                    PlayerEffectManager.ScalePriority.OVERRIDE,
                    null
                ) // will be small "forever" until exiting
            }

            val a = nextNodeLoc.clone().subtract(currentNodeLoc).toVector().normalize()

            val distanceToNextNode = pLoc.distanceSquared(nextNodeLoc)
            if (isEntering || p.velocity.lengthSquared() > 1) { // get slowly sucked in, then go fast but dont go too much faster than 1b/t
                a.multiply(Vector(0.1, 0.3, 0.1))
                if (isEntering) { // the closer you get to the entry, the stronger the pull
                    val multiply = (9 - distanceToNextNode) / 6
                    a.multiply(Vector(multiply, 1.5, multiply))
                }
            }
            p.velocity = p.velocity.add(a)
            val reachedNextNode = if (isEntering) distanceToNextNode < 0.5 else distanceToNextNode < 1 // you must be closer to the first node to have reached it
            // this secures that the player is actually inside the pipe
            if (reachedNextNode) {
                if (onLastNode) {
                    exitPlayer(p)
                } else {
                    playerEdge[p] = nextNode
                }
            }
        }
        playerEdge.entries.removeIf { it.value == -2 }
        if (playerEdge.isEmpty() && !shuttingDown) setPipeStatus(PipeStatus.INACTIVE)
        if (tick % 4 == 0) {
            updateAnimation()
        }
    }

    fun exitPlayer(p: Player) {
        playerEdge[p] = -2 // the player exited and will be removed
        val bp = BotBows.getBotBowsPlayer(p)
        bp.effectManager.applyScale(
            PlayerEffectManager.ScaleSource.STEAM_PIPE,
            0.4,
            PlayerEffectManager.ScalePriority.OVERRIDE,
            3L,
            10
        )
        BotBows.debugMessage("3. will be big in 3 ticks")
    }

    fun updateAnimation() {
        val entryBulbs = if (pipeStatus == PipeStatus.ACTIVE) firstBulbs else secondBulbs
        val exitBulbs = if (pipeStatus == PipeStatus.ACTIVE) secondBulbs else firstBulbs

        if (entryBulbs.isNotEmpty()) {
            entryBulbs.forEach { it.editData<CopperBulb> { isPowered = false } }
            val entryBulb = tick / 4 % entryBulbs.size
            entryBulbs[entryBulb].editData<CopperBulb> { isPowered = true }
        }
        if (exitBulbs.isNotEmpty()) {
            exitBulbs.forEach { it.editData<CopperBulb> { isLit = false } }
            val exitBulb = tick / 4 % exitBulbs.size
            exitBulbs[exitBulb].editData<CopperBulb> { isLit = true }
        }
    }

    private enum class PipeStatus {
        INACTIVE,
        ACTIVE,
        ACTIVE_REVERSED
    }
}
