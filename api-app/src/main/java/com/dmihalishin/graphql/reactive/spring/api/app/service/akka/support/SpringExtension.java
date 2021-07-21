package com.dmihalishin.graphql.reactive.spring.api.app.service.akka.support;

import akka.actor.AbstractExtensionId;
import akka.actor.ExtendedActorSystem;
import akka.actor.Extension;
import akka.actor.Props;
import org.springframework.context.ApplicationContext;

import java.util.Optional;

/**
 * Spring Extension
 *
 * @see AbstractExtensionId
 */
public class SpringExtension extends AbstractExtensionId<SpringExtension.SpringExt> {

    public static final SpringExtension SPRING_EXTENSION_PROVIDER = new SpringExtension();

    /**
     * {@inheritDoc}
     *
     * @see AbstractExtensionId#createExtension(ExtendedActorSystem)
     */
    @Override
    public SpringExt createExtension(ExtendedActorSystem system) {
        return new SpringExt();
    }

    /**
     * Injects Spring application context
     */
    public static class SpringExt implements Extension {
        private volatile ApplicationContext applicationContext;

        /**
         * Initialize the application context
         * @param applicationContext    Spring application context
         */
        public void initialize(ApplicationContext applicationContext) {
            this.applicationContext = applicationContext;
        }

        /**
         * Retrieves {@link Props} configuration object used to create an actor
         *
         * @param actorBeanName Name of the Spring bean, that should exist and will be used as actor
         * @return              {@link Props} object used to create the actor
         */
        public Props props(String actorBeanName) {
            return Props.create(SpringActorProducer.class, applicationContext, actorBeanName);
        }
    }
}
