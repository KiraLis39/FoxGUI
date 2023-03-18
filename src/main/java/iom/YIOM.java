package iom;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.exc.InvalidDefinitionException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Slf4j
@Component
@AllArgsConstructor
public final class YIOM {
    private final ObjectMapper mapper = new ObjectMapper(new YAMLFactory().disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER));
    private final ArrayList<Content> contentList = new ArrayList<>();
    private final boolean isLogEnabled = true;

    public synchronized void add(HEADERS header, Path sourcePath) {
        try {
            if (Files.notExists(sourcePath)) {
                Files.createDirectories(sourcePath.getParent());
                Files.createFile(sourcePath);
            }

            Content tmp;
            try {
                tmp = mapper.readValue(sourcePath.toFile(), Content.class);
                tmp.setSource(sourcePath);
            } catch (MismatchedInputException mie) {
                log.warn("File is empty. Will be created a new one...");
                tmp = new Content(sourcePath);
            } catch (InvalidDefinitionException ide) {
                log.warn("Cannot construct instance! May be its nothing..");
                tmp = new Content(sourcePath);
            }

            tmp.setHeader(header.name());
            tmp.setDate(LocalDate.now());
            contentList.add(tmp);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void set(HEADERS header, String key, Object... values) {
        for (Content c : contentList) {
            if (c.getHeader().equals(header.name())) {
                c.setOrAdd(key, values);
                return;
            }
        }
        log.error("Header '" + header + "' was not found! Use 'add(HEADERS, Path)' method.");
    }

    public Object get(HEADERS header, String key) {
        for (Content c : contentList) {
            if (c.getHeader().equals(header.name())) {

                for (Content.Pair pair : c.getPairs()) {
                    if (pair.getKey().equals(key)) {
                        return pair.getPairValues();
                    }
                }
                log.error("Pair key '" + key + "' is not found!");
                return null;
            }
        }
        log.error("Header '" + header + "' was not found! Use 'add(HEADERS, Path)' method.");
        return null;
    }

    public void saveAll() {
        for (Content c : contentList) {
            try {
                save(c);
            } catch (IOException e) {
                log.error("Exception by saving: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public void save(Content content) throws IOException {
        mapper.writeValue(content.getSource().toFile(), content);
    }

    public enum HEADERS {REGISTRY, CONFIG, USER_SAVE, LAST_USER, USER_LIST, SECURE, TEMP} // несколько готовых примеров (можно задавать свои)

    @Data
    @NoArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    private static class Content {
        Path source;
        String header;
        LocalDate date;
        @JsonProperty("content")
        List<Pair> pairs = new ArrayList<>();

        public Content(Path source) {
            this.source = source;
            this.date = LocalDate.now();
        }

        public void setOrAdd(String key, Object[] values) {
            for (Pair pair : pairs) {
                if (pair.getKey().equals(key)) {
                    pair.setPairValues(values);
                    return;
                }
            }

            pairs.add(new Pair(key, values));
        }

        @Data
        @FieldDefaults(level = AccessLevel.PRIVATE)
        private static class Pair {
            String key;
            @JsonIgnore
            Map<String, Object> valuesMap = new HashMap<>();

            public Pair(String key, Object[] values) {
                this.key = key;
                for (Object o : values) {
                    if (o == null) {
                        this.valuesMap.put("NA", null);
                    } else {
                        setPairValues(values);
                    }
                }
            }

            public Object getPairValues() {
                return valuesMap;
            }

            public void setPairValues(Object[] values) {
                for (Object o : values) {
                    String[] inKey = o.toString().split(":");
                    this.valuesMap.put(inKey.length <= 1 ? "none" : inKey[0], inKey.length <= 1 ? inKey[0] : inKey[1]);
                }
            }
        }
    }
}
