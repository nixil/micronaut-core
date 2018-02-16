/*
 * Copyright 2017 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.particleframework.management.health.indicator;

import org.particleframework.core.async.publisher.AsyncSingleResultPublisher;
import org.particleframework.health.HealthStatus;
import org.particleframework.scheduling.executor.IOExecutorServiceConfig;
import org.reactivestreams.Publisher;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.concurrent.ExecutorService;

/**
 * <p>A base health indicator class to extend from that catches exceptions
 * thrown from the {@link #getHealthInformation()} method and updates
 * the {@link HealthResult} with the exception information.</p>
 *
 * @author James Kleeh
 * @since 1.0
 */
public abstract class AbstractHealthIndicator<T> implements HealthIndicator {

    protected ExecutorService executorService;
    protected HealthStatus healthStatus;

    @Inject
    public void setExecutorService(@Named(IOExecutorServiceConfig.NAME) ExecutorService executorService) {
        this.executorService = executorService;
    }

    @Override
    public Publisher<HealthResult> getResult() {
        if(executorService == null) {
            throw new IllegalStateException("I/O ExecutorService is null");
        }
        return new AsyncSingleResultPublisher<>(executorService, () -> {
            HealthResult.Builder builder = HealthResult.builder(getName());
            try {
                builder.details(getHealthInformation());
                builder.status(this.healthStatus);
            } catch (Exception e) {
                builder.status(HealthStatus.DOWN);
                builder.exception(e);
            }
            return builder.build();
        });

    }

    /**
     * Provides information (typically a Map) to be returned. Set the
     * {@link #healthStatus} field during execution, otherwise {@link HealthStatus#UNKNOWN}
     * will be used.
     *
     * @return Any details to be included in the response.
     */
    protected abstract T getHealthInformation();

    /**
     * Used to populate the {@link HealthResult}. Provides a key to go
     * along with the health information.
     *
     * @return The name of the indicator
     */
    protected abstract String getName();
}