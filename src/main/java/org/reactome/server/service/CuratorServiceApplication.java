package org.reactome.server.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;


//@EnableAsync and @EnableScheduling  equivalent to <task:annotation-driven> in servlet.xml, leave it for now as it doesn't hurt
@EnableScheduling
@EnableAsync
// TODO: &&&& @EntityScan({"org.reactome.server.service.model", "org.reactome.server.graph.curator.domain.model"})
// TODO: &&&& @EnableNeo4jRepositories("org.reactome.server.graph.curator.repository")
@SpringBootApplication(scanBasePackages = {"org.reactome.server"})
public class CuratorServiceApplication extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(CuratorServiceApplication.class);
    }

    public static void main(String[] args) {
        SpringApplication.run(CuratorServiceApplication.class, args);
    }
}
