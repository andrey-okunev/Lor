package com.okunev.lor.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.okunev.lor.model.Protocol;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ProtocolService {

    private final ObjectMapper mapper = new ObjectMapper();

    public List<Protocol> loadProtocols() {
        try (InputStream is = getClass().getResourceAsStream("/protocols_all.json")) {
            if (is == null) {
                System.err.println("Файл protocols_all.json не найден в ресурсах!");
                return new ArrayList<>();
            }
            return mapper.readValue(is, new TypeReference<List<Protocol>>() {});
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}