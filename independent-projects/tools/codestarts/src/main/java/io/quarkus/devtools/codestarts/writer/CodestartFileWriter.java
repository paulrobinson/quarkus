package io.quarkus.devtools.codestarts.writer;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public interface CodestartFileWriter extends Closeable {

    CodestartFileWriter DEFAULT = new DefaultCodestartFileWriter();

    boolean matches(String fileName);

    String cleanFileName(String fileName);

    void process(String content, Path targetPath, String languageName, Map<String, Object> data) throws IOException;

    @Override
    default void close() throws IOException {

    }

    class DefaultCodestartFileWriter implements CodestartFileWriter {

        @Override
        public boolean matches(String fileName) {
            return false;
        }

        @Override
        public String cleanFileName(String fileName) {
            return fileName;
        }

        @Override
        public void process(String content, Path targetPath, String languageName, Map<String, Object> data) throws IOException {
            Files.write(targetPath, content.getBytes());
        }
    }

}
