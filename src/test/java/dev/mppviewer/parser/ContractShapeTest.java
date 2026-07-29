package dev.mppviewer.parser;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
class ContractShapeTest {

    private static final Pattern FULL_DATE_TIME =
            Pattern.compile("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}");

    @Autowired
    private WebApplicationContext context;

    private JsonNode parse(String file) throws Exception {
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        String json = mockMvc.perform(post("/parse")
                        .contentType(MediaType.APPLICATION_OCTET_STREAM)
                        .content(Files.readAllBytes(Path.of("testdata", file))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        return JsonMapper.builder().build().readTree(json);
    }

    @Test
    void topLevelFieldsMatchContract() throws Exception {
        assertThat(names(parse("real/mpp14baseline.mpp")))
                .containsExactlyInAnyOrder(
                        "contract_version", "project", "calendar", "resources", "tasks", "relations");
    }

    @Test
    void taskFieldsMatchContract() throws Exception {
        assertThat(names(parse("real/mpp14baseline.mpp").get("tasks").get(0)))
                .containsExactlyInAnyOrder(
                        "id", "parent_id", "name", "wbs", "outline_number", "outline_level",
                        "start", "finish", "duration", "percent_complete",
                        "is_summary", "is_milestone", "is_critical",
                        "notes", "baseline", "assignments");
    }

    @Test
    void relationAndCalendarFieldsMatchContract() throws Exception {
        JsonNode root = parse("cyrillic_full.xml");

        assertThat(names(root.get("relations").get(0)))
                .containsExactlyInAnyOrder("id", "predecessor_id", "successor_id", "type", "lag");
        assertThat(names(root.get("relations").get(0).get("lag")))
                .containsExactlyInAnyOrder("value", "units");
        assertThat(names(root.get("calendar")))
                .containsExactlyInAnyOrder("name", "non_working_weekdays", "exceptions");
        assertThat(names(root.get("calendar").get("exceptions").get(0)))
                .containsExactlyInAnyOrder("from", "to", "working", "name");
    }

    @Test
    void everyDateCarriesSeconds() throws Exception {
        for (String file : List.of("real/mpp14baseline.mpp", "cyrillic_full.xml")) {
            List<String> dates = new ArrayList<>();
            collectDates(parse(file), dates);

            assertThat(dates).as("no dates found in %s", file).isNotEmpty();
            for (String date : dates) {
                assertThat(date).as("date in %s", file).matches(FULL_DATE_TIME);
            }
        }
    }

    @Test
    void rootTaskKeepsExplicitNullParent() throws Exception {
        JsonNode root = parse("cyrillic_full.xml").get("tasks").get(0);

        assertThat(root.has("parent_id")).isTrue();
        assertThat(root.get("parent_id").isNull()).isTrue();
    }

    private static List<String> names(JsonNode node) {
        List<String> out = new ArrayList<>();
        node.propertyNames().forEach(out::add);
        return out;
    }

    private static void collectDates(JsonNode node, List<String> out) {
        for (String field : List.of("start", "finish", "from", "to")) {
            JsonNode value = node.get(field);
            if (value != null && value.isString()) {
                out.add(value.stringValue());
            }
        }
        node.values().forEach(child -> {
            if (child.isObject() || child.isArray()) {
                collectDates(child, out);
            }
        });
    }
}
