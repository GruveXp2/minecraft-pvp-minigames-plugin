package gruvexp.bbminigames.twtClassic.avatar;

public interface BotBowsAvatar {
    void eliminate();
    void revive();
    void setHP(int hp);
    void setMaxHP(int maxHP);
    void remove(); // removes the player from the game
    void reset(); // its like removing and recreating this avatar, but reusing the object
    void readyBattle();
}
