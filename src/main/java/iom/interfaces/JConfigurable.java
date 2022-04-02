package iom.interfaces;

import java.io.Serializable;
import java.nio.file.Path;

public interface JConfigurable extends Serializable {
    void setSource(Path source);
    Path getSource();
}
