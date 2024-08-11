package fox.games;


public class FoxExperience {

    private FoxExperience() {
    }

    /**
     * Метод расчета экспириенса.
     *
     * @param playerLVL уровень игрока.
     * @param aimLVL    уровень уничтожаемой цели.
     * @param mod       модификатор опыта.
     * @param space     допустимая разница в уровнях, когда может падать опыт.
     * @return экспириенс, что должен получить player за уничтожение aim.
     */
    public static double getExp(float playerLVL, float aimLVL, float mod, float space) {
        return Math.max(0, mod * (space + aimLVL - playerLVL) / (space + playerLVL));
    }
}
