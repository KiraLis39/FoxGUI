package fox.player;

import fox.player.interfaces.iPlayer;
import fox.player.playerUtils.PlayThread;
import fox.player.playerUtils.VolumeConverter;
import fox.utils.MediaCache;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
public final class FoxPlayer implements iPlayer {
    @Getter
    private static final VolumeConverter volumeConverter = new VolumeConverter();
    private static final MediaCache cache = new MediaCache().getInstance();

    private final LinkedHashMap<String, File> trackMap = new LinkedHashMap<>();
    private final ArrayList<PlayThread> playThreads = new ArrayList<>();
    private final String name;
    private String lastTrack;

    @Getter
    private volatile int playerVolumePercent = 100;
    private volatile boolean isMuted = false, isLooped = false, isParallelPlayable = false;

    public FoxPlayer(@NonNull String name) {
        this.name = name;
    }

    public synchronized void loadFromPath(@NonNull Path audioDirectoryPath) {
        File[] files = audioDirectoryPath.toFile().listFiles();
        if (files == null || files.length == 0) {
            log.warn("Media directory {} is empty?", audioDirectoryPath.toFile().getPath());
        }
        assert files != null;
        Arrays.stream(files).forEach(file -> add(file.getName().substring(0, file.getName().length() - 4), file));
    }

    @Override
    public synchronized void add(@NonNull String trackName, @NonNull File sourceFile) {
        trackMap.put(trackName, sourceFile);
    }

    public void play(@NonNull String trackName) {
        play(trackName, playerVolumePercent, isLooped);
    }

    public void play(@NonNull String trackName, boolean isLooped) {
        play(trackName, playerVolumePercent, isLooped);
    }

    public void play(@NonNull String trackName, int volumePercent) {
        play(trackName, volumePercent, isLooped);
    }

    @Override
    public synchronized void play(@NonNull String trackName, int volumePercent, boolean isLooped) {
        lastTrack = trackName;
        if (isMuted) {
            return;
        }

        if (trackMap.containsKey(trackName)) {
            log.debug("The track '" + trackName + "' was found in the local track map.");
            if (!isParallelPlayable) {
                stop();
            }
            playThreads.add(new PlayThread(
                    this.name, trackMap.get(trackName), volumeConverter.volumePercentToGain(volumePercent), isLooped));
        } else if (cache.hasKey(trackName)) {
            log.debug("The track '" + trackName + "' was found in the media cache.");
            if (!isParallelPlayable) {
                stop();
            }
            playThreads.add(new PlayThread(this.name + "_" + trackName,
                    cache.getAudioStream(trackName), volumeConverter.volumePercentToGain(volumePercent), isLooped));
        } else {
            stop();
            log.warn("The track '" + trackName + "' is absent in the trackMap.");
        }

        cleanThreads();
        log.debug("Player {} > Потоков аудио: {} ({})", name, playThreads.size(), playThreads.stream().map(Thread::getName).toList());
    }

    private void cleanThreads() {
        playThreads.removeIf(pt -> pt == null || !pt.isAlive() || pt.isInterrupted());
    }

    public synchronized void playNext() {
        String nextTrackName = lastTrack;

        Iterator<Map.Entry<String, File>> triterator = trackMap.entrySet().iterator();
        while (triterator.hasNext()) {
            Map.Entry<String, File> elem = triterator.next();
            if (elem.getKey().equals(lastTrack)) {
                if (triterator.hasNext()) {
                    nextTrackName = triterator.next().getKey();
                    break;
                }
                nextTrackName = trackMap.keySet().stream().findFirst().orElse(lastTrack);
            }
        }

        play(nextTrackName, playerVolumePercent, isLooped);
    }

    @Override
    public void mute(boolean mute) {
        isMuted = mute;
        for (PlayThread playThread : playThreads) {
            playThread.mute(isMuted);
        }
    }

    @Override
    public void setGlobalVolumePercent(int volume) {
        playerVolumePercent = volume;
        for (PlayThread playThread : playThreads) {
            playThread.setVolume(volumeConverter.volumePercentToGain(playerVolumePercent));
        }
    }

    @Override
    public void stop() {
        for (PlayThread thread : playThreads) {
            if (thread == null) {
                continue;
            }
            thread.close();
            if (thread.getException() != null) {
                log.error("Error here: " + thread.getException());
            }
        }
        playThreads.clear();
    }

    public void setLooped(boolean loop) {
        this.isLooped = loop;
    }

    public void setUseExperimentalQualityFormat(boolean b) {
        for (PlayThread playThread : playThreads) {
            playThread.setUseExperimentalQualityFormat(b);
        }
    }

    public void setUseUnsignedFormat(boolean b) {
        for (PlayThread playThread : playThreads) {
            playThread.setUseUnsignedFormat(b);
        }
    }

    public void setVolumeFlowEnabled(boolean b) {
        for (PlayThread playThread : playThreads) {
            playThread.setVolumeFlowEnabled(b);
        }
    }

    public void setParallelPlayable(boolean b) {
        isParallelPlayable = b;
    }

    public boolean isMuted() {
        return isMuted;
    }
}
