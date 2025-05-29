package gruvexp.bbminigames.mechanics;

import gruvexp.bbminigames.Main;
import gruvexp.bbminigames.Util;
import net.kyori.adventure.text.Component;
import org.bukkit.Axis;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.data.type.CopperBulb;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class SteamPipe {

    public final boolean isDualWay;
    private PipeStatus pipeStatus = PipeStatus.INACTIVE;
    private boolean shuttingDown = false;
    private final List<Location> nodes;
    private final HashMap<Player, Integer> playerEdge = new HashMap<>();

    private int animationTick = 0;
    private final List<CopperBulb> entryBulbs;
    private final List<CopperBulb> exitBulbs;

    public SteamPipe(boolean isDualWay, List<Location> nodes, Axis entryAxis, Axis exitAxis) {
        this.isDualWay = isDualWay;
        this.nodes = nodes;
        this.entryBulbs = Util.getOrthogonalLocations(nodes.getFirst(), entryAxis).stream()
                .filter(location -> location.getBlock().getType().data.equals(CopperBulb.class))
                .map(location -> (CopperBulb) (location.getBlock().getBlockData()))
                .collect(Collectors.toList());
        this.exitBulbs = Util.getOrthogonalLocations(nodes.getLast(), exitAxis).stream()
                .filter(location -> location.getBlock().getType().data.equals(CopperBulb.class))
                .map(location -> (CopperBulb) (location.getBlock().getBlockData()))
                .collect(Collectors.toList());
    }

    // the chunks that has the entry and exit. only check if players are near entry/exit if theyre in these chunks, to save performance
    public Set<Chunk> getTickedChunks() {
        Set<Chunk> chunks = Util.getChunksAround(nodes.getFirst(), 3);
        if (isDualWay) chunks.addAll(Util.getChunksAround(nodes.getLast(), 3));
        return chunks;
    }

    private void setPipeStatus(PipeStatus status) {
        if (this.pipeStatus == status) return;
        switch (status) {
            case ACTIVE -> {
                pipeStatus = status;
                entryBulbs.forEach(bulb -> bulb.setLit(true));
                exitBulbs.forEach(bulb -> bulb.setPowered(true));
            }
            case ACTIVE_REVERSED -> {
                pipeStatus = status;
                entryBulbs.forEach(bulb -> bulb.setPowered(true));
                exitBulbs.forEach(bulb -> bulb.setLit(true));
            }
            case INACTIVE -> {
                shuttingDown = true;

                Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> {
                    if (!playerEdge.isEmpty()) return;
                    pipeStatus = status;
                    entryBulbs.forEach(bulb -> bulb.setPowered(false));
                    entryBulbs.forEach(bulb -> bulb.setLit(false));

                    exitBulbs.forEach(bulb -> bulb.setPowered(false));
                    exitBulbs.forEach(bulb -> bulb.setLit(false));
                    shuttingDown = false;
                }, 20L);
            }
        }
    }

    public void checkProximity(Player p) {
        if (playerEdge.containsKey(p)) return;
        if (isDualWay) {
            if (pipeStatus != PipeStatus.ACTIVE_REVERSED && checkProximity(true , p)) return;
            if (pipeStatus != PipeStatus.ACTIVE          && checkProximity(false, p)) return;
            return;
        }
        checkProximity(true, p);
    }

    private boolean checkProximity(boolean firstEntry, Player p) {
        Location enterLocation = firstEntry ? nodes.getFirst() : nodes.getLast();
        if (enterLocation.distanceSquared(p.getLocation()) < 9) {
            playerEdge.put(p, firstEntry ? -1 : nodes.size());
            Bukkit.broadcast(Component.text("Registerd enter"));

            if (firstEntry) setPipeStatus(PipeStatus.ACTIVE);
            else setPipeStatus(PipeStatus.ACTIVE_REVERSED);

            return true;
        }
        return false;
    }

    public void tick() {
        if (pipeStatus == PipeStatus.INACTIVE) return;

        playerEdge.forEach((p, lastNode) -> {
            int nextNode = pipeStatus == PipeStatus.ACTIVE ? lastNode + 1 : lastNode - 1;
            //Bukkit.broadcast(Component.text("Next edg: " + nextNode));
            Location nextNodeLoc = nodes.get(nextNode);
            Vector a = nextNodeLoc.clone().subtract(p.getLocation()).toVector().normalize();

            boolean isEntering = pipeStatus == PipeStatus.ACTIVE ? lastNode == -1 : lastNode == nodes.size();
            a.multiply(isEntering ? new Vector(0.1, 0.3, 0.1) : new Vector(0.8, 0.7, 0.8));
            if (isEntering) {
                if (nextNodeLoc.clone().distanceSquared(p.getLocation()) > 12) {
                    playerEdge.put(p, -2); // the player exited and will be removed
                    Bukkit.broadcast(Component.text("Player canceld"));
                }
                AttributeInstance scaleAttribute = p.getAttribute(Attribute.SCALE);
                double scale = scaleAttribute.getBaseValue();
                scale -= 0.05;
                if (scale < 0.4) scale = 0.4;
                scaleAttribute.setBaseValue(scale);
            }

            Vector v = p.getVelocity().add(a);
            p.setVelocity(v);
            if (p.getLocation().distanceSquared(nextNodeLoc) < 1) {
                boolean onLastNode = pipeStatus == PipeStatus.ACTIVE ? nextNode == nodes.size() - 1 : nextNode == 0;
                if (onLastNode) {
                    playerEdge.put(p, -2); // the player exited and will be removed
                    Bukkit.broadcast(Component.text("Player exited"));
                    Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> p.getAttribute(Attribute.SCALE).setBaseValue(1.0), 10L);
                } else {
                    playerEdge.put(p, nextNode);
                    Bukkit.broadcast(Component.text("Player enters next node: " + nextNode + " (" + p.getLocation().distanceSquared(nextNodeLoc) + ")"));
                }
            }
        });
        playerEdge.entrySet().removeIf(entry -> entry.getValue() == -2);
        if (playerEdge.isEmpty() && !shuttingDown) setPipeStatus(PipeStatus.INACTIVE);
        updateAnimation();
    }

    void updateAnimation() {
        animationTick++;
        if (animationTick % 4 != 0) return;

        if (!entryBulbs.isEmpty()) {
            entryBulbs.forEach(bulb -> bulb.setPowered(false));
            int entryBulb = (animationTick / 4) % entryBulbs.size();
            entryBulbs.get(entryBulb).setPowered(true);
        }
        if (!exitBulbs.isEmpty()) {
            exitBulbs.forEach(bulb -> bulb.setLit(false));
            int exitBulb = (animationTick / 4) % exitBulbs.size();
            exitBulbs.get(exitBulb).setLit(true);
        }
    }

    private enum PipeStatus {
        INACTIVE,
        ACTIVE,
        ACTIVE_REVERSED
    }
}
