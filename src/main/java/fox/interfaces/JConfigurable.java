package fox.interfaces;

import java.io.Serializable;
import java.nio.file.Path;

public interface JConfigurable extends Serializable {
    void setSource(Path path);
    Path getSource();
}
