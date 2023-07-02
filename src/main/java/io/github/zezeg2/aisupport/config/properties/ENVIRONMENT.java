package io.github.zezeg2.aisupport.config.properties;

/**
 * This enum represents the available environments for program execution.
 * <p>
 * The environments include SYNCHRONOUS and EVENTLOOP.
 *
 * @since 1.0
 */
public enum ENVIRONMENT {

    /**
     * The SYNCHRONOUS environment represents a synchronous execution model.
     * In this environment, tasks are executed sequentially, one after another,
     * without concurrent execution or event-driven processing.
     * it is fit for servlet base web application or cli application
     */
    SYNCHRONOUS,
    /**
     * The EVENTLOOP environment represents an event-driven execution model.
     * In this environment, tasks are executed asynchronously based on events
     * and callbacks, allowing for non-blocking and concurrent execution.
     * it is fit for webflux web application
     */
    EVENTLOOP
}