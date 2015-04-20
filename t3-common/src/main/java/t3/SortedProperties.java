/**
 * (C) Copyright 2014-2015 T3Soft
 * (http://t3soft.org) and others.
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
package t3;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.TreeSet;

import org.apache.commons.io.FileUtils;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.shared.filtering.MavenResourcesExecution;

/**
 * <p>
 * This inner-class extends java.util.Properties with all properties sorted
 * alphabetically. Also, the setProperty method is overridden to support
 * multiple input types and check for null values.
 * </p>
 *
 * @author Mathieu Debove &lt;mad@t3soft.org&gt;
 *
 */
public class SortedProperties extends Properties {
	private static final long serialVersionUID = 3733070302160913988L;

	@Override
    public synchronized Enumeration<Object> keys() {
        return Collections.enumeration(new TreeSet<Object>(super.keySet()));
    }

	@Override
	public synchronized Object setProperty(String key, String value) {
		if (value != null) {
			return super.setProperty(key, value);
		}
		return null;
	}

	public synchronized Object setProperty(String key, BigInteger value) {
		if (value != null) {
			return super.setProperty(key, value.toString());
		}
		return null;
	}

	public synchronized Object setProperty(String key, Boolean value) {
		if (value != null) {
			return super.setProperty(key, value.toString());
		}
		return null;
	}

}
