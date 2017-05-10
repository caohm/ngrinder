// Copyright (C) 2004 - 2008 Philip Aston
// All rights reserved.
//
// This file is part of The Grinder software distribution. Refer to
// the file LICENSE which is part of The Grinder distribution for
// licensing details. The Grinder distribution is available on the
// Internet at http://grinder.sourceforge.net/
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
// "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
// LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
// FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
// COPYRIGHT HOLDERS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
// INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
// (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
// SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
// HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
// STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
// ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
// OF THE POSSIBILITY OF SUCH DAMAGE.

package net.grinder.util;

import net.grinder.common.Closer;
import net.grinder.common.GrinderException;
import net.grinder.common.UncheckedInterruptedException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;

import java.io.*;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;


/**
 * Pairing of relative filename and file contents.
 *
 * @author Philip Aston
 */
public final class FileContents implements Serializable {

	private static final long serialVersionUID = -3140708892260600117L;

	/**
	 * @serial The file name.
	 */
	private final File m_filename;

	/**
	 * @serial The file data.
	 */
	private final byte[] m_contents;

	/**
	 * Constructor. Builds a FileContents from local file system.
	 *
	 * @param baseDirectory Base directory used to resolve relative filenames.
	 * @param file          Relative filename.
	 * @throws FileContentsException If an error occurs.
	 */
	public FileContents(File baseDirectory, File file)
		throws FileContentsException {

		if (file.isAbsolute()) {
			throw new FileContentsException(
				"Original file name '" + file + "' is not relative");
		}

		m_filename = file;

		final File localFile = new File(baseDirectory, file.getPath());

		final ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();

		try {
			new StreamCopier(4096, true).copy(new FileInputStream(localFile),
				byteOutputStream);
		} catch (IOException e) {
			UncheckedInterruptedException.ioException(e);
			throw new FileContentsException(
				"Failed to read file: " + e.getMessage(), e);
		}

		m_contents = byteOutputStream.toByteArray();
	}


	/**
	 * Allow unit tests access to the relative file name.
	 *
	 * @return The file name.
	 */
	File getFilename() {
		return m_filename;
	}

	/**
	 * Allow unit tests access to the file contents.
	 *
	 * @return a <code>byte[]</code> value
	 */
	byte[] getContents() {
		return m_contents;
	}

	/**
	 * Write the <code>FileContents</code> to the given directory,
	 * overwriting any existing content.
	 *
	 * @param baseDirectory The base directory.
	 * @param m_logger
	 * @throws FileContentsException If an error occurs.
	 */
	public void create(Directory baseDirectory, Logger m_logger) throws FileContentsException {

		final File localFile = baseDirectory.getFile(getFilename());

		localFile.getParentFile().mkdirs();

		OutputStream outputStream = null;

		try {
			outputStream = new FileOutputStream(localFile);
			outputStream.write(getContents());
		} catch (IOException e) {
			UncheckedInterruptedException.ioException(e);
			throw new FileContentsException(
				"Failed to create file: " + e.getMessage(), e);
		} finally {
			Closer.close(outputStream);
		}
		try {
			if (getFilename().getName().equals("dist.zip")) {
				m_logger.info("unzip dist.zip");
				unZipFiles(localFile, baseDirectory.getFile());
				m_logger.info("delete dist.zip");
				FileUtils.forceDelete(localFile);
			}
		} catch (Exception e) {
			m_logger.info("zip error " + e.getMessage(), e);
            throw new FileContentsException("Failed to unzip dist.zip : " + e.getMessage(), e);
		} finally {
		}
	}

	/**
	 * 解压文件到指定目录
	 *
	 * @param zipFile
	 * @param descDir
	 * @author isea533
	 */
	public static void unZipFiles(File zipFile, File descDir) throws IOException {
		File pathFile = descDir;
		if (!pathFile.exists()) {
			pathFile.mkdirs();
		}
		ZipFile zip = new ZipFile(zipFile);
		for (Enumeration entries = zip.entries(); entries.hasMoreElements(); ) {
			ZipEntry entry = (ZipEntry) entries.nextElement();
			String zipEntryName = entry.getName();
			InputStream in = zip.getInputStream(entry);
			String outPath = (descDir.getAbsolutePath() + "/" + zipEntryName).replaceAll("\\*", "/");
			//判断路径是否存在,不存在则创建文件路径
			File file = new File(outPath.substring(0, outPath.lastIndexOf('/')));
			if (!file.exists()) {
				file.mkdirs();
			}
			//判断文件全路径是否为文件夹,如果是上面已经上传,不需要解压
			if (new File(outPath).isDirectory()) {
				continue;
			}
			OutputStream out = new FileOutputStream(outPath);
			IOUtils.copy(in, out);
			IOUtils.closeQuietly(in);
			IOUtils.closeQuietly(out);
		}
		IOUtils.closeQuietly(zip);
	}

	/**
	 * Return a description of the <code>FileContents</code>.
	 *
	 * @return The description.
	 */
	public String toString() {
		return "\"" + getFilename() + "\" (" + getContents().length + " bytes)";
	}

	/**
	 * Exception that indicates a <code>FileContents</code> related
	 * problem.
	 */
	public static final class FileContentsException extends GrinderException {
		FileContentsException(String message) {
			super(message);
		}

		FileContentsException(String message, Throwable nested) {
			super(message, nested);
		}
	}
}
