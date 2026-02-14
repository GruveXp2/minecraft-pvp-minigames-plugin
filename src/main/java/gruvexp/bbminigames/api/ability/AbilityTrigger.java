package gruvexp.bbminigames.api.ability;

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
    interface OnBlockPlace {
        void onPlace(AbilityContext.BlockPlace ctx);
    }
}
