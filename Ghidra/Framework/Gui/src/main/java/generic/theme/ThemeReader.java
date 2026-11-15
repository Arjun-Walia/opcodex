/* ###
 * IP: GHIDRA
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package generic.theme;

import java.io.*;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.FileUtils;

import ghidra.framework.Application;
import utilities.util.FileUtilities;

/**
 * Reads Themes from a file or {@link Reader}
 */
class ThemeReader extends AbstractThemeReader {

	private static final int MAX_ZIP_ENTRIES = 2048;
	private static final int MAX_ZIP_ENTRY_BYTES = 8 * 1024 * 1024;
	private static final long MAX_ZIP_TOTAL_BYTES = 64L * 1024 * 1024;

	private File file;
	private GTheme theme;

	/**
	 * Constructor for reading a theme from a file.
	 * @param file the file to read as a theme
	 * @throws IOException if an I/O error occurs reading the theme file
	 */
	ThemeReader(File file) throws IOException {
		super(file.getAbsolutePath());
		this.file = file;
	}

	public GTheme readTheme() throws IOException {
		if (file.getName().endsWith(GTheme.FILE_EXTENSION)) {
			return readFileTheme();
		}
		if (file.getName().endsWith(GTheme.ZIP_FILE_EXTENSION)) {
			return readZipTheme();
		}

		throw new IOException("Imported File must end in either " + GTheme.FILE_EXTENSION + " or " +
			GTheme.ZIP_FILE_EXTENSION);
	}

	/**
	 * Assumes the file is a theme file and reads it.
	 */
	private GTheme readFileTheme() throws IOException {
		try (Reader reader = new FileReader(file)) {
			read(reader);
		}
		if (theme == null) {
			throw new IOException("Invalid Theme file: " + file);
		}
		return theme;
	}

	private GTheme readZipTheme() throws IOException {
		try (ZipFile zipFile = new ZipFile(file)) {
			if (zipFile.size() > MAX_ZIP_ENTRIES) {
				throw new IOException("Theme archive contains too many entries");
			}
			List<? extends ZipEntry> entries =
				zipFile.stream().filter(entry -> !entry.isDirectory()).toList();
			validateEntries(entries);
			for (ZipEntry entry : entries) {
				String name = entry.getName();
				int size = (int) entry.getSize();
				try (InputStream is = zipFile.getInputStream(entry)) {
					byte[] contents = is.readNBytes(size + 1);
					if (contents.length != size) {
						throw new IOException("Invalid size for theme archive entry: " + name);
					}
					InputStream data = new ByteArrayInputStream(contents);
					if (name.endsWith(".theme")) {
						read(new InputStreamReader(data));
					}
					else {
						FileUtils.copyInputStreamToFile(data, iconDestination(name));
					}
				}
			}
		}
		if (theme == null) {
			throw new IOException("Invalid Theme file: " + file);
		}
		return theme;
	}

	private void validateEntries(List<? extends ZipEntry> entries) throws IOException {
		long total = 0;
		int themeFiles = 0;
		for (ZipEntry entry : entries) {
			long size = entry.getSize();
			if (size < 0 || size > MAX_ZIP_ENTRY_BYTES || total + size > MAX_ZIP_TOTAL_BYTES) {
				throw new IOException("Theme archive exceeds the extraction size limit");
			}
			total += size;
			if (entry.getName().endsWith(".theme")) {
				themeFiles++;
			}
			else {
				iconDestination(entry.getName());
			}
		}
		if (themeFiles != 1) {
			throw new IOException("Theme archive must contain exactly one .theme file");
		}
	}

	// for testing
	GTheme readTheme(Reader reader) throws IOException {
		read(reader);
		return theme;
	}

	@Override
	protected void processNoSection(Section section) throws IOException {
		String themeName = section.getValue(ThemeWriter.THEME_NAME_KEY);
		if (themeName == null) {
			throw new IOException("Missing theme name!");
		}
		String lookAndFeelName = section.getValue(ThemeWriter.THEME_LOOK_AND_FEEL_KEY);
		LafType lookAndFeel = LafType.fromName(lookAndFeelName);
		if (lookAndFeel == null) {
			throw new IOException(
				"Invalid or missing lookAndFeel name: \"" + lookAndFeelName + "\"");
		}
		boolean isDark = Boolean.valueOf(section.getValue(ThemeWriter.THEME_USE_DARK_DEFAULTS));

		theme = new GTheme(file, themeName, lookAndFeel, isDark);
		section.remove(ThemeWriter.THEME_NAME_KEY);
		section.remove(ThemeWriter.THEME_LOOK_AND_FEEL_KEY);
		section.remove(ThemeWriter.THEME_USE_DARK_DEFAULTS);
		processValues(theme, section);
	}

	@Override
	protected void processDefaultSection(Section section) throws IOException {
		error(section.getLineNumber(), "[Defaults] section not allowed in theme files!");
	}

	@Override
	protected void processDarkDefaultSection(Section section) throws IOException {
		error(section.getLineNumber(), "[Dark Defaults] section not allowed in theme files!");
	}

	@Override
	protected void processCustomSection(Section section) throws IOException {
		error(section.getLineNumber(),
			"Custom sections not allowed in theme files! " + section.getName());
	}

	private File iconDestination(String path) throws IOException {
		String archivePath = "/" + path;
		String imageFolder = "/images/";
		int index = archivePath.indexOf(imageFolder);
		if (index < 0 || path.indexOf('\\') >= 0 || path.indexOf(':') >= 0) {
			throw new IOException("Invalid icon path in theme archive: " + path);
		}
		String relative = archivePath.substring(index + imageFolder.length());
		File settings = Application.getUserSettingsDirectory();
		File images = new File(settings, "images");
		File destination = new File(images, relative);
		if (relative.isBlank() ||
			!FileUtilities.isPathContainedWithin(settings, images) ||
			!FileUtilities.isPathContainedWithin(images, destination) ||
			destination.getCanonicalFile().equals(images.getCanonicalFile())) {
			throw new IOException("Icon path escapes the theme images directory: " + path);
		}
		return destination;
	}

}
