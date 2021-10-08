package fox;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JIOM {
    public enum HEADERS {REGISTRY, CONFIG, USER_SAVE, LAST_USER, USER_LIST, SECURE, TEMP} // несколько готовых примеров (можно задавать свои)

    private final ObjectMapper jsnMapper = new ObjectMapper();
}
