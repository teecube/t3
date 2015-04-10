/**
 * (C) Copyright 2014-2015 T3Soft
 * (http://t3soft.org) and others.
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
package t3;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.maven.model.FileSet;
import org.codehaus.plexus.util.FileUtils;

/**
*
* @author Mathieu Debove &lt;mad@t3soft.org&gt;
*
*/
public class Utils {

    private static String toCommaSeparatedString(List<String> strings) {
        StringBuilder sb = new StringBuilder();
        for (String string : strings) {
            if (sb.length() > 0) {
				sb.append(", ");
            }
            sb.append(string);
        }
        return sb.toString();
    }

	public static List<File> toFileList(FileSet fileSet) throws IOException {
        File directory = new File(fileSet.getDirectory());
        String includes = toCommaSeparatedString(fileSet.getIncludes());
        String excludes = toCommaSeparatedString(fileSet.getExcludes());
        return FileUtils.getFiles(directory, includes, excludes);
    }

}
