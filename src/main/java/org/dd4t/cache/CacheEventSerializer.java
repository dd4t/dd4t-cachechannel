package org.dd4t.cache;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.tridion.cache.CacheEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class CacheEventSerializer extends StdSerializer<CacheEvent> {

    private static final Logger LOG = LoggerFactory.getLogger(CacheEventSerializer.class);

    public CacheEventSerializer() {
        this(null);
    }

    public CacheEventSerializer(Class<CacheEvent> vc) {
        super(vc);
    }


    @Override
    public void serialize(CacheEvent cacheEvent, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException, JsonGenerationException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeNumberField("type", cacheEvent.getType());
        jsonGenerator.writeStringField("regionPath", cacheEvent.getRegionPath());
        jsonGenerator.writeStringField("key", (String) cacheEvent.getKey());
        jsonGenerator.writeEndObject();
    }
}
