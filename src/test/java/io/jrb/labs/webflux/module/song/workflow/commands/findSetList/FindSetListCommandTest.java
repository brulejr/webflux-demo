package io.jrb.labs.webflux.module.song.workflow.commands.findSetList;

import io.jrb.labs.webflux.module.song.model.SetListEntity;
import io.jrb.labs.webflux.module.song.service.ISetListService;
import io.jrb.labs.webflux.module.song.workflow.buildSlides.BuildSlidesWorkflowContext;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class FindSetListCommandTest {

    @Mock
    private ISetListService setListService;

    private IFindSetListCommand command;

    @BeforeEach
    public void init() {
        command = new FindSetListCommand(setListService);
    }

    @Test
    public void givenNameThenReturnSetListEntity() {
        final String setListName = RandomStringUtils.randomAlphabetic(10, 25);

        final SetListEntity setListEntity = SetListEntity.builder()
                .name(setListName)
                .build();
        when(setListService.findByName(eq(setListName))).thenReturn(Mono.just(setListEntity));

        final BuildSlidesWorkflowContext context = spy(BuildSlidesWorkflowContext.builder()
                .setListName(setListName)
                .build());

        StepVerifier.create(command.run(context))
                .assertNext(subctx -> {
                    assertThat(subctx, is(notNullValue()));
                    assertThat(context.getSetListEntity(), is(setListEntity));
                })
                .verifyComplete();
    }

    @Test
    public void givenNameThenThrowError() {
        final String setListName = RandomStringUtils.randomAlphabetic(10, 25);
        final String errorMessage = RandomStringUtils.randomAlphabetic(10, 25);

        when(setListService.findByName(eq(setListName)))
                .thenReturn(Mono.error(new IllegalArgumentException(errorMessage)));

        final BuildSlidesWorkflowContext context = spy(BuildSlidesWorkflowContext.builder()
                .setListName(setListName)
                .build());

        StepVerifier.create(command.run(context))
                .expectErrorMatches(throwable ->
                        throwable instanceof IllegalArgumentException &&
                        throwable.getMessage().equals(errorMessage)
                ).verify();
    }
}
