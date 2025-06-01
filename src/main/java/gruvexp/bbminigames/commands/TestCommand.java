package gruvexp.bbminigames.commands;

import gruvexp.bbminigames.Main;
import gruvexp.bbminigames.Util;
import gruvexp.bbminigames.extras.StickSlap;
import gruvexp.bbminigames.mechanics.RotatingStructure;
import gruvexp.bbminigames.twtClassic.BotBows;
import gruvexp.bbminigames.twtClassic.BotBowsMap;
import gruvexp.bbminigames.twtClassic.BotBowsPlayer;
import gruvexp.bbminigames.twtClassic.Lobby;
import gruvexp.bbminigames.twtClassic.ability.AbilityType;
import gruvexp.bbminigames.twtClassic.ability.abilities.ThunderBowAbility;
import gruvexp.bbminigames.twtClassic.botbowsTeams.BotBowsTeam;
import gruvexp.bbminigames.twtClassic.hazard.HazardChance;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class TestCommand implements CommandExecutor {

    public static boolean rotation = true;
    public static boolean verboseDebugging = false;
    public static boolean debugging = false;
    public static boolean test1 = false;
    public static boolean test2 = false;
    public static boolean testAbilities = false;
    public static RotatingStructure rotatingStructure;
    public static Inventory testInv = Bukkit.createInventory(null, 54, Component.text("Lagre-Chest"));

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {

        Player p;
        if (sender instanceof Player) {
            p = (Player) sender;
        } else {
            p = Bukkit.getPlayer("GruveXp");
        }

        if (args.length >= 1) {
            switch (args[0]) {
                case "init_wheel" -> {
                    float x = Float.parseFloat(args[1]);
                    float y = Float.parseFloat(args[2]);
                    float z = Float.parseFloat(args[3]);
                    Location centerLocation = new Location(p.getWorld(), x, y, z);
                    rotatingStructure = new RotatingStructure(centerLocation);
                    p.sendMessage(Component.text("Made a weel at that location"));
                }
                case "register_blocks" -> {
                    int firstX = Integer.parseInt(args[1]);
                    int firstY = Integer.parseInt(args[2]);
                    int firstZ = Integer.parseInt(args[3]);
                    int secondX = Integer.parseInt(args[4]);
                    int secondY = Integer.parseInt(args[5]);
                    int secondZ = Integer.parseInt(args[6]);

                    int x0 = Math.min(firstX, secondX);
                    int x1 = Math.max(firstX, secondX);
                    int y0 = Math.min(firstY, secondY);
                    int y1 = Math.max(firstY, secondY);
                    int z0 = Math.min(firstZ, secondZ);
                    int z1 = Math.max(firstZ, secondZ);

                    int totalAdded = 0;
                    String tag = args[7];
                    if (tag == null) tag = "rotasjon";
                    for (int x = x0; x < x1 + 1; x++) {
                        for (int y = y0; y < y1 + 1; y++) {
                            for (int z = z0; z < z1 + 1; z++) {
                                Block block = Main.WORLD.getBlockAt(x, y, z);
                                //p.sendMessage(Component.text("Block at " + x + " " + y + " " + z + ": " + block.getType()));
                                if (block.getType() != Material.AIR) {
                                    BlockDisplay display = (BlockDisplay) Main.WORLD.spawnEntity(new Location(Main.WORLD, x, y, z), EntityType.BLOCK_DISPLAY);
                                    BlockData blockData = block.getBlockData();
                                    display.setBlock(blockData);
                                    display.addScoreboardTag(tag);
                                    rotatingStructure.addDisplay(display);
                                    block.setType(Material.AIR);
                                    totalAdded++;
                                }
                            }
                        }
                    }
                    p.sendMessage(Component.text("Registerd a total of " + totalAdded + " blocks"));
                }
                case "w" -> {
                    Player gruveXp = Bukkit.getPlayer("GruveXp");
                    Player judith = Bukkit.getPlayer("Spionagent54");
                    if (judith == null) {
                        p.sendMessage(Component.text("Error! Judiths bruker ække inne på serveren! Join med skolepcen"));
                        return true;
                    }
                    Lobby lobby = BotBows.getLobby(0);
                    lobby.joinGame(gruveXp);
                    lobby.joinGame(judith);
                    lobby.settings.setMap(BotBowsMap.STEAMPUNK);
                    lobby.settings.stormHazard.setChance(HazardChance.DISABLED);
                    lobby.settings.earthquakeHazard.setChance(HazardChance.DISABLED);
                    lobby.settings.ghostHazard.setChance(HazardChance.DISABLED);
                    BotBowsPlayer gruveBp = lobby.getBotBowsPlayer(gruveXp);
                    BotBowsPlayer judithBp = lobby.getBotBowsPlayer(judith);
                    gruveBp.setReady(true, 4);
                    judithBp.setReady(true, 4);

                    //BotBows.getLobby(0).settings.healthMenu.enableCustomHP();
                    //Player judithP = Bukkit.getPlayer("Spionagent54");
                    //BotBowsPlayer judith = BotBows.getLobby(judithP).getBotBowsPlayer(judithP);

                    //judith.setMaxHP(20);
                    //Bukkit.dispatchCommand(Objects.requireNonNull(Bukkit.getPlayer("GruveXp")), "botbows:start");  // tester om dungeonen funker
                }
                case "q" -> {
                    Lobby lobby  = BotBows.getLobby(Bukkit.getPlayer("GruveXp"));
                    lobby.joinGame(Bukkit.getPlayer("Spionagent54"));
                }
                case "a" -> {
                    rotation = !rotation;
                    BotBows.debugMessage("New location logic set to: " + rotation);
                }
                case "b" -> {
                    verboseDebugging = !verboseDebugging;
                    BotBows.debugMessage("Verbose debugging set to: " + verboseDebugging);
                }
                case "c" -> {
                    String playerName = args[1];
                    if (playerName == null) playerName = "GruveXp";
                    BotBowsTeam team = BotBows.getLobby(Bukkit.getPlayer(playerName)).getBotBowsPlayer(Bukkit.getPlayer(playerName)).getTeam();
                    BotBows.debugMessage("The team of " + playerName + " is " + team.name);
                }
                case "toggle_debugging" -> {
                    debugging = !debugging;
                    BotBows.debugMessage("Debugging set to: " + debugging);
                }
                case "t" -> {
                    BotBows.debugMessage("Team1: " + BotBows.getLobby(0).settings.team1.name);
                    BotBows.debugMessage("Team2: " + BotBows.getLobby(0).settings.team2.name);
                }
                case "t1" -> {
                    test1 = !test1;
                    BotBows.debugMessage("Test1 set to: " + test1);
                }
                case "t2" -> {
                    test2 = !test2;
                    BotBows.debugMessage("Test2 set to: " + test2);
                }
                case "ta", "d" -> {
                    testAbilities = !testAbilities;
                    BotBows.debugMessage("testAbilities set to: " + testAbilities);
                }
                case "give_ability_items" -> {
                    for (AbilityType type : AbilityType.values()) {
                        ((Player) sender).getInventory().addItem(type.getAbilityItem());
                    }
                }
                case "test_arc" -> {
                    if (args.length < 7) {
                        sender.sendMessage("Not enough args (need 8)");
                    }
                    Location loc1 = Util.toLocation(Main.WORLD, args[1], args[2], args[3]);
                    Location loc2 = Util.toLocation(Main.WORLD, args[4], args[5], args[6]);
                    ThunderBowAbility.createElectricArc(loc1, loc2, Color.RED, 1.0);
                }
                case "inv" -> p.openInventory(testInv);
                case "set_blaze_rod_cooldown" -> StickSlap.cooldown = Integer.parseInt(args[1]);
                default -> sender.sendMessage("Wrong arg (" + args[0] + ")");
            }
            return true;
        }
        //BotBowsManager.debugMessage(STR."\{p.getName()}is \{isInDungeon(p) ? "" : "not"} in a dungeon\{BotBowsManager.isInDungeon(p) ? STR.", section\{BotBowsManager.getSection(p)}" : ""}");
        return true;
    }
}
