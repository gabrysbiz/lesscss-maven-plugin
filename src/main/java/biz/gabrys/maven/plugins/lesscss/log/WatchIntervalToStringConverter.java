/*
 * LessCSS Maven Plugin
 * http://lesscss-maven-plugin.projects.gabrys.biz/
 *
 * Copyright (c) 2015 Adam Gabryś
 *
 * This file is licensed under the BSD 3-Clause (the "License").
 * You may not use this file except in compliance with the License.
 * You may obtain:
 *  - a copy of the License at project page
 *  - a template of the License at https://opensource.org/licenses/BSD-3-Clause
 */
package biz.gabrys.maven.plugins.lesscss.log;

import biz.gabrys.maven.plugin.util.parameter.converter.ValueToStringConverter;
import biz.gabrys.maven.plugin.util.timer.TimeSpan;

public class WatchIntervalToStringConverter implements ValueToStringConverter {

    private static final long MILLISECONDS_IN_SECOND = 1000L;

    @Override
    public String convert(final Object value) {
        final Integer number = (Integer) value;
        final StringBuilder text = new StringBuilder();
        text.append(number);
        if (number > 0) {
            text.append(" (");
            text.append(new TimeSpan(number * MILLISECONDS_IN_SECOND));
            text.append(')');
        }
        return text.toString();
    }
}
