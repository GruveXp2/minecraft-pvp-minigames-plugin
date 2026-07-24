package gruvexp.bbminigames.api.ability

import gruvexp.bbminigames.api.ability.AbilityContext.*
import org.bukkit.event.entity.AreaEffectCloudApplyEvent
import org.bukkit.event.entity.LingeringPotionSplashEvent
import org.bukkit.event.entity.ProjectileHitEvent

interface AbilityTrigger {
    interface OnEntityPlace {
        fun trigger(ctx: EntityPlace)
    }

    interface OnMelee {
        fun trigger(ctx: Melee)
    }

    interface OnLaunch {
        fun onLaunch(ctx: Launch)
    }

    interface OnProjectileHit {
        fun onHit(e: ProjectileHitEvent)
    }

    interface OnLingeringPotionUse {
        fun onSplash(e: LingeringPotionSplashEvent)
        fun onCloudApply(e: AreaEffectCloudApplyEvent)
    }

    interface OnBlockPlace {
        fun onPlace(ctx: BlockPlace)
    }
}
