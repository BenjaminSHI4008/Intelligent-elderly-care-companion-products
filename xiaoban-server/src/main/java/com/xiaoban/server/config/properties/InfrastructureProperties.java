package com.xiaoban.server.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "infrastructure")
public class InfrastructureProperties {

    private Server server = new Server();
    private Database database = new Database();
    private Upload upload = new Upload();

    @Data
    public static class Server {
        private int port = 8080;
        private String address = "0.0.0.0";
    }

    @Data
    public static class Database {
        private String host = "localhost";
        private int port = 3306;
        private String name = "xiaoban_mvp";
        private String username = "root";
        private String password = "123456";
        private String params = "useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai";

        public String getJdbcUrl() {
            return "jdbc:mysql://" + host + ":" + port + "/" + name + "?" + params;
        }
    }

    @Data
    public static class Upload {
        private String dir = "uploads";
    }
}
