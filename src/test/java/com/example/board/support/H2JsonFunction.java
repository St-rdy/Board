package com.example.board.support;

import com.fasterxml.jackson.databind.ObjectMapper;

public class H2JsonFunction {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static String jsonbExtractPathText(String json, String key) {
        try {
            return objectMapper.readTree(json).path(key).asText(null);
        } catch (Exception e) {
            return null;
        }
    }
}
