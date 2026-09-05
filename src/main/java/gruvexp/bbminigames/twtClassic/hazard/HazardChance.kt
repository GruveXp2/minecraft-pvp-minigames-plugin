package gruvexp.bbminigames.twtClassic.hazard;

import gruvexp.bbminigames.twtClassic.BotBows;

import java.util.List;

public enum HazardChance {

    ALWAYS(100, "ALWAYS"),    // 100% sjangs
    FIFTY(50, "50%"),      // 50% sjangs
    TWENTY_FIVE(25, "25%"),// 25% sjangs
    TEN(10, "10%"),        // 10% sjangs
    FIVE(5, "5%"),        // 5% sjangs
    DISABLED(0, "DISABLED");

    private final int percent;
    private final String chanceString;

    HazardChance(int percent, String chanceString) {
        this.percent = percent;
        this.chanceString = chanceString;
    }

    public int getPercent() {
        return percent;
    }

    @Override
    public String toString() {
        return chanceString;
    }

    // Sjekker om sjangsen slår til
    public boolean occurs() {
        return BotBows.RANDOM.nextInt(100) < percent;
    }

    public static List<String> getPercentStrings() {
        return List.of("5%", "10%", "25%", "50%", "ALWAYS");
    }

    public static HazardChance of(String chanceString) {
        for (HazardChance chance : HazardChance.values()) {
            if (chance.chanceString.equalsIgnoreCase(chanceString)) {
                return chance;
            }
        }
        throw new IllegalArgumentException("Invalid HazardChance string: " + chanceString);
    }
}
