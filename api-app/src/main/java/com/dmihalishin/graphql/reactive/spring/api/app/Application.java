package com.dmihalishin.graphql.reactive.spring.api.app;

import com.dmihalishin.graphql.reactive.spring.api.app.dao.Task;
import com.dmihalishin.graphql.reactive.spring.api.app.dao.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.autoconfigure.cassandra.CassandraHealthContributorAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.cassandra.CassandraDataAutoConfiguration;
import org.springframework.boot.autoconfigure.data.cassandra.CassandraReactiveDataAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.web.reactive.config.EnableWebFlux;

import java.util.stream.IntStream;

@SpringBootApplication(exclude = {WebMvcAutoConfiguration.class,
        CassandraDataAutoConfiguration.class,
        CassandraReactiveDataAutoConfiguration.class,
        CassandraHealthContributorAutoConfiguration.class})
@EnableWebFlux
public class Application {

    @Autowired
    private TaskRepository taskRepository;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void doSomethingAfterStartup() {
        IntStream.range(1, 100).boxed().forEach(v -> {
            final Task task = new Task();
            task.setId(v);
            taskRepository.save(task);
        });
    }
}

