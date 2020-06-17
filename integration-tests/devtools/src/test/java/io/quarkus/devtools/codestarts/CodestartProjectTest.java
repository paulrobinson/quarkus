package io.quarkus.devtools.codestarts;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.quarkus.bootstrap.model.AppArtifactKey;
import io.quarkus.devtools.PlatformAwareTestBase;
import io.quarkus.devtools.ProjectTestUtil;

class CodestartProjectTest extends PlatformAwareTestBase {

    private static final Path projectPath = Paths.get("target/codestarts-test");

    @BeforeAll
    static void setUp() throws IOException {
        ProjectTestUtil.delete(projectPath.toFile());
    }

    private Map<String, Object> getTestInputData() {
        return getTestInputData(null);
    }

    private Map<String, Object> getTestInputData(final Map<String, Object> override) {
        final HashMap<String, Object> data = new HashMap<>();
        data.put("project.version", "1.0.0-codestart");
        data.put("quarkus.platform.group-id", getPlatformDescriptor().getBomGroupId());
        data.put("quarkus.platform.artifact-id", getPlatformDescriptor().getBomArtifactId());
        data.put("quarkus.platform.version", "1.5.2.Final");
        data.put("quarkus.plugin.group-id", "io.quarkus");
        data.put("quarkus.plugin.artifact-id", "quarkus-maven-plugin");
        data.put("quarkus.plugin.version", "1.5.2.Final");
        if (override != null)
            data.putAll(override);
        return data;
    }

    @Test
    void generateCodestartProjectEmpty() throws IOException {
        final CodestartInput input = CodestartInput.builder(getPlatformDescriptor())
                .addData(getTestInputData())
                .build();
        final CodestartProject codestartProject = Codestarts.prepareProject(input);
        Codestarts.generateProject(codestartProject, projectPath.resolve("empty"));
    }

    @Test
    void generateCodestartProjectEmptyWithExamples() throws IOException {
        final CodestartInput input = CodestartInput.builder(getPlatformDescriptor())
                .includeExamples()
                .addData(getTestInputData())
                .build();
        final CodestartProject codestartProject = Codestarts.prepareProject(input);
        Codestarts.generateProject(codestartProject, projectPath.resolve("empty-examples"));
    }

    @Test
    void generateCodestartProjectMavenResteasyJava() throws IOException {
        final CodestartInput input = CodestartInput.builder(getPlatformDescriptor())
                .includeExamples()
                .addExtension(AppArtifactKey.fromString("io.quarkus:quarkus-resteasy"))
                .addData(getTestInputData())
                .build();
        final CodestartProject codestartProject = Codestarts.prepareProject(input);
        Codestarts.generateProject(codestartProject, projectPath.resolve("maven-resteasy-java"));
    }

    @Test
    void generateCodestartProjectMavenResteasyKotlin() throws IOException {
        final CodestartInput input = CodestartInput.builder(getPlatformDescriptor())
                .includeExamples()
                .addExtension(AppArtifactKey.fromString("io.quarkus:quarkus-resteasy"))
                .addExtension(AppArtifactKey.fromString("io.quarkus:quarkus-kotlin"))
                .addData(getTestInputData())
                .build();
        final CodestartProject codestartProject = Codestarts.prepareProject(input);
        Codestarts.generateProject(codestartProject, projectPath.resolve("maven-resteasy-kotlin"));
    }

    @Test
    void generateCodestartProjectMavenResteasyScala() throws IOException {
        final CodestartInput input = CodestartInput.builder(getPlatformDescriptor())
                .includeExamples()
                .addExtension(AppArtifactKey.fromString("io.quarkus:quarkus-resteasy"))
                .addExtension(AppArtifactKey.fromString("io.quarkus:quarkus-scala"))
                .addData(getTestInputData())
                .build();
        final CodestartProject codestartProject = Codestarts.prepareProject(input);
        Codestarts.generateProject(codestartProject, projectPath.resolve("maven-resteasy-scala"));
    }

    @Test
    void generateCodestartProjectGradleResteasyJava() throws IOException {
        final CodestartInput input = CodestartInput.builder(getPlatformDescriptor())
                .includeExamples()
                .addExtension(AppArtifactKey.fromString("io.quarkus:quarkus-resteasy"))
                .addData(getTestInputData())
                .putData("buildtool.name", "gradle")
                .build();
        final CodestartProject codestartProject = Codestarts.prepareProject(input);
        Codestarts.generateProject(codestartProject, projectPath.resolve("gradle-resteasy-java"));
    }

    @Test
    void generateCodestartProjectGradleResteasyKotlin() throws IOException {
        final CodestartInput input = CodestartInput.builder(getPlatformDescriptor())
                .includeExamples()
                .addExtension(AppArtifactKey.fromString("io.quarkus:quarkus-resteasy"))
                .addExtension(AppArtifactKey.fromString("io.quarkus:quarkus-kotlin"))
                .putData("buildtool.name", "gradle")
                .addData(getTestInputData())
                .build();
        final CodestartProject codestartProject = Codestarts.prepareProject(input);
        Codestarts.generateProject(codestartProject, projectPath.resolve("gradle-resteasy-kotlin"));
    }

    @Test
    void generateCodestartProjectGradleResteasyScala() throws IOException {
        final CodestartInput input = CodestartInput.builder(getPlatformDescriptor())
                .includeExamples()
                .addExtension(AppArtifactKey.fromString("io.quarkus:quarkus-resteasy"))
                .addExtension(AppArtifactKey.fromString("io.quarkus:quarkus-scala"))
                .addData(getTestInputData())
                .putData("buildtool.name", "gradle")
                .build();
        final CodestartProject codestartProject = Codestarts.prepareProject(input);
        Codestarts.generateProject(codestartProject, projectPath.resolve("gradle-resteasy-scala"));
    }

    @Test
    void generateCodestartProjectMavenOptaplannerJava() throws IOException {
        final CodestartInput input = CodestartInput.builder(getPlatformDescriptor())
                .includeExamples()
                .addCodestart("optaplanner")
                .addData(getTestInputData())
                .build();
        final CodestartProject codestartProject = Codestarts.prepareProject(input);
        Codestarts.generateProject(codestartProject, projectPath.resolve("maven-optaplanner-java"));
    }

    @Test
    void generateCodestartProjectMavenOptaplannerJavaYamlConfig() throws IOException {
        final CodestartInput input = CodestartInput.builder(getPlatformDescriptor())
                .includeExamples()
                .addExtension(AppArtifactKey.fromString("io.quarkus:quarkus-config-yaml"))
                .addCodestart("optaplanner")
                .addData(getTestInputData())
                .build();
        final CodestartProject codestartProject = Codestarts.prepareProject(input);
        Codestarts.generateProject(codestartProject, projectPath.resolve("maven-optaplanner-java-yaml-config"));
    }

}
