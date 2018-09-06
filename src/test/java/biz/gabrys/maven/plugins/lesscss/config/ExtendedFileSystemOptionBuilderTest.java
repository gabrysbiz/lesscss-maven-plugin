package biz.gabrys.maven.plugins.lesscss.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

public class ExtendedFileSystemOptionBuilderTest {

    private ExtendedFileSystemOptionBuilder builder;

    @Before
    public void setup() {
        builder = new ExtendedFileSystemOptionBuilder();
    }

    @Test
    public void build_parametersIsNull_buildsWithEmptyMap() {
        final String className = "className";
        final String dateProviderClassName = "dateProviderClassName";
        final String dateProviderMethodName = "dateProviderMethodName";

        final ExtendedFileSystemOption option = builder.className(className)//
                .cacheContent(true)//
                .dateProvider(dateProviderClassName, dateProviderMethodName, false)//
                .build();

        assertThat(option).isEqualTo(new ExtendedFileSystemOption(className, Collections.<String, String>emptyMap(), true, false,
                new DateProviderConfig(dateProviderClassName, dateProviderMethodName, false)));
    }

    @Test
    public void build_parametersIsNotNull_buildsOption() {
        final String className = "className";
        final Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("key", "value");
        final String dateProviderClassName = "dateProviderClassName";
        final String dateProviderMethodName = "dateProviderMethodName";

        final ExtendedFileSystemOption option = builder.className(className)//
                .parameters(parameters)//
                .cacheRedirects(true)//
                .dateProvider(dateProviderClassName, dateProviderMethodName, false)//
                .build();

        assertThat(option).isEqualTo(new ExtendedFileSystemOption(className, parameters, false, true,
                new DateProviderConfig(dateProviderClassName, dateProviderMethodName, false)));
    }
}
