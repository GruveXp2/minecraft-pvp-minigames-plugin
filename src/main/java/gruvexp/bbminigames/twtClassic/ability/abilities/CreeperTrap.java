package gruvexp.bbminigames.twtClassic.ability.abilities;

import gruvexp.bbminigames.Main;
import gruvexp.bbminigames.api.ability.AbilityContext;
import gruvexp.bbminigames.api.ability.AbilityTrigger;
import gruvexp.bbminigames.twtClassic.BotBows;
import gruvexp.bbminigames.twtClassic.BotBowsPlayer;
import gruvexp.bbminigames.twtClassic.Lobby;
import gruvexp.bbminigames.twtClassic.ability.Ability;
import gruvexp.bbminigames.twtClassic.ability.AbilityType;
import gruvexp.bbminigames.twtClassic.botbowsTeams.BotBowsTeam;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.data.Lightable;
import org.bukkit.entity.*;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class CreeperTrap extends Ability implements AbilityTrigger.OnEntityPlace {

    public static final float BLOCK_PX = 0.0625f;
    protected static final float CREEPER_SCALE = 0.75f;
    protected static final float CREEPER_PX = BLOCK_PX * CREEPER_SCALE;

    public static int ACTIVATION_DELAY  = 3;
    public static double BLAST_RADIUS = 4;

    protected static HashMap<Creeper, BotBowsPlayer> creeperOwners = new HashMap<>();

    public static void glowCreepers(BotBowsTeam team, int seconds) {
        Set<Creeper> creepers = creeperOwners.entrySet().stream()
                .filter(entry -> entry.getValue().getTeam() == team)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());

        creepers.forEach(creeper -> creeper.setGlowing(true));
        Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> creepers.forEach(creeper -> creeper.setGlowing(false)), 20L * seconds);
    }

    Creeper creeper;
    CreeperTicker creeperTicker;

    public CreeperTrap(BotBowsPlayer bp, int hotBarSlot) {
        super(bp, hotBarSlot, AbilityType.CREEPER_TRAP);
    }

    public static void ignite(Creeper creeper) {
        if (!creeperOwners.containsKey(creeper)) {
            creeper.ignite();
            return;
        }
        CreeperTrap ability = (CreeperTrap) creeperOwners.get(creeper).getAbility(AbilityType.CREEPER_TRAP);
        ability.creeperTicker.ignite();
    }

    public static void igniteAllCreepers() {
        Set<Creeper> creepers = new HashSet<>(creeperOwners.keySet());
        creepers.forEach(CreeperTrap::ignite);
    }

    @Override
    public void trigger(AbilityContext.EntityPlace ctx) {
        use();

        Location loc = ctx.loc();
        // explode already placed creepers (so players cant farm creeper mines and trap another player completely)
        Set<Creeper> creepers = creeperOwners.entrySet().stream().filter(entry -> entry.getValue() == bp)
                .map(Map.Entry::getKey).collect(Collectors.toSet());
        creepers.forEach(CreeperTrap::ignite);

        creeper = (Creeper) bp.player.getWorld().spawnEntity(loc, EntityType.CREEPER);
        creeper.setAI(false);
        creeper.getAttribute(Attribute.SCALE).setBaseValue(0.75);
        creeperOwners.put(creeper, bp);


        // Center location of the top face of the creeper's head (assuming head is 2 blocks tall)
        loc.add(-3*CREEPER_PX, 23*CREEPER_PX, -3*CREEPER_PX); // creeper height

        // Outer display (Red Stained Glass with 75% of the Creeper's head size)
        BlockDisplay glassDisplay = (BlockDisplay) loc.getWorld().spawnEntity(loc, EntityType.BLOCK_DISPLAY);
        glassDisplay.setBlock(Bukkit.createBlockData(Material.getMaterial(bp.getTeam().dyeColor.name() + "_STAINED_GLASS")));
        float glassSize = 6*CREEPER_PX; // headSize * % of headSize to use
        glassDisplay.setTransformation(new Transformation(new Vector3f(0, 0, 0), new AxisAngle4f(0, 0, 0, 1), new Vector3f(glassSize, glassSize, glassSize), new AxisAngle4f(0, 0, 0, 1)));

        loc.add(CREEPER_PX, CREEPER_PX, CREEPER_PX); // creeper height

        // Inner display (Redstone Lamp with 50% of the Creeper's head size)
        BlockDisplay lampDisplay = (BlockDisplay) loc.getWorld().spawnEntity(loc, EntityType.BLOCK_DISPLAY);
        lampDisplay.setBlock(Bukkit.createBlockData(Material.WEATHERED_COPPER_BULB));
        float lampSize = 4*CREEPER_PX;
        lampDisplay.setTransformation(new Transformation(new Vector3f(0, 0, 0), new AxisAngle4f(0, 0, 0, 1), new Vector3f(lampSize, lampSize, lampSize), new AxisAngle4f(0, 0, 0, 1)));

        creeperTicker = new CreeperTicker(creeper, lampDisplay, glassDisplay, bp);
        creeperTicker.runTaskTimer(Main.getPlugin(), ACTIVATION_DELAY * 20L, 5);
    }

    public static class CreeperTicker extends BukkitRunnable {

        private final Creeper creeper;
        private final BlockDisplay lampDisplay;
        private final BlockDisplay glassDisplay;
        private final BotBowsPlayer owner;
        private int ticks = 0;
        private boolean igniting = false;
        private final HashSet<BotBowsPlayer> hitPlayers = new HashSet<>();

        public CreeperTicker(Creeper creeper, BlockDisplay lampDisplay, BlockDisplay glassDisplay, BotBowsPlayer owner) {
            this.lampDisplay = lampDisplay;
            this.glassDisplay = glassDisplay;
            this.creeper = creeper;
            this.owner = owner;
        }

        @Override
        public void run() {
            ticks += 5;
            if (ticks % 10 == 0 || igniting) {
                Lightable lampData = (Lightable) lampDisplay.getBlock();
                lampData.setLit(!lampData.isLit());
                lampDisplay.setBlock(lampData);
            }
            if (ticks == 0) {
                lampDisplay.remove();
                glassDisplay.remove();
                cancel();
                explode();
            }
            for (Entity entity : creeper.getWorld().getNearbyEntities(creeper.getLocation(), BLAST_RADIUS, BLAST_RADIUS, BLAST_RADIUS, entity -> entity instanceof Player)) {
                Player p = (Player) entity;
                if (!p.hasLineOfSight(creeper)) continue;
                Lobby lobby = BotBows.getLobby(p);
                if (lobby == null) continue;
                if (lobby != owner.lobby) continue;
                BotBowsPlayer bp = lobby.getBotBowsPlayer(p);
                if (bp.getTeam() == owner.getTeam() && bp.player.getLocation().distanceSquared(creeper.getLocation()) > 1) continue;
                if (!bp.isAlive()) continue;

                hitPlayers.add(bp);
            }
            if (!hitPlayers.isEmpty() && !igniting) {
                ignite();
            }
        }

        public void ignite() {
            lampDisplay.setBlock(Bukkit.createBlockData(Material.COPPER_BULB)); // normal copper bulb that will give off more light
            creeper.ignite();
            igniting = true;
            ticks = -20;
            creeperOwners.remove(creeper);
        }

        public void explode() {
            Color attackerTeamColor = owner.getTeam().dyeColor.getColor();
            World world = creeper.getWorld();
            world.spawnParticle(Particle.EXPLOSION_EMITTER, creeper.getLocation(), 5, BLAST_RADIUS /4, BLAST_RADIUS /4, BLAST_RADIUS /4, 5);
            world.spawnParticle(Particle.DUST, creeper.getLocation(), 1000, 2, 2, 2, 0.4, new Particle.DustOptions(attackerTeamColor, 5));  // Red color
            for (Entity entity : world.getNearbyEntities(creeper.getLocation(), BLAST_RADIUS, BLAST_RADIUS, BLAST_RADIUS, entity -> entity instanceof Player)) {
                Player p = (Player) entity;
                Lobby lobby = BotBows.getLobby(p);
                if (lobby == null) return;
                if (lobby != owner.lobby) return;
                BotBowsPlayer bp = lobby.getBotBowsPlayer(p);
                hitPlayers.add(bp);
            }
            hitPlayers.forEach(bp -> bp.handleHit(Component.text(" hugged "), owner, Component.text("'s creeper")));
        }
    }
}
