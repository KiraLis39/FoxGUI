package fox.player;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
public class VolumeConverter {
    private static final float MIN = -80f;
    private static final float MAX = 6f;
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

    /**
     * Использовать ли новый, улучшенный расчет громкости
     * @param useBetterConvert переключатель условия.
     */
    public void setUseBetterConvert(boolean useBetterConvert) {
        this.useBetterConvert = useBetterConvert;
    }

    public float getMinimum() {
        return MIN;
    }

    public float getMaximum() {
        return MAX;
    }
}
