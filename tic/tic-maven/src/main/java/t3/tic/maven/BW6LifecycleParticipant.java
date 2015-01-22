package t3.tic.maven;

import java.io.IOException;
import java.util.List;

import org.apache.maven.AbstractMavenLifecycleParticipant;
import org.apache.maven.MavenExecutionException;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.eclipse.tycho.core.maven.TychoMavenLifecycleParticipant;

import t3.tic.maven.prepare.EclipsePluginConvertor;

@Component(role = AbstractMavenLifecycleParticipant.class, hint = "BW6LifecycleListener")
public class BW6LifecycleParticipant extends TychoMavenLifecycleParticipant {

	@Requirement
	private Logger logger;

	@Override
	public void afterProjectsRead(MavenSession session)	throws MavenExecutionException {
		List<MavenProject> projects = session.getProjects();

		EclipsePluginConvertor convertor = new EclipsePluginConvertor(logger);

		for (MavenProject mavenProject : projects) {
			if ("bw6-app-module".equals(mavenProject.getPackaging())) {
				try {
					convertor.prepareBW6AppModule(mavenProject);
				} catch (Exception e) {
					throw new MavenExecutionException(e.getLocalizedMessage(), e);
				}
			}
		}

		super.afterProjectsRead(session);
		logger.info("");
		logger.info("~-> TIC is loaded.");
	}
}