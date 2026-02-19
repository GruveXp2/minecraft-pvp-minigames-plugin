package gruvexp.bbminigames.twtClassic;

import gruvexp.bbminigames.Main;
import gruvexp.bbminigames.commands.TestCommand;
import gruvexp.bbminigames.twtClassic.avatar.BotBowsAvatar;
import org.bukkit.scheduler.BukkitRunnable;

public class SneakManager {
    public final int CROUCH_LIMIT = 10; // sec
    private final BotBowsAvatar avatar;
    private SneakCooldown sneakCooldown;

    public SneakManager(BotBowsAvatar avatar) {
        this.avatar = avatar;
        sneakCooldown = new SneakCooldown();
        sneakCooldown.runTaskTimer(Main.getPlugin(), 0L, 1L);
    }

    public boolean isSneakingExhausted() {
        return sneakCooldown.isExhausted;
    }

    public void destroy() {
        sneakCooldown.cancel();
        sneakCooldown = null;
        avatar.updateSneakStamina(0);
    }

    private class SneakCooldown extends BukkitRunnable {

        int time = 20;
        boolean isExhausted = false;

        @Override
        public void run() {
            BotBows.debugMessage("\n========", TestCommand.test3);
            if (avatar.isSneaking()) {
                BotBows.debugMessage("sneaking", TestCommand.test3);
                if (time < CROUCH_LIMIT * 20 - 1) {
                    time += 2;
                    float progress = (float) time /(CROUCH_LIMIT * 20);
                    avatar.updateSneakStamina(progress);
                } else {
                    avatar.updateSneakStamina(1);
                    isExhausted = true;
                }
            } else if (time == 0) {
                BotBows.debugMessage("not sneaking, t=0", TestCommand.test3);
                isExhausted = false;
                avatar.updateSneakStamina(0);
            } else {
                BotBows.debugMessage("not sneaking, t>0", TestCommand.test3);
                time -= 1;
                float progress = (float) time /(CROUCH_LIMIT * 20);
                avatar.updateSneakStamina(progress);
            }
        }
    }
}
