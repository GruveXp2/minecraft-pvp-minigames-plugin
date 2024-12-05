package gruvexp.bbminigames.twtClassic.hazard.hazards;

import gruvexp.bbminigames.twtClassic.BotBows;
import gruvexp.bbminigames.twtClassic.Settings;
import gruvexp.bbminigames.twtClassic.hazard.Hazard;

public class GhostHazard extends Hazard {

    public GhostHazard(Settings settings) {
        super(settings);
    }

    @Override
    protected void trigger() {
        BotBows.messagePlayers("Ghost mode was triggered (but its not implemented yet)");
    }
}
