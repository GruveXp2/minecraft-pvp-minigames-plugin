package gruvexp.bbminigames.menu;

import gruvexp.bbminigames.commands.TestCommand;
import gruvexp.bbminigames.twtClassic.BotBows;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class MenuSlider {

    protected final Inventory inventory;
    protected final int startSlot;
    protected final Material filledTrackMaterial;
    protected final NamedTextColor filledTrackColor;
    private static final Material EMPTY_TRACK_MATERIAL = Material.WHITE_STAINED_GLASS_PANE;
    private static final NamedTextColor EMPTY_TRACK_COLOR = NamedTextColor.WHITE;
    protected final List<String> sliderSteps;

    public MenuSlider(Inventory inventory, int startSlot, Material filledTrackMaterial, NamedTextColor filledTrackColor, List<String> sliderSteps) {
        this.inventory = inventory;
        this.startSlot = startSlot;
        this.filledTrackMaterial = filledTrackMaterial;
        this.filledTrackColor = filledTrackColor;
        this.sliderSteps = sliderSteps;
    }

    public void setProgressSlots(int slots) {
        slots = Math.min(slots, sliderSteps.size()); // Begrenser slots til sliderens størrelse
        for (int i = 0; i < sliderSteps.size(); i++) {
            ItemStack is = i < slots ? Menu.makeItem(filledTrackMaterial, Component.text(sliderSteps.get(i), filledTrackColor))
                    : Menu.makeItem(EMPTY_TRACK_MATERIAL, Component.text(sliderSteps.get(i), EMPTY_TRACK_COLOR));
            inventory.setItem(i + startSlot, is);
        }
    }

    public void setProgress(String progressTick) {
        if (!sliderSteps.contains(progressTick)) {
            setProgressSlots(0);
            BotBows.debugMessage(progressTick + " Doesnt exist", TestCommand.log);
        }
        setProgressSlots(sliderSteps.indexOf(progressTick) + 1);
    }

    public int size() {
        return sliderSteps.size();
    }

    public int getStartSlot() {
        return startSlot;
    }

    public String getNext(String step) {
        int i = sliderSteps.indexOf(step);
        //BotBows.debugMessage(String.format("current(%d): %s", i, sliderSteps.get(i)));
        i++;
        if (i == sliderSteps.size()) {
            i = 0;
        }
        //BotBows.debugMessage(String.format("next(%d): %s", i, sliderSteps.get(i)));
        return sliderSteps.get(i);
    }
}
