package io.quarkus.devtools.codestarts;

import static io.quarkus.devtools.codestarts.CodestartData.LegacySupport.BUILDTOOL;
import static io.quarkus.devtools.codestarts.CodestartLoader.loadAllCodestarts;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Codestarts {

    public static CodestartProject prepareProject(final CodestartInput input) throws IOException {
        final Optional<String> buildtool = Maps.getNestedDataValue(input.getData(), BUILDTOOL.getKey());
        final Set<String> selectedCodestartNames = Stream.concat(
                input.getCodestarts().stream(),
                Stream.of(buildtool.orElse(null)).filter(Objects::nonNull))
                .collect(Collectors.toSet());

        final List<Codestart> allCodestarts = loadAllCodestarts(input);

        final List<Codestart> selectedCodestarts = new ArrayList<>();
        selectedCodestarts.addAll(resolveSelectedBaseCodestarts(allCodestarts, selectedCodestartNames));
        selectedCodestarts.addAll(resolveSelectedExtraCodestarts(input, selectedCodestartNames, allCodestarts));

        // Hack for CommandMode activation
        if (input.includeExamples() && selectedCodestarts.stream().noneMatch(c -> c.getSpec().isExample())) {
            final Codestart commandmodeExampleCodestart = allCodestarts.stream()
                    .filter(c -> c.getSpec().getName().equals("commandmode-example"))
                    .findFirst().orElseThrow(() -> new IllegalStateException("commandmode-example codestart not found"));
            selectedCodestarts.add(commandmodeExampleCodestart);
        }

        return new CodestartProject(input, selectedCodestarts);
    }

    public static void generateProject(final CodestartProject codestartProject, final Path targetDirectory) throws IOException {
        final String languageName = codestartProject.getLanguageName();
        final Map<String, Object> data = Maps.deepMerge(Stream.of(
                codestartProject.getSharedData(),
                codestartProject.getDepsData(),
                codestartProject.getCodestartProjectData()));
        CodestartProcessor processor = new CodestartProcessor(codestartProject.getCodestartInput().getDescriptor(),
                languageName, targetDirectory, data);
        processor.checkTargetDir();
        for (Codestart codestart : codestartProject.getCodestarts()) {
            processor.process(codestart);
        }
        processor.close();
    }

    private static Collection<Codestart> resolveSelectedExtraCodestarts(CodestartInput input,
            Set<String> selectedCodestartNames,
            Collection<Codestart> allCodestarts) {
        return allCodestarts.stream()
                .filter(c -> !c.getSpec().getType().isBase())
                .filter(c -> c.getSpec().isPreselected() || selectedCodestartNames.contains(c.getSpec().getRef()))
                .filter(c -> !c.getSpec().isExample() || input.includeExamples())
                .collect(Collectors.toList());
    }

    private static Collection<Codestart> resolveSelectedBaseCodestarts(Collection<Codestart> allCodestarts,
            Set<String> selectedCodestartNames) {

        return allCodestarts.stream()
                .filter(c -> c.getSpec().getType().isBase())
                .filter(c -> c.getSpec().isFallback() || selectedCodestartNames.contains(c.getSpec().getRef()))
                .filter(c -> !c.getSpec().isExample())
                .collect(Collectors.toMap(c -> c.getSpec().getType(), c -> c, (a, b) -> {
                    if (a.getSpec().isFallback() && b.getSpec().isFallback()) {
                        throw new IllegalStateException(
                                "Multiple fallback found for a codestart that must be single: " + a.getSpec().getType());
                    }
                    if (!a.getSpec().isFallback() && !b.getSpec().isFallback()) {
                        throw new IllegalStateException(
                                "Multiple selection for a codestart that must be single: " + a.getSpec().getType());
                    }
                    return !a.getSpec().isFallback() ? a : b;
                })).values();
    }

}
