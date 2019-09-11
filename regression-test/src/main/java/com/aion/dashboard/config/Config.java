package com.aion.dashboard.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.file.Files;
import java.nio.file.Path;

public class Config {
    private static final Config instance =  initialize();

    public static Config getInstance() {
        return instance;
    }
    public final HostConfig hosts;
    public final String[] exemptTables;

    @JsonCreator
    public Config(
        @JsonProperty("hosts") HostConfig hosts,
        @JsonProperty("exemptTables") String[] exemptTables) {
        this.hosts = hosts;
        this.exemptTables = exemptTables;
    }

    private static Config initialize() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            byte[] configBytes = Files.readAllBytes(Path.of("config.json"));
            return mapper.readValue(configBytes, Config.class);
        } catch (Exception e){
            System.out.println("Failed to initialize config");
            e.printStackTrace();
            System.exit(255);
            return null;// realistically we never get here. I am just satisfying the constructor
        }
    }
}
