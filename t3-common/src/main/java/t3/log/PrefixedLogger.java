/**
 * (C) Copyright 2016-2018 teecube
 * (http://teecu.be) and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package t3.log;

import org.apache.maven.monitor.logging.DefaultLog;
import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.logging.Logger;

import java.lang.reflect.Field;

public class PrefixedLogger extends DefaultLog {

    private final String debugPrefix;
    private final String infoPrefix;
    private final String warnPrefix;
    private final String errorPrefix;

    public PrefixedLogger(Logger logger, String prefix) {
        this(logger, prefix, prefix, prefix, prefix);
    }

    public PrefixedLogger(Logger logger, String debugPrefix, String infoPrefix, String warnPrefix, String errorPrefix) {
        super(logger);

        this.debugPrefix = debugPrefix;
        this.infoPrefix = infoPrefix;
        this.warnPrefix = warnPrefix;
        this.errorPrefix = errorPrefix;
    }

    public static Logger getLoggerFromLog(Log log) {
        if (log == null) return null;

        try {
            Field loggerField = log.getClass().getDeclaredField("logger");
            loggerField.setAccessible(true);
            return (Logger) loggerField.get(log);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            return null;
        }
    }

    @Override
    public void debug(CharSequence content) {
        super.debug(debugPrefix + content);
    }

    @Override
    public void info(CharSequence content) {
        super.info(infoPrefix + content);
    }

    @Override
    public void warn(CharSequence content) {
        super.warn(warnPrefix + content);
    }

    @Override
    public void error(CharSequence content) {
        super.error(errorPrefix + content);
    }
}
