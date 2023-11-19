package fox.player.playerUtils;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.BooleanControl;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.SourceDataLine;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;

import static fox.player.FoxPlayer.getVolumeConverter;

@Slf4j
public class PlayThread extends Thread {
    private static final int audioBufDim = 8192; // default 4096
    private final Thread currentThread;
    private final File track;
    private final boolean isLooped;
    private final float volume;
    private Thread vfThread;
    private FloatControl masterVolume;
    private BooleanControl muteControl;
    private Exception ex;
    private volatile boolean isBroken = false;
    private volatile boolean isLoopFloatedAlready = false;
    private boolean useExperimentalQualityFormat = false;
    private boolean useUnsignedFormat = false;

    public PlayThread(@NonNull String name, File track, float volume, boolean isLooped) {
        setName(name);
        currentThread = this;

        this.track = track;
        this.volume = volume;
        this.isLooped = isLooped;

        start();
    }

    @Override
    public void run() {
        log.debug("FoxPlayer.play: The '" + track.getName() + "' is played...");

        SourceDataLine line = null;
        do {
            try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(track)); // default 8192 byte
                 AudioInputStream in = AudioSystem.getAudioInputStream(bis)
            ) {
                if (in == null) {
                    throw new NullPointerException("The track '" + track.getName() + "' has problem with input stream?..");
                }

                AudioFormat targetFormat = useExperimentalQualityFormat ?
                        (useUnsignedFormat ? new UnsignedFormat24(in.getFormat()) : new DefaultFormat24(in.getFormat()))
                        : new DefaultFormat16(in.getFormat());
                try (AudioInputStream dataIn = AudioSystem.getAudioInputStream(targetFormat, in)) {
                    DataLine.Info info = new DataLine.Info(SourceDataLine.class, targetFormat); // get a line from a mixer in the system with the wanted format
                    line = (SourceDataLine) AudioSystem.getLine(info);
                    if (line == null) {
                        throw new NullPointerException("The track line is null. " +
                                "A problem with info or format?\n\t(target:\n" + info + ";\n\tformat:\n" + targetFormat + ").");
                    } else {
                        line.open();
                    }
                    getControls(line, masterVolume == null ? volume : masterVolume.getValue());
                    line.start();

                    byte[] buffer = new byte[audioBufDim];
                    if (!isBroken && !currentThread.isInterrupted() && !isInterrupted()) {
                        if (!isLoopFloatedAlready) {
                            volumeFloater(1);
                        }

                        int nBytesRead;
                        while ((nBytesRead = dataIn.read(buffer, 0, buffer.length)) != -1) {
                            if (isBroken() || currentThread.isInterrupted() || isInterrupted()) {
                                break;
                            }
                            try {
                                line.write(buffer, 0, nBytesRead);
                            } catch (IllegalArgumentException iae) {
                                log.error("Line write exception: {}", iae.getMessage());
                                interrupt();
                            }
                        }
                    }
                }
            } catch (Exception e) {
                log.error("Play thread exception: {}", e.getMessage());
                ex = e;
                interrupt();
                currentThread.interrupt();
            } finally {
                if (line != null) {
//                    line.drain();
                    line.stop();
                    line.close();
                }
            }
        } while (isLooped && !isInterrupted());
    }

    private void volumeFloater(int vector) {
        if (vfThread != null && vfThread.isAlive()) {
            vfThread.interrupt();
        }
        float vfStep = 0.25f;
        float aimVolume;
        if (vector == 1) {
            aimVolume = masterVolume.getValue();
        } else {
            aimVolume = getVolumeConverter().getMinimum();
        }

        vfThread = new Thread(() -> {
            // setDaemon(true);
            if (vector == 1) {
                // set half of the gain volume for start:
                masterVolume.setValue(getVolumeConverter().getMinimum() / 2);
            }

            try {
                if (vector == 1) {
                    while (masterVolume.getValue() < aimVolume - 1) {
                        masterVolume.setValue(masterVolume.getValue() + vfStep);
                        sleep(50);
                    }
                    masterVolume.setValue(aimVolume);
                } else {
                    while (masterVolume.getValue() > aimVolume + 1) {
                        masterVolume.setValue(masterVolume.getValue() - vfStep);
                        sleep(20);
                    }
                    masterVolume.setValue(aimVolume);
                }
            } catch (InterruptedException e) {
                log.debug("Volume float thread exception: {}", e.getMessage());
                //currentThread().interrupt();
            } finally {
                isLoopFloatedAlready = true;
            }
        });
        vfThread.start();
    }

    private boolean isBroken() {
        return isBroken;
    }

    public void close() {
        new Thread(() -> {
            try {
                volumeFloater(0);
                vfThread.join();
                isBroken = true;
                interrupt();
                currentThread.interrupt();
            } catch (InterruptedException e) {
                log.error("Play thread exception: {}", e.getMessage());
                interrupt();
            }
        }).start();
    }

    public Throwable getException() {
        return ex;
    }

    public void mute(boolean isMuted) {
        muteControl.setValue(isMuted);
    }

    /**
     * Set a volume gain.
     *
     * @param gain - gain of current FloatControl.
     */
    public void setVolume(float gain) {
        log.debug("Setting up the volume from {} to {}", masterVolume.getValue(), gain);
        masterVolume.setValue(gain);
    }

    private void getControls(SourceDataLine line, float volume) {
        muteControl = (BooleanControl) line.getControl(BooleanControl.Type.MUTE);
        muteControl.setValue(false);

        masterVolume = (FloatControl) line.getControl(FloatControl.Type.MASTER_GAIN);
        masterVolume.setValue(volume);
    }

    /**
     * Использовать ли новый, экспериментальный функционал при воспроизведении.
     * К примеру - более качественный, но ещё не протестированный как следует формат аудио и т.п.
     *
     * @param useExperimentalQualityFormat - переключатель экспериментального функционала.
     */
    public void setUseExperimentalQualityFormat(boolean useExperimentalQualityFormat) {
        this.useExperimentalQualityFormat = useExperimentalQualityFormat;
    }

    public void setUseUnsignedFormat(boolean useUnsignedFormat) {
        this.useUnsignedFormat = useUnsignedFormat;
    }

    private static class DefaultFormat16 extends AudioFormat {
        public DefaultFormat16(AudioFormat baseFormat) {
            super(Encoding.PCM_SIGNED, baseFormat.getSampleRate(),
                    16,
                    baseFormat.getChannels(),
                    baseFormat.getChannels() * 2,
                    baseFormat.getSampleRate(),
                    false);
        }
    }

    private static class DefaultFormat24 extends AudioFormat {
        public DefaultFormat24(AudioFormat baseFormat) {
            super(Encoding.PCM_SIGNED, baseFormat.getSampleRate(),
                    24,
                    baseFormat.getChannels(),
                    baseFormat.getChannels() * 2,
                    baseFormat.getSampleRate(),
                    false);
        }
    }

    private static class UnsignedFormat24 extends AudioFormat {
        public UnsignedFormat24(AudioFormat baseFormat) {
            super(Encoding.PCM_UNSIGNED, baseFormat.getSampleRate(),
                    24,
                    baseFormat.getChannels(),
                    baseFormat.getChannels() * 2,
                    baseFormat.getSampleRate(),
                    false);
        }
    }
}
