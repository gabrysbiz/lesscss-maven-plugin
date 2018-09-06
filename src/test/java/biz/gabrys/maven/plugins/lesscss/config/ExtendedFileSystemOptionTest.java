package biz.gabrys.maven.plugins.lesscss.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class ExtendedFileSystemOptionTest {

    @Test(expected = IllegalArgumentException.class)
    public void constructs_dateProviderConfigIsNull_throwsException() {
        new ExtendedFileSystemOption("className", Collections.<String, String>emptyMap(), false, false, null);
    }

    @Test
    public void hashCode_objectsAreTheSame_returnsTheSameNumber() {
        final Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("key", "value");
        final DateProviderConfig dateProviderConfig = mock(DateProviderConfig.class);
        final ExtendedFileSystemOption option1 = new ExtendedFileSystemOption("className", parameters, true, false, dateProviderConfig);
        final ExtendedFileSystemOption option2 = new ExtendedFileSystemOption("className", parameters, true, false, dateProviderConfig);

        final int hashCode1 = option1.hashCode();
        final int hashCode2 = option2.hashCode();

        assertThat(hashCode1).isEqualTo(hashCode2);
    }

    @Test
    public void hashCode_cacheContentsAreDifferent_returnsDifferentNumbers() {
        final Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("key", "value");
        final DateProviderConfig dateProviderConfig = mock(DateProviderConfig.class);
        final ExtendedFileSystemOption option1 = new ExtendedFileSystemOption("className", parameters, true, false, dateProviderConfig);
        final ExtendedFileSystemOption option2 = new ExtendedFileSystemOption("className", parameters, false, false, dateProviderConfig);

        final int hashCode1 = option1.hashCode();
        final int hashCode2 = option2.hashCode();

        assertThat(hashCode1).isNotEqualTo(hashCode2);
    }

    @Test
    public void hashCode_cacheRedirectsAreDifferent_returnsDifferentNumbers() {
        final Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("key", "value");
        final DateProviderConfig dateProviderConfig = mock(DateProviderConfig.class);
        final ExtendedFileSystemOption option1 = new ExtendedFileSystemOption("className", parameters, true, false, dateProviderConfig);
        final ExtendedFileSystemOption option2 = new ExtendedFileSystemOption("className", parameters, true, true, dateProviderConfig);

        final int hashCode1 = option1.hashCode();
        final int hashCode2 = option2.hashCode();

        assertThat(hashCode1).isNotEqualTo(hashCode2);
    }

    @Test
    public void hashCode_dateProviderConfigsAreDifferent_returnsDifferentNumbers() {
        final Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("key", "value");
        final DateProviderConfig dateProviderConfig1 = mock(DateProviderConfig.class);
        final DateProviderConfig dateProviderConfig2 = mock(DateProviderConfig.class);
        final ExtendedFileSystemOption option1 = new ExtendedFileSystemOption("className", parameters, true, false, dateProviderConfig1);
        final ExtendedFileSystemOption option2 = new ExtendedFileSystemOption("className", parameters, true, false, dateProviderConfig2);

        final int hashCode1 = option1.hashCode();
        final int hashCode2 = option2.hashCode();

        assertThat(hashCode1).isNotEqualTo(hashCode2);
    }

    @Test
    public void equals_objectsAreTheSame_returnsTrue() {
        final Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("key", "value");
        final DateProviderConfig dateProviderConfig = mock(DateProviderConfig.class);
        final ExtendedFileSystemOption option1 = new ExtendedFileSystemOption("className", parameters, true, false, dateProviderConfig);
        final ExtendedFileSystemOption option2 = new ExtendedFileSystemOption("className", parameters, true, false, dateProviderConfig);

        assertThat(option1.equals(option1)).isTrue();
        assertThat(option1.equals(option2)).isTrue();
        assertThat(option2.equals(option1)).isTrue();
    }

    @Test
    public void equals_cacheContentsAreDifferent_returnsFalse() {
        final Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("key", "value");
        final DateProviderConfig dateProviderConfig = mock(DateProviderConfig.class);
        final ExtendedFileSystemOption option1 = new ExtendedFileSystemOption("className", parameters, true, false, dateProviderConfig);
        final ExtendedFileSystemOption option2 = new ExtendedFileSystemOption("className", parameters, false, false, dateProviderConfig);

        assertThat(option1.equals(option2)).isFalse();
        assertThat(option2.equals(option1)).isFalse();
    }

    @Test
    public void equals_cacheRedirectsAreDifferent_returnsFalse() {
        final Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("key", "value");
        final DateProviderConfig dateProviderConfig = mock(DateProviderConfig.class);
        final ExtendedFileSystemOption option1 = new ExtendedFileSystemOption("className", parameters, true, true, dateProviderConfig);
        final ExtendedFileSystemOption option2 = new ExtendedFileSystemOption("className", parameters, true, false, dateProviderConfig);

        assertThat(option1.equals(option2)).isFalse();
        assertThat(option2.equals(option1)).isFalse();
    }

    @Test
    public void equals_dateProviderConfigsAreDifferent_returnsFalse() {
        final Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("key", "value");
        final DateProviderConfig dateProviderConfig1 = mock(DateProviderConfig.class);
        final DateProviderConfig dateProviderConfig2 = mock(DateProviderConfig.class);
        final ExtendedFileSystemOption option1 = new ExtendedFileSystemOption("className", parameters, true, false, dateProviderConfig1);
        final ExtendedFileSystemOption option2 = new ExtendedFileSystemOption("className", parameters, true, false, dateProviderConfig2);

        assertThat(option1.equals(option2)).isFalse();
        assertThat(option2.equals(option1)).isFalse();
    }

    @Test
    public void equals_nullAndOtherClass_returnFalse() {
        final Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("key", "value");
        final DateProviderConfig dateProviderConfig = mock(DateProviderConfig.class);
        final ExtendedFileSystemOption option = new ExtendedFileSystemOption("className", parameters, true, true, dateProviderConfig);

        assertThat(option.equals(new Object())).isFalse();
        assertThat(option.equals(null)).isFalse();
    }
}
