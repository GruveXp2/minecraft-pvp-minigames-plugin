package gruvexp.bbminigames.menu;

import gruvexp.bbminigames.twtClassic.BotBows;
import gruvexp.bbminigames.twtClassic.Settings;

public abstract class SettingsMenu extends Menu {

    protected Settings settings;

    @Override
    public void setMenuItems() {
        this.settings = BotBows.settings;
    }
}
