package io.quarkus.devtools.codestarts.writer;

import io.fabric8.maven.Maven;
import io.fabric8.maven.merge.SmartModelMerger;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.maven.model.Model;

public class MavenPomMergeCodestartFileWriter implements CodestartFileWriter {

    private static final String FLAG = ".pom-merge";

    private final Map<Path, List<Model>> models = new HashMap<>();

    @Override
    public boolean matches(String fileName) {
        return fileName.contains(FLAG);
    }

    @Override
    public String cleanFileName(String fileName) {
        return fileName.replace(FLAG, "");
    }

    @Override
    public void process(String content, Path targetPath, String languageName, Map<String, Object> data) {
        if (!Files.exists(targetPath)) {
            throw new IllegalStateException(
                    "Using .part is not possible when the target file does not exist already: " + targetPath);
        }
        models.putIfAbsent(targetPath, new ArrayList<>());
        models.get(targetPath).add(Maven.readModel(new StringReader(content)));
    }

    @Override
    public void close() {
        for (Map.Entry<Path, List<Model>> entry : models.entrySet()) {
            final SmartModelMerger merger = new SmartModelMerger();
            final Model targetModel = Maven.readModel(entry.getKey());
            for (Model model : entry.getValue()) {
                merger.merge(targetModel, model, true, null);
            }
            Maven.writeModel(targetModel);
        }
    }
}
