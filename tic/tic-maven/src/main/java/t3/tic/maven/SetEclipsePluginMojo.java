package t3.tic.maven;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

@Mojo(name="set-eclipse-plugin", defaultPhase = LifecyclePhase.VALIDATE)
public class SetEclipsePluginMojo extends AbstractBW6Mojo {

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		getLog().info("Setting POM packaging to 'eclipse-plugin'");
		getLog().info(project.getPackaging());
		project.setPackaging("eclipse-plugin");
		getLog().info(project.getPackaging());
	}

}
