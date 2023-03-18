package iom;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import iom.interfaces.JConfigurable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Data
@Slf4j
@Component
@AllArgsConstructor
public final class JIOM {
    private final ObjectMapper mapper;

    private static void checkFileExisting(Path path) throws IOException {
        if (Files.notExists(path)) {
            if (Files.notExists(path.getParent())) {
                Files.createDirectories(path.getParent());
            }
            Files.createFile(path);
        }
    }

    public <T extends JConfigurable> T fileToDto(final Path dtoPath, Class<T> clazz) throws Exception {
        checkFileExisting(dtoPath);

        T dto;
        try {
            dto = mapper.readValue(dtoPath.toFile(), clazz);
        } catch (MismatchedInputException mie) {
            log.info("File '{}' is empty. Nothing read.", dtoPath.getFileName());
            dto = clazz.getDeclaredConstructor().newInstance();
        }

        dto.setSource(dtoPath);
        return dto;
    }

    public void dtoToFile(final JConfigurable dto) throws IOException {
        if (dto.getSource() == null) {
            throw new RuntimeException("Source of class '" + dto.getClass() + "' is NULL!");
        }

        checkFileExisting(dto.getSource());
        mapper.writerWithDefaultPrettyPrinter().writeValue(dto.getSource().toFile(), dto);
    }
}
