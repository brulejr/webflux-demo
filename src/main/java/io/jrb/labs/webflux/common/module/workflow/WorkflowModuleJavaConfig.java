/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2020 Jon Brule <brulejr@gmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.jrb.labs.webflux.common.module.workflow;

import com.google.common.collect.Maps;
import io.jrb.labs.webflux.common.module.ModuleJavaConfigSupport;
import io.jrb.labs.webflux.common.module.workflow.service.IWorkflowContext;
import io.jrb.labs.webflux.common.module.workflow.service.IWorkflowFactory;
import io.jrb.labs.webflux.common.module.workflow.service.IWorkflowService;
import io.jrb.labs.webflux.common.module.workflow.service.IWorkflowStateRepository;
import io.jrb.labs.webflux.common.module.workflow.service.WorkflowService;
import io.jrb.labs.webflux.common.module.workflow.service.WorkflowStateDiskStateRepository;
import io.jrb.labs.webflux.common.module.workflow.web.CommonWorkflowHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import java.util.Map;
import java.util.Optional;

import static org.springframework.web.reactive.function.server.RequestPredicates.DELETE;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Slf4j
@Configuration
@EnableConfigurationProperties(WorkflowModuleConfig.class)
public class WorkflowModuleJavaConfig extends ModuleJavaConfigSupport {

    private static final String MODULE_NAME = "Song";

    public WorkflowModuleJavaConfig() {
        super(MODULE_NAME, log);
    }

    @Bean
    public CommonWorkflowHandler commonWorkflowHandler(final IWorkflowService workflowService) {
        return new CommonWorkflowHandler(workflowService);
    }

    @Bean
    public RouterFunction<ServerResponse> workflowRoutes(final CommonWorkflowHandler commonWorkflowHandler) {
        return route(
                DELETE("/api/v1/workflow/{workflowName}/{claimTicketNumber}"),
                commonWorkflowHandler::deleteWorkflowContext
        ).andRoute(
                GET("/api/v1/workflow/{workflowName}/{claimTicketNumber}/status"),
                commonWorkflowHandler::getWorkflowStatus
        );
    }

    @Bean
    public IWorkflowService workflowService(
            final Map<String, IWorkflowFactory<? extends IWorkflowContext>> workflowFactories,
            final IWorkflowStateRepository workflowStateRepository,
            final WorkflowModuleConfig workflowModuleConfig
    ) {
        final Map<String, String> workflowAliases = Optional.ofNullable(workflowModuleConfig.workflowAliases())
                .orElse(Maps.newHashMap());
        return new WorkflowService(workflowFactories, workflowAliases, workflowStateRepository);
    }

    @Bean
    public IWorkflowStateRepository workflowStateRepository(final WorkflowModuleConfig workflowModuleConfig) {
        return new WorkflowStateDiskStateRepository(workflowModuleConfig.baseDirectory());
    }

}
