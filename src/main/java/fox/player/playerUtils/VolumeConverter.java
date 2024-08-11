package fox.player.playerUtils;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Setter
@Getter
public class VolumeConverter {
    private static float MIN = -80f;
    private static float MAX = 6f;
    /**
     * -- SETTER --
     * Использовать ли новый, улучшенный расчет громкости
     */
    private boolean useBetterConvert = true;

    /**
     * Метод преобразовывает значения процентов громкости в
     * gain для аудио-устройств.
     *
     * @param percent текущий процент громкости (от 0 до 100)
     * @return gain аудио-устройства (от {@param minimum} до {@param maximum}).
     */
    public float volumePercentToGain(int percent) {
        float gain;

        if (useBetterConvert) {
            float cor = Math.abs(MIN) + Math.abs(MAX);
            gain = MIN + (cor / 100f) * percent;
        } else {
            gain = MIN - (MIN - MAX) * (percent / 100f);
        }

        log.debug("Income percent: " + percent + "; Gain: " + gain);
        return gain;
    }

    /**
     * Метод преобразовывает gain аудио-устройства в
     * значение процентов громкости для ползунков.
     *
     * @param gain текущий гейн аудио-устройства (от {@param minimum} до {@param maximum})
     * @return значение процентов (от 0 до 100).
     */
    public int gainToVolumePercent(float gain) {
        float cor = Math.abs(MIN) + Math.abs(MAX);
        float del = cor / 100f;
        return Math.round((gain - MIN) / del);
    }

    public float getMinimum() {
        return MIN;
    }

    public float getMaximum() {
        return MAX;
    }

    public void setMinimum(float min) {
        if (min > 6) {
            log.warn("Минимальная указанная громкость ({}) выше возможной максимальной (6), будет инвертирована на отрицательную.", min);
            min = -min;
        }
        MIN = min;
    }

    public void setMaximum(float max) {
        if (max > 6) {
            log.warn("Максимальная указанная громкость ({}) выше возможной максимальной (6), будет ограничена ей.", max);
            max = 6;
        }
        MAX = max;
    }
}
