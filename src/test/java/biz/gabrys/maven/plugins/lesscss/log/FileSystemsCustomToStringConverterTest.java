package biz.gabrys.maven.plugins.lesscss.log;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMapOf;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Spy;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import biz.gabrys.maven.plugins.lesscss.FileSystems.CustomFileSystem;
import biz.gabrys.maven.plugins.lesscss.FileSystems.CustomFileSystem.DateProvider;

@RunWith(MockitoJUnitRunner.class)
public class FileSystemsCustomToStringConverterTest {

    @Spy
    private FileSystemsCustomToStringConverter converter;

    @Test
    public void convert() {
        final CustomFileSystem[] fileSystems = new CustomFileSystem[0];
        doAnswer(new AppendTextAnswer("file-systems")).when(converter).appendFileSystems(any(StringBuilder.class), eq(fileSystems));

        final String result = converter.convert(fileSystems);

        assertThat(result).isEqualTo("[file-systems]");
    }

    @Test
    public void appendFileSystems_arrayIsEmpty_appendsNothing() {
        final StringBuilder text = new StringBuilder();

        converter.appendFileSystems(text, new CustomFileSystem[0]);

        assertThat(text.length()).isZero();
    }

    @Test
    public void appendFileSystems_arrayIsNotEmpty_appendsFileSystems() {
        final StringBuilder text = new StringBuilder();
        final CustomFileSystem fileSystem1 = mock(CustomFileSystem.class);
        doAnswer(new AppendTextAnswer("file-system-1")).when(converter).appendFileSystem(text, fileSystem1);
        final CustomFileSystem fileSystem2 = mock(CustomFileSystem.class);
        doAnswer(new AppendTextAnswer("file-system-2")).when(converter).appendFileSystem(text, fileSystem2);

        converter.appendFileSystems(text, Arrays.asList(fileSystem1, fileSystem2).toArray(new CustomFileSystem[0]));

        assertThat(text.toString()).isEqualTo("{\nfile-system-1\n              }, {\nfile-system-2\n              }");
    }

    @SuppressWarnings("deprecation")
    @Test
    public void appendFileSystem() {
        final StringBuilder text = new StringBuilder();
        final CustomFileSystem fileSystem = mock(CustomFileSystem.class);
        doAnswer(new AppendTextAnswer("class-name")).when(converter).appendClassName(text, null);
        doAnswer(new AppendTextAnswer("cache-content")).when(converter).appendCacheContent(text, fileSystem);
        doAnswer(new AppendTextAnswer("parameters")).when(converter).appendParameters(eq(text), anyMapOf(String.class, String.class));
        doAnswer(new AppendTextAnswer("data-provider")).when(converter).appendDateProvider(text, fileSystem);

        converter.appendFileSystem(text, fileSystem);

        assertThat(text.toString()).isEqualTo("class-name\ncache-content\nparameters\ndata-provider");
    }

    @Test
    public void appendClassName() {
        final StringBuilder text = new StringBuilder();

        converter.appendClassName(text, "org.example.Type");

        assertThat(text.toString()).isEqualTo("                className: org.example.Type");
    }

    @Test
    public void appendCacheContent_cacheContentIsNull_appendsCalculated() {
        final StringBuilder text = new StringBuilder();
        final CustomFileSystem fileSystem = mock(CustomFileSystem.class);
        when(fileSystem.getCacheContent()).thenReturn(null);
        when(fileSystem.isCacheContent()).thenReturn(Boolean.TRUE);

        converter.appendCacheContent(text, fileSystem);

        assertThat(text.toString()).isEqualTo("                cacheContent: null (calculated: true)");
    }

    @Test
    public void appendCacheContent_cacheContentIsNotNull_doesNotAppendCaclulated() {
        final StringBuilder text = new StringBuilder();
        final CustomFileSystem fileSystem = mock(CustomFileSystem.class);
        when(fileSystem.getCacheContent()).thenReturn(Boolean.TRUE);

        converter.appendCacheContent(text, fileSystem);

        assertThat(text.toString()).isEqualTo("                cacheContent: true");
    }

    @Test
    public void appendParameters_parametersIsEmpty() {
        final StringBuilder text = new StringBuilder();
        final Map<String, String> parameters = Collections.emptyMap();

        converter.appendParameters(text, parameters);

        assertThat(text.toString()).isEqualTo("                parameters: {}");
    }

    @Test
    public void appendParameters_parametersContainsOneEntry() {
        final StringBuilder text = new StringBuilder();
        final Map<String, String> parameters = new LinkedHashMap<String, String>();
        parameters.put("name", "value");

        converter.appendParameters(text, parameters);

        final StringBuilder result = new StringBuilder();
        result.append("                parameters: {\n");
        result.append("                  name: value\n");
        result.append("                }");
        assertThat(text.toString()).isEqualTo(result.toString());
    }

    @Test
    public void appendParameters_parametersContainsThreeEntries() {
        final StringBuilder text = new StringBuilder();
        final Map<String, String> parameters = new LinkedHashMap<String, String>();
        parameters.put("param", "value");
        parameters.put("type", null);
        parameters.put("clazz", "org.example.Test");

        converter.appendParameters(text, parameters);

        final StringBuilder result = new StringBuilder();
        result.append("                parameters: {\n");
        result.append("                  param: value\n");
        result.append("                  type: null\n");
        result.append("                  clazz: org.example.Test\n");
        result.append("                }");
        assertThat(text.toString()).isEqualTo(result.toString());
    }

    @Test
    public void appendDateProvider_dateProviderIsNull() {
        final StringBuilder text = new StringBuilder();
        final CustomFileSystem fileSystem = mock(CustomFileSystem.class);

        converter.appendDateProvider(text, fileSystem);

        assertThat(text.toString()).isEqualTo("                dateProvider: null");
    }

    @Test
    public void appendDateProvider_dateProviderIsNotNull_staticMethodIsNull_appendsCalculated() {
        final StringBuilder text = new StringBuilder();
        final DateProvider dateProvider = mock(DateProvider.class);
        when(dateProvider.getClassName()).thenReturn("org.example.DateProvider");
        when(dateProvider.getMethodName()).thenReturn("getDate");
        when(dateProvider.getStaticMethod()).thenReturn(null);
        when(dateProvider.isStaticMethod()).thenReturn(Boolean.FALSE);
        final CustomFileSystem fileSystem = mock(CustomFileSystem.class);
        when(fileSystem.getDateProvider()).thenReturn(dateProvider);

        converter.appendDateProvider(text, fileSystem);

        final StringBuilder result = new StringBuilder();
        result.append("                dateProvider: {\n");
        result.append("                  className: org.example.DateProvider\n");
        result.append("                  methodName: getDate\n");
        result.append("                  staticMethod: null (calculated: false)\n");
        result.append("                }");
        assertThat(text.toString()).isEqualTo(result.toString());
    }

    @Test
    public void appendDateProvider_dateProviderIsNotNull_classNameIsNull_appendsCalculated() {
        final StringBuilder text = new StringBuilder();
        final DateProvider dateProvider = mock(DateProvider.class);
        when(dateProvider.getMethodName()).thenReturn("getDate");
        when(dateProvider.getStaticMethod()).thenReturn(Boolean.TRUE);
        final CustomFileSystem fileSystem = mock(CustomFileSystem.class);
        when(fileSystem.getDateProvider()).thenReturn(dateProvider);
        when(fileSystem.getClassName()).thenReturn("org.example.FileSystem");

        converter.appendDateProvider(text, fileSystem);

        final StringBuilder result = new StringBuilder();
        result.append("                dateProvider: {\n");
        result.append("                  className: null (calculated: org.example.FileSystem)\n");
        result.append("                  methodName: getDate\n");
        result.append("                  staticMethod: true\n");
        result.append("                }");
        assertThat(text.toString()).isEqualTo(result.toString());
    }

    private static class AppendTextAnswer implements Answer<Void> {

        private final String content;

        public AppendTextAnswer(final String content) {
            this.content = content;
        }

        @Override
        public Void answer(final InvocationOnMock invocation) throws Throwable {
            final StringBuilder text = (StringBuilder) invocation.getArguments()[0];
            text.append(content);
            return null;
        }
    }
}
