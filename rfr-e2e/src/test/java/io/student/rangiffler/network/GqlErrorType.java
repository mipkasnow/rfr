package io.student.rangiffler.network;

import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

public enum GqlErrorType {
    BAD_REQUEST,
    NOT_FOUND,
    UNAUTHORIZED,
    FORBIDDEN,
    INTERNAL_ERROR;

    public String response(String message) {
        ObjectNode response = JsonMapper.shared().createObjectNode();
        response.putArray("errors").addObject()
                .put("message", message)
                .putObject("extensions").put("classification", name());
        response.putNull("data");
        return response.toString();
    }
}
