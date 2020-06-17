package io.quarkus.devtools.codestarts.writer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.quarkus.devtools.codestarts.Maps;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class SmartConfigMergeCodestartFileWriter implements CodestartFileWriter {

    private static final ObjectMapper YAML_MAPPER = new ObjectMapper(new YAMLFactory());
    private static final String FLAG = ".smart-config";

    private final Map<Path, Map<String, Object>> configs = new HashMap<>();

    @Override
    public boolean matches(String fileName) {
        return fileName.contains(FLAG);
    }

    @Override
    public String cleanFileName(String fileName) {
        return fileName.replace(FLAG, "");
    }

    @Override
    @SuppressWarnings({ "rawtypes" })
    public void process(String content, Path targetPath, String languageName, Map<String, Object> data) throws IOException {
        final String fileName = targetPath.getFileName().toString();
        if (!fileName.endsWith(".yml")) {
            throw new IllegalStateException("Invalid config-merge file, should be .yml: " + fileName);
        }

        final String configType = getConfigType(data);
        final Path typedTargetPath = getTypedTargetPath(configType, targetPath);

        configs.putIfAbsent(typedTargetPath, new HashMap<>());
        if (!content.trim().isEmpty()) {
            final Map o = YAML_MAPPER.readerFor(Map.class).readValue(content);
            Maps.deepMerge(configs.get(typedTargetPath), o);
        }
    }

    @Override
    public void close() throws IOException {
        for (Map.Entry<Path, Map<String, Object>> config : configs.entrySet()) {
            if (Files.exists(config.getKey())) {
                throw new IllegalStateException("This config file should not exists: " + config.getKey().toString());
            }
            final String fileName = config.getKey().getFileName().toString();
            if (fileName.endsWith(".yml")) {
                writeYamlConfig(config);
            } else if (fileName.endsWith(".properties")) {
                writePropertiesConfig(config);
            } else {
                throw new IllegalStateException("Invalid file type: " + fileName);
            }

        }
    }

    private static void writeYamlConfig(Map.Entry<Path, Map<String, Object>> config) throws IOException {
        YAML_MAPPER.writerFor(Map.class).writeValue(config.getKey().toFile(), config.getValue());
    }

    private static void writePropertiesConfig(Map.Entry<Path, Map<String, Object>> config) throws IOException {
        final StringBuilder builder = new StringBuilder();
        final HashMap<String, String> flat = new HashMap<>();
        flatten("", flat, config.getValue());
        for (Map.Entry<String, String> entry : flat.entrySet()) {
            builder.append(entry.getKey()).append("=").append(entry.getValue()).append("\n");
        }
        Files.write(config.getKey(), builder.toString().getBytes());
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static void flatten(String prefix, Map<String, String> target, Map<String, ?> map) {
        for (Map.Entry entry : map.entrySet()) {
            if (entry.getValue() instanceof Map) {
                flatten(entry.getKey() + ".", target, (Map) entry.getValue());
            } else {
                // TODO: handle different types of values
                target.put(prefix + entry.getKey(), entry.getValue().toString());
            }
        }
    }

    private static Path getTypedTargetPath(String configType, Path targetPath) {
        if (Objects.equals(configType, "config-properties")) {
            return targetPath.getParent().resolve(targetPath.getFileName().toString().replace(".yml", ".properties"));
        }
        if (Objects.equals(configType, "config-yaml")) {
            return targetPath;
        }
        throw new IllegalStateException("Unsupported config type: " + configType);
    }

    private static String getConfigType(Map<String, Object> data) {
        final Optional<String> config = Maps.getNestedDataValue(data, "codestart-project.config.name");
        return config.orElseThrow(() -> new IllegalStateException("Config type is required"));
    }
}
