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
package t3.plugin;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.matcher.Matchers;
import com.google.inject.spi.TypeListener;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.classrealm.ClassRealmManager;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.*;
import org.apache.maven.plugin.internal.DefaultMavenPluginManager;
import org.apache.maven.plugin.internal.PluginDependenciesResolver;
import org.apache.maven.plugin.version.PluginVersionResolver;
import org.apache.maven.project.MavenProject;
import org.apache.maven.rtinfo.RuntimeInformation;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.logging.LoggerManager;
import org.eclipse.aether.RepositorySystem;
import t3.CommonMojo;
import t3.MojosFactory;
import t3.log.NoOpLogger;
import t3.plugin.annotations.injection.ParametersListener;

import java.lang.reflect.Field;
import java.util.Map;

/**
 *
 * @author Mathieu Debove &lt;mad@teecu.be&gt;
 *
 */
@Component( role = MavenPluginManager.class )
public class PluginManager extends DefaultMavenPluginManager {

    public static ClassLoader extensionClassLoader;
    // actual Mojos factory is implemented in each Maven plugin
    protected MojosFactory mojosFactory;

    private Map<String, String> ignoredParameters;

    private boolean silentMojo = false;

    public static DefaultMavenPluginManager getDefaultMavenPluginManager(BuildPluginManager pluginManager) {
        DefaultMavenPluginManager result = null;
        try {
            Field f = pluginManager.getClass().getDeclaredField("mavenPluginManager");
            f.setAccessible(true);
            result = (DefaultMavenPluginManager) f.get(pluginManager);
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static PluginManager getCustomMavenPluginManager(BuildPluginManager pluginManager) throws MojoExecutionException {
            PluginManager result = null;
            try {
                Field f = pluginManager.getClass().getDeclaredField("mavenPluginManager");
                f.setAccessible(true);
                result = (PluginManager) f.get(pluginManager);
            } catch (Exception e) {
                throw new MojoExecutionException(e.getLocalizedMessage(), e);
            }
            return result;
    }

    public static void registerCustomPluginManager(BuildPluginManager pluginManager, MojosFactory mojosFactory) {
        registerCustomPluginManager(pluginManager, mojosFactory, null);
    }

    public static void registerCustomPluginManager(BuildPluginManager pluginManager, MojosFactory mojosFactory, Map<String,String> ignoredParameters) {
        extensionClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            Field f = pluginManager.getClass().getDeclaredField("mavenPluginManager");
            f.setAccessible(true);
            DefaultMavenPluginManager oldMavenPluginManager = (DefaultMavenPluginManager) f.get(pluginManager);
            PluginManager mavenPluginManager = new PluginManager(oldMavenPluginManager, mojosFactory);
            if (ignoredParameters != null) {
                mavenPluginManager.setIgnoredParameters(ignoredParameters);
            }
            f.set(pluginManager, mavenPluginManager);
        } catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException e) {
            // no trace
        }
    }

    private void setIgnoredParameters(Map<String, String> ignoredParameters) {
        this.ignoredParameters = ignoredParameters;
    }

    protected <T> void copyField(String fieldName, Class<T> fieldType, DefaultMavenPluginManager defaultMavenPluginManager) throws Exception {
        Field oldField = defaultMavenPluginManager.getClass().getDeclaredField(fieldName);
        oldField.setAccessible(true);

        Class<?> parentClass = DefaultMavenPluginManager.class;

        Field newField = parentClass.getDeclaredField(fieldName);
        newField.setAccessible(true);

        T fieldValue = fieldType.cast(oldField.get(defaultMavenPluginManager));
        newField.set(this, fieldValue);
    }

    protected PluginManager() {
    }

    public PluginManager(DefaultMavenPluginManager defaultMavenPluginManager, MojosFactory mojosFactory) {
        if (defaultMavenPluginManager == null) return;

        this.mojosFactory = mojosFactory;

        // quick shallow copy of the original MavenPluginManager
        try {
            copyField("logger", Logger.class, defaultMavenPluginManager);
            copyField("loggerManager", LoggerManager.class, defaultMavenPluginManager);
            copyField("container", PlexusContainer.class, defaultMavenPluginManager);
            copyField("classRealmManager", ClassRealmManager.class, defaultMavenPluginManager);
            copyField("pluginDescriptorCache", PluginDescriptorCache.class, defaultMavenPluginManager);
            copyField("pluginRealmCache", PluginRealmCache.class, defaultMavenPluginManager);
            copyField("pluginDependenciesResolver", PluginDependenciesResolver.class, defaultMavenPluginManager);
            copyField("runtimeInformation", RuntimeInformation.class, defaultMavenPluginManager);
            copyField("extensionRealmCache", ExtensionRealmCache.class, defaultMavenPluginManager);
            copyField("pluginVersionResolver", PluginVersionResolver.class, defaultMavenPluginManager);
            copyField("pluginArtifactsCache", PluginArtifactsCache.class, defaultMavenPluginManager);
        } catch (Exception e) {
            // no trace
        }
    }

    protected <T> TypeListener getListener(T configuredMojo, MavenProject currentProject, MavenSession session) {
        return new ParametersListener<T>(configuredMojo, session.getCurrentProject(), session, ignoredParameters);
    }

    @org.apache.maven.plugins.annotations.Component
    private RepositorySystem repoSystem;

    @org.apache.maven.plugins.annotations.Component
    private LegacySupport legacySupport;

    @org.apache.maven.plugins.annotations.Component
    protected ArtifactRepository localRepository;

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getConfiguredMojo(Class<T> mojoInterface, final MavenSession session, MojoExecution mojoExecution) throws PluginConfigurationException, PluginContainerException {
        final T configuredMojo = super.getConfiguredMojo(mojoInterface, session, mojoExecution); // retrieve configuredMojo to know the actual type of the Mojo to configure

        if (this.silentMojo) {
            ((AbstractMojo) configuredMojo).setLog(new NoOpLogger());
        }

        Class<? extends CommonMojo> type;
        try {
            type = (Class<? extends CommonMojo>) configuredMojo.getClass();
            if (!(configuredMojo instanceof CommonMojo)) {
                return configuredMojo;
            }
        } catch (ClassCastException e) {
            return configuredMojo;
        }
        T rawMojo = (T) mojosFactory.getMojo(type); // retrieve a brand new and unconfigured Mojo of the actual type (with annotations)

        if (rawMojo != null) { // inject @MojoParameter and @GlobalParameter annotations
            Injector i = Guice.createInjector(new AbstractModule() {
                @Override
                protected void configure() {
                    bindListener(Matchers.any(), getListener(configuredMojo, session.getCurrentProject(), session)); // WARN: using getCurrentProject() ?
                }
            });

            i.injectMembers(rawMojo); // will also inject in configuredMojo
        }

        return configuredMojo;
    }

    public static void addIgnoredParametersInPluginManager(BuildPluginManager pluginManager, Map<String, String> ignoredParameters) throws MojoExecutionException {
        PluginManager customPluginManager = PluginManager.getCustomMavenPluginManager(pluginManager);

        customPluginManager.setIgnoredParameters(ignoredParameters);
    }

    public static void setSilentMojoInPluginManager(BuildPluginManager pluginManager, boolean silentMojo) throws MojoExecutionException {
        DefaultMavenPluginManager customPluginManager = PluginManager.getDefaultMavenPluginManager(pluginManager);

        Field silentMojoField = null;
        try {
            silentMojoField = customPluginManager.getClass().getDeclaredField("silentMojo");
            silentMojoField.setAccessible(true);
            silentMojoField.set(customPluginManager, silentMojo);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new MojoExecutionException(e.getLocalizedMessage(), e);
        }
    }

}
