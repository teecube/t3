/**
 * (C) Copyright 2016-2016 teecube
 * (http://teecu.be) and others.
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;
import org.apache.maven.model.FileSet;
import org.codehaus.plexus.util.FileUtils;

/**
 *
 * @author Mathieu Debove &lt;mad@teecu.be&gt;
 *
 */
public class Utils {

    static class PathResolutionException extends RuntimeException {
		private static final long serialVersionUID = 2723212952556555691L;

		PathResolutionException(String msg) {
            super(msg);
        }
    }

    /**
     * <p>
     * Get the relative path from one file to another, specifying the directory
     * separator. 
     * If one of the provided resources does not exist, it is assumed to be a
     * file unless it ends with '/' or '\'.
     * </p>
     * 
     * @param targetPath targetPath is calculated to this file
     * @param basePath basePath is calculated from this file
     * @param pathSeparator directory separator. The platform default is not assumed so that we can test Unix behaviour when running on Windows (for example)
     * @return
     */
    public static String getRelativePath(String targetPath, String basePath, String pathSeparator) {
        // Normalize the paths
        String normalizedTargetPath = FilenameUtils.normalizeNoEndSeparator(targetPath);
        String normalizedBasePath = FilenameUtils.normalizeNoEndSeparator(basePath);

        // Undo the changes to the separators made by normalization
        if (pathSeparator.equals("/")) {
            normalizedTargetPath = FilenameUtils.separatorsToUnix(normalizedTargetPath);
            normalizedBasePath = FilenameUtils.separatorsToUnix(normalizedBasePath);

        } else if (pathSeparator.equals("\\")) {
            normalizedTargetPath = FilenameUtils.separatorsToWindows(normalizedTargetPath);
            normalizedBasePath = FilenameUtils.separatorsToWindows(normalizedBasePath);

        } else {
            throw new IllegalArgumentException("Unrecognised dir separator '" + pathSeparator + "'");
        }

        String[] base = normalizedBasePath.split(Pattern.quote(pathSeparator));
        String[] target = normalizedTargetPath.split(Pattern.quote(pathSeparator));

        // First get all the common elements. Store them as a string,
        // and also count how many of them there are.
        StringBuffer common = new StringBuffer();

        int commonIndex = 0;
        while (commonIndex < target.length && commonIndex < base.length
                && target[commonIndex].equals(base[commonIndex])) {
            common.append(target[commonIndex] + pathSeparator);
            commonIndex++;
        }

        if (commonIndex == 0) {
            // No single common path element. This most
            // likely indicates differing drive letters, like C: and D:.
            // These paths cannot be relativized.
            throw new PathResolutionException("No common path element found for '" + normalizedTargetPath + "' and '" + normalizedBasePath + "'");
        }   

        // The number of directories we have to backtrack depends on whether the base is a file or a dir
        // For example, the relative path from
        //
        // /foo/bar/baz/gg/ff to /foo/bar/baz
        // 
        // ".." if ff is a file
        // "../.." if ff is a directory
        //
        // The following is a heuristic to figure out if the base refers to a file or dir. It's not perfect, because
        // the resource referred to by this path may not actually exist, but it's the best I can do
        boolean baseIsFile = true;

        File baseResource = new File(normalizedBasePath);

        if (baseResource.exists()) {
            baseIsFile = baseResource.isFile();

        } else if (basePath.endsWith(pathSeparator)) {
            baseIsFile = false;
        }

        StringBuffer relative = new StringBuffer();

        if (base.length != commonIndex) {
            int numDirsUp = baseIsFile ? base.length - commonIndex - 1 : base.length - commonIndex;

            for (int i = 0; i < numDirsUp; i++) {
                relative.append(".." + pathSeparator);
            }
        }
        relative.append(normalizedTargetPath.substring(common.length()));
        return relative.toString();
    }

    private static String toCommaSeparatedString(List<String> strings) {
        StringBuilder sb = new StringBuilder();
        for (String string : strings) {
            if (sb.length() > 0) {
				sb.append(", ");
            }
            sb.append(string);
        }
        return sb.toString();
    }

	public static List<File> toFileList(FileSet fileSet) throws IOException {
        File directory = new File(fileSet.getDirectory());
        String includes = toCommaSeparatedString(fileSet.getIncludes());
        String excludes = toCommaSeparatedString(fileSet.getExcludes());
        return FileUtils.getFiles(directory, includes, excludes);
    }

	/**
	 * <p>
	 * Convert an expression with wildcards to a regex.
	 *
	 * source = http://www.rgagnon.com/javadetails/java-0515.html
	 * </p>
	 *
	 * @param wildcard
	 * @return
	 */
    public static String wildcardToRegex(String wildcard) {
        StringBuffer s = new StringBuffer(wildcard.length());
        s.append('^');
        for (int i = 0, is = wildcard.length(); i < is; i++) {
            char c = wildcard.charAt(i);
            switch(c) {
                case '*':
                    s.append(".*");
                    break;
                case '?':
                    s.append(".");
                    break;
                    // escape special regexp-characters
                case '(': case ')': case '[': case ']': case '$':
                case '^': case '.': case '{': case '}': case '|':
                case '\\':
                    s.append("\\");
                    s.append(c);
                    break;
                default:
                    s.append(c);
                    break;
            }
        }
        s.append('$');
        return(s.toString());
    }

    public static Integer countMatchesInFile(File file, String pattern) throws IOException {
		Integer result = 0;
		if (file == null || !file.exists()) {
			return result;
		}

		Pattern p = Pattern.compile(pattern);

		String string = null;
		BufferedReader reader = new BufferedReader(new FileReader(file));

		while ((string = reader.readLine()) != null) {
		    Matcher matcher = p.matcher(string);

		    while (matcher.find()) {
		        result++;
		    }
		}
		reader.close();

		return result;
    }

    public static List<String> getMatchesFromFile(File file, String pattern) throws IOException {
		List<String> result = new ArrayList<String>();
		if (file == null || !file.exists()) {
			return result;
		}

		Pattern p = Pattern.compile(pattern);

		String string = null;
		BufferedReader reader = new BufferedReader(new FileReader(file));

		while ((string = reader.readLine()) != null) {
			Matcher matcher = p.matcher(string);

			while (matcher.find()) {
				result.add(matcher.group());
			}
		}
		reader.close();

		return result;
    }
}
