package fox.interfaces;

import java.nio.file.Path;

public interface Configurable {
    void setSource(Path path);
    Path getSource();
}
