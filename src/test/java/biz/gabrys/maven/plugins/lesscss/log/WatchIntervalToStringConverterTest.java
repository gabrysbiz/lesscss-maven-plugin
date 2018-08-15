package biz.gabrys.maven.plugins.lesscss.log;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import biz.gabrys.maven.plugin.util.timer.TimeSpan;

public class WatchIntervalToStringConverterTest {

    @Test
    public void convert_numberIsSmallerThanZero() {
        final String result = new WatchIntervalToStringConverter().convert(Integer.valueOf(-5));
        assertThat(result).isEqualTo("-5");
    }

    @Test
    public void convert_numberIsEqualToZero() {
        final String result = new WatchIntervalToStringConverter().convert(Integer.valueOf(0));
        assertThat(result).isEqualTo("0");
    }

    @Test
    public void convert_numberIsBiggerThanZero() {
        final Integer value = Integer.valueOf(5);
        final TimeSpan timeSpan = new TimeSpan(5000L);

        final String result = new WatchIntervalToStringConverter().convert(value);

        assertThat(result).isEqualTo(String.format("5 (%s)", timeSpan));
    }
}
