package com.dmihalishin.graphql.reactive.spring.api.app.config.akka;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.cluster.Cluster;
import akka.cluster.routing.ClusterRouterGroup;
import akka.cluster.routing.ClusterRouterGroupSettings;
import akka.routing.RoundRobinGroup;
import akka.routing.SmallestMailboxPool;
import com.dmihalishin.graphql.reactive.spring.api.app.service.akka.DataLoaderActor;
import com.dmihalishin.graphql.reactive.spring.api.app.service.akka.TaskLoaderActor;
import com.dmihalishin.graphql.reactive.spring.api.app.service.akka.support.SpringExtension;
import com.dmihalishin.graphql.reactive.spring.api.app.util.AkkaUtils;
import com.typesafe.config.ConfigFactory;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Date;
import java.util.Set;

@Profile("!test")
@Configuration
public class AkkaConfig {

    public static final String DATA_ACTOR_REF_NAME = "dataLoaderActorRef";
    /* Task Actors */
    public static final String TASK_ROUTER_NAME = "taskRouterRef";
    public static final String TASK_REF_NAME = "taskLoaderActorRef";

    @Bean
    public ActorSystem actorSystem(final ApplicationContext applicationContext) {
        final ActorSystem system = ActorSystem.create("AkkaClusterSystem", ConfigFactory.load());
        SpringExtension.SPRING_EXTENSION_PROVIDER.get(system).initialize(applicationContext);
        return system;
    }

    @Bean
    public ThreadPoolTaskScheduler scheduler() {
        final ThreadPoolTaskScheduler threadPoolTaskScheduler
                = new ThreadPoolTaskScheduler();
        threadPoolTaskScheduler.setPoolSize(1);
        threadPoolTaskScheduler.setThreadNamePrefix("ThreadPoolTaskScheduler");
        return threadPoolTaskScheduler;
    }

    @Bean("akkaCluster")
    public Cluster cluster(final ActorSystem system,
                           @Value("${akka.cluster.init.delay.seconds: 60}") final Integer delaySeconds,
                           final ThreadPoolTaskScheduler taskScheduler) {
        final Cluster cluster = Cluster.get(system);
        taskScheduler.schedule(
                () -> AkkaUtils.joinToCluster(cluster, System.getenv()),
                new Date(ZonedDateTime.now().plusSeconds(delaySeconds).toInstant().toEpochMilli())
        );
        return cluster;
    }

    @Bean(TASK_REF_NAME)
    public ActorRef productLoaderActor(final ActorSystem system) {
        final String poolSize = System.getenv("PRODUCT_POOL_SIZE");
        final int poolSizeValue = NumberUtils.isDigits(poolSize) ? Integer.parseInt(poolSize) : 4;
        return system.actorOf(
                SpringExtension.SPRING_EXTENSION_PROVIDER.get(system)
                        .props(TaskLoaderActor.NAME)
                        .withRouter(new SmallestMailboxPool(poolSizeValue)),
                TaskLoaderActor.NAME);
    }

    @Bean(TASK_ROUTER_NAME)
    public ActorRef productRouter(final ActorSystem system) {
        final Iterable<String> routeesPaths = Collections.singletonList(TaskLoaderActor.ACTOR_NAME);
        final Set<String> useRoles = AkkaUtils.getClusterRoles(System.getenv());
        return system.actorOf(
                new ClusterRouterGroup(
                        new RoundRobinGroup(routeesPaths),
                        new ClusterRouterGroupSettings(1000, routeesPaths, true, useRoles))
                        .props(),
                "taskRouter");
    }

    @Bean(DATA_ACTOR_REF_NAME)
    public ActorRef dataLoaderActor(final ActorSystem system) {
        final String poolSize = System.getenv("DATA_LOADER_POOL_SIZE");
        final int poolSizeValue = NumberUtils.isDigits(poolSize) ? Integer.parseInt(poolSize) : 4;
        return system.actorOf(SpringExtension.SPRING_EXTENSION_PROVIDER.get(system)
                .props(DataLoaderActor.NAME).withRouter(new SmallestMailboxPool(poolSizeValue)), DataLoaderActor.NAME);
    }

}
