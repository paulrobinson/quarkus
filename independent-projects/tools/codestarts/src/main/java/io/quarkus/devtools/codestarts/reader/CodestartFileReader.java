package io.quarkus.devtools.codestarts.reader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;

public interface CodestartFileReader {

    CodestartFileReader DEFAULT = new DefaultCodestartFileReader();

    boolean matches(String fileName);

    String cleanFileName(String fileName);

    Optional<String> read(Path sourcePath, String languageName, Map<String, Object> data) throws IOException;

    class DefaultCodestartFileReader implements CodestartFileReader {

        @Override
        public boolean matches(String fileName) {
            return false;
        }

        @Override
        public String cleanFileName(String fileName) {
            return fileName;
        }

        @Override
        public Optional<String> read(Path sourcePath, String languageName, Map<String, Object> data) throws IOException {
            return Optional.of(new String(Files.readAllBytes(sourcePath), StandardCharsets.UTF_8));
        }
    }
}
