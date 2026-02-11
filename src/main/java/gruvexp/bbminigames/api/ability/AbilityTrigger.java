package gruvexp.bbminigames.api.ability;

public interface AbilityTrigger {
    interface OnPlace {
        void trigger(AbilityContext.Place ctx);
    }
    interface OnMelee {
        void trigger(AbilityContext.Melee ctx);
    }
}
