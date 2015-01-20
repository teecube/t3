package t3.tic.maven;

import org.apache.maven.plugins.annotations.Parameter;

public class BW6Requirement {

	@Parameter
	private String type;
	@Parameter
	private String id;
	@Parameter
	private String versionRange;

	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}

	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}

	public String getVersionRange() {
		return versionRange;
	}
	public void setVersionRange(String versionRange) {
		this.versionRange = versionRange;
	}
}
