package io.quarkus.devtools.codestarts;

import io.quarkus.bootstrap.model.AppArtifactKey;
import io.quarkus.dependencies.Extension;
import io.quarkus.platform.descriptor.QuarkusPlatformDescriptor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class CodestartInputBuilder {
    private QuarkusPlatformDescriptor descriptor;
    private Collection<AppArtifactKey> extensions = new ArrayList<>();
    private Collection<String> codestarts = new ArrayList<>();
    private boolean includeExamples = false;
    private Map<String, Object> data = new HashMap<>();

    CodestartInputBuilder(QuarkusPlatformDescriptor descriptor) {
        this.descriptor = descriptor;
    }

    public CodestartInputBuilder addExtensions(Collection<AppArtifactKey> extensions) {
        this.extensions.addAll(extensions);
        this.addCodestarts(extractCodestartNamesFromExtensions(descriptor, extensions));
        return this;
    }

    public CodestartInputBuilder addExtension(AppArtifactKey extension) {
        return this.addExtensions(Collections.singletonList(extension));
    }

    public CodestartInputBuilder addCodestarts(Collection<String> codestarts) {
        this.codestarts.addAll(codestarts);
        return this;
    }

    public CodestartInputBuilder includeExamples() {
        return includeExamples(true);
    }

    public CodestartInputBuilder includeExamples(boolean includeExamples) {
        this.includeExamples = includeExamples;
        return this;
    }

    public CodestartInputBuilder addData(Map<String, Object> data) {
        this.data.putAll(data);
        return this;
    }

    public CodestartInputBuilder addCodestart(String name) {
        this.codestarts.add(name);
        return this;
    }

    public CodestartInputBuilder putData(String key, Object value) {
        this.data.put(key, value);
        return this;
    }

    public CodestartInput build() {
        return new CodestartInput(descriptor, extensions, codestarts, includeExamples, Maps.unflatten(data));
    }

    private static Set<String> extractCodestartNamesFromExtensions(QuarkusPlatformDescriptor descriptor,
            Collection<AppArtifactKey> extensions) {
        return descriptor.getExtensions().stream()
                .filter(e -> extensions
                        .contains(new AppArtifactKey(e.getGroupId(), e.getArtifactId(), e.getClassifier(),
                                e.getType() == null ? "jar" : e.getType())))
                .map(Extension::getCodestart)
                .collect(Collectors.toSet());
    }
}
