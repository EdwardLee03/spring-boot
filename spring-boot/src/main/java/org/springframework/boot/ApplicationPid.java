
package org.springframework.boot;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.management.ManagementFactory;

import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

/**
 * An application process ID.
 * 应用程序的进程ID。
 *
 * @author Phillip Webb
 */
public class ApplicationPid {

	/**
	 * 进程ID
	 */
	private final String pid;

	public ApplicationPid() {
		this.pid = getPid();
	}

	protected ApplicationPid(String pid) {
		this.pid = pid;
	}

	private String getPid() {
		try {
			// JVM对象名称
			String jvmName = ManagementFactory.getRuntimeMXBean().getName();
			return jvmName.split("@")[0];
		}
		catch (Throwable ex) {
			return null;
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj != null && obj instanceof ApplicationPid) {
			return ObjectUtils.nullSafeEquals(this.pid, ((ApplicationPid) obj).pid);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return ObjectUtils.nullSafeHashCode(this.pid);
	}

	@Override
	public String toString() {
		return (this.pid != null) ? this.pid : "???";
	}

	/**
	 * Write the PID to the specified file.
	 * @param file the PID file
	 * @throws IllegalStateException if no PID is available.
	 * @throws IOException if the file cannot be written
	 */
	public void write(File file) throws IOException {
		Assert.state(this.pid != null, "No PID available");
		createParentFolder(file);
		FileWriter writer = new FileWriter(file);
		try {
			writer.append(this.pid);
		}
		finally {
			writer.close();
		}
	}

	private void createParentFolder(File file) {
		File parent = file.getParentFile();
		if (parent != null) {
			parent.mkdirs();
		}
	}

}
