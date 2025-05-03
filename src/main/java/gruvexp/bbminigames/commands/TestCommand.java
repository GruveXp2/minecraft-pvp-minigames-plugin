package gruvexp.bbminigames.commands;

import gruvexp.bbminigames.Main;
import gruvexp.bbminigames.Util;
import gruvexp.bbminigames.extras.StickSlap;
import gruvexp.bbminigames.twtClassic.BotBows;
import gruvexp.bbminigames.twtClassic.BotBowsMap;
import gruvexp.bbminigames.twtClassic.Lobby;
import gruvexp.bbminigames.twtClassic.ability.AbilityType;
import gruvexp.bbminigames.twtClassic.ability.abilities.ThunderBowAbility;
import gruvexp.bbminigames.twtClassic.botbowsTeams.BotBowsTeam;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class TestCommand implements CommandExecutor {

    public static boolean rotation = true;
    public static boolean verboseDebugging = false;
    public static boolean debugging = true;
    public static boolean test1 = false;
    public static boolean test2 = false;
    public static boolean testAbilities = false;
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
                case "w" -> {
                    BotBows.getLobby(0).joinGame(Bukkit.getPlayer("GruveXp"));
                    BotBows.getLobby(0).joinGame(Bukkit.getPlayer("Spionagent54"));
                    BotBows.getLobby(0).settings.setMap(BotBowsMap.ICY_RAVINE);
                    BotBows.getLobby(0).settings.setWinScoreThreshold(0);
                    //BotBows.getLobby(0).settings.healthMenu.enableCustomHP();
                    //Player judithP = Bukkit.getPlayer("Spionagent54");
                    //BotBowsPlayer judith = BotBows.getLobby(judithP).getBotBowsPlayer(judithP);

                    //judith.setMaxHP(20);
                    Bukkit.dispatchCommand(Objects.requireNonNull(Bukkit.getPlayer("GruveXp")), "botbows:start");  // tester om dungeonen funker
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
