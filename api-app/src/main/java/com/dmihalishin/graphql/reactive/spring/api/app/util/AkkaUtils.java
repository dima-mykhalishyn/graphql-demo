package com.dmihalishin.graphql.reactive.spring.api.app.util;

import akka.actor.Address;
import akka.cluster.Cluster;
import com.dmihalishin.graphql.reactive.spring.api.app.service.akka.state.ActorState;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.Validate;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Akka Utils
 */
@Slf4j
public final class AkkaUtils {

    public static final Duration DURATION = Duration.ofSeconds(30);

    private static final String SEED_NODES_PROGRAMMATICALLY = "SEED_NODES_PROGRAMMATICALLY";

    private static final String CLUSTER_ROLES = "CLUSTER_ROLES";

    private static final String DEFAULT_CLUSTER_ROLE = "v1";

    private static final String ARROW = "=>";

    private static final String UNDERSCORE = "_";

    private static final Pattern SEED_PATTERN = Pattern.compile("^(.*)://(.*)@(.*):(.*)$");


    private AkkaUtils() {
        // utility class
    }

    /**
     * Join current node to the cluster.
     *
     * @param cluster      akka cluster. Cannot be {@code null}
     * @param environments list of environment variables. Cannot be {@code null}
     */
    public static void joinToCluster(final Cluster cluster, final Map<String, String> environments) {

        int index = 0;
        String seed;
        final List<Address> availableSeeds = new ArrayList<>();

        while ((seed = environments.get(SEED_NODES_PROGRAMMATICALLY + UNDERSCORE + index)) != null) {
            final Matcher matcher = SEED_PATTERN.matcher(seed);
            Validate.isTrue(matcher.matches(), "Akka seed config require full path to other akka host");

            final String host = matcher.group(3);
            if (isHostResolvable(host)) {
                availableSeeds.add(extractAddress(matcher, host));
            }
            index++;
        }

        if (availableSeeds.isEmpty()) {
            log.error("NO seeds available, cannot join to the cluster. Working in standalone mode");
        } else {
            cluster.joinSeedNodes(availableSeeds);
        }
    }

    /**
     * Retrieves set of cluster roles from the ENV
     *
     * @param environments ENV map. Cannot be {@code null}
     * @return set of strings
     */
    public static Set<String> getClusterRoles(final Map<String, String> environments) {
        int index = 0;
        String role;
        final Set<String> roles = new HashSet<>();
        while ((role = environments.get(CLUSTER_ROLES + UNDERSCORE + index)) != null) {
            roles.add(role);
            index++;
        }
        // we want to have this default `v1` role if nothing come from the ENV variable
        // NOTE: this value stored as default in `application.conf`
        return roles.isEmpty() ? Collections.singleton(DEFAULT_CLUSTER_ROLE) : roles;
    }

    private static Address extractAddress(Matcher matcher, String host) {
        final int port = Integer.parseInt(matcher.group(4));
        final String protocol = matcher.group(1);
        final String system = matcher.group(2);

        return new Address(protocol, system, host, port);
    }

    private static boolean isHostResolvable(final String host) {
        try {
            return Objects.nonNull(InetAddress.getByName(host));
        } catch (UnknownHostException e) {
            log.debug(e.getMessage(), e);
        }

        return false;
    }

    /**
     * Consumer Response
     *
     * @param response        response object. Cannot be {@code null}
     * @param responseTraceId response trace ID. Cannot be {@code null}
     * @param state           actor state. Can be {@code null}
     * @param logic           logic to execute Cannot be {@code null}
     * @param <A>             Actor state type
     * @param <T>             Response type
     */
    public static <A extends ActorState, T> void consumeResponse(final T response,
                                                                 final String responseTraceId,
                                                                 final Map<String, A> state,
                                                                 final BiConsumer<T, A> logic) {
        final String stateId = AkkaUtils.getStateId(responseTraceId);
        consumeResponseWithStateId(response, stateId, state, logic);
    }

    /**
     * Consumer Response with state ID
     *
     * @param response response object. Cannot be {@code null}
     * @param stateId  state ID. Cannot be {@code null}
     * @param state    actor state. Can be {@code null}
     * @param logic    logic to execute Cannot be {@code null}
     * @param <A>      Actor state type
     * @param <T>      Response type
     */
    public static <A extends ActorState, T> void consumeResponseWithStateId(final T response,
                                                                            final String stateId,
                                                                            final Map<String, A> state,
                                                                            final BiConsumer<T, A> logic) {
        final A actorState = state.get(stateId);
        if (actorState == null) {
            log.warn("Actor State was not found for {}: {} " + response.getClass().toString(), stateId);
        } else {
            logic.accept(response, actorState);
            if (actorState.isDone()) {
                state.remove(stateId);
            }
        }
    }

    /**
     * Generate trace UUID from another trace UUID
     *
     * @param traceId parent trace ID. Cannot be {@code null}
     * @return generated trace UUID
     */
    public static String generateTraceUUID(final String traceId) {
        return traceId.concat(ARROW).concat(generateTraceUUID());
    }

    /**
     * Generate trace UUID
     *
     * @return generated trace UUID
     */
    public static String generateTraceUUID() {
        return UUID.randomUUID().toString();
    }

    /**
     * Retrieves state ID from the trace UUID.
     *
     * @param traceId trace UUID. Cannot be {@code null}
     * @return state id
     */
    public static String getStateId(final String traceId) {
        return traceId.substring(0, traceId.lastIndexOf(ARROW));
    }
}
