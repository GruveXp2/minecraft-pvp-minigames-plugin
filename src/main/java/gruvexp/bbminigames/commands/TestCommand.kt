package gruvexp.bbminigames.commands

import gruvexp.bbminigames.Main
import gruvexp.bbminigames.Util
import gruvexp.bbminigames.extras.StickSlap
import gruvexp.bbminigames.mechanics.Hatch
import gruvexp.bbminigames.mechanics.RotatingStructure
import gruvexp.bbminigames.model.stat.ResultDisplay
import gruvexp.bbminigames.twtClassic.BotBows
import gruvexp.bbminigames.twtClassic.BotBowsPlayer
import gruvexp.bbminigames.twtClassic.ability.AbilityType
import gruvexp.bbminigames.twtClassic.ability.abilities.ThunderBow
import gruvexp.bbminigames.twtClassic.hazard.HazardChance
import gruvexp.bbminigames.twtClassic.hazard.HazardType
import gruvexp.bbminigames.twtClassic.map.BotBowsMap
import io.papermc.paper.block.BlockPredicate
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.ItemAdventurePredicate
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.*
import org.bukkit.block.BlockFace
import org.bukkit.block.data.Directional
import org.bukkit.block.structure.StructureRotation
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.BlockDisplay
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import kotlin.math.max
import kotlin.math.min

//import gruvexp.bbminigames.ZTesting;
//import gruvexp.bbminigames.model.stat.MatchResult;
class TestCommand : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        val p = sender as? Player ?: Bukkit.getPlayer("GruveXp")

        if (args.isNotEmpty()) {
            when (args[0]) {
                "tde" -> display!!.deathsTab.isExpanded = !display!!.deathsTab.isExpanded
                "tde2" -> display!!.abilityTab.isExpanded = !display!!.abilityTab.isExpanded
                "end_round" -> {
                    val bp = BotBows.getBotBowsPlayer(p)
                    if (bp == null) {
                        p!!.sendMessage(Component.text("You arent even in a game!", NamedTextColor.RED))
                        return true
                    }
                    val lobby = bp.lobby
                    if (!lobby.isGameActive) {
                        p!!.sendMessage(Component.text("Game hasnt started yet!", NamedTextColor.RED))
                        return true
                    }
                    val game = lobby.botBowsGame
                    if (!game!!.activeRound) {
                        p!!.sendMessage(Component.text("No ongoing round to end", NamedTextColor.RED))
                        return true
                    }
                    game.endRoundTimeout()
                }

                "tb" -> {
                    val lobby = BotBows.getLobby(0)
                    lobby.joinGame(Bukkit.getPlayer("GruveXp")!!)
                    lobby.addBot()
                    lobby.startGame(p!!)
                }

                "qk" -> {
                    val lobby = BotBows.getLobby(0)
                    lobby.joinGame(Bukkit.getPlayer("GruveXp")!!)
                    val uuid = lobby.addBot()
                    val botBp = lobby.getBotBowsPlayer(uuid)

                    val settings = lobby.settings
                    val abilitySettings = settings.abilitySettings
                    abilitySettings.maxAbilities = 2
                    abilitySettings.ban(AbilityType.ENDER_PEARL)
                    settings.mapSettings.currentMap = BotBowsMap.ROYAL_CASTLE
                    val gxbp = BotBows.getBotBowsPlayer(Bukkit.getPlayer("GruveXp"))
                    gxbp.equipAbility(AbilityType.BABY_POTION)
                    gxbp.equipAbility(AbilityType.KARMA_POTION)
                    gxbp.equipAbility(AbilityType.RADAR)
                    botBp!!.equipAbility(AbilityType.LASER_TRAP)
                    if (args.size > 1) return true
                    lobby.startGame(p!!)
                }

                "h" -> {
                    val loc = Location(
                        Main.WORLD,
                        args[1].toInt().toDouble(),
                        args[2].toInt().toDouble(),
                        args[3].toInt().toDouble()
                    )
                    val rotation = StructureRotation.valueOf(args[4])
                    hatch = Hatch(7, loc, rotation, "copper_hatch_weathered")
                }

                "ha" -> {
                    hatch!!.toggle()
                }

                "gg" -> {
                    val structure = Bukkit.getStructureManager().loadStructure(NamespacedKey("botbows", "copper_wheel"))
                    if (structure == null) {
                        BotBows.debugMessage("ERROR! Structure \"botbows:copper_wheel\" failed to load") // TODO: apparently pga man måtte legge strukturfilan inn i datapk folder systemer
                        return true
                    }
                    BotBows.debugMessage("it wørk.")
                    return true
                }

                "vote" -> {
                    val bp = BotBows.getBotBowsPlayer(p)
                    val playerName = args[1].replace("_", " ")
                    val votingBp = bp.lobby.getPlayers().stream()
                        .filter { lbp: BotBowsPlayer? -> lbp!!.avatar.getEntity().name == playerName }.findFirst()
                        .orElse(null)
                    if (votingBp == null) {
                        p!!.sendMessage(Component.text("That botbowsplayer doesnt exist.", NamedTextColor.RED))
                        return true
                    }
                    val mapName = args[2].uppercase()
                    val map = BotBowsMap.valueOf(mapName)
                    bp.lobby.settings.mapSettings.mapVotingSession.vote(votingBp, map)
                    BotBows.debugMessage("$playerName voted for $mapName")
                }

                "get_karma" -> {
                    val bp = BotBows.getBotBowsPlayer(p)
                    if (bp == null) {
                        p!!.sendMessage(Component.text("u need 2 join the game first"))
                        return true
                    }
                    bp.applyKarmaDebuff()
                }

                "5_bots" -> {
                    val lobby = BotBows.getLobby(0)
                    if (args.size != 3) {
                        lobby.joinGame(Bukkit.getPlayer("GruveXp")!!)
                        repeat(10) { lobby.addBot() }
                    }
                    val settings = lobby.settings
                    val abilitySettings = settings.abilitySettings
                    abilitySettings.maxAbilities = 3
                    abilitySettings.ban(AbilityType.ENDER_PEARL)
                    abilitySettings.ban(AbilityType.BABY_POTION)
                    abilitySettings.maxAbilities = 3
                    abilitySettings.cooldownMultiplier = 1.25f
                    settings.mapSettings.currentMap = BotBowsMap.ICY_RAVINE
                    settings.winConditionSettings.winScoreThreshold = 67
                    val gxbp = BotBows.getBotBowsPlayer(Bukkit.getPlayer("GruveXp"))
                    gxbp.equipAbility(AbilityType.THUNDER_BOW)
                    gxbp.equipAbility(AbilityType.SPLASH_BOW)
                    gxbp.equipAbility(AbilityType.RADAR)
                    if (args.size > 1) return true
                    lobby.startGame(p!!)
                }

                "add_bot" -> {
                    if (args.size == 1) {
                        BotBows.getLobby(0).addBot()
                    } else {
                        val lobbyId = args[1].toInt()
                        BotBows.getLobby(lobbyId).addBot()
                    }
                }

                "toggle_experimental" -> {
                    val lobby = BotBows.getLobby(p)
                    if (lobby == null) {
                        p!!.sendMessage(Component.text("Go in a lobby and try again"))
                        return true
                    }
                    lobby.settings.usingExperimentalFeatures = !lobby.settings.usingExperimentalFeatures
                    p!!.sendMessage("Exprimental features is now ${if (lobby.settings.usingExperimentalFeatures) "enabled" else "disabled"}")
                }

                "w1" -> {
                    val below = p!!.location.block.getRelative(BlockFace.DOWN)
                    below.type = Material.RED_SHULKER_BOX
                }

                "w2" -> {
                    val below = p!!.location.block.getRelative(BlockFace.DOWN)
                    orientable = below.blockData as Directional
                    below.type = Material.RED_SHULKER_BOX
                    val newo = below.blockData as Directional
                    newo.facing = orientable!!.facing
                    below.blockData = newo
                }

                "w3" -> {
                    val below = p!!.location.block.getRelative(BlockFace.DOWN)
                    below.blockData = orientable!!
                }

                "print_eq" -> {
                    val bp = BotBows.getBotBowsPlayer(p)
                    bp.equippedAbilities.forEach { p!!.sendMessage("a: ${it.displayName}") }
                }

                "add_spinning" -> {
                    val tag = args[1]
                    val s0 = p!!.location.add(-1.0, -1.0, -1.0)
                    val s1 = p.location.add(1.0, -1.0, 1.0)
                    val center = p.location.add(0.0, -1.0, 0.0).toCenterLocation()

                    rotatingStructure = RotatingStructure(center)
                    for (x in s0.blockX..s1.blockX) {
                        for (y in s0.blockY..s1.blockY) {
                            for (z in s0.blockZ..s1.blockZ) {
                                val block = Main.WORLD.getBlockAt(x, y, z)
                                //p.sendMessage(Component.text("Block at " + x + " " + y + " " + z + ": " + block.getType()));
                                if (block.type != Material.AIR) {
                                    val display = Main.WORLD.spawn(
                                        Location(
                                            Main.WORLD,
                                            x.toDouble(),
                                            y.toDouble(),
                                            z.toDouble()
                                        ), BlockDisplay::class.java
                                    )
                                    val blockData = block.blockData
                                    display.block = blockData
                                    display.addScoreboardTag(tag)
                                    rotatingStructure!!.addDisplay(display)
                                    block.type = Material.AIR
                                }
                            }
                        }
                    }
                }

                "init_wheel" -> {
                    val x = args[1].toDouble() + 0.5
                    val y = args[2].toDouble() + 0.5
                    val z = args[3].toDouble() + 0.5
                    val centerLocation = Location(p!!.world, x, y, z)
                    rotatingStructure = RotatingStructure(centerLocation)
                    p.sendMessage(Component.text("Made a weel at that location"))
                }

                "register_blocks" -> {
                    val firstX = args[1].toInt()
                    val firstY = args[2].toInt()
                    val firstZ = args[3].toInt()
                    val secondX = args[4].toInt()
                    val secondY = args[5].toInt()
                    val secondZ = args[6].toInt()

                    val x0 = min(firstX, secondX)
                    val x1 = max(firstX, secondX)
                    val y0 = min(firstY, secondY)
                    val y1 = max(firstY, secondY)
                    val z0 = min(firstZ, secondZ)
                    val z1 = max(firstZ, secondZ)

                    var totalAdded = 0
                    var tag: String? = args[7]
                    if (tag == null) tag = "rotasjon"
                    for (x in x0..x1) {
                        for (y in y0..y1) {
                            for (z in z0..z1) {
                                val block = Main.WORLD.getBlockAt(x, y, z)
                                //p.sendMessage(Component.text("Block at " + x + " " + y + " " + z + ": " + block.getType()));
                                if (block.type != Material.AIR) {
                                    val display = Main.WORLD.spawn(
                                        Location(
                                            Main.WORLD,
                                            x.toDouble(),
                                            y.toDouble(),
                                            z.toDouble()
                                        ), BlockDisplay::class.java
                                    )
                                    val blockData = block.blockData
                                    display.block = blockData
                                    display.addScoreboardTag(tag)
                                    rotatingStructure!!.addDisplay(display)
                                    block.type = Material.AIR
                                    totalAdded++
                                }
                            }
                        }
                    }
                    p!!.sendMessage(Component.text("Registerd a total of $totalAdded blocks"))
                }

                "w" -> {
                    val gruveXp = Bukkit.getPlayer("GruveXp")
                    val judith = Bukkit.getPlayer("SamTheRabbit5")
                    if (judith == null) {
                        p!!.sendMessage(Component.text("Error! Judiths bruker ække inne på serveren! Join med skolepcen"))
                        return true
                    }
                    val lobby = BotBows.getLobby(0)
                    lobby.joinGame(gruveXp!!)
                    lobby.joinGame(judith)
                    lobby.settings.mapSettings.currentMap = BotBowsMap.SPACE_STATION
                    //lobby.settings.getHazards().values().forEach(h -> h.setChance(HazardChance.DISABLED));
                    val gruveBp = lobby.getBotBowsPlayer(gruveXp)
                    val judithBp = lobby.getBotBowsPlayer(judith)
                    gruveBp!!.equipAbility(1, AbilityType.LASER_TRAP)
                    judithBp!!.equipAbility(1, AbilityType.LASER_TRAP)
                    judithBp.setReady(true, 4)
                    Bukkit.getScheduler().runTaskLater(Main.getPlugin(), Runnable { gruveBp.setReady(true, 4) }, 10)
                    Bukkit.getScheduler().runTaskLater(
                        Main.getPlugin(),
                        Runnable { gruveXp.teleport(Location(gruveXp.getWorld(), 150.0, 87.0, 208.0)) },
                        20
                    )


                    //BotBows.getLobby(0).settings.healthMenu.enableCustomHP();
                    //Player judithP = Bukkit.getPlayer("Spionagent54");
                    //BotBowsPlayer judith = BotBows.getLobby(judithP).getBotBowsPlayer(judithP);

                    //judith.setMaxHP(20);
                    //Bukkit.dispatchCommand(Objects.requireNonNull(Bukkit.getPlayer("GruveXp")), "botbows:start");  // tester om dungeonen funker
                }

                "s" -> {
                    val item1 = ItemStack(Material.STONE)
                    //item1.setData(DataComponentTypes.CAN_PLACE_ON, ItemAdventurePredicate.itemAdventurePredicate().addPredicate(B).build());
                    val meta = item1.itemMeta
                    //meta.setCanPlaceOn(Set.of());
                    item1.setItemMeta(meta)
                    val item2 = ItemStack(Material.COBBLED_DEEPSLATE)

                    item2.setData<ItemAdventurePredicate?>(
                        DataComponentTypes.CAN_PLACE_ON,
                        ItemAdventurePredicate.itemAdventurePredicate().addPredicate(BlockPredicate.predicate().build())
                    )
                    Bukkit.getPlayer("GruveXp")!!.give(item1, item2)
                }

                "q" -> {
                    val gruveXp = Bukkit.getPlayer("GruveXp")
                    val judith = Bukkit.getPlayer("SamTheRabbit5")

                    val lobby = BotBows.getLobby(0)
                    lobby.settings.hazardSettings.setChance(HazardType.STORM, HazardChance.DISABLED)
                    lobby.joinGame(gruveXp!!)
                    lobby.joinGame(judith!!)
                    val judithBp = lobby.getBotBowsPlayer(judith)
                    judithBp!!.setReady(true, 4)
                    val gruveBp = lobby.getBotBowsPlayer(gruveXp)
                    gruveBp!!.equipAbility(AbilityType.CREEPER_TRAP)
                    gruveBp.equipAbility(AbilityType.LASER_TRAP)
                }

                "a" -> {
                    rotation = !rotation
                    BotBows.debugMessage("New location logic set to: $rotation")
                }

                "b" -> {
                    verboseDebugging = !verboseDebugging
                    BotBows.debugMessage("Verbose debugging set to: $verboseDebugging")
                }

                "db" -> Main.getPlugin().statsService.printOutInfo()
                "c" -> {
                    var playerName: String? = args[1]
                    if (playerName == null) playerName = "GruveXp"
                    val team = BotBows.getLobby(Bukkit.getPlayer(playerName))
                        .getBotBowsPlayer(Bukkit.getPlayer(playerName)!!)!!.team
                    BotBows.debugMessage("The team of $playerName is ${team.displayName}")
                }

                "toggle_debugging" -> {
                    debugging = !debugging
                    BotBows.debugMessage("Debugging set to: $debugging")
                }

                "t" -> {
                    BotBows.debugMessage("Team1: ${BotBows.getLobby(0).settings.team1.displayName}")
                    BotBows.debugMessage("Team2: ${BotBows.getLobby(0).settings.team2.displayName}")
                }

                "t1" -> {
                    test1 = !test1
                    BotBows.debugMessage("Test1 set to: $test1")
                }

                "t2" -> {
                    test2 = !test2
                    BotBows.debugMessage("Test2 set to: $test2")
                }

                "t3" -> {
                    test3 = !test3
                    BotBows.debugMessage("Test3 set to: $test3")
                }

                "ta", "d" -> {
                    testAbilities = !testAbilities
                    BotBows.debugMessage("testAbilities set to: $testAbilities")
                }

                "give_ability_items" -> {
                    for (type in AbilityType.entries) {
                        p!!.inventory.addItem(type.abilityItem)
                    }
                }

                "test_arc" -> {
                    if (args.size < 7) {
                        sender.sendMessage("Not enough args (need 8)")
                    }
                    val loc1 = Util.toLocation(Main.WORLD, args[1], args[2], args[3])
                    val loc2 = Util.toLocation(Main.WORLD, args[4], args[5], args[6])
                    val strong = args.size > 7 && args[7] == "strong"
                    ThunderBow.createElectricArc(loc1, loc2, Color.RED, 1.0, strong)
                }

                "inv" -> p!!.openInventory(testInv)
                "set_blaze_rod_cooldown" -> StickSlap.cooldown = args[1].toInt()
                else -> sender.sendMessage("Wrong arg (${args[0]})")
            }
            return true
        } else {
//            BotBowsPlayer bp = BotBows.getBotBowsPlayer(p);
//            if (bp == null) {
//                p.sendMessage(Component.text("You must be in a lobby to test", NamedTextColor.RED));
//                return true;
//            }
//            testAbilities(bp);
            testYellow()
        }
        //BotBowsManager.debugMessage(STR."\{p.getName()}is \{isInDungeon(p) ? "" : "not"} in a dungeon\{BotBowsManager.isInDungeon(p) ? STR.", section\{BotBowsManager.getSection(p)}" : ""}");
        return true
    }

    private fun testYellow() {
        val lobby = BotBows.getLobby(0)
        lobby.joinGame(Bukkit.getPlayer("GruveXp")!!)
        val gruveBp = lobby.getBotBowsPlayer(Bukkit.getPlayer("GruveXp")!!)
        val abilitySettings = lobby.settings.abilitySettings
        abilitySettings.maxAbilities = 2
        abilitySettings.isUniqueMode = true
        repeat(4) {
            val id = lobby.addBot()
            val bp = BotBows.getBotBowsPlayer(id)
            bp!!.equipAbility(AbilityType.ENDER_PEARL)
            assertThat(bp.hasAbilityEquipped(AbilityType.ENDER_PEARL), "bot has enderpearl", gruveBp!!)
        }
    }

    private fun testHazards(bp: BotBowsPlayer) {
        val settings = bp.lobby.settings
        settings.hazardSettings.setChance(HazardType.STORM, HazardChance.ALWAYS)
        assertThat(settings.hazardSettings.getChance(HazardType.STORM) == HazardChance.ALWAYS, "setup storm chance", bp)

        val preset = Main.getPlugin().presetService.getPreset("test")
        assertThat(preset != null, "preset exists", bp)
        checkNotNull(preset)
        assertThat(preset.hazards[HazardType.STORM] == HazardChance.FIFTY, "preset has 50% storm", bp)

        settings.applyBattlePreset(preset)
        assertThat(
            settings.hazardSettings.getChance(HazardType.STORM) == HazardChance.FIFTY,
            "preset applied, has 50% storm now",
            bp
        )
    }

    private fun testAbilities(bp: BotBowsPlayer) {
        val settings = bp.lobby.settings
        val abilitySettings = settings.abilitySettings
        abilitySettings.unban(AbilityType.ENDER_PEARL)
        assertThat(!abilitySettings.isBanned(AbilityType.ENDER_PEARL), "enderpearl initially allowed", bp)

        val preset = Main.getPlugin().presetService.getPreset("test")
        assertThat(preset != null, "preset exists", bp)
        checkNotNull(preset)
        assertThat(preset.abilities.bannedAbilities != null, "some abilities are banned", bp)
        assertThat(preset.abilities.bannedAbilities!!.contains(AbilityType.ENDER_PEARL), "preset has banned pearl", bp)

        settings.applyBattlePreset(preset)
        assertThat(abilitySettings.isBanned(AbilityType.ENDER_PEARL), "enderpearl banned", bp)
    }

    private fun assertThat(condition: Boolean, message: String, tester: BotBowsPlayer) {
        if (condition) {
            tester.avatar.message(Component.text("✔ PASS: ", NamedTextColor.GREEN).append(Component.text(message)))
        } else {
            tester.avatar.message(Component.text("✘ FAIL: ", NamedTextColor.RED).append(Component.text(message)))
        }
    }

    companion object {
        @JvmField
        var rotation: Boolean = true
        @JvmField
        var verboseDebugging: Boolean = false
        @JvmField
        var debugging: Boolean = true
        var test1: Boolean = false
        var test2: Boolean = false
        var test3: Boolean = false
        @JvmField
        var testAbilities: Boolean = false
        var rotatingStructure: RotatingStructure? = null
        var testInv: Inventory = Bukkit.createInventory(null, 54, Component.text("Lagre-Chest"))
        private var hatch: Hatch? = null

        var orientable: Directional? = null
        var display: ResultDisplay? = null
    }
}
