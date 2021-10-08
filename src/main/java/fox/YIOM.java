package fox;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

public class YIOM {
    public enum HEADERS {REGISTRY, CONFIG, USER_SAVE, LAST_USER, USER_LIST, SECURE, TEMP} // несколько готовых примеров (можно задавать свои)

    private final ObjectMapper jsnMapper = new ObjectMapper(new YAMLFactory());

    public YIOM() {
        jsnMapper.findAndRegisterModules();
    }

    public synchronized void add(Path sourcePath) {
        Content order = jsnMapper.readValue(sourcePath.toFile(), Content.class);
    }

    public class Content {
        private String name;
        private LocalDate date;
        private String customerName;
        private List<OrderLine> orderLines;

        // Constructors, Getters, Setters and toString

        public class OrderLine {
            private String item;
            private int quantity;
            private BigDecimal unitPrice;

            // Constructors, Getters, Setters and toString
        }
    }
}
