package io.quarkus.devtools.codestarts;

import static io.quarkus.devtools.codestarts.Codestart.BASE_LANGUAGE;

import io.quarkus.devtools.codestarts.reader.CodestartFileReader;
import io.quarkus.devtools.codestarts.reader.QuteCodestartFileReader;
import io.quarkus.devtools.codestarts.writer.AppenderCodestartFileWriter;
import io.quarkus.devtools.codestarts.writer.CodestartFileWriter;
import io.quarkus.devtools.codestarts.writer.MavenPomMergeCodestartFileWriter;
import io.quarkus.devtools.codestarts.writer.SmartConfigMergeCodestartFileWriter;
import io.quarkus.platform.descriptor.QuarkusPlatformDescriptor;
import java.io.Closeable;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

final class CodestartProcessor implements Closeable {

    private final QuarkusPlatformDescriptor descriptor;
    private final String languageName;
    private final Path targetDirectory;
    private final Map<String, Object> data;
    private final List<CodestartFileReader> readers;
    private final List<CodestartFileWriter> writers;

    CodestartProcessor(final QuarkusPlatformDescriptor descriptor, final String languageName, final Path targetDirectory,
            final Map<String, Object> data) {
        this.descriptor = descriptor;
        this.languageName = languageName;
        this.targetDirectory = targetDirectory;
        this.data = data;
        this.readers = newCodestartFileReaders();
        this.writers = newCodestartFileWriters();
    }

    void process(final Codestart codestart) throws IOException {
        descriptor.loadResourcePath(codestart.getResourceName(), p -> {
            resolveDirectoriesToProcessAsStream(p, languageName)
                    .forEach(dirPath -> processCodestartDir(dirPath,
                            CodestartData.buildCodestartData(codestart, languageName, data)));
            return null;
        });
    }

    static Stream<Path> resolveDirectoriesToProcessAsStream(final Path sourceDirectory, final String languageName)
            throws IOException {
        if (!Files.isDirectory(sourceDirectory)) {
            throw new IllegalStateException("Codestart sourceDirectory is not a directory: " + sourceDirectory);
        }
        return Stream.of(BASE_LANGUAGE, languageName)
                .map(sourceDirectory::resolve)
                .filter(Files::isDirectory);
    }

    void processCodestartDir(final Path sourceDirectory, final Map<String, Object> finalData) {

        try {
            final Collection<Path> sources = Files.walk(sourceDirectory)
                    .filter(path -> !path.equals(sourceDirectory))
                    .collect(Collectors.toList());
            for (Path sourcePath : sources) {
                final Path relativePath = sourceDirectory.relativize(sourcePath);
                if (!Files.isDirectory(sourcePath)) {
                    final String fileName = relativePath.getFileName().toString();
                    final Path targetPath = targetDirectory.resolve(relativePath.toString());
                    final Optional<CodestartFileReader> possibleReader = readers.stream()
                            .filter(r -> r.matches(fileName))
                            .findFirst();
                    final Optional<CodestartFileWriter> possibleWriter = writers.stream()
                            .filter(r -> r.matches(fileName))
                            .findFirst();

                    if (!possibleWriter.isPresent() && !possibleReader.isPresent()) {
                        processStaticFile(sourcePath, targetPath);
                    } else {
                        final CodestartFileReader reader = possibleReader.orElse(CodestartFileReader.DEFAULT);
                        final Optional<String> content = reader.read(sourcePath,
                                languageName, finalData);
                        if (content.isPresent()) {
                            final CodestartFileWriter writer = possibleWriter.orElse(CodestartFileWriter.DEFAULT);
                            final String cleanFileName = writer.cleanFileName(reader.cleanFileName(fileName));
                            final Path parent = targetPath.getParent();
                            Files.createDirectories(parent);
                            writer.process(content.get(), parent.resolve(cleanFileName),
                                    languageName, finalData);
                        }
                    }
                }
            }
            ;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

    }

    private static void processStaticFile(Path path, Path targetPath) throws IOException {
        Files.createDirectories(targetPath.getParent());
        Files.copy(path, targetPath, StandardCopyOption.REPLACE_EXISTING);
    }

    void checkTargetDir() throws IOException {
        if (!Files.exists(targetDirectory)) {
            boolean mkdirStatus = targetDirectory.toFile().mkdirs();
            if (!mkdirStatus) {
                throw new IOException("Failed to create the project directory: " + targetDirectory);
            }
            return;
        }
        if (!Files.isDirectory(targetDirectory)) {
            throw new IOException("Project path needs to point to a directory: " + targetDirectory);
        }
        final String[] files = targetDirectory.toFile().list();
        if (files != null && files.length > 0) {
            throw new IOException("You can't create a project when the directory is not empty: " + targetDirectory);
        }
    }

    static List<CodestartFileReader> newCodestartFileReaders() {
        return Collections.unmodifiableList(Arrays.asList(
                new QuteCodestartFileReader()));
    }

    static List<CodestartFileWriter> newCodestartFileWriters() {
        return Collections.unmodifiableList(Arrays.asList(
                new AppenderCodestartFileWriter(),
                new MavenPomMergeCodestartFileWriter(),
                new SmartConfigMergeCodestartFileWriter()));
    }

    @Override
    public void close() throws IOException {
        for (CodestartFileWriter writer : writers) {
            writer.close();
        }
    }
}
