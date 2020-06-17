package io.quarkus.devtools.codestarts;

import io.quarkus.bootstrap.model.AppArtifactKey;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class CodestartData {

    private CodestartData() {
    }

    public enum LegacySupport {
        BOM_GROUP_ID("quarkus.platform.group-id", "bom_groupId"),
        BOM_ARTIFACT_ID("quarkus.platform.artifact-id", "bom_artifactId"),
        BOM_VERSION("quarkus.platform.version", "bom_version"),
        PROJECT_GROUP_ID("project.group-id", "project_groupId"),
        PROJECT_ARTIFACT_ID("project.artifact-id", "project_artifactId"),
        PROJECT_VERSION("project.version", "project_version"),
        QUARKUS_PLUGIN_GROUP_ID("quarkus.plugin.group-id", "plugin_groupId"),
        QUARKUS_PLUGIN_ARTIFACT_ID("quarkus.plugin.artifact-id", "plugin_artifactId"),
        QUARKUS_PLUGIN_VERSION("quarkus.plugin.version", "plugin_version"),
        QUARKUS_VERSION("quarkus.version", "quarkus_version"),
        BUILDTOOL("buildtool.name", null),
        // MAVEN_REPOSITORIES("maven_repositories"), // TODO add compatibility with repo override
        // MAVEN_PLUGIN_REPOSITORIES("maven_plugin_repositories"), // TODO add compatibility
        ;

        private final String key;
        private final String legacyKey;

        LegacySupport(String key, String legacyKey) {
            this.key = key;
            this.legacyKey = legacyKey;
        }

        public String getKey() {
            return key;
        }

        public String getLegacyKey() {
            return legacyKey;
        }

        public static Map<String, Object> convertFromLegacy(Map<String, Object> legacy) {
            return Maps.unflatten(Stream.of(values())
                    .filter(v -> v.getLegacyKey() != null)
                    .filter(v -> legacy.containsKey(v.getLegacyKey()))
                    .map(v -> new HashMap.SimpleImmutableEntry<>(v.getKey(), legacy.get(v.getLegacyKey())))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
        }
    }

    static Map<String, Object> buildCodestartData(final Codestart codestart, final String languageName,
            final Map<String, Object> data) {
        return Maps.deepMerge(Stream.of(codestart.getLocalData(languageName), data));
    }

    public static Map<String, Object> buildCodestartProjectData(Collection<Codestart> codestarts) {
        final HashMap<String, Object> data = new HashMap<>();
        codestarts.forEach((c) -> data.put("codestart-project." + c.getSpec().getType().toString().toLowerCase() + ".name",
                c.getSpec().getName()));
        return Maps.unflatten(data);
    }

    static Map<String, Object> buildDependenciesData(Stream<Codestart> codestartsStream, String languageName,
            Collection<AppArtifactKey> extensions) {
        final Map<String, List<CodestartSpec.CodestartDep>> depsData = new HashMap<>();
        final List<CodestartSpec.CodestartDep> extensionsAsDeps = extensions.stream()
                .map(k -> k.getGroupId() + ":" + k.getArtifactId()).map(CodestartSpec.CodestartDep::new)
                .collect(Collectors.toList());
        depsData.put("dependencies", new ArrayList<>(extensionsAsDeps));
        depsData.put("test-dependencies", new ArrayList<>());
        codestartsStream
                .flatMap(s -> Stream.of(s.getBaseLanguageSpec(), s.getLanguageSpec(languageName)))
                .forEach(d -> {
                    depsData.get("dependencies").addAll(d.getDependencies());
                    depsData.get("test-dependencies").addAll(d.getTestDependencies());
                });
        return Collections.unmodifiableMap(depsData);
    }

}
