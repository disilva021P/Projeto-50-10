package ipcaProjeto50.Grupo62026.SiteEntArtes.Helper;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class HashidSerializer extends StdSerializer<Integer> {

    @Autowired
    private IdHasher idHasher;

    public HashidSerializer() {
        super(Integer.class);
    }

    @Override
    public void serialize(Integer value, JsonGenerator gen, SerializationContext provider) throws JacksonException {
        if (value != null) {
            gen.writeString(idHasher.encode(value));
        } else {
            gen.writeNull();
        }
    }
}