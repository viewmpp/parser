package dev.mppviewer.parser;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
class ParseEndpointTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc() {
        return MockMvcBuilders.webAppContextSetup(context).build();
    }

    private String parse(String file) throws Exception {
        byte[] bytes = Files.readAllBytes(Path.of("testdata", file));
        return mockMvc().perform(post("/parse")
                        .contentType(MediaType.APPLICATION_OCTET_STREAM)
                        .content(bytes))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
    }

    @Test
    void dumpsRealMpp() throws Exception {
        String json = parse("real/mpp14baseline.mpp");
        Files.createDirectories(Path.of("build"));
        Files.writeString(Path.of("build/sample-mpp14baseline.json"), json, StandardCharsets.UTF_8);
        assertThat(json).contains("\"contract_version\":1");
    }

    @Test
    void dumpsCyrillic() throws Exception {
        String json = parse("cyrillic_full.xml");
        Files.createDirectories(Path.of("build"));
        Files.writeString(Path.of("build/sample-cyrillic.json"), json, StandardCharsets.UTF_8);
        assertThat(json).contains("Фаза");
    }

    @Test
    void rejectsGarbageWithMachineCode() throws Exception {
        String body = mockMvc().perform(post("/parse")
                        .contentType(MediaType.APPLICATION_OCTET_STREAM)
                        .content("this is not a project file".getBytes(StandardCharsets.UTF_8)))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        assertThat(body).contains("\"code\":\"unsupported_format\"");
    }

    @Test
    void neverLeaksLibraryInternals() throws Exception {
        byte[] truncated = new byte[4096];
        System.arraycopy(Files.readAllBytes(Path.of("testdata", "real/mpp14baseline.mpp")),
                0, truncated, 0, truncated.length);

        String body = mockMvc().perform(post("/parse")
                        .contentType(MediaType.APPLICATION_OCTET_STREAM)
                        .content(truncated))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        assertThat(body).doesNotContain("org.mpxj").doesNotContain("org.apache.poi").doesNotContain("java.");
    }
}
