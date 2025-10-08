package ru.svifty7.services;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.servers.Server;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignAutoConfiguration;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Locale;
import java.util.Optional;
import java.util.TimeZone;

@OpenAPIDefinition(servers = {@Server(url = "/", description = "Default Server URL")})
@Slf4j
@EnableJpaRepositories
@SpringBootApplication
@EnableAsync
@EnableScheduling
@EnableRetry
@EnableFeignClients
@ImportAutoConfiguration({FeignAutoConfiguration.class})
public class ServicesApplication {

    static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Moscow"));
        Locale.setDefault(Locale.forLanguageTag("ru-RU"));
        SpringApplication app = new SpringApplication(ServicesApplication.class);
        Environment env = app.run(args).getEnvironment();
        logApplicationStartup(env);
    }

    private static void logApplicationStartup(Environment env) {
        String protocol = Optional.ofNullable(env.getProperty("server.ssl.key-store")).map(key -> "https").orElse("http");
        String serverPort = env.getProperty("server.port");

        String contextPath = Optional
                .ofNullable(env.getProperty("server.servlet.context-path"))
                .filter(StringUtils::isNotBlank)
                .orElse("/");

        String hostAddress = "localhost";

        try {
            hostAddress = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            log.warn("The host name could not be determined, using `localhost` as fallback");
        }

        log.info(
                """
                        
                        ----------------------------------------------------------
                        \tApplication '{}' is running! Access URLs:
                        \tLocal: \t\t{}://localhost:{}{}
                        \tExternal: \t{}://{}:{}{}
                        \tProfile(s): \t{}
                        ----------------------------------------------------------""",
                env.getProperty("spring.application.name"),
                protocol,
                serverPort,
                contextPath,
                protocol,
                hostAddress,
                serverPort,
                contextPath,
                env.getActiveProfiles()
        );

        String configServerStatus = env.getProperty("configserver.status");

        if (configServerStatus == null) {
            configServerStatus = "Not found or not setup for this application";
        }

        log.info(
                """
                        
                        ----------------------------------------------------------
                        \tConfig Server: \t{}
                        ----------------------------------------------------------""",
                configServerStatus
        );
    }

}
