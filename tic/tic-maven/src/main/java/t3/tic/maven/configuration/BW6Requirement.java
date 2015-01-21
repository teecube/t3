package t3.tic.maven.configuration;

/**
 * <p>
 * A BW6 requirement is specified as a &lt;requirement> child element of the
 * &lt;dependencies> element of this plugin configuration.
 * </p>
 * <p>For instance:
 * 	<pre>
 *&lt;configuration>
 *  &lt;dependencies>
 *    &lt;requirement>
 *      &lt;type>eclipse-plugin&lt;/type>
 *      &lt;id>com.tibco.bw.core.model&lt;/id>
 *      &lt;versionRange>6.0.0&lt;/versionRange>
 *    &lt;/requirement>
 *  &lt;/dependencies>
 *&lt;/configuration>
 * 	</pre>
 * </p>
 *
 * @author Mathieu Debove <mad@t3soft.org>
 *
 */
public class BW6Requirement {

	private String type;
	private String id;
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
