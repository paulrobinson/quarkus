package io.quarkus.devtools.codestarts;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.quarkus.platform.descriptor.QuarkusPlatformDescriptor;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.io.FilenameUtils;

final class CodestartLoader {
    private static final ObjectMapper YAML_MAPPER = new ObjectMapper(new YAMLFactory())
            .enable(DeserializationFeature.READ_ENUMS_USING_TO_STRING)
            .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS);

    private static final String CODESTARTS_DIR_BUNDLED = "bundled-codestarts";
    private static final String CODESTARTS_DIR_FROM_EXTENSIONS = "codestarts";

    private CodestartLoader() {
    }

    public static List<Codestart> loadAllCodestarts(CodestartInput input) throws IOException {
        return Stream.concat(loadBundledCodestarts(input).stream(),
                loadCodestartsFromExtensions(input).stream()).collect(Collectors.toList());
    }

    public static Collection<Codestart> loadBundledCodestarts(CodestartInput input) throws IOException {
        return loadCodestarts(input.getDescriptor(), CODESTARTS_DIR_BUNDLED);
    }

    public static Collection<Codestart> loadCodestartsFromExtensions(CodestartInput input)
            throws IOException {
        // TODO resolve codestarts which live inside extensions. Using a directory is just a temporary workaround.
        return loadCodestarts(input.getDescriptor(), CODESTARTS_DIR_FROM_EXTENSIONS);
    }

    static Collection<Codestart> loadCodestarts(final QuarkusPlatformDescriptor descriptor, final String directoryName)
            throws IOException {
        return descriptor.loadResourcePath(directoryName,
                path -> toResourceNameWalker(directoryName, path).filter(n -> n.matches(".*/codestart\\.ya?ml$"))
                        .map(n -> {
                            try {
                                final CodestartSpec spec = YAML_MAPPER.readerFor(CodestartSpec.class)
                                        .readValue(descriptor.getTemplate(n));
                                return new Codestart(n.replaceAll("/?codestart\\.ya?ml", ""), spec);
                            } catch (IOException e) {
                                throw new UncheckedIOException("Failed to parse codestart spec: " + n, e);
                            }
                        }).collect(Collectors.toList()));
    }

    private static Stream<String> toResourceNameWalker(final String dirName, final Path dirPath) throws IOException {
        return Files.walk(dirPath).map(p -> resolveResourceName(dirName, dirPath, p));
    }

    private static String resolveResourceName(final String dirName, final Path dirPath, final Path resourcePath) {
        return FilenameUtils.concat(dirName, dirPath.relativize(resourcePath).toString().replace('\\', '/'));
    }
}
