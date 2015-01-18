package neo.requirements.sat.minisatp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.PrintWriter;

import neo.requirements.sat.minisatp.MinisatpSolver.SearchDirection;

public class MinisatpSolver {
	
	public static enum SearchDirection {MINIMIZE, MAXIMIZE}

	private static String MINISATP_PATH= "./";
	private static String MINISATP_EXE = MINISATP_PATH+"minisat+_64-bit_static";
	
	private boolean keepOdpFiles;

	public MinisatpSolver(boolean keepOdpFiles) {
		this.keepOdpFiles = keepOdpFiles;
	}
	
	public MinisatpSolver() {
		this(true);
	}

	public Integer solveMinisatpInstance(String pb, SearchDirection direction) {
		try {
	
			File tmp = File.createTempFile("fm-minisat", ".odp", new File("."));
			FileOutputStream fos = new FileOutputStream (tmp);
	
			PrintWriter pw = new PrintWriter(fos);
			pw.println(pb);
			pw.close();
	
			Process p = Runtime.getRuntime().exec(
					new String []{MINISATP_EXE,"-a",tmp.getAbsolutePath()});
	
			p.getOutputStream().close();
			InputStream is = p.getInputStream();
	
			MinisatReader mr = new MinisatReader();
			mr.parse(is);
			is.close();
	
			p.waitFor();
	
			if (!keepOdpFiles) {
				tmp.delete();
			}
	
			if (mr.isOptimalValueFound())
			{
				return (direction.equals(SearchDirection.MAXIMIZE)?-1:1) * mr.getOptimalValue();
			}
			else
			{
				return null;
			}
	
		} catch (Exception  e) {
			throw new RuntimeException (e);
		}
	}

	public boolean isKeepOdpFiles() {
		return keepOdpFiles;
	}

	public void setKeepOdpFiles(boolean keepOdpFiles) {
		this.keepOdpFiles = keepOdpFiles;
	}
}