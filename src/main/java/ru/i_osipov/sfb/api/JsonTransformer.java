package ru.i_osipov.sfb.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.i_osipov.sfb.api.dto.ErrorDto;
import spark.ResponseTransformer;

public final class JsonTransformer implements ResponseTransformer {

    private static JsonTransformer INSTANCE = new JsonTransformer();

    private static final Logger logger = LoggerFactory.getLogger(JsonTransformer.class);

    private ObjectMapper mapper = new ObjectMapper();

    public static JsonTransformer get() {
        return INSTANCE;
    }

    @Override
    public String render(Object model) throws Exception {
        return mapper.writeValueAsString(model);
    }

    public String safeRender(Object model) {
        try {
            return render(model);
        } catch (Exception e) {
            logger.error("Error during data parsing to json", e);
            return safeRender(ErrorDto.create("Error during parsing data to json. " + e.getMessage()));
        }
    }
}
