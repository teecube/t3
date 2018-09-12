/**
 * (C) Copyright 2016-2018 teecube
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
package t3.utils;

import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.DependencyManagement;
import org.apache.maven.model.Model;
import org.apache.maven.model.Profile;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Settings;
import org.apache.maven.settings.io.xpp3.SettingsXpp3Writer;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.*;
import java.util.Iterator;
import java.util.List;

/**
 * <p>
 * This class handles Maven settings file manipulation using the Maven API.
 * </p>
 *
 * @author Mathieu Debove &lt;mad@teecu.be&gt;
 *
 */
public class SettingsManager {

    public static File saveSettingsToTempFile(Settings settings) throws IOException {
        File temporaryFile = File.createTempFile("settings", ".xml");

        saveSettingsToFile(settings, temporaryFile);

        return temporaryFile;
    }

    public static void saveSettingsToFile(Settings settings, File file) throws IOException {
        SettingsXpp3Writer settingsWriter = new SettingsXpp3Writer();

        try (FileOutputStream fos = new FileOutputStream(file)){
            settingsWriter.write(fos, settings);
        }
    }
}
