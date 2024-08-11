package fox.player.playerUtils;

import fox.player.FoxPlayer;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.BooleanControl;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.SourceDataLine;
import java.io.File;

@Slf4j
public class PlayThread extends Thread {
    private static final int audioBufDim = 4096; // (default 4096) 8192
    private final Thread currentThread;
    private final File track;
    private final AudioInputStream ais;
    private final boolean isLooped;
    private final float volume;
    private Thread vfThread;
    private FloatControl masterVolumeControl;
    private BooleanControl muteControl;
    private LineEvent lineEvent;
    private Exception ex;
    private volatile boolean isBroken = false;

    @Setter
    private volatile boolean isVolumeFlowEnabled = false;
    private volatile boolean isLoopFloatedAlready = false;

    @Setter
    private boolean useExperimentalQualityFormat = false;

    @Setter
    private boolean useUnsignedFormat = false;

    public PlayThread(@NonNull String name, @NonNull AudioInputStream ais, float volume, boolean isLooped) {
        this(name, null, ais, volume, isLooped);
    }

    public PlayThread(@NonNull String name, @NonNull File track, float volume, boolean isLooped) {
        this(name, track, null, volume, isLooped);
    }

    private PlayThread(@NonNull String name, @Nullable File track, @Nullable AudioInputStream ais, float volume, boolean isLooped) {
        setName(name);
        currentThread = this;

        this.ais = ais;
        this.track = track;
        this.volume = volume;
        this.isLooped = isLooped;

        start();
    }

    @Override
    public void run() {
        log.debug("FoxPlayer.play: The '" + (track != null ? track.getName() : "audio stream") + "' is played...");

        SourceDataLine line = null;
        do {
            try (AudioInputStream in = (ais == null && track != null ? AudioSystem.getAudioInputStream(track) : ais)) {
                if (in == null) {
                    throw new NullPointerException(
                            "The track '" + (track != null ? track.getName() : "audio stream") + "' has problem with input stream?..");
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

                    buildControls(line, masterVolumeControl != null ? masterVolumeControl.getValue() : volume);
                    line.start();

                    byte[] buffer = new byte[audioBufDim];
                    if (!isBroken && !currentThread.isInterrupted() && !isInterrupted()) {
                        if (isVolumeFlowEnabled && !isLoopFloatedAlready) {
                            volumeFloater(1);
                        }

                        dataIn.reset(); // чтобы сбросить на начало трека для повторов (if isLooped = true).
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
                    stop(line);
                }
            }
        } while (isLooped && !isInterrupted());
    }

    private void stop(SourceDataLine line) {
        line.drain();
        line.close();
    }

    private void pause(SourceDataLine line) {
        line.stop(); // на самом деле это пауза...
    }

    private void volumeFloater(int vector) {
        if (vfThread != null && vfThread.isAlive()) {
            vfThread.interrupt();
        }
        float vfStep = 0.25f;
        float aimVolume;
        if (vector == 1) {
            aimVolume = masterVolumeControl.getValue();
        } else {
            aimVolume = FoxPlayer.getVolumeConverter().getMinimum();
        }

        vfThread = new Thread(() -> {
            isLoopFloatedAlready = false;

            // setDaemon(true);
            if (vector == 1) {
                // set half of the gain volume for start:
                masterVolumeControl.setValue(FoxPlayer.getVolumeConverter().getMinimum() / 2);
            }

            try {
                if (vector == 1) {
                    while (masterVolumeControl.getValue() < aimVolume - 1) {
                        masterVolumeControl.setValue(masterVolumeControl.getValue() + vfStep);
                        sleep(50);
                    }
                    masterVolumeControl.setValue(aimVolume);
                } else {
                    while (masterVolumeControl.getValue() > aimVolume + 1) {
                        masterVolumeControl.setValue(masterVolumeControl.getValue() - vfStep);
                        sleep(20);
                    }
                    masterVolumeControl.setValue(aimVolume);
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
                if (isVolumeFlowEnabled) {
                    volumeFloater(0);
                }
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
        log.debug("Setting up the volume from {} to {}", masterVolumeControl.getValue(), gain);
        masterVolumeControl.setValue(gain);
    }

    private void buildControls(SourceDataLine line, float volume) {
        muteControl = (BooleanControl) line.getControl(BooleanControl.Type.MUTE);
        muteControl.setValue(false);

        masterVolumeControl = (FloatControl) line.getControl(FloatControl.Type.MASTER_GAIN);
        masterVolumeControl.setValue(volume);

        lineEvent = new LineEvent(line, LineEvent.Type.OPEN, 0);

        log.debug("Создан masterVolumeControl с громкостью {}", masterVolumeControl.getValue());
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
