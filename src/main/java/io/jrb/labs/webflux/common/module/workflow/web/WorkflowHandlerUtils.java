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
package io.jrb.labs.webflux.common.module.workflow.web;

import com.google.common.collect.ImmutableMap;
import io.jrb.labs.webflux.common.module.workflow.service.MismatchedClaimTicketException;
import io.jrb.labs.webflux.common.module.workflow.service.UnknownClaimTicketException;
import io.jrb.labs.webflux.common.module.workflow.service.WorkflowException;
import io.jrb.labs.webflux.common.module.workflow.service.WorkflowUserException;
import io.jrb.labs.webflux.common.web.ErrorDTO;
import io.jrb.labs.webflux.common.web.ResponseException;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.server.ServerRequest;

import java.util.function.Function;

public class WorkflowHandlerUtils {

    private WorkflowHandlerUtils() {}

    public static final Function<WorkflowException, ResponseException> WORKFLOW_EXCEPTION_MAPPER = e -> {
        final ImmutableMap.Builder<String, String> metadataBuilder = ImmutableMap.builder();
        final ErrorDTO.ErrorDTOBuilder errorBuilder = ErrorDTO.builder()
                .description(e.getMessage())
                .eventType("WORKFLOW_ENGINE");
        if (e instanceof MismatchedClaimTicketException) {
            metadataBuilder.put("claimTicket", ((MismatchedClaimTicketException) e).getClaimTicketNumber());
            metadataBuilder.put("workflowName", ((MismatchedClaimTicketException) e).getWorkflowName());
            errorBuilder.errorCode("WFE-003");
        } else if (e instanceof UnknownClaimTicketException) {
            metadataBuilder.put("claimTicket", ((UnknownClaimTicketException) e).getClaimTicketNumber());
            errorBuilder.errorCode("WFE-002");
        } else {
            errorBuilder.errorCode("WFE-001");
        }
        return new ResponseException(
                e instanceof WorkflowUserException ? HttpStatus.BAD_REQUEST : HttpStatus.INTERNAL_SERVER_ERROR,
                metadataBuilder.build(),
                errorBuilder.build()
        );
    };

    public static String claimTicketNumber(final ServerRequest r) {
        return r.pathVariable("claimTicketNumber");
    }

    public static String workflowName(final ServerRequest r) {
        return r.pathVariable("workflowName");
    }
    
}
