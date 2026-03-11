package gruvexp.bbminigames.twtClassic.avatar

import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.entity.LivingEntity
import org.bukkit.scoreboard.Scoreboard
import org.bukkit.scoreboard.Team

class TeamManager(private val scoreboard: Scoreboard) { // singleton
    private val teams: Map<NamedTextColor, Team> = NamedTextColor.NAMES.values().associateWith { color ->
        val teamName = "glow_$color"
        val team = scoreboard.getTeam(teamName) ?: scoreboard.registerNewTeam(teamName)
        team.color(color)
        team
    }

    fun setColor(livingEntity: LivingEntity, color: NamedTextColor) {
        teams[color]?.addEntity(livingEntity)
    }
}