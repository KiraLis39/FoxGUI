package fox.player;

import fox.player.interfaces.iPlayer;
import lombok.Data;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.file.Path;
import java.util.*;

@Data
@Slf4j
public class FoxPlayer implements iPlayer {
    private static final LinkedHashMap<String, File> trackMap = new LinkedHashMap<>();
    private final ArrayList<PlayThread> playThreads = new ArrayList<>();
    private static final VolumeConverter vConv = new VolumeConverter();
    private volatile int currentPlayerVolumePercent = 0;
    private volatile boolean isMuted = false;
    private volatile boolean isLooped = true;
    private volatile boolean isParallelPlayable = false;

    private final String name;
    private String lastTrack;

    public FoxPlayer(@NonNull String name) {
        this.name = name;
    }

    public static VolumeConverter getVolumeConverter() {
        return vConv;
    }

    public synchronized void loadFromPath(@NonNull Path audioDirectoryPath) {
        File[] files = audioDirectoryPath.toFile().listFiles();
        if (files == null || files.length == 0) {
            log.warn("Media directory {} is empty?", audioDirectoryPath.toFile().getPath());
        }
        Arrays.stream(files).forEach(file -> add(file.getName().substring(0, file.getName().length() - 4), file));
    }

    @Override
    public synchronized void add(@NonNull String trackName, @NonNull File sourceFile) {
        trackMap.put(trackName, sourceFile);
    }

    public void play(@NonNull String trackName) {
        play(trackName, currentPlayerVolumePercent, isLooped);
    }

    public void play(@NonNull String trackName, boolean isLooped) {
        play(trackName, currentPlayerVolumePercent, isLooped);
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
            log.debug("The track '" + trackName + "' was found in the trackMap.");
            if (!isParallelPlayable) {
                stop();
            }
            playThreads.add(new PlayThread(this.name, trackMap.get(trackName), vConv.volumePercentToGain(volumePercent), isLooped));
        } else {
            stop();
            log.warn("The track '" + trackName + "' is absent in the trackMap.");
        }
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

        play(nextTrackName, currentPlayerVolumePercent, isLooped);
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
        currentPlayerVolumePercent = volume;
        for (PlayThread playThread : playThreads) {
            playThread.setVolume(vConv.volumePercentToGain(currentPlayerVolumePercent));
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
                thread.getException().printStackTrace();
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
}
