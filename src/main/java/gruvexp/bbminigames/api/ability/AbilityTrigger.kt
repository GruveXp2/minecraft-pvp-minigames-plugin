package gruvexp.bbminigames.api.ability;

import org.bukkit.event.entity.AreaEffectCloudApplyEvent;
import org.bukkit.event.entity.LingeringPotionSplashEvent;
import org.bukkit.event.entity.ProjectileHitEvent;

public interface AbilityTrigger {
    interface OnEntityPlace {
        void trigger(AbilityContext.EntityPlace ctx);
    }
    interface OnMelee {
        void trigger(AbilityContext.Melee ctx);
    }
    interface OnLaunch {
        void onLaunch(AbilityContext.Launch ctx);
    }
    interface OnProjectileHit {
        void onHit(ProjectileHitEvent e);
    }
    interface OnLingeringPotionUse {
        void onSplash(LingeringPotionSplashEvent e);
        void onCloudApply(AreaEffectCloudApplyEvent e);
    }
    interface OnBlockPlace {
        void onPlace(AbilityContext.BlockPlace ctx);
    }
}
