package neo.requirements.sat.cplex;

import ilog.concert.IloException;
import ilog.cplex.IloCplex;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.PrintWriter;

import neo.requirements.sat.minisatp.MinisatReader;
import neo.requirements.sat.minisatp.MinisatpSolver.SearchDirection;

public class CplexSolver {
	
	public static class Result {
		public int value;
		public double [] solution;
		
		public Result(int value, double [] solution) {
			this.value=value;
			this.solution = solution;
		}
	}

	public CplexSolver() {
	}
	
	public Result solveIlpInstance(Modelo modelo) {	
		try {
			modelo.cplex.setOut(null);
			
			modelo.cplex.setParam(IloCplex.DoubleParam.EpInt, 1E-9);
			modelo.cplex.setParam(IloCplex.DoubleParam.EpGap, 0);
			modelo.cplex.setParam(IloCplex.DoubleParam.EpOpt, 1E-9);
			
			if (modelo.cplex.solve()) {
				return new Result((int)Math.round(modelo.cplex.getObjValue()), modelo.cplex.getValues(modelo.variables));
			} else {
				return null;
			}
		} catch (IloException  e) {
			throw new RuntimeException (e);
		}
		
	}


}