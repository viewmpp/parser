package dev.mppviewer.parser.util;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


@Component
public class Vcs {

    public static String version() {
        try (InputStream in = Vcs.class.getResourceAsStream("/git.properties")) {
            if (in == null) {
                return "dev mode";
            }

            Properties p = new Properties();
            p.load(in);

            String revision = p.getProperty("git.commit.id.abbrev", "");
            boolean dirty = Boolean.parseBoolean(
                    p.getProperty("git.dirty", "false"));

            if (revision.isEmpty()) {
                return "dev mode";
            }

            return dirty ? revision + "-dirty" : revision;
        } catch (IOException e) {
            return "dev mode";
        }
    }
}
