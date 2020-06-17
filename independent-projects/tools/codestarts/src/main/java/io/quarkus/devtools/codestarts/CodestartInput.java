package io.quarkus.devtools.codestarts;

import io.quarkus.bootstrap.model.AppArtifactKey;
import io.quarkus.platform.descriptor.QuarkusPlatformDescriptor;
import java.util.Collection;
import java.util.Map;

public class CodestartInput {
    private final QuarkusPlatformDescriptor descriptor;
    private final Collection<AppArtifactKey> extensions;
    private final boolean includeExamples;
    private final Map<String, Object> data;
    private final Collection<String> codestarts;

    CodestartInput(QuarkusPlatformDescriptor descriptor, Collection<AppArtifactKey> extensions,
            Collection<String> codestarts, boolean includeExamples, Map<String, Object> data) {
        this.descriptor = descriptor;
        this.extensions = extensions;
        this.codestarts = codestarts;
        this.includeExamples = includeExamples;
        this.data = data;
    }

    public static CodestartInputBuilder builder(QuarkusPlatformDescriptor descriptor) {
        return new CodestartInputBuilder(descriptor);
    }

    public QuarkusPlatformDescriptor getDescriptor() {
        return descriptor;
    }

    public Collection<String> getCodestarts() {
        return codestarts;
    }

    public Collection<AppArtifactKey> getExtensions() {
        return extensions;
    }

    public boolean includeExamples() {
        return includeExamples;
    }

    public Map<String, Object> getData() {
        return data;
    }
}
