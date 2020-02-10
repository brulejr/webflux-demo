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
package io.jrb.labs.webflux.module.song.workflow.buildSlides;

import io.jrb.labs.webflux.common.module.workflow.model.WorkflowStatus;
import io.jrb.labs.webflux.common.module.workflow.service.IFinalContentWorkflowContext;
import io.jrb.labs.webflux.common.module.workflow.service.IWorkflowContext;
import io.jrb.labs.webflux.module.song.model.SetListEntity;
import io.jrb.labs.webflux.module.song.model.SongEntity;
import io.jrb.labs.webflux.module.song.workflow.commands.buildSlideShow.IBuildSlideShowContext;
import io.jrb.labs.webflux.module.song.workflow.commands.findSetList.IFindSetListContext;
import io.jrb.labs.webflux.module.song.workflow.commands.findSongsForSetList.IFindSongsForSetListContext;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.http.MediaType;

import java.util.Map;
import java.util.Optional;

import static java.util.UUID.randomUUID;

@Data
@Accessors(chain = true)
@Builder(toBuilder = true)
public class BuildSlidesWorkflowContext implements IWorkflowContext, IFinalContentWorkflowContext<byte[]>,
        IFindSetListContext, IFindSongsForSetListContext, IBuildSlideShowContext {

    @Builder.Default
    private String claimTicket = randomUUID().toString();

    private byte[] content;

    private MediaType contentType;

    private SetListEntity setListEntity;

    private String setListName;

    private Map<String, SongEntity> songs;

    private WorkflowStatus status;

    private final String workflowName = "build-slides";

    @Override
    public byte[] getFinalContent() {
        return Optional.ofNullable(content).orElse(new byte[] {});
    }

    @Override
    public MediaType getFinalContentType() {
        return Optional.ofNullable(contentType).orElse(MediaType.TEXT_PLAIN);
    }

}
