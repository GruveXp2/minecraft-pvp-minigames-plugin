package gruvexp.bbminigames.twtClassic.ability.abilities;

import gruvexp.bbminigames.Main;
import gruvexp.bbminigames.twtClassic.BotBowsPlayer;
import gruvexp.bbminigames.twtClassic.ability.Ability;
import gruvexp.bbminigames.twtClassic.ability.AbilityType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.WeatherType;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.data.Lightable;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.EntityType;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

public class CreeperTrapAbility extends Ability {

    public static final float CREEPER_SCALE = 0.75f;
    public static final float BLOCK_PX = 0.0625f;
    public static final float CREEPER_PX = BLOCK_PX * CREEPER_SCALE;

    Creeper creeperTrap;

    public CreeperTrapAbility(BotBowsPlayer bp, int hotBarSlot) {
        super(bp, hotBarSlot, AbilityType.CREEPER_TRAP);
    }

    public void use(Location loc) {
        super.use();
        creeperTrap = (Creeper) Main.WORLD.spawnEntity(loc, EntityType.CREEPER);
        creeperTrap.setAI(false);
        creeperTrap.getAttribute(Attribute.SCALE).setBaseValue(0.75);


        // Center location of the top face of the creeper's head (assuming head is 2 blocks tall)
        loc.add(-3*CREEPER_PX, 23*CREEPER_PX, -3*CREEPER_PX); // creeper height

        // Outer display (Red Stained Glass with 75% of the Creeper's head size)
        BlockDisplay GlassDisplay = (BlockDisplay) loc.getWorld().spawnEntity(loc, EntityType.BLOCK_DISPLAY);
        GlassDisplay.setBlock(Bukkit.createBlockData(Material.RED_STAINED_GLASS));
        float glassSize = 6*CREEPER_PX; // headSize * % of headSize to use
        GlassDisplay.setTransformation(new Transformation(new Vector3f(0, 0, 0), new AxisAngle4f(0, 0, 0, 1), new Vector3f(glassSize, glassSize, glassSize), new AxisAngle4f(0, 0, 0, 1)));

        loc.add(CREEPER_PX, CREEPER_PX, CREEPER_PX); // creeper height

        // Inner display (Redstone Lamp with 50% of the Creeper's head size)
        BlockDisplay lampDisplay = (BlockDisplay) loc.getWorld().spawnEntity(loc, EntityType.BLOCK_DISPLAY);
        lampDisplay.setBlock(Bukkit.createBlockData(Material.REDSTONE_LAMP));
        float lampSize = 4*CREEPER_PX;
        lampDisplay.setTransformation(new Transformation(new Vector3f(0, 0, 0), new AxisAngle4f(0, 0, 0, 1), new Vector3f(lampSize, lampSize, lampSize), new AxisAngle4f(0, 0, 0, 1)));

        Bukkit.getScheduler().runTaskTimer(Main.getPlugin(), task -> { // makes the light bulb on the creeper blink to show its a mine
            Lightable lampData = (Lightable) lampDisplay.getBlock();
            lampData.setLit(!lampData.isLit());
            lampDisplay.setBlock(lampData);
        }, 0, 10L);
    }
}
