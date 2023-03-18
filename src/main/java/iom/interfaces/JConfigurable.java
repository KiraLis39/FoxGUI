package iom.interfaces;

import java.io.Serializable;
import java.nio.file.Path;

public interface JConfigurable extends Serializable {
    Path getSource();

    void setSource(Path source);
}
