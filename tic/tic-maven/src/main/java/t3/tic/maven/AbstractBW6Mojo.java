package t3.tic.maven;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

public abstract class AbstractBW6Mojo extends AbstractMojo {

	@Parameter( defaultValue = "${project}", readonly = true, required = true )
    protected MavenProject project;

}
