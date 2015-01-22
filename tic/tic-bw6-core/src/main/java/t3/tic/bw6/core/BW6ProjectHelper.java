package t3.tic.bw6.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.maven.project.MavenProject;
import org.eclipse.core.internal.resources.ProjectDescription;
import org.eclipse.core.internal.resources.ProjectDescriptionReader;
import org.eclipse.core.internal.resources.Workspace;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.xml.sax.InputSource;

import com.tibco.bw.maven.gatherers.BWProjectInfoGatherer;
import com.tibco.bw.maven.gatherers.IBWProjectInfoGatherer;
import com.tibco.bw.maven.utils.BWProjectInfo;

public class BW6ProjectHelper {

	private static MavenProject getIProjectFromMavenProject(final MavenProject mavenProject) throws CoreException, IOException {
		if (mavenProject == null) return null;

		String filename = mavenProject.getFile().getParent() + File.separator + ".project";

		InputSource is = new InputSource(new FileInputStream(new File(filename)));
		ProjectDescription result = new ProjectDescriptionReader().read(is);
		
		Workspace workspace = new Workspace();
		
		IPath projectDotProjectFile = new Path(mavenProject.getFile().getParent() + File.separator + ".project");
		workspace.loadProjectDescription(projectDotProjectFile);
		IProjectDescription projectDescription = workspace.loadProjectDescription(projectDotProjectFile);
//		MavenProject project = workspace.getRoot().getProject(projectDescription.getName());
		 
		return mavenProject;		
	}

	public static BWProjectInfo readBWProjectInfo(MavenProject mavenProject) throws Exception {
		MavenProject project = getIProjectFromMavenProject(mavenProject);
		IBWProjectInfoGatherer gatherer = new BWProjectInfoGatherer(project);
		BWProjectInfo info = gatherer.gather();
		return info;
	}

}
