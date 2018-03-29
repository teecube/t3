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
package t3.site;

import org.apache.maven.model.FileSet;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 *
 * @author Mathieu Debove &lt;mad@teecu.be&gt;
 *
 */
public abstract class AbstractReplaceMojo extends AbstractReplaceAllMojo {

    protected abstract String getFileNameToReplace();

    @Override
    protected List<File> getHTMLFiles() throws IOException {
        FileSet htmlFiles = new FileSet();
        htmlFiles.setDirectory(outputDirectory.getAbsolutePath());

        htmlFiles.addInclude(getFileNameToReplace());

        return toFileList(htmlFiles);
    }

    
}
