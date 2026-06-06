package gruvexp.bbminigames.sumo;

import gruvexp.bbminigames.Main;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

import static org.bukkit.Bukkit.getServer;

public class SumoData {
    // ---------------- Alltid konstant ----------------
    private static final Main PLUGIN = Main.getPlugin();
    private static final Location[] SPAWNPOS = new Location[2]; // de 2 spawnpointsene
    private static final int MAX_ROUNDS = 5;
    // ---------------- Heile turneen ----------------
    public static ArrayList<Player> playerList = new ArrayList<>(); //liste me alle deltagerne i spillet
    public static ArrayList<ArrayList<ArrayList<Player>>> rounds = new ArrayList<>(MAX_ROUNDS); // ROUNDS = 5
    public static ArrayList<ArrayList<PlayerData>> playerPoints = new ArrayList<>(MAX_ROUNDS); // ROUNDS = 5
    public static HashMap<Player, Integer> playerIDs = new HashMap<>(); // IDen til playerPoints
    public static HashMap<Player, Integer> playerScores = new HashMap<>();
    // ---------------- Counters ----------------
    private static int round = 0;
    private static int tourney = 0;
    // ---------------- Inni en turné ----------------
    public static ArrayList<Player[]> battleList = new ArrayList<>(); // liste over battels som skal utkjempes i nåværende turné
    private static int battleNum = 0;
    private static Player[] inBattle = new Player[2]; // De som kjemper i arenaen rn

    public static void init() {
        // Spawnpoint
        for (Entity e: getServer().getWorld("Sumo").getEntities()) {
            if (e instanceof ArmorStand) {
                try {
                    if (e.getCustomName().equals("north_spawn")) {
                        SPAWNPOS[0] = e.getLocation().add(0, 2, 0);
                        if (SPAWNPOS[1] != null) break;
                    } else if (e.getCustomName().equals("south_spawn")) {
                        SPAWNPOS[1] = e.getLocation().add(0, 2, 0);
                        if (SPAWNPOS[0] != null) break;
                    }
                } catch (NullPointerException ignored) {}
            }
        }
        if (SPAWNPOS[0] == null) {
            messagePlayers(Component.text("ERROR!! CANT FIND ARMOR STAND FOR SPAWNPOINT", NamedTextColor.RED));
        }

        // Round init, legger til runder som tomme lists
        for (int i = 0; i < MAX_ROUNDS; i++) {
            rounds.add(new ArrayList<>(2));
        }

        // p_points init, fyller 2d listen med null values
        for (int i = 0; i < MAX_ROUNDS; i++) {
            ArrayList<PlayerData> a = new ArrayList<>(playerList.size()); // arraylist of round
            for (int j = 0; j < playerList.size(); j++) {
                a.add(null);
            }
            playerPoints.add(a);
        }

        // lager player id'er
        for (int i = 0; i < playerList.size(); i++) {
            playerIDs.put(playerList.get(i), i);
            playerScores.put(playerList.get(i), 0);
        }

        //legger til main turné i round 1
        SumoData.rounds.get(0).add(playerList);
    } //definerer spawnpos, arraylist init

    // ---------------- Getters & Setters ----------------
    public static Player[] getBattle() {
        return inBattle;
    }

    public static Location[] getSpawnPos() {
        return SPAWNPOS;
    }
    public static int getRound() {
        return round;
    }
    public static int getTourney() {return tourney;}

    // ---------------- Tournament progression ----------------
    public static void startNextRound() {
        Bukkit.getPlayer("GruveXp").sendMessage(Component.text("rounds.get(round + 1).size() = " + rounds.get(round + 1).size() + " (startNextRound), and round number is: " + round));
        if (round == MAX_ROUNDS - 1 || rounds.get(round + 1).isEmpty()) { // viss man er på siste tilatte runde eller det ikke er noen fler
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.sendMessage(Component.text(round + " == " + (MAX_ROUNDS - 1) + "   or   " + rounds.get(round + 1).size() + " == 0", NamedTextColor.GRAY));
            }

            postGame();
            return;
        }
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.sendMessage(Component.text("Someone got the same score and will have a rematch!"));
        }
        round ++;
        tourney = -1; //kommer til å bli 0 med en gang startNextTourney blir kjørt!
        startNextTourney(true);
    }

    public static void startNextTourney(boolean increaseTourney) { //starter en sub turnament. i bynnelsen er det alle sammen

        Bukkit.getPlayer("GruveXp").sendMessage(Component.text("rounds.get(round + 1).size() = " + rounds.get(round + 1).size() + " (startNextTourney), and round number is: " + round));
        if (tourney == rounds.get(round).size() - 1 && increaseTourney) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.sendMessage(Component.text("Starting next round...", NamedTextColor.GRAY));
            }
            Bukkit.getPlayer("GruveXp").sendMessage(Component.text("rounds.get(round + 1).size() = " + rounds.get(round + 1).size() + " (startNextTourney2), and round number is: " + round));
            startNextRound();
            return;
        }

        if (increaseTourney) {tourney++;}

        //Neste turné har starta!
        for (Player p : rounds.get(round).get(tourney)) { //lager en tom PlayerData til alle i runda, sånn at man kan gette og adde points etterpå.
            playerPoints.get(round).set(playerIDs.get(p), new PlayerData(p));
        }

        for (Player p : rounds.get(round).get(tourney)) { // når det blir nye runder så blir det gråe steder som viser rundene som ikke har blitt gjort enda
            Board.updateScore(p);
        }

        setUpBattles(); // setter opp åssen battels som skal utkjempes
        startNextBattle(); // starter den første battlen
    }

    private static void setUpBattles() { //setter opp battlelist og sender melding

        ArrayList<Player> players = rounds.get(round).get(tourney); // de playersene som skal være med i denne turneen

        for (int i = 0; i < players.size() - 1; i++) { // går gjennom alle kombinasjoner av players og adder duoene i battlelisten
            for (int j=i+1; j < players.size(); j++) {
                battleList.add(new Player[]{players.get(i), players.get(j)});
            }
        }
        for (int i = 0; i < battleList.size(); i++) {
            Main.getPlugin().getLogger().info("[SUMO]: Battle " + i + ": " + battleList.get(i)[0].getName() + " vs " + battleList.get(i)[1].getName());
        }
    }

    public static void startNextBattle() {

        if (battleNum >= battleList.size()) {
            postTourney();
            return;
        } //if there is no more battles

        inBattle = battleList.get(battleNum);
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.sendMessage(Component.text("Battle #" + (battleNum + 1) + " has started! ")
                    .append(inBattle[0].name())
                    .append(Component.text("vs"))
                    .append(inBattle[1].name()));
            p.sendMessage(Component.text("Battle starting in 5.."));
        }
        inBattle[0].teleport(SPAWNPOS[0]);
        inBattle[1].teleport(SPAWNPOS[1]);
        inBattle[0].setGameMode(GameMode.ADVENTURE);
        inBattle[1].setGameMode(GameMode.ADVENTURE);
        ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
        Bukkit.dispatchCommand(console, frame(0, "north"));
        Bukkit.dispatchCommand(console, frame(0, "south"));

        new BattleInit(PLUGIN).runTaskTimer(PLUGIN, 40L, 2L); //animasjon at veggene går ned

        battleNum++; //neste gang en ny battel startes så er det neste battel
    } //starts the next battle if there is one

    public static void postBattle() { // Etter hver eneste battle, oppdaterer scoreboard

        List<PlayerData> tourneyData = getTourneyPlayersSorted();

        int lastScore = 0; // dette viser scoren den forrige playeren fikk
        for (int i = 0; i < tourneyData.size(); i++) {

            Player p = tourneyData.get(i).PLAYER; // tar ut player fra index i

            if (i == 0) { // den med lavest poeng blir uansett 0, siden det er ikke noen med lavere å sammenligne med
                playerPoints.get(round).get(playerIDs.get(p)).setScore(0);
            } else {
                if (tourneyData.get(i).getWins() == tourneyData.get(i - 1).getWins()) { // hvis playeren har samme score som den forrige
                    playerPoints.get(round).get(playerIDs.get(p)).setScore(lastScore);
                } else { // hvis playeren har mer poeng så settes score til i (feks hvis playeren hadde høyere enn den forrige og var tredj dårligst så blir score 3)
                    playerPoints.get(round).get(playerIDs.get(p)).setScore(i);
                    lastScore = i;
                }
            }
        }

        for (PlayerData playerData : tourneyData) {
            Player p = playerData.PLAYER;
            int score = 0;
            for (int i = 0; i < round + 1; i++) {
                score += playerPoints.get(i).get(playerIDs.get(p)).getScore(); // legger til scoren på runde i
            }
            playerScores.put(p, score); // legger til i hashmappet
        }
    }

    private static void postTourney() {

        Arrays.fill(inBattle, null); //resetter inbattle så de slipper å bøgge når de treffer golvet
        battleList.clear(); //clearer liste med battles
        battleNum = 0; //resetter battle num
        // sjekker om noen hadde samme score

        // ta de som har samme poeng inn i ny turné
        List<PlayerData> tourneyData = getTourneyPlayersSorted();

        ArrayList<Player> rematchPlayers = new ArrayList<>(tourneyData.size()); // players som blir med i neste runde
        for (int i = 1; i < tourneyData.size(); i++) {

            if (tourneyData.get(i).getWins() == tourneyData.get(i - 1).getWins()) {
                messagePlayers(Component.text("", NamedTextColor.GRAY)
                        .append(tourneyData.get(i).PLAYER.name())
                        .append(Component.text("("+ tourneyData.get(i).getScore() + ") has the same score as "))
                        .append(tourneyData.get(i - 1).PLAYER.name())
                        .append(Component.text("(" + tourneyData.get(i - 1).getScore() + "), adding to rematch-list.")));
                if (rematchPlayers.isEmpty()) {
                    rematchPlayers.add(tourneyData.get(i - 1).PLAYER);
                }
                rematchPlayers.add(tourneyData.get(i).PLAYER);
            } else if (!rematchPlayers.isEmpty()) {
                messagePlayers(Component.text("", NamedTextColor.GRAY)
                        .append(tourneyData.get(i).PLAYER.name())
                        .append(Component.text("(" + tourneyData.get(i).getScore() + ") didnt have the the same score as "))
                        .append(tourneyData.get(i - 1).PLAYER.name())
                        .append(Component.text("(" + tourneyData.get(i - 1).getScore() + "), those in the rematch list will rematch.")));

                messagePlayers(Component.text("Some players got the same amount of points and will rematch! These players are:", NamedTextColor.GRAY));

                rounds.get(round + 1).add(rematchPlayers);
                for (Player q : rounds.get(round + 1).get(0)) {
                    messagePlayers(q.name().color(NamedTextColor.GRAY));
                }
                messagePlayers(Component.text("Rematch count: " + rounds.get(round + 1).size(), NamedTextColor.GRAY));

                rematchPlayers.clear();
            } else {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.sendMessage(Component.text("", NamedTextColor.GRAY)
                            .append(tourneyData.get(i).PLAYER.name())
                            .append(Component.text("(" + tourneyData.get(i).getScore() + ") has the same score as "))
                            .append(tourneyData.get(i - 1).PLAYER.name())
                            .append(Component.text("(" + tourneyData.get(i - 1).getScore() + "), and nobody are in rematch list.")));
                }
            }
        }

        if (!rematchPlayers.isEmpty()) {
            messagePlayers(Component.text("Some players got the same amount of points and will rematch! These players are:", NamedTextColor.GRAY));
            rounds.get(round + 1).add(rematchPlayers);
            for (Player q : rounds.get(round + 1).get(0)) {
                messagePlayers(q.name().color(NamedTextColor.GRAY));
            }
            messagePlayers(Component.text("Rematch count: " + rounds.get(round + 1).size(), NamedTextColor.GRAY));
        }

        Bukkit.getPlayer("GruveXp").sendMessage(Component.text("rounds.get(round + 1).size() = " + rounds.get(round + 1).size() + " (postTourney), and round number is: " + round));
        startNextTourney(true);
    }

    public static void postGame() { // etter heile turneen er ferdig

        List<Player> playerlistSorted = playerList.stream()
                .sorted(Comparator.comparing(p -> playerScores.get(p)))
                .collect(Collectors.toList());


        for (Player p : playerlistSorted) { // Alle blir telportert til podium
            p.teleport(new Location(Main.WORLD, 35.0, 23.0, -138.0));
        }
        // telporterer top 3 oppå podium
        playerlistSorted.get(playerlistSorted.size() - 1).teleport(new Location(Main.WORLD, 35.5, 27.0, -131.5));
        playerlistSorted.get(playerlistSorted.size() - 2).teleport(new Location(Main.WORLD, 39.5, 26.0, -131.5));
        if (playerList.size() > 2) {
            playerlistSorted.get(playerlistSorted.size() - 3).teleport(new Location(Main.WORLD, 31.5, 25.0, -131.5));
        }

        //printer resultater
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.sendMessage(Component.text("==== Tournament results ====", NamedTextColor.GREEN, TextDecoration.BOLD));
        }
        for (int i = playerList.size() - 1; i >= 0; i--) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.sendMessage(ChatColor.BLUE + "" + (playerList.size() - playerScores.get(playerlistSorted.get(i))) + " " + Board.getScoreString(playerlistSorted.get(i))); // antall i gamet - score = hvilken plass man kom på
            }
        }

        playerList.clear(); // clearer liste med players som er i gamet
        playerPoints.clear();
        rounds.clear();
        playerIDs.clear();
        playerScores.clear();
        Board.reset(); //fjerner det som er på boarden
        SumoManager.activeBattle = 0; // sånn at nye turneer kan startes
        tourney = 0;
        round = 0;
    }

    // ---------------- Other ----------------
    public static void messagePlayers(Component component) {
        playerList.forEach(p -> p.sendMessage(component));
    }

    private static List<PlayerData> getTourneyPlayersSorted() { // gir liste over players i denne turneen sortert etter wins

        ArrayList<Player> currentTourney = rounds.get(round).get(tourney); // liste med playersene i denne turneen
        List<PlayerData> tourneyData = new ArrayList<>(currentTourney.size()); //playerdata av de som er i denne turneen

        for (Player p : currentTourney) { // kun de i pågående turne blir adda
            tourneyData.add(playerPoints.get(round).get(playerIDs.get(p))); // adder PlayerData sånn at man kan sorte det errerpå
        }

        tourneyData = tourneyData.stream()
                .sorted(Comparator.comparing(PlayerData::getWins))
                .collect(Collectors.toList()); //sorterer etter wins

        return tourneyData;
    }

    public static String frame(int frame, String rotation) { //frame 0 er den røde
        Location startPos = new Location(getServer().getWorld("Sumo"), 21, 22, -204);
        Location endPos = new Location(getServer().getWorld("Sumo"), 23, 26, -200);
        String offset = "~-1 ~-1 ~-2";
        switch (rotation) { //east er default og location er start:location
            case "west":
                startPos = startPos.add(6, 0, 0);
                endPos = endPos.add(6, 0, 0);
                break;
            case "south":
                startPos = startPos.add(12, 0, 0);
                endPos = endPos.add(2+12, 0, -2);
                offset = "~-2 ~-1 ~-1";
                break;
            case "north":
                startPos = startPos.add(18, 0, 0);
                endPos = endPos.add(2+18, 0, -2);
                offset = "~-2 ~-1 ~-1";
                break;
        }
        startPos = startPos.add(0, 0, -6*frame);
        endPos = endPos.add(0, 0, -6*frame);
        String start = (int) startPos.getX()+" "+(int) startPos.getY()+" "+(int) startPos.getZ();
        String end = (int) endPos.getX()+" "+(int) endPos.getY()+" "+(int) endPos.getZ();
        return "execute as @e[type=minecraft:armor_stand,name="+rotation+"_spawn] at @s run clone "+start+" "+end+" "+offset;
    }

}
