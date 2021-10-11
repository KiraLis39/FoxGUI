package fox;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.exc.InvalidDefinitionException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.*;

public class YIOM {
    public enum HEADERS {REGISTRY, CONFIG, USER_SAVE, LAST_USER, USER_LIST, SECURE, TEMP} // несколько готовых примеров (можно задавать свои)

    private static ObjectMapper objectMapper;
    private static ArrayList<Content> contentList = new ArrayList<>();
    private static boolean isLogEnabled = true;

    private YIOM() {}

    private static void init() {
        objectMapper = new ObjectMapper(new YAMLFactory().disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER));
        objectMapper.findAndRegisterModules();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); // tells Jackson to just write our date as a String
    }

    public static synchronized void add(HEADERS header, Path sourcePath) {
        if (objectMapper == null) {init();}

        try {
            if (Files.notExists(sourcePath)) {
                Files.createDirectories(sourcePath.getParent());
                Files.createFile(sourcePath);
            }

            Content tmp;
            try {
                tmp = objectMapper.readValue(sourcePath.toFile(), Content.class);
                tmp.setSource(sourcePath);
            } catch (MismatchedInputException mie) {
                log("WARN: File is empty. Will be created a new one...");
                tmp = new Content(sourcePath);
            } catch (InvalidDefinitionException ide) {
                log("WARN: Cannot construct instance! May be its nothing..");
                tmp = new Content(sourcePath);
            }

            tmp.setHeader(header.name());
            tmp.setDate(LocalDate.now());
            contentList.add(tmp);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static synchronized void set(HEADERS header, String key, Object... values) {
        for (Content c : contentList) {
            if (c.getHeader().equals(header.name())) {
                c.setOrAdd(key, values);
                return;
            }
        }
        log("ERR: Header '" + header + "' was not found! Use 'add(HEADERS, Path)' method.");
    }

    public static Object get(HEADERS header, String key) {
        for (Content c : contentList) {
            if (c.getHeader().equals(header.name())) {

                for (Content.Pair pair : c.getPairs()) {
                    if (pair.getKey().equals(key)) {
                        return pair.getPairValues();
                    }
                }
                log("ERR: Pair key '" + key + "' is not found!");
                return null;
            }
        }
        log("ERR: Header '" + header + "' was not found! Use 'add(HEADERS, Path)' method.");
        return null;
    }

    public static void saveAll() {
        for (Content c : contentList) {
            try {save(c);
            } catch (IOException e) {
                log("Exception by saving: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public static void save(Content content) throws IOException {
        objectMapper.writeValue(content.getSource().toFile(), content);
    }


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

            public void setPairValues(Object[] values) {
                for (Object o: values) {
                    String[] inKey = o.toString().split(":");
                    this.valuesMap.put(inKey.length <= 1 ? "none" : inKey[0], inKey.length <= 1 ? inKey[0] : inKey[1]);
                }
            }

            public Object getPairValues() {
                return valuesMap;
            }
        }
    }

    private static void log(String s) {
        if (isLogEnabled) {
            System.out.println(s);
        }
    }
}
