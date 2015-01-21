package t3.tic.maven;

import java.io.File;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuilder;
import org.apache.maven.settings.Settings;

public abstract class AbstractBW6Mojo extends AbstractMojo {

	@Parameter( property = "project.build.directory")
    protected File outputDirectory;

	@Parameter( property = "project.basedir")
	protected File projectBasedir;

    @Parameter ( defaultValue = "${session}", readonly = true)
    protected MavenSession session;

    @Parameter ( defaultValue = "${project}", readonly = true)
    protected MavenProject project;

    @Parameter ( defaultValue = "${mojoExecution}", readonly = true)
    protected MojoExecution mojoExecution;

    @Parameter ( defaultValue = "${plugin}", readonly = true)
	protected PluginDescriptor pluginDescriptor; // plugin descriptor of this plugin

    @Parameter ( defaultValue = "${settings}", readonly = true)
    protected Settings settings;

    @Component
    protected ProjectBuilder builder;

}
