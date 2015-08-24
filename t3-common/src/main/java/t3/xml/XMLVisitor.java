/**
 * (C) Copyright 2014-2015 teecube
 * (http://teecube.org) and others.
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
package t3.xml;

/**
 *
 * @author Mathieu Debove &lt;mad@teecube.org&gt;
 *
 */
public abstract class XMLVisitor<T> implements XMLFromPropertiesMapping {

	private T visited = null;

	public XMLVisitor(T visited) {
		this.visited = visited;
	}

	@SuppressWarnings("unchecked") // must check!
	protected T visited() {
		return visited != null ? visited : (T) this;
	}

}
