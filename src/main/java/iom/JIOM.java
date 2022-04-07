package iom;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import fox.Out;
import iom.interfaces.JConfigurable;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
@ToString
@EqualsAndHashCode
@FieldDefaults(level = AccessLevel.PRIVATE)
public class JIOM {
    static ObjectMapper objectMapper;

    private JIOM() {}

    private static void init() {
        final JavaTimeModule timeModule = new JavaTimeModule();
        timeModule.addSerializer(
                LocalDateTime.class,
                new LocalDateTimeSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"))
        );
        objectMapper = new ObjectMapper() {
            {
                registerModule(timeModule);
                disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
                disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            }
        };
    }

    public static <T extends JConfigurable> T fileToDto(final Path dtoPath, Class<T> clazz) throws Exception {
        if (objectMapper == null) {
            init();
        }

        checkFileExisting(dtoPath);

        T dto;
        try {
            dto = objectMapper.readValue(dtoPath.toFile(), clazz);
        } catch (MismatchedInputException mie) {
            Out.Print(JIOM.class, Out.LEVEL.ACCENT, "File '" + dtoPath.getFileName() + "' is empty. Nothing read.");
            dto = clazz.getDeclaredConstructor().newInstance();
        }

        dto.setSource(dtoPath);
        return dto;
    }

    public static void dtoToFile(final JConfigurable dto) throws IOException {
        if (dto.getSource() == null) {
            throw new RuntimeException("Source of class '" + dto.getClass() + "' is NULL!");
        }

        checkFileExisting(dto.getSource());
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(dto.getSource().toFile(), dto);
    }

    private static void checkFileExisting(Path path) throws IOException {
        if (Files.notExists(path)) {
            if (Files.notExists(path.getParent())) {
                Files.createDirectories(path.getParent());
            }
            Files.createFile(path);
        }
    }

    public static ObjectMapper getMapper() {
        if (objectMapper == null) {
            init();
        }
        return objectMapper;
    }
}
