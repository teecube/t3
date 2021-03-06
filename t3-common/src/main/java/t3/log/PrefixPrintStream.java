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

import java.io.PrintStream;

public class PrefixPrintStream extends PrintStream {

    private final String prefix;
    private final PrintStream printStream;

    public PrefixPrintStream(PrintStream printStream, String prefix) {
        super(printStream);

        this.prefix = prefix;
        this.printStream = printStream;
    }

    @Override
    public void println(String line) {
        super.println(prefix + line);
    }
}
