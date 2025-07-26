package bookShop.model.request;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;

public class TrimmingRegisterRequestDeserializer extends JsonDeserializer<RegisterRequest> {
    @Override
    public RegisterRequest deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        JsonNode node = p.getCodec().readTree(p);

        if (!(node instanceof ObjectNode)) {
            throw JsonMappingException.from(p, "Некорректный формат JSON");
        }
        ObjectNode objNode = (ObjectNode) node;
        RegisterRequest req = new RegisterRequest();
        req.setUsername(getText(objNode, "username"));
        req.setPassword(getText(objNode, "password"));
        req.setPhone(getText(objNode, "phone"));
        req.setEmail(getText(objNode, "email"));
        if (objNode.has("role") && !objNode.get("role").isNull()) {
            req.setRole(parseRole(getText(objNode, "role"), p));
        }
        return req;
    }

    private String getText(ObjectNode node, String field) {
        JsonNode value = node.get(field);
        return (value != null && !value.isNull()) ? value.asText().trim() : null;
    }

    private bookShop.model.Role parseRole(String value, JsonParser p) throws JsonMappingException {
        if (value == null || value.isBlank()) return null;
        try {
            return bookShop.model.Role.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw JsonMappingException.from(p, "Некорректное значение role: " + value);
        }
    }
}