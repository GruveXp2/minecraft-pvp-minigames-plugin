package gruvexp.bbminigames.api.ability;

public interface AbilityTrigger {
    interface PlaceableAbility {
        void trigger(AbilityContext.Place ctx);
    }
}
