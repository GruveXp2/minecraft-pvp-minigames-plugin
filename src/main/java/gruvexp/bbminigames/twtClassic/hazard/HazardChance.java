package gruvexp.bbminigames.twtClassic.hazard;

import java.util.Random;

public enum HazardChance {

    ALWAYS(100),    // 100% sjangs
    FIFTY(50),      // 50% sjangs
    TWENTY_FIVE(25),// 25% sjangs
    TEN(10),        // 10% sjangs
    FIVE(5),        // 5% sjangs
    DISABLED(0);

    private final int percent;
    private static final Random RANDOM = new Random();

    HazardChance(int percent) {
        this.percent = percent;
    }

    // Henter prosentverdien
    public int getPercent() {
        return percent;
    }

    // Sjekker om sjangsen slår til
    public boolean occurs() {
        return RANDOM.nextInt(100) < percent;
    }
}
