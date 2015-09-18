package neo.requirements.sat.minisatp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.PrintWriter;

import neo.requirements.sat.minisatp.MinisatpSolver.SearchDirection;

public class MinisatpSolver {
	
	public static enum SearchDirection {MINIMIZE, MAXIMIZE}
	public static class Result {
		public int value;
		public String solution;
		
		public Result(int value, String solution) {
			this.value=value;
			this.solution = solution;
		}
	}

	private static String MINISATP_PATH= "./";
	private static String MINISATP_EXE = MINISATP_PATH+"minisat+_64-bit_static";
	
	private boolean keepOdpFiles;

	public MinisatpSolver(boolean keepOdpFiles) {
		this.keepOdpFiles = keepOdpFiles;
	}
	
	public MinisatpSolver() {
		this(true);
	}

	public Result solveMinisatpInstance(String pb, SearchDirection direction) {
		try {
	
			File tmp = File.createTempFile("fm-minisat", ".odp", new File("."));
			//System.out.println("Solving "+tmp);
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
	
			if (mr.isOptimalValueFound()) {
				int value = (direction.equals(SearchDirection.MAXIMIZE)?-1:1) * mr.getOptimalValue();
				String solution = mr.getOptimalSolution();	
				return new Result(value, solution);
			} else {
				return null;
			}
	
		} catch (Exception  e) {
			throw new RuntimeException (e);
		}
	}
	
	public boolean solveMinisatDecisionInstance(String minisatInstance) {
		try {
			File tmp = File.createTempFile("fm-minisat", ".odp", new File("."));
			//System.out.println("Solving "+tmp);
			FileOutputStream fos = new FileOutputStream (tmp);
	
			PrintWriter pw = new PrintWriter(fos);
			pw.println(minisatInstance);
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
	
			if (mr.isOptimalValueFound()) {
				boolean result = mr.isSatisfiable();
				//System.out.println("Result: "+(result?"SAT":"UNSAT"));
				return result;
			} else {
				throw new RuntimeException ("Minisat problem");
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