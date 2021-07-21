package com.dmihalishin.graphql.reactive.spring.api.app.config.akka;

import akka.actor.ActorSystem;
import akka.cluster.Cluster;
import akka.cluster.Member;
import akka.cluster.MemberStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Profile("!test")
@Component
@Slf4j
public class AkkaHealthCheck implements HealthIndicator {

    private final Cluster cluster;

    private final AtomicBoolean terminated = new AtomicBoolean(false);

    @Autowired
    public AkkaHealthCheck(final ActorSystem system,
                           @Qualifier("akkaCluster") final Cluster cluster) {
        system.registerOnTermination(() -> {
            log.info("Akka System Terminated");
            terminated.compareAndSet(false, true);
        });
        this.cluster = cluster;
    }

    @Override
    public Health health() {
        final Map<String, MemberStatus> status = StreamSupport.stream(
                cluster.state().getMembers().spliterator(),
                false).collect(Collectors.toMap(member -> member.address().toString(), Member::status));
        final boolean up = !terminated.get();
        final Health.Builder builder = up ? Health.up() : Health.down();
        builder.withDetail("akka.cluster.size", status.size());
        status.forEach((key, value) -> builder.withDetail(key, value.toString()));
        return builder.build();
    }
}
