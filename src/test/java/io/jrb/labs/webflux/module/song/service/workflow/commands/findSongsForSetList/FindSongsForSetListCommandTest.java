package io.jrb.labs.webflux.module.song.service.workflow.commands.findSongsForSetList;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.jrb.labs.webflux.module.song.model.SetListEntity;
import io.jrb.labs.webflux.module.song.model.SongEntity;
import io.jrb.labs.webflux.module.song.service.ISongService;
import io.jrb.labs.webflux.module.song.service.workflow.buildSlides.BuildSlidesWorkflowContext;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Map;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FindSongsForSetListCommandTest {

    @Mock
    private ISongService songService;


    private IFindSongsForSetListCommand command;

    @BeforeEach
    public void init() {
        command = new FindSongsForSetListCommand(songService);
    }

    @Test
    public void givenNameThenReturnSetListEntity() {
        final String setListName = randomAlphabetic(10, 25);
        final String song1Name = randomAlphabetic(10, 25);
        final String song2Name = randomAlphabetic(10, 25);
        final String song3Name = randomAlphabetic(10, 25);

        final SetListEntity setListEntity = SetListEntity.builder()
                .name(setListName)
                .songs(ImmutableList.of(song1Name, song2Name, song3Name))
                .build();
        final SongEntity song1 = SongEntity.builder()
                .title(song1Name)
                .build();
        final SongEntity song2 = SongEntity.builder()
                .title(song2Name)
                .build();
        final SongEntity song3 = SongEntity.builder()
                .title(song3Name)
                .build();
        when(songService.findByTitle(eq(song1Name))).thenReturn(Mono.just(song1));
        when(songService.findByTitle(eq(song2Name))).thenReturn(Mono.just(song2));
        when(songService.findByTitle(eq(song3Name))).thenReturn(Mono.just(song3));

        final Map<String, SongEntity> songMap = ImmutableMap.<String, SongEntity>builder()
                .put(song1Name, song1)
                .put(song2Name, song2)
                .put(song3Name, song3)
                .build();

        final BuildSlidesWorkflowContext context = spy(BuildSlidesWorkflowContext.builder()
                .setListEntity(setListEntity)
                .build());

        StepVerifier.create(command.run(context))
                .assertNext(subctx -> {
                    assertThat(subctx, is(notNullValue()));
                    assertThat(context.getSongs(), is(songMap));
                })
                .verifyComplete();
    }

    @Test
    public void givenNameThenThrowError() {
        final String setListName = randomAlphabetic(10, 25);
        final String song1Name = randomAlphabetic(10, 25);
        final String errorMessage = RandomStringUtils.randomAlphabetic(10, 25);

        final SetListEntity setListEntity = SetListEntity.builder()
                .name(setListName)
                .songs(ImmutableList.of(song1Name))
                .build();

        when(songService.findByTitle(eq(song1Name)))
                .thenReturn(Mono.error(new IllegalArgumentException(errorMessage)));

        final BuildSlidesWorkflowContext context = spy(BuildSlidesWorkflowContext.builder()
                .setListEntity(setListEntity)
                .build());

        StepVerifier.create(command.run(context))
                .expectErrorMatches(throwable ->
                        throwable instanceof IllegalArgumentException &&
                                throwable.getMessage().equals(errorMessage)
                ).verify();
    }

}
