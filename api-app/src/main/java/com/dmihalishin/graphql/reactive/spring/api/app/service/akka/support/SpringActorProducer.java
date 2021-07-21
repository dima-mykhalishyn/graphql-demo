package com.dmihalishin.graphql.reactive.spring.api.app.service.akka.support;

import akka.actor.Actor;
import akka.actor.IndirectActorProducer;
import org.springframework.context.ApplicationContext;

/**
 * Spring Actor Producer
 *
 * @see IndirectActorProducer
 */
public class SpringActorProducer implements IndirectActorProducer {

    private final ApplicationContext applicationContext;

    private final String beanActorName;

    /**
     * Spring Actor Producer constructor
     *
     * @param applicationContext Spring Application context. Cannot be {@code null}
     * @param beanActorName      Spring bean actor name. Cannot be {@code blank}
     */
    public SpringActorProducer(final ApplicationContext applicationContext,
                               final String beanActorName) {
        this.applicationContext = applicationContext;
        this.beanActorName = beanActorName;
    }

    /**
     * {@inheritDoc}
     *
     * @see IndirectActorProducer#produce()
     */
    @Override
    public Actor produce() {
        return (Actor) applicationContext.getBean(beanActorName);
    }

    /**
     * {@inheritDoc}
     *
     * @see IndirectActorProducer#actorClass()
     */
    @Override
    @SuppressWarnings("unchecked")
    public Class<? extends Actor> actorClass() {
        return (Class<? extends Actor>) applicationContext
                .getType(beanActorName);
    }
}
