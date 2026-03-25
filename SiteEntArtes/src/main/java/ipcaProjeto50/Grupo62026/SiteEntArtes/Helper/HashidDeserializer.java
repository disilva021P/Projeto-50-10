package ipcaProjeto50.Grupo62026.SiteEntArtes.Helper;


import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.deser.std.StdDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class HashidDeserializer extends StdDeserializer<Integer> {

    @Autowired
    private IdHasher idHasher;
    public HashidDeserializer() {
        super(Integer.class);
    }
    @Override
    public Integer deserialize(JsonParser p, DeserializationContext ctxt) {
        String hash = p.getValueAsString();
        if (hash == null || hash.isEmpty()) {
            return null;
        }
        return idHasher.decode(hash);
    }
}