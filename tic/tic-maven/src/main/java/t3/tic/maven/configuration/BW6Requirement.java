/**
 * (C) Copyright 2014-2015 T3Soft
 * (http://www.t3soft.org) and others.
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

	@Override
	public boolean equals(Object o) {
		if (o == null) {
			return false;
		} else {
			return o.hashCode() == this.hashCode();
		}
	}

	@Override
	public int hashCode() {
		String concat =
		(this.type == null ? "" : this.type)
		+
		(this.id == null ? "" : this.id)
		+
		(this.versionRange == null ? "" : this.versionRange);
	    return concat.hashCode();
	}

}
