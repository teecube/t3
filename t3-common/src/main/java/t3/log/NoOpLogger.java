/**
 * (C) Copyright 2016-2019 teecube
 * (https://teecu.be) and others.
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

import org.apache.maven.plugin.logging.Log;

/**
 * <p>
 * This no-operation logger implements org.apache.maven.plugin.logging.Log and
 * can be used when Maven goals are called programmatically to make these goals
 * silent.
 * </p>
 *
 * @author Mathieu Debove &lt;mad@teecu.be&gt;
 *
 */
public class NoOpLogger implements Log {

    @Override
    public boolean isDebugEnabled() {
        return false;
    }

    @Override
    public void debug(CharSequence content) {
    }

    @Override
    public void debug(CharSequence content, Throwable error) {
    }

    @Override
    public void debug(Throwable error) {
    }

    @Override
    public boolean isInfoEnabled() {
        return false;
    }

    @Override
    public void info(CharSequence content) {
    }

    @Override
    public void info(CharSequence content, Throwable error) {
    }

    @Override
    public void info(Throwable error) {
    }

    @Override
    public boolean isWarnEnabled() {
        return false;
    }

    @Override
    public void warn(CharSequence content) {
    }

    @Override
    public void warn(CharSequence content, Throwable error) {
    }

    @Override
    public void warn(Throwable error) {
    }

    @Override
    public boolean isErrorEnabled() {
        return false;
    }

    @Override
    public void error(CharSequence content) {
    }

    @Override
    public void error(CharSequence content, Throwable error) {
    }

    @Override
    public void error(Throwable error) {
    }

}
