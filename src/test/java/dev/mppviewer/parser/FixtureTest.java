package dev.mppviewer.parser;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
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
class FixtureTest {

    private static final Path FIXTURES = Path.of("fixtures");

    @Autowired
    private WebApplicationContext context;

    @ParameterizedTest(name = "{1}")
    @CsvSource({
            "real/Xtask-dates-project2003-mpp8.mpp, mpp8.json",
            "real/mpp9baseline.mpp,                 mpp9.json",
            "real/mpp12baseline.mpp,                mpp12.json",
            "real/mpp14baseline.mpp,                mpp14baseline.json",
            "cyrillic_full.xml,                     cyrillic.json",
            "mspdi.xml,                             mspdi.json",
            "real/large.mpp,                        large.json"
    })
    void outputMatchesFixture(String source, String fixture) throws Exception {
        String actual = parse(source.trim());
        Path path = FIXTURES.resolve(fixture.trim());

        if ("1".equals(System.getenv("UPDATE_FIXTURES"))) {
            Files.createDirectories(FIXTURES);
            Files.writeString(path, actual, StandardCharsets.UTF_8);
            return;
        }

        assertThat(path)
                .as("fixture is missing — regenerate deliberately with UPDATE_FIXTURES=1")
                .exists();

        assertThat(actual)
                .as("output drifted from %s — review the change, then UPDATE_FIXTURES=1", path)
                .isEqualTo(Files.readString(path, StandardCharsets.UTF_8));
    }

    private String parse(String file) throws Exception {
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        return mockMvc.perform(post("/parse")
                        .contentType(MediaType.APPLICATION_OCTET_STREAM)
                        .content(Files.readAllBytes(Path.of("testdata", file))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
    }
}
