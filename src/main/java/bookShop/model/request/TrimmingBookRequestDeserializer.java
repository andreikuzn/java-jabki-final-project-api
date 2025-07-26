package bookShop.model.request;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;

public class TrimmingBookRequestDeserializer extends JsonDeserializer<BookRequest> {
    @Override
    public BookRequest deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        JsonNode node = p.getCodec().readTree(p);

        if (!(node instanceof ObjectNode)) {
            throw JsonMappingException.from(p, "Некорректный формат JSON");
        }
        ObjectNode objNode = (ObjectNode) node;
        BookRequest req = new BookRequest();
        req.setTitle(getText(objNode, "title"));
        req.setAuthor(getText(objNode, "author"));
        req.setPrice(getDouble(objNode, "price"));
        req.setCopiesAvailable(getInt(objNode, "copiesAvailable"));
        return req;
    }

    private String getText(ObjectNode node, String field) {
        JsonNode value = node.get(field);
        return (value != null && !value.isNull()) ? value.asText().trim() : null;
    }
    private Double getDouble(ObjectNode node, String field) {
        JsonNode value = node.get(field);
        return (value != null && !value.isNull()) ? value.asDouble() : null;
    }
    private Integer getInt(ObjectNode node, String field) {
        JsonNode value = node.get(field);
        return (value != null && !value.isNull()) ? value.asInt() : null;
    }
}