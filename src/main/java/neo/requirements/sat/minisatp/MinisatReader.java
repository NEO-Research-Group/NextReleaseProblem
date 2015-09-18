package neo.requirements.sat.minisatp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class MinisatReader {

	/**
	 * @param args
	 */

	private int optimalValue;
	private boolean solutionFound=false;
	private String optimalSolution;
	private boolean satisfiable;

	public boolean isOptimalValueFound() {
		return solutionFound;
	}
	
	public boolean isSatisfiable() {
		return satisfiable;
	}


	public void parse(InputStream is) throws IOException
	{
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		String line;
		
		while ((line=br.readLine())!=null)
		{
			switch (line.charAt(0))
			{
				case 'c':
					if (line.contains("Optimal solution:"))
					{
						String [] parts= line.split(":");
						/*
						System.out.println(line);
						for (int i = 0; i < parts.length; i++) {
							System.out.println(parts[i]);
						}*/
						optimalValue = Integer.parseInt(parts[1].trim());
						solutionFound = true;
					}
					// Comment line
					break;
				case 'v':
					optimalSolution = line.substring(1).trim();
					break;
				case 's':
					if (line.contains("UNSAT")) {
						solutionFound=true;
						satisfiable=false;
					} else if (line.contains("SAT")) {
						solutionFound=true;
						satisfiable=true;
					}
					break;
			}
		}
		
	}
	
	public int getOptimalValue()
	{
		return optimalValue;
	}
	
	public String getOptimalSolution() {
		return optimalSolution;
	}

}
