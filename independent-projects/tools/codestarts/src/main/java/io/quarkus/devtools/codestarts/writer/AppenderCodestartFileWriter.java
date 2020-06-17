package io.quarkus.devtools.codestarts.writer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class AppenderCodestartFileWriter implements CodestartFileWriter {

    private static final String FLAG = ".append";

    private final Map<Path, StringBuilder> files = new HashMap<>();

    @Override
    public boolean matches(String fileName) {
        return fileName.contains(FLAG);
    }

    @Override
    public String cleanFileName(String fileName) {
        return fileName.replace(FLAG, "");
    }

    @Override
    public void process(String content, Path targetPath, String languageName, Map<String, Object> data) throws IOException {
        files.putIfAbsent(targetPath, new StringBuilder());
        final StringBuilder builder = files.get(targetPath);
        builder.append(builder.length() > 0 ? "\n" + content : content);
    }

    @Override
    public void close() throws IOException {
        for (Map.Entry<Path, StringBuilder> entry : files.entrySet()) {
            Files.write(entry.getKey(), entry.getValue().toString().getBytes());
        }
    }
}
