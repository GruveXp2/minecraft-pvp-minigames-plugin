package gruvexp.bbminigames.mechanics;

import gruvexp.bbminigames.Util;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.CopperBulb;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class SteamPipe {

    public final boolean isDualWay;
    private PipeStatus pipeStatus = PipeStatus.INACTIVE;
    private final ArrayList<BlockFace> edgeDirections;
    private final ArrayList<Location> nodes;
    private final HashMap<Player, Integer> playerEdgeLoc = new HashMap<>();

    private int animationTick = 0;
    private final List<CopperBulb> entryBulbs;
    private final List<CopperBulb> exitBulbs;

    public SteamPipe(boolean isDualWay, ArrayList<Location> nodes, ArrayList<BlockFace> edgeDirections) {
        this.isDualWay = isDualWay;
        this.nodes = nodes;
        this.edgeDirections = edgeDirections;
        this.entryBulbs = Util.getOrthogonalLocations(nodes.getFirst(), edgeDirections.getFirst()).stream()
                .filter(location -> location.getBlock().getType().data.equals(CopperBulb.class))
                .map(location -> (CopperBulb) (location.getBlock()))
                .collect(Collectors.toList());
        this.exitBulbs = Util.getOrthogonalLocations(nodes.getLast(), edgeDirections.getLast()).stream()
                .filter(location -> location.getBlock().getType().data.equals(CopperBulb.class))
                .map(location -> (CopperBulb) (location.getBlock()))
                .collect(Collectors.toList());
    }

    private void setPipeStatus(PipeStatus status) {
        if (this.pipeStatus == status) return;
        pipeStatus = status;
        switch (status) {
            case ACTIVE -> {
                entryBulbs.forEach(bulb -> bulb.setLit(true));
                exitBulbs.forEach(bulb -> bulb.setPowered(true));
            }
            case ACTIVE_REVERSED -> {
                entryBulbs.forEach(bulb -> bulb.setPowered(true));
                exitBulbs.forEach(bulb -> bulb.setLit(true));
            }
            case INACTIVE -> {
                entryBulbs.forEach(bulb -> bulb.setPowered(false));
                entryBulbs.forEach(bulb -> bulb.setLit(false));

                exitBulbs.forEach(bulb -> bulb.setPowered(false));
                exitBulbs.forEach(bulb -> bulb.setLit(false));
            }
        }
    }

    public void checkProximity(Player p) {
        if (playerEdgeLoc.containsKey(p)) return;
        if (isDualWay) {
            if (pipeStatus != PipeStatus.ACTIVE_REVERSED && checkProximity(true , p)) return;
            if (pipeStatus != PipeStatus.ACTIVE          && checkProximity(false, p)) return;
            return;
        }
        checkProximity(true, p);
    }

    private boolean checkProximity(boolean firstEntry, Player p) {
        Location enterLocation = firstEntry ? nodes.getFirst() : nodes.getLast();
        if (enterLocation.distanceSquared(p.getLocation()) < 2) {
            playerEdgeLoc.put(p, firstEntry ? -1 : edgeDirections.size());

            if (firstEntry) setPipeStatus(PipeStatus.ACTIVE);
            else setPipeStatus(PipeStatus.ACTIVE_REVERSED);

            return true;
        }
        return false;
    }

    public void tick() {
        if (pipeStatus == PipeStatus.INACTIVE) return;

        playerEdgeLoc.forEach((p, i) -> {
            int nextIndex = pipeStatus == PipeStatus.ACTIVE ? i + 1 : i - 1;
            Location nextNode = nodes.get(nextIndex);
            Vector a = nextNode.clone().subtract(p.getLocation()).toVector().normalize();

            boolean isEntering = pipeStatus == PipeStatus.ACTIVE ? i == -1 : i == nodes.size();
            a.multiply(isEntering ? 0.1 : 0.3);

            Vector v = p.getVelocity().add(a);
            p.setVelocity(v);
            if (p.getLocation().distanceSquared(nextNode) < 1) {
                boolean onLastNode = pipeStatus == PipeStatus.ACTIVE ? i == nodes.size() - 1 : i == 0;
                if (onLastNode) {
                    playerEdgeLoc.put(p, -2); // the player exited and will be removed
                } else {
                    playerEdgeLoc.put(p, nextIndex);
                }
            }
        });
        playerEdgeLoc.entrySet().removeIf(entry -> entry.getValue() == -2);
        if (playerEdgeLoc.isEmpty()) setPipeStatus(PipeStatus.INACTIVE);
        updateAnimation();
    }

    void updateAnimation() {
        animationTick++;
        if (animationTick % 4 != 0) return;

        entryBulbs.forEach(bulb -> bulb.setPowered(false));
        exitBulbs.forEach(bulb -> bulb.setLit(false));

        int entryBulb = (animationTick / 4) % entryBulbs.size();
        int exitBulb = (animationTick / 4) % exitBulbs.size();

        entryBulbs.get(entryBulb).setPowered(true);
        exitBulbs.get(exitBulb).setLit(true);
    }

    private enum PipeStatus {
        INACTIVE,
        ACTIVE,
        ACTIVE_REVERSED
    }
}
