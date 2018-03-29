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
package t3;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.project.MavenProject;
import org.joox.JOOX;
import org.joox.Match;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

public class LifecyclesUtils {

    public static List<Lifecycle<Phase>> parse(File componentsFile, MavenProject project, MavenSession session) throws SAXException, IOException {
        List<Lifecycle<Phase>> lifecycles = new ArrayList<Lifecycle<Phase>>();

        Match lifecyclesElements;
        lifecyclesElements = JOOX.$(componentsFile).xpath("//component[implementation='org.apache.maven.lifecycle.mapping.DefaultLifecycleMapping']");
        for (Element element : lifecyclesElements) {
            List<Phase> phases = new ArrayList<Phase>();
            Match phasesElements = JOOX.$(element).xpath("configuration/phases/*");
            for (Element phase : phasesElements) {
                phases.add(new Phase(phase.getNodeName(), phase.getTextContent(), project, session));
            }
            lifecycles.add(new Lifecycle<Phase>(JOOX.$(element).xpath("role-hint").text(), phases));
        }
        return lifecycles;
    }

    public static class Lifecycle<P extends Phase> {
        private String packagingName;
        private List<P> phases;

        public Lifecycle(String packagingName, List<P> phases) {
            this.packagingName = packagingName;
            this.phases = phases;
        }

        public String getPackagingName() {
            return packagingName;
        }
        public void setPackagingName(String packagingName) {
            this.packagingName = packagingName;
        }

        public List<P> getPhases() {
            return phases;
        }
        public void setPhases(List<P> phases) {
            this.phases = phases;
        }

    }

    public static class Phase {
        private String phaseName;
        private List<String> goals;
        private final MavenProject mavenProject;

        public Phase(String phaseName, String goals, MavenProject mavenProject, MavenSession session) {
            this.phaseName = phaseName;
            this.mavenProject = mavenProject;

            CommonMojo propertiesManager = CommonMojo.propertiesManager(session, mavenProject);

            this.goals = Arrays.asList(goals.split("\\s*,\\s*"));
            for (ListIterator<String> iterator = this.goals.listIterator(); iterator.hasNext();) {
                String goal = iterator.next();

                iterator.set(propertiesManager.replaceProperties(goal.trim()));
            }

        }

        public List<String> getGoals() {
            return goals;
        }
        public void setGoals(List<String> goals) {
            this.goals = goals;
        }
        public MavenProject getMavenProject() {
            return mavenProject;
        }
        public String getPhaseName() {
            return phaseName;
        }
        public void setPhaseName(String phaseName) {
            this.phaseName = phaseName;
        }
    }
}