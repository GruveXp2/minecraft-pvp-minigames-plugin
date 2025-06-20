package gruvexp.bbminigames.mechanics;

import gruvexp.bbminigames.Main;
import gruvexp.bbminigames.Util;
import org.bukkit.Axis;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
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

    private int tick = 0;
    private final List<Block> firstBulbs; // dette funker ikke engang pga man må faktisk ta setblockstate for at noesomhelst skal skje!
    private final List<Block> secondBulbs;

    public SteamPipe(boolean isDualWay, List<Location> nodes, Axis entryAxis, Axis exitAxis) {
        this.isDualWay = isDualWay;
        this.nodes = nodes;
        this.firstBulbs = Util.getOrthogonalLocations(nodes.getFirst(), entryAxis).stream()
                .map(Location::getBlock)
                .filter(block -> block.getType().data.equals(CopperBulb.class))
                .collect(Collectors.toList());
        this.secondBulbs = Util.getOrthogonalLocations(nodes.getLast(), exitAxis).stream()
                .map(Location::getBlock)
                .filter(block -> block.getType().data.equals(CopperBulb.class))
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
                firstBulbs.forEach(bulb -> setLit(bulb,true));
                secondBulbs.forEach(bulb -> setPowered(bulb, true));
            }
            case ACTIVE_REVERSED -> {
                pipeStatus = status;
                firstBulbs.forEach(bulb -> setPowered(bulb, true));
                secondBulbs.forEach(bulb -> setLit(bulb,true));
            }
            case INACTIVE -> {
                shuttingDown = true;

                Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> {
                    if (!playerEdge.isEmpty()) return;
                    pipeStatus = status;
                    firstBulbs.forEach(bulb -> setPowered(bulb, false));
                    firstBulbs.forEach(bulb -> setLit(bulb, false));

                    secondBulbs.forEach(bulb -> setPowered(bulb, false));
                    secondBulbs.forEach(bulb -> setLit(bulb, false));
                    shuttingDown = false;
                }, 20L);
            }
        }
    }

    private static void setPowered(Block block, boolean powered) {
        CopperBulb bulb = (CopperBulb) block.getBlockData();
        bulb.setPowered(powered);
        block.setBlockData(bulb);
    }

    private static void setLit(Block block, boolean lit) {
        CopperBulb bulb = (CopperBulb) block.getBlockData();
        bulb.setLit(lit);
        block.setBlockData(bulb);
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
            Vector playerEntryDir = enterLocation.clone().subtract(p.getLocation()).toVector();
            Location nextNode = firstEntry ? nodes.get(1) : nodes.get(nodes.size() - 2);
            Vector pipeEntryDir = nextNode.clone().subtract(enterLocation).toVector();
            if (playerEntryDir.normalize().dot(pipeEntryDir.normalize()) < 0.6) return false; // if youre close to the entry point, but not standing at the front
            // you wont get sucked in. this is to fix getting stuck at the back of the entry
            // 0.6 here means around 55 deg, so if angle between player->entry and entry->nextnode is more than that its not gonna suck the player in

            playerEdge.put(p, firstEntry ? -1 : nodes.size());
            //Bukkit.broadcast(Component.text("Registerd enter"));

            if (firstEntry) setPipeStatus(PipeStatus.ACTIVE);
            else setPipeStatus(PipeStatus.ACTIVE_REVERSED);

            return true;
        }
        return false;
    }

    public void tick() {
        if (pipeStatus == PipeStatus.INACTIVE) return;
        tick++;

        playerEdge.forEach((p, lastNode) -> {
            int nextNode = pipeStatus == PipeStatus.ACTIVE ? lastNode + 1 : lastNode - 1;
            Location currentNodeLoc = (lastNode < 0 || lastNode == nodes.size()) ? p.getLocation() : nodes.get(lastNode);
            Location nextNodeLoc = nodes.get(nextNode);
            Location pLoc = p.getLocation();

            int nodeX = currentNodeLoc.getBlockX();
            int nodeY = currentNodeLoc.getBlockY();
            int nodeZ = currentNodeLoc.getBlockZ();
            if (tick % 5 == 0) { // if the player goes outside of the pipe bounds, they exited
                if ((nodeX == nextNodeLoc.getBlockX() && Math.abs(pLoc.getBlockX() - nodeX) > 1)
                 || (nodeY == nextNodeLoc.getBlockY() && Math.abs(pLoc.getBlockY() - nodeY) > 1)
                 || (nodeZ == nextNodeLoc.getBlockZ() && Math.abs(pLoc.getBlockZ() - nodeZ) > 1)) {
                    exitPlayer(p);
                    return;
                }
            }

            boolean isEntering = pipeStatus == PipeStatus.ACTIVE ? lastNode == -1 : lastNode == nodes.size();
            boolean onFirstNode = pipeStatus == PipeStatus.ACTIVE ? nextNode == 0 : lastNode == nodes.size() - 1;
            boolean onLastNode = pipeStatus == PipeStatus.ACTIVE ? nextNode == nodes.size() - 1 : nextNode == 0;
            if (isEntering) {
                if (nextNodeLoc.clone().distanceSquared(pLoc) > 12) {
                    playerEdge.put(p, -2); // the player exited and will be removed
                }
                AttributeInstance scaleAttribute = p.getAttribute(Attribute.SCALE);
                double scale = scaleAttribute.getBaseValue();
                scale -= 0.05;
                if (scale < 0.4) scale = 0.4;
                scaleAttribute.setBaseValue(scale);
            } else if (onFirstNode) {
                AttributeInstance scaleAttribute = p.getAttribute(Attribute.SCALE);
                scaleAttribute.setBaseValue(0.4);
            }

            Vector a = nextNodeLoc.clone().subtract(currentNodeLoc).toVector().normalize();
            Vector v = p.getVelocity();

            double distanceToNextNode = pLoc.distanceSquared(nextNodeLoc);
            if ((isEntering || v.lengthSquared() > 1)) { // get slowly sucked in, then go fast but dont go too much faster than 1b/t
                a.multiply(new Vector(0.1, 0.3, 0.1));
                if (isEntering) { // the closer you get to the entry, the stronger the pull
                    double multiply = (9 - distanceToNextNode) / 6;
                    a.multiply(new Vector(multiply, 1.5, multiply));
                }
            }
            v.add(a);
            p.setVelocity(v);
            boolean reachedNextNode = isEntering ? distanceToNextNode < 0.5 : distanceToNextNode < 1; // you must be closer to the first node to have reached it
            // this secures that the player is actually inside the pipe
            if (reachedNextNode) {
                if (onLastNode) {
                    exitPlayer(p);
                } else {
                    playerEdge.put(p, nextNode);
                }
            }
        });
        playerEdge.entrySet().removeIf(entry -> entry.getValue() == -2);
        if (playerEdge.isEmpty() && !shuttingDown) setPipeStatus(PipeStatus.INACTIVE);
        if (tick % 4 == 0) {
            updateAnimation();
        }
    }

    void exitPlayer(Player p) {
        playerEdge.put(p, -2); // the player exited and will be removed
        Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> p.getAttribute(Attribute.SCALE).setBaseValue(1.0), 2L);
    }

    void updateAnimation() {
        List<Block> entryBulbs = pipeStatus == PipeStatus.ACTIVE ? firstBulbs : secondBulbs;
        List<Block> exitBulbs = pipeStatus == PipeStatus.ACTIVE ? secondBulbs : firstBulbs;

        if (!entryBulbs.isEmpty()) {
            entryBulbs.forEach(bulb -> setPowered(bulb, false));
            int entryBulb = (tick / 4) % entryBulbs.size();
            setPowered(entryBulbs.get(entryBulb), true);
        }
        if (!exitBulbs.isEmpty()) {
            exitBulbs.forEach(bulb -> setLit(bulb,false));
            int exitBulb = (tick / 4) % exitBulbs.size();
            setLit(exitBulbs.get(exitBulb), true);
        }
    }

    private enum PipeStatus {
        INACTIVE,
        ACTIVE,
        ACTIVE_REVERSED
    }
}
