package t3.tic.maven;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

@Mojo(name="bw6-package", defaultPhase = LifecyclePhase.PACKAGE)
public class PackageBW6Mojo extends AbstractBW6Mojo {

}
