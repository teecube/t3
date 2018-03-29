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

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import t3.plugin.annotations.Mojo;
import t3.plugin.annotations.helpers.AnnotationsHelper;

import java.util.*;

/**
 *
 * @author Mathieu Debove &lt;mad@teecu.be&gt;
 *
 */
public class MojosFactory {

    public <T extends AbstractMojo> T getMojo(Class<T> type) {
        return null;
    }

    public static <T> Map<String, LifecyclePhase> getMojosGoals(Class<T> type, boolean onlyRequiresProject) {
        Map<String, LifecyclePhase> result = new HashMap<String, LifecyclePhase>();

        Set<Class<?>> mojos = AnnotationsHelper.getTypesAnnotatedWith(type, Mojo.class);

        for (Class<?> mojo : mojos) {
            Mojo m = mojo.getAnnotation(Mojo.class);
            String goal = m.name();
            LifecyclePhase phase = m.defaultPhase();
            if (!onlyRequiresProject || m.requiresProject()) {
                result.put(goal, phase);
            }
        }

        return result;
    }

    public static <T> List<String> getMojosGoalsForLifecyclePhase(Class<T> type, LifecyclePhase lifecyclePhase, boolean onlyRequiresProject) {
        List<String> result = new ArrayList<String>();

        Map<String, LifecyclePhase> goals = getMojosGoals(type, onlyRequiresProject);
        for (String goal : goals.keySet()) {
            LifecyclePhase phase = goals.get(goal);
            if (phase.ordinal() <= lifecyclePhase.ordinal()) {
                result.add(goal);
            }
        }

        return result;
    }
}
