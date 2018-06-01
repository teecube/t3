/**
 * (C) Copyright 2016-2018 teecube
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
package t3.utils;

import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.MojoExecutionException;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 *
 * @author Mathieu Debove &lt;mad@teecu.be&gt;
 *
 */
public class ZipUtils {

    public static boolean extractDirectoryFromZip(File zipFile, String directoryToCopy, File outputDirectory) throws MojoExecutionException {
        boolean result = false;

        ZipInputStream zipStream = null;
        try {
            zipStream = new ZipInputStream(new FileInputStream(zipFile));
            ZipEntry entry;

            while ((entry = zipStream.getNextEntry()) != null) {
                if (entry.getName().startsWith(directoryToCopy)) {
                    File fileToExtract = new File(outputDirectory, entry.getName().replace(directoryToCopy, ""));
                    if (!fileToExtract.exists()) {
                        if (entry.isDirectory()) {
                            fileToExtract.mkdirs();
                        } else {
                            result = true;
                            fileToExtract.createNewFile();
                            OutputStream fileToExtractOutputStream = null;
                            try {
                                fileToExtractOutputStream = new FileOutputStream(fileToExtract);
                                IOUtils.copy(zipStream, fileToExtractOutputStream);
                            } finally {
                                IOUtils.closeQuietly(fileToExtractOutputStream);
                            }
                        }

                    }
                }
            }
        } catch (IOException e) {
            throw new MojoExecutionException(e.getLocalizedMessage(), e);
        }

        return result;
    }

    public static File extractFileFromZip(File zipFile, String fileToCopy, File outputDirectory) throws MojoExecutionException {
        boolean result = false;

        ZipInputStream zipStream = null;
        try {
            zipStream = new ZipInputStream(new FileInputStream(zipFile));
            ZipEntry entry;

            while ((entry = zipStream.getNextEntry()) != null) {
                if (entry.getName().equals(fileToCopy)) {
                    String name = new File(entry.getName()).getName();
                    File fileToExtract = new File(outputDirectory, name);
                    outputDirectory.mkdirs();

                    fileToExtract.createNewFile();
                    OutputStream fileToExtractOutputStream = null;
                    try {
                        fileToExtractOutputStream = new FileOutputStream(fileToExtract);
                        IOUtils.copy(zipStream, fileToExtractOutputStream);
                    } finally {
                        IOUtils.closeQuietly(fileToExtractOutputStream);
                    }
                    return fileToExtract;
                }
            }
        } catch (IOException e) {
            throw new MojoExecutionException(e.getLocalizedMessage(), e);
        }
        return null;
    }

}
