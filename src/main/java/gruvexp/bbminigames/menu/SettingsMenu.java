package gruvexp.bbminigames.menu;

import gruvexp.bbminigames.twtClassic.Settings;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public abstract class SettingsMenu extends Menu {

    protected final Settings settings;

    protected static final Component STATUS_ENABLED = Component.text("Enabled", NamedTextColor.GREEN);
    protected static final Component STATUS_DISABLED = Component.text("Disabled", NamedTextColor.RED);

    protected SettingsMenu(Settings settings) {
        this.settings = settings;
    }
}
