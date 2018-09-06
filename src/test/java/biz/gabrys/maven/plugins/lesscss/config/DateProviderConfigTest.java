package biz.gabrys.maven.plugins.lesscss.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class DateProviderConfigTest {

    @Test(expected = IllegalArgumentException.class)
    public void constructs_classNameIsNull_throwsException() {
        new DateProviderConfig(null, "methodName", false);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructs_classNameIsBlank_throwsException() {
        new DateProviderConfig(" ", "methodName", false);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructs_methodNameIsNull_throwsException() {
        new DateProviderConfig("className", null, true);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructs_methodNameIsBlank_throwsException() {
        new DateProviderConfig("className", " ", true);
    }

    @Test
    public void hashCode_objectsAreTheSame_returnsTheSameNumber() {
        final DateProviderConfig config1 = new DateProviderConfig("className", "methodName", true);
        final DateProviderConfig config2 = new DateProviderConfig("className", "methodName", true);

        final int hashCode1 = config1.hashCode();
        final int hashCode2 = config2.hashCode();

        assertThat(hashCode1).isEqualTo(hashCode2);
    }

    @Test
    public void hashCode_classNamesAreDifferent_returnsDifferentNumbers() {
        final DateProviderConfig config1 = new DateProviderConfig("className1", "methodName", true);
        final DateProviderConfig config2 = new DateProviderConfig("className2", "methodName", true);

        final int hashCode1 = config1.hashCode();
        final int hashCode2 = config2.hashCode();

        assertThat(hashCode1).isNotEqualTo(hashCode2);
    }

    @Test
    public void hashCode_methodNamesAreDifferent_returnsDifferentNumbers() {
        final DateProviderConfig config1 = new DateProviderConfig("className", "methodName1", true);
        final DateProviderConfig config2 = new DateProviderConfig("className", "methodName2", true);

        final int hashCode1 = config1.hashCode();
        final int hashCode2 = config2.hashCode();

        assertThat(hashCode1).isNotEqualTo(hashCode2);
    }

    @Test
    public void hashCode_staticMethodsAreDifferent_returnsDifferentNumbers() {
        final DateProviderConfig config1 = new DateProviderConfig("className", "methodName", true);
        final DateProviderConfig config2 = new DateProviderConfig("className", "methodName", false);

        final int hashCode1 = config1.hashCode();
        final int hashCode2 = config2.hashCode();

        assertThat(hashCode1).isNotEqualTo(hashCode2);
    }

    @Test
    public void equals_objectsAreTheSame_returnsTrue() {
        final DateProviderConfig config1 = new DateProviderConfig("className", "methodName", true);
        final DateProviderConfig config2 = new DateProviderConfig("className", "methodName", true);

        assertThat(config1.equals(config1)).isTrue();
        assertThat(config1.equals(config2)).isTrue();
        assertThat(config2.equals(config1)).isTrue();
    }

    @Test
    public void equals_classNamesAreDifferent_returnsFalse() {
        final DateProviderConfig config1 = new DateProviderConfig("className1", "methodName", true);
        final DateProviderConfig config2 = new DateProviderConfig("className2", "methodName", true);

        assertThat(config1.equals(config2)).isFalse();
        assertThat(config2.equals(config1)).isFalse();
    }

    @Test
    public void equals_methodNamesAreDifferent_returnsFalse() {
        final DateProviderConfig config1 = new DateProviderConfig("className", "methodName1", true);
        final DateProviderConfig config2 = new DateProviderConfig("className", "methodName2", true);

        assertThat(config1.equals(config2)).isFalse();
        assertThat(config2.equals(config1)).isFalse();
    }

    @Test
    public void equals_staticMethodsAreDifferent_returnsFalse() {
        final DateProviderConfig config1 = new DateProviderConfig("className", "methodName", true);
        final DateProviderConfig config2 = new DateProviderConfig("className", "methodName", false);

        assertThat(config1.equals(config2)).isFalse();
        assertThat(config2.equals(config1)).isFalse();
    }

    @Test
    public void equals_nullAndOtherClass_returnFalse() {
        final DateProviderConfig config = new DateProviderConfig("className", "methodName", true);

        assertThat(config.equals(new Object())).isFalse();
        assertThat(config.equals(null)).isFalse();
    }
}
