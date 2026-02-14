package gruvexp.bbminigames.listeners;

import gruvexp.bbminigames.Main;
import gruvexp.bbminigames.api.ability.AbilityContext;
import gruvexp.bbminigames.api.ability.AbilityTrigger;
import gruvexp.bbminigames.commands.TestCommand;
import gruvexp.bbminigames.twtClassic.BotBows;
import gruvexp.bbminigames.twtClassic.BotBowsPlayer;
import gruvexp.bbminigames.twtClassic.Lobby;
import gruvexp.bbminigames.twtClassic.ability.Ability;
import gruvexp.bbminigames.twtClassic.ability.AbilityCategory;
import gruvexp.bbminigames.twtClassic.ability.AbilityType;
import gruvexp.bbminigames.twtClassic.ability.PotionAbility;
import gruvexp.bbminigames.twtClassic.ability.abilities.*;
import gruvexp.bbminigames.twtClassic.hazard.HazardType;
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

public class AbilityListener implements Listener {

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

                ((CreeperTrap) bp.getAbility(type)).trigger(new AbilityContext.EntityPlace(placeLoc));
            }
            case LASER_TRAP -> {
                Block clickedBlock = e.getClickedBlock();
                if (clickedBlock == null) return;
                BlockFace face = e.getBlockFace();
                Block spawnBlock = clickedBlock.getRelative(face);
                if (spawnBlock.getType() != Material.AIR) {
                    e.setCancelled(true);
                    return;
                }
                ((LaserTrap) bp.getAbility(type)).onPlace(new AbilityContext.BlockPlace(spawnBlock, face));
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
        if (attackerBp.getTeam() == defenderBp.getTeam()) {
            e.setCancelled(true);
            return;
        }
        AbilityType type = AbilityType.fromItem(weapon);
        Ability ability = attackerBp.getAbility(type);
        if (ability instanceof AbilityTrigger.OnMelee meleeAbility) {
            meleeAbility.trigger(new AbilityContext.Melee(defenderBp));
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
            ItemStack itemInMainHand = p.getInventory().getItemInMainHand();
            if (AbilityType.fromItem(itemInMainHand) == AbilityType.SPLASH_BOW) {
                ((SplashBow) bp.getAbility(AbilityType.SPLASH_BOW)).onLaunch(new AbilityContext.Launch(arrow));
            } else if (itemInMainHand.getType() == Material.CROSSBOW) {
                arrow.setGravity(false);
                 if (bp.hasAbilityEquipped(AbilityType.THUNDER_BOW)
                         && ((ThunderBow) bp.getAbility(AbilityType.THUNDER_BOW)).isActive()) {
                     ((ThunderBow) bp.getAbility(AbilityType.THUNDER_BOW)).onLaunch(new AbilityContext.Launch(arrow));
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
        if (!(arrow.getShooter() instanceof Player)) {return;} // den som skøyt

        if (projectile.hasMetadata("botbows_ability")) {
            Object value = projectile.getMetadata("botbows_ability").getFirst().value();

            if (value instanceof AbilityTrigger.OnProjectileHit handler) {
                handler.onHit(e);
            }
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
