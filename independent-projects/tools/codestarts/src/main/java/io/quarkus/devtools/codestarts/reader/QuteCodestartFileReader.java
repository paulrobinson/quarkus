package io.quarkus.devtools.codestarts.reader;

import io.quarkus.qute.Engine;
import io.quarkus.qute.Expression;
import io.quarkus.qute.ResultMapper;
import io.quarkus.qute.Results;
import io.quarkus.qute.TemplateException;
import io.quarkus.qute.TemplateLocator;
import io.quarkus.qute.TemplateNode;
import io.quarkus.qute.Variant;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;

public class QuteCodestartFileReader implements CodestartFileReader {

    private static final String FLAG = ".qute";
    public static final String INCLUDE_QUTE_FLAG = ".include-qute";

    @Override
    public boolean matches(String fileName) {
        return fileName.contains(FLAG) || fileName.contains(INCLUDE_QUTE_FLAG);
    }

    @Override
    public String cleanFileName(String fileName) {
        return fileName.replace(FLAG, "");
    }

    public Optional<String> read(Path sourcePath, String languageName, Map<String, Object> data) throws IOException {
        if (sourcePath.getFileName().toString().contains(INCLUDE_QUTE_FLAG)) {
            return Optional.empty();
        }
        return Optional.of(readQuteFile(sourcePath, languageName, data));
    }

    public static Engine newEngine() {
        return Engine.builder().addDefaults()
                .addResultMapper(new MissingValueMapper())
                .build();
    }

    public static String readQuteFile(Path path, String languageName, Map<String, Object> data) throws IOException {
        final String content = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
        final Engine engine = Engine.builder().addDefaults()
                .addResultMapper(new MissingValueMapper())
                .removeStandaloneLines(true)
                .addLocator(id -> findIncludeTemplate(path, languageName, id).map(IncludeTemplateLocation::new))
                .build();
        try {
            return engine.parse(content).render(data);
        } catch (TemplateException e) {
            throw new IOException("Error while rendering template: " + path.toString(), e);
        }
    }

    private static Optional<Path> findIncludeTemplate(Path path, String languageName, String name) {
        // FIXME looking at the parent dir is a bit random
        final Path codestartPath = path.getParent().getParent();
        final String includeFileName = name + INCLUDE_QUTE_FLAG;
        final Path languageIncludeTemplate = codestartPath.resolve(languageName + "/" + includeFileName);
        if (Files.isRegularFile(languageIncludeTemplate)) {
            return Optional.of(languageIncludeTemplate);
        }
        final Path baseIncludeTemplate = codestartPath.resolve("base/" + includeFileName);
        if (Files.isRegularFile(baseIncludeTemplate)) {
            return Optional.of(baseIncludeTemplate);
        }
        return Optional.empty();
    }

    private static class IncludeTemplateLocation implements TemplateLocator.TemplateLocation {

        private final Path path;

        private IncludeTemplateLocation(Path path) {
            this.path = path;
        }

        @Override
        public Reader read() {
            try {
                return Files.newBufferedReader(path);
            } catch (IOException e) {
                return null;
            }
        }

        @Override
        public Optional<Variant> getVariant() {
            return Optional.empty();
        }
    }

    static class MissingValueMapper implements ResultMapper {

        public boolean appliesTo(TemplateNode.Origin origin, Object result) {
            return Results.Result.NOT_FOUND.equals(result);
        }

        public String map(Object result, Expression expression) {
            throw new IllegalStateException("Missing required data: {" + expression.toOriginalString() + "}");
        }
    }
}
