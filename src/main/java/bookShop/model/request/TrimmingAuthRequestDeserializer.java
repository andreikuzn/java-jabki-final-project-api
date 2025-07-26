package bookShop.model.request;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;

public class TrimmingAuthRequestDeserializer extends JsonDeserializer<AuthRequest> {
    @Override
    public AuthRequest deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        JsonNode node = p.getCodec().readTree(p);

        if (!(node instanceof ObjectNode)) {
            throw JsonMappingException.from(p, "Некорректный формат JSON");
        }
        ObjectNode objNode = (ObjectNode) node;
        AuthRequest req = new AuthRequest();
        req.setUsername(getText(objNode, "username"));
        req.setPassword(getText(objNode, "password"));
        return req;
    }

    private String getText(ObjectNode node, String field) {
        JsonNode value = node.get(field);
        return (value != null && !value.isNull()) ? value.asText().trim() : null;
    }
}