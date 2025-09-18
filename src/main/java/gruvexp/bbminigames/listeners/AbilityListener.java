package gruvexp.bbminigames.listeners;

import gruvexp.bbminigames.Main;
import gruvexp.bbminigames.commands.TestCommand;
import gruvexp.bbminigames.twtClassic.BotBows;
import gruvexp.bbminigames.twtClassic.BotBowsPlayer;
import gruvexp.bbminigames.twtClassic.Lobby;
import gruvexp.bbminigames.twtClassic.ability.AbilityCategory;
import gruvexp.bbminigames.twtClassic.ability.AbilityType;
import gruvexp.bbminigames.twtClassic.ability.PotionAbility;
import gruvexp.bbminigames.twtClassic.ability.abilities.*;
import gruvexp.bbminigames.twtClassic.hazard.HazardType;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;

public class AbilityListener implements Listener {

    public static HashMap<Arrow, BukkitTask> thunderArrows = new HashMap<>();
    public static HashMap<Arrow, BukkitTask> splashArrows = new HashMap<>();

    public static void onAbilityUse(PlayerInteractEvent e) {
        if (e.getItem() == null) return;
        Player p = e.getPlayer();
        Lobby lobby = BotBows.getLobby(p);
        if (lobby == null) return;
        BotBowsPlayer bp = lobby.getBotBowsPlayer(p);
        ItemStack abilityItem = e.getItem();
        AbilityType type = AbilityType.fromItem(abilityItem);
        if (type == null) return;
        if (!lobby.isGameActive() && !TestCommand.testAbilities) {
            e.setCancelled(true); // kanke bruke abilities i lobbyen
            return;
        }
        switch (type) {
            case ENDER_PEARL, RADAR, THUNDER_BOW, SALMON_SLAP, LINGERING_POTION -> bp.getAbility(type).use();
            case BUBBLE_JET -> {
                p.resetPlayerWeather();
                p.getInventory().getItemInMainHand().addEnchantment(Enchantment.RIPTIDE, 3);
                Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> {
                    if (!bp.lobby.settings.getHazard(HazardType.STORM).isActive()) {
                        p.setPlayerWeather(WeatherType.CLEAR);
                    } else {
                        p.resetPlayerWeather();
                    }
                }, 60L);
            }
            case CREEPER_TRAP -> {
                Block clickedBlock = e.getClickedBlock();
                if (clickedBlock == null) return;
                BlockFace face = e.getBlockFace();
                Block spawnBlock = clickedBlock.getRelative(face);
                if (spawnBlock.getRelative(BlockFace.UP).getType().isSolid()) return;
                while (!spawnBlock.getRelative(BlockFace.DOWN).getType().isSolid()) {
                    spawnBlock = spawnBlock.getRelative(BlockFace.DOWN);
                }
                Location placeLoc = spawnBlock.getLocation().add(0.5, 0, 0.5);

                CreeperTrap creeperTrap = (CreeperTrap) bp.getAbility(type);
                creeperTrap.use(placeLoc);
            }
            case LASER_TRAP -> {
                Block clickedBlock = e.getClickedBlock();
                if (clickedBlock == null) return;
                BlockFace face = e.getBlockFace();
                Block spawnBlock = clickedBlock.getRelative(face);
                if (spawnBlock.getType() != Material.AIR) {
                    e.setCancelled(true);
                    return;
                };
                LaserTrap laserAbility = (LaserTrap) bp.getAbility(AbilityType.LASER_TRAP);

                Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> laserAbility.use(spawnBlock, face), 1);
            }
            default -> {
                if (type.category == AbilityCategory.POTION) {
                    int particleCount = 200;
                    int radius = PotionAbility.RADIUS;
                    Location loc = p.getLocation().add(0, 0.1, 0);
                    double y = loc.getY();
                    for (int i = 0; i < particleCount; i++) {
                        double θ = 2 * Math.PI * i / particleCount;
                        double x = loc.getX() + radius * Math.cos(θ);
                        double z = loc.getZ() + radius * Math.sin(θ);

                        Location particleLoc = new Location(loc.getWorld(), x, y, z);
                        p.getWorld().spawnParticle(Particle.DUST, particleLoc, 1, 0, 0, 0, 0.4,
                                new Particle.DustOptions(bp.getTeam().dyeColor.getColor(), 2.5f));
                    }
                }
            }
        }
    }

    public static void onSlap(EntityDamageByEntityEvent e, Player attacker, Player defender, ItemStack weapon) {
        Lobby lobby = BotBows.getLobby(attacker);
        if (lobby == null) return;
        BotBowsPlayer attackerBp = lobby.getBotBowsPlayer(attacker);
        BotBowsPlayer defenderBp = lobby.getBotBowsPlayer(defender);
        if (defenderBp == null) return;
        AbilityType type = AbilityType.fromItem(weapon);
        if (attackerBp.getTeam() == defenderBp.getTeam()) {
            e.setCancelled(true);
            return;
        }
        if (type == AbilityType.LONG_ARMS) {
            attackerBp.getAbility(AbilityType.LONG_ARMS).use();
            defenderBp.handleHit(Component.text(" was long-slapped by "), attackerBp);
        } else if (weapon.getType() == Material.SALMON) {
            defenderBp.handleHit(Component.text(" was slapped by "), attackerBp);
        }
        e.setCancelled(true);
    }

    @EventHandler
    public void onProjectileLaunch(ProjectileLaunchEvent e) {
        if (!(e.getEntity().getShooter() instanceof Player p)) return;
        Lobby lobby = BotBows.getLobby(p);
        if (lobby == null) return;
        BotBowsPlayer bp = lobby.getBotBowsPlayer(p);
        if (e.getEntity() instanceof Arrow arrow) {
            if (p.getInventory().getItemInMainHand().getType() == Material.BOW) {
                if (bp.hasAbilityEquipped(AbilityType.SPLASH_BOW)) {
                    arrow.setColor(Color.RED);
                    BukkitTask arrowTrail = new SplashBow.SplashArrowTrailGenerator(arrow, bp.getTeam().dyeColor.getColor())
                            .runTaskTimer(Main.getPlugin(), 1L, 1L);
                    arrow.getVelocity().multiply(0.5f);
                    splashArrows.put(arrow, arrowTrail);
                    bp.getAbility(AbilityType.SPLASH_BOW).use();
                }
            } else if (p.getInventory().getItemInMainHand().getType() == Material.CROSSBOW) {
                arrow.setGravity(false);
                 if (bp.hasAbilityEquipped(AbilityType.THUNDER_BOW)
                         && ((ThunderBow) bp.getAbility(AbilityType.THUNDER_BOW)).isActive()) {
                    arrow.setColor(Color.AQUA);
                    BukkitTask arrowTrail = new ThunderBow.ThunderArrowTrailGenerator(arrow, bp.getTeam().dyeColor.getColor())
                            .runTaskTimer(Main.getPlugin(), 1L, 1L);
                    thunderArrows.put(arrow, arrowTrail);
                    BotBows.debugMessage("Spawning a thunder arrow", TestCommand.test2);
                }
            }
        } else if (e.getEntity() instanceof ThrownPotion potion) {
            if (potion.getItem().getType() == Material.LINGERING_POTION) {
                LingeringPotionTrap.giveRandomEffect(potion);
            }
        }
    }

    @EventHandler
    public void onPotionAbilityUse(PlayerItemConsumeEvent e) {
        Player p = e.getPlayer();
        Lobby lobby = BotBows.getLobby(p);
        if (lobby == null) return;
        BotBowsPlayer bp = lobby.getBotBowsPlayer(p);
        AbilityType type = AbilityType.fromItem(e.getItem());
        if (type == null) return;
        if (!lobby.isGameActive()) {
            e.setCancelled(true); // kanke bruke abilities i lobbyen
            return;
        }
        switch (type) {
            case INVIS_POTION, BABY_POTION, CHARGE_POTION, KARMA_POTION -> bp.getAbility(type).use();
        }
    }

    @EventHandler
    public void onPotionSplash(LingeringPotionSplashEvent e) {
        ThrownPotion potion = e.getEntity();
        if (!(potion.getShooter() instanceof Player thrower)) return;

        Lobby lobby = BotBows.getLobby(thrower);
        if (lobby == null) return;
        BotBowsPlayer throwerBp = lobby.getBotBowsPlayer(thrower);

        boolean hasUnluck = potion.getEffects().stream()
                .anyMatch(effect -> effect.getType() == PotionEffectType.UNLUCK);

        if (!hasUnluck) return;

        LingeringPotionTrap ability = (LingeringPotionTrap) throwerBp.getAbility(AbilityType.LINGERING_POTION);
        ability.addSizeIncreaseAreaEffect(potion.getLocation());
    }

    @EventHandler
    public void onAbilityDrop(PlayerDropItemEvent e) {
        Player p = e.getPlayer();
        Lobby lobby = BotBows.getLobby(p);
        if (lobby == null) return;
        BotBowsPlayer bp = lobby.getBotBowsPlayer(p);
        AbilityType type = AbilityType.fromItem(e.getItemDrop().getItemStack());
        if (type == null) return;
        if (!bp.hasAbilityEquipped(type)) return; // kan droppe itemet hvis det ikke var equippa
        e.setCancelled(true); // kanke droppe ability items
    }

    @EventHandler
    public void onArrowHit(ProjectileHitEvent e) {
        Projectile projectile = e.getEntity();
        if (!(projectile instanceof Arrow arrow)) return;
        if (!(arrow.getShooter() instanceof Player attacker)) {return;} // den som skøyt
        if (splashArrows.containsKey(arrow)) {
            Location hitLoc;
            if (e.getHitEntity() != null) {
                hitLoc = e.getHitEntity().getLocation();
            } else {
                hitLoc = e.getHitBlock().getLocation();
            }
            SplashBow.handleArrowHit(attacker, hitLoc);
            splashArrows.get(arrow).cancel();
            splashArrows.remove(arrow);
            arrow.remove();
        } else if (thunderArrows.containsKey(arrow)) {
            if (e.getHitEntity() != null) return; // det handles på et ant sted
            Location hitLoc = e.getHitBlock().getLocation();
            ThunderBow.handleArrowHitBlock(hitLoc);
            thunderArrows.get(arrow).cancel();
            thunderArrows.remove(arrow);
            arrow.remove();
        }
    }

    @EventHandler
    public void onSwing(PlayerAnimationEvent e) { // left clicking but not hitting anything
        Player p = e.getPlayer();
        Lobby lobby = BotBows.getLobby(p);
        if (lobby == null) return;
        BotBowsPlayer bp = lobby.getBotBowsPlayer(p);
        AbilityType type = AbilityType.fromItem(p.getInventory().getItemInMainHand());
        if (type == null) return;
        if (lobby.isGameActive() && type == AbilityType.LONG_ARMS) {
            bp.getAbility(AbilityType.LONG_ARMS).use();
        }
    }

    @EventHandler
    public void onRiptide(PlayerRiptideEvent e) {
        Player attacker = e.getPlayer();
        Lobby lobby = BotBows.getLobby(attacker);
        if (lobby == null) return;
        BotBowsPlayer attackerBp = lobby.getBotBowsPlayer(attacker);

        if (!attackerBp.hasAbilityEquipped(AbilityType.BUBBLE_JET)) return;
        Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> {
            if (!attackerBp.lobby.settings.getHazard(HazardType.STORM).isActive()) {
                attacker.setPlayerWeather(WeatherType.CLEAR);
            } else {
                attacker.resetPlayerWeather();
            }
        }, 10L);
        attackerBp.getAbility(AbilityType.BUBBLE_JET).use();
    }
}
