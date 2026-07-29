package dev.mppviewer.parser;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;


@SpringBootTest
class ErrorCodeTest {

    @Autowired
    private WebApplicationContext context;

    static Stream<Arguments> garbage() throws IOException {
        byte[] real = Files.readAllBytes(Path.of("testdata", "real/mpp14baseline.mpp"));

        byte[] headerThenJunk = real.clone();
        Arrays.fill(headerThenJunk, 1024, headerThenJunk.length, (byte) 0x5A);

        return Stream.of(
                Arguments.of("plain text", "this is not a project file".getBytes(StandardCharsets.UTF_8)),
                Arguments.of("jpeg", new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0,
                        0x00, 0x10, 'J', 'F', 'I', 'F', 0x00, 0x01}),
                Arguments.of("zeros", new byte[8192]),
                Arguments.of("truncated mpp", Arrays.copyOf(real, 4096)),
                Arguments.of("half mpp", Arrays.copyOf(real, real.length / 2)),
                Arguments.of("ole2 header then junk", headerThenJunk)
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("garbage")
    void garbageIsRejectedAsUnsupportedFormat(String name, byte[] body) throws Exception {
        String response = perform(body);

        assertThat(response).contains("\"code\":\"unsupported_format\"");
        assertThat(response).doesNotContain("org.mpxj").doesNotContain("org.apache.poi");
    }

    @Test
    void emptyBodyIsRejectedByFramework() throws Exception {
        assertThat(perform(new byte[0])).contains("\"code\":\"bad_request\"");
    }

    private String perform(byte[] body) throws Exception {
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        return mockMvc.perform(post("/parse")
                        .contentType(MediaType.APPLICATION_OCTET_STREAM)
                        .content(body))
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
    }
}
