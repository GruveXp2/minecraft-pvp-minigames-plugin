package gruvexp.bbminigames.mechanics;

import io.papermc.paper.entity.LookAnchor;
import org.bukkit.Location;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.joml.Vector3f;

import java.util.HashSet;

public class RotatingStructure {

    private final HashSet<BlockDisplay> displays = new HashSet<>();
    private final Location centerLocation;
    private float rotation = 0f;

    public RotatingStructure(Location centerLocation) {
        this.centerLocation = centerLocation;
    }

    public void addDisplay(BlockDisplay display) {
        Vector3f Δpos = display.getLocation().subtract(centerLocation).toVector().toVector3f();
        //Bukkit.broadcast(Component.text("the translation is gonna be " + Δpos.x + " " + Δpos.y + " " + Δpos.z));
        display.teleport(centerLocation);
        Transformation transformation = display.getTransformation();
        transformation.getTranslation().set(Δpos);
        display.setTransformation(transformation);

        //Transformation transformation2 = display.getTransformation();
        //Vector3f vec = transformation2.getTranslation();
        //Bukkit.broadcast(Component.text("the translation is actually " + vec.x + " " + vec.y + " " + vec.z));

        display.setTeleportDuration(1);
        displays.add(display);
    }

    public void rotate(float Δpitch) {
        rotation += Δpitch;
        Location lookLocation = centerLocation.clone().add(new Vector(0, Math.sin(Δpitch), Math.cos(Δpitch)));
        for (BlockDisplay display : displays) {
            display.lookAt(lookLocation, LookAnchor.EYES);
        }
    }
}
