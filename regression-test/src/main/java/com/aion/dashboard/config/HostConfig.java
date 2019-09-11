package com.aion.dashboard.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@SuppressWarnings("WeakerAccess")
public class HostConfig {
    public final HostDetails host1;
    public final String db1;
    public final String db2;


    @JsonCreator
    public HostConfig(
        @JsonProperty("host1") HostDetails host1, @JsonProperty("db1") String db1, @JsonProperty("db2") String db2) {
        this.host1 = host1;
        this.db1 = db1;
        this.db2 = db2;
    }

    @SuppressWarnings("WeakerAccess")
    public static class HostDetails{
        public final String url;
        public final String port;
        public final String user;
        public final String password;

        @JsonCreator
        public HostDetails( @JsonProperty("url") String url,
            @JsonProperty("port") String port,
            @JsonProperty("user") String user,
            @JsonProperty("password") String password) {
            this.url = url;
            this.port = port;
            this.user = user;
            this.password = password;
        }
    }


}
