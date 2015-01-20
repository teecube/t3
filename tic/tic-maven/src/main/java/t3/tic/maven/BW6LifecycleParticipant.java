package t3.tic.maven;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.AbstractMavenLifecycleParticipant;
import org.apache.maven.MavenExecutionException;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Plugin;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.Xpp3DomBuilder;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.eclipse.tycho.core.maven.TychoMavenLifecycleParticipant;

@Component(role = AbstractMavenLifecycleParticipant.class, hint = "BW6LifecycleListener")
public class BW6LifecycleParticipant extends TychoMavenLifecycleParticipant {

	@Requirement
	private Logger logger;

	/**
	 * Used to retrieve BW6 requirements from this plugin (self) configuration
	 * 
	 * @throws IOException 
	 * @throws XmlPullParserException 
	 */
	private List<BW6Requirement> getBW6Requirements(MavenProject mavenProject) throws XmlPullParserException, IOException {
		List<BW6Requirement> requirements = new ArrayList<BW6Requirement>();

		List<Plugin> plugins = mavenProject.getBuild().getPlugins();
		for (Plugin plugin : plugins) {
			// TODO: if (plugin.equals(self)) {
			String config = plugin.getConfiguration().toString();
			
			Xpp3Dom dom = Xpp3DomBuilder.build(new ByteArrayInputStream(config.getBytes()), "UTF-8"); // FIXME: encoding
			
			Xpp3Dom dependenciesNode = dom.getChild(0); // FIXME: really find <dependencies>
			if (dependenciesNode != null && "dependencies".equals(dependenciesNode.getName())) {
				for (Xpp3Dom requirementNode : dependenciesNode.getChildren()) {
					if ("requirement".equals(requirementNode.getName())) {
						Xpp3Dom type = requirementNode.getChild("type");
						Xpp3Dom id = requirementNode.getChild("id");
						Xpp3Dom versionRange = requirementNode.getChild("versionRange");
						if (type != null && id != null && versionRange != null) {
							BW6Requirement requirement = new BW6Requirement();
							requirement.setType(type.getValue());
							requirement.setId(id.getValue());
							requirement.setVersionRange(versionRange.getValue());
							requirements.add(requirement);
						} else {
							logger.warn("wrong requirement");
						}
					}
				}
			}
		}

		return requirements;
	}

	private MavenProject prepareBW6AppModule(MavenProject mavenProject) throws XmlPullParserException, IOException {
		mavenProject.setPackaging("eclipse-plugin"); // change packaging of the POM to "eclipse-plugin"

		List<BW6Requirement> requirements = getBW6Requirements(mavenProject);

		for (BW6Requirement bw6Requirement : requirements) {
			logger.info(bw6Requirement.getId());
		}

		return mavenProject;
	}

	@Override
	public void afterProjectsRead(MavenSession session)	throws MavenExecutionException {
		List<MavenProject> projects = session.getProjects();

		for (MavenProject mavenProject : projects) {
			if ("bw6-app-module".equals(mavenProject.getPackaging())) {
				try {
					prepareBW6AppModule(mavenProject);
				} catch (XmlPullParserException | IOException e) {
					throw new MavenExecutionException(e.getLocalizedMessage(), e);
				}
			}
		}

		// super.afterProjectsRead(session);
		logger.info("TIC...");
	}
}