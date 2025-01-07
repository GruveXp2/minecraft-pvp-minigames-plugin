package gruvexp.bbminigames.menu;

import gruvexp.bbminigames.twtClassic.BotBows;
import gruvexp.bbminigames.twtClassic.Settings;

public abstract class SettingsMenu extends Menu {

    protected final Settings settings;

    protected SettingsMenu(Settings settings) {
        this.settings = settings;
    }
}
