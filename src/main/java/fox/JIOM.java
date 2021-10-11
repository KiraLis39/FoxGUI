package fox;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import fox.interfaces.Configurable;
import lombok.AccessLevel;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class JIOM {
    static boolean isLogEnabled = true;
    static ObjectMapper objectMapper;

    private JIOM() {}

    private static void init() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @SneakyThrows
    public static <T extends Configurable> T fromFile(Class<T> clazz, final Path path) throws IOException {
        if (objectMapper == null) {
            init();
        }

        checkFileExisting(path);

        T result;
        try {
            result = objectMapper.readValue(path.toFile(), clazz);
        } catch (MismatchedInputException mie) {
            log("File '" + path.getFileName() + "' is empty. Nothing read.");
            result = clazz.getDeclaredConstructor().newInstance();
        }

        result.setSource(path);
        return result;
    }

    private static void log(String s) {
        if (isLogEnabled) {
            System.out.println(s);
        }
    }

    private static void checkFileExisting(Path path) throws IOException {
        if (Files.notExists(path)) {
            if (Files.notExists(path.getParent())) {
                Files.createDirectories(path);
            }
            Files.createFile(path);
        }
    }

    public static void toFile(final Configurable confClass) throws IOException {
        if (confClass.getSource() == null) {
            throw new RuntimeException("Source of class '" + confClass.getClass() + "' is NULL!");
        }

        checkFileExisting(confClass.getSource());
        objectMapper.writeValue(confClass.getSource().toFile(), confClass);
    }
}
