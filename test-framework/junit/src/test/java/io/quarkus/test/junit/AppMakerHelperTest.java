package io.quarkus.test.junit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class AppMakerHelperTest {

    @Test
    public void testInferProjectRootFromMavenStructure(@TempDir Path tempDir) throws IOException {
        // Setup: /temp/my-module/target/test-classes
        Path moduleRoot = tempDir.resolve("my-module");
        Path targetDir = moduleRoot.resolve("target");
        Path testClassesDir = targetDir.resolve("test-classes");
        Files.createDirectories(testClassesDir);

        // Create pom.xml to identify this as a Maven project
        Path pomXml = moduleRoot.resolve("pom.xml");
        Files.writeString(pomXml, "<project></project>");

        // Act
        Path result = AppMakerHelper.inferProjectRoot(testClassesDir);

        // Assert
        assertEquals(moduleRoot, result, "Should infer module root from Maven target/test-classes structure");
    }

    @Test
    public void testInferProjectRootFromGradleStructure(@TempDir Path tempDir) throws IOException {
        // Setup: /temp/my-module/build/classes/java/test
        Path moduleRoot = tempDir.resolve("my-module");
        Path buildDir = moduleRoot.resolve("build");
        Path classesDir = buildDir.resolve("classes").resolve("java").resolve("test");
        Files.createDirectories(classesDir);

        // Create build.gradle to identify this as a Gradle project
        Path buildGradle = moduleRoot.resolve("build.gradle");
        Files.writeString(buildGradle, "// gradle build file");

        // Act
        Path result = AppMakerHelper.inferProjectRoot(classesDir);

        // Assert
        assertEquals(moduleRoot, result, "Should infer module root from Gradle build/classes structure");
    }

    @Test
    public void testInferProjectRootFromGradleKotlinDSL(@TempDir Path tempDir) throws IOException {
        // Setup: /temp/my-module/build/classes/java/test
        Path moduleRoot = tempDir.resolve("my-module");
        Path buildDir = moduleRoot.resolve("build");
        Path classesDir = buildDir.resolve("classes").resolve("java").resolve("test");
        Files.createDirectories(classesDir);

        // Create build.gradle.kts to identify this as a Gradle Kotlin DSL project
        Path buildGradleKts = moduleRoot.resolve("build.gradle.kts");
        Files.writeString(buildGradleKts, "// gradle kotlin build file");

        // Act
        Path result = AppMakerHelper.inferProjectRoot(classesDir);

        // Assert
        assertEquals(moduleRoot, result, "Should infer module root from Gradle Kotlin DSL build.gradle.kts");
    }

    @Test
    public void testInferProjectRootWithoutBuildFile(@TempDir Path tempDir) throws IOException {
        // Setup: /temp/my-module/target/test-classes but NO pom.xml
        Path moduleRoot = tempDir.resolve("my-module");
        Path targetDir = moduleRoot.resolve("target");
        Path testClassesDir = targetDir.resolve("test-classes");
        Files.createDirectories(testClassesDir);

        // No pom.xml or build.gradle created

        // Act
        Path result = AppMakerHelper.inferProjectRoot(testClassesDir);

        // Assert
        // Should fallback to CWD since no build file was found
        Path cwd = Path.of("").normalize().toAbsolutePath();
        assertEquals(cwd, result, "Should fallback to CWD when no build file is found");
    }

    @Test
    public void testInferProjectRootFromNestedMavenModule(@TempDir Path tempDir) throws IOException {
        // Setup: /temp/parent/module1/target/test-classes
        // This simulates a multi-module Maven build
        Path parentRoot = tempDir.resolve("parent");
        Path moduleRoot = parentRoot.resolve("module1");
        Path targetDir = moduleRoot.resolve("target");
        Path testClassesDir = targetDir.resolve("test-classes");
        Files.createDirectories(testClassesDir);

        // Create pom.xml files for both parent and module
        Path parentPom = parentRoot.resolve("pom.xml");
        Files.writeString(parentPom, "<project><packaging>pom</packaging></project>");

        Path modulePom = moduleRoot.resolve("pom.xml");
        Files.writeString(modulePom, "<project></project>");

        // Act
        Path result = AppMakerHelper.inferProjectRoot(testClassesDir);

        // Assert
        // Should return module1, NOT parent
        assertEquals(moduleRoot, result, "Should infer the module root, not the parent root in multi-module builds");
    }

    @Test
    public void testInferProjectRootFromShallowPath(@TempDir Path tempDir) throws IOException {
        // Setup: Just a simple directory, not under target or build
        Path simpleDir = tempDir.resolve("some-dir");
        Files.createDirectories(simpleDir);

        // Act
        Path result = AppMakerHelper.inferProjectRoot(simpleDir);

        // Assert
        // Should fallback to CWD
        Path cwd = Path.of("").normalize().toAbsolutePath();
        assertEquals(cwd, result, "Should fallback to CWD for non-standard directory structures");
    }

    @Test
    public void testInferProjectRootFromAbsolutePath(@TempDir Path tempDir) throws IOException {
        // Setup: Ensure the path is absolute
        Path moduleRoot = tempDir.resolve("my-module").toAbsolutePath();
        Path targetDir = moduleRoot.resolve("target");
        Path testClassesDir = targetDir.resolve("test-classes");
        Files.createDirectories(testClassesDir);

        Path pomXml = moduleRoot.resolve("pom.xml");
        Files.writeString(pomXml, "<project></project>");

        // Act
        Path result = AppMakerHelper.inferProjectRoot(testClassesDir);

        // Assert
        assertTrue(result.isAbsolute(), "Result should be an absolute path");
        assertEquals(moduleRoot, result, "Should correctly handle absolute paths");
    }
}
