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
	private boolean optimalValueFound=false;
	private String optimalSolution;

	public boolean isOptimalValueFound() {
		return optimalValueFound;
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
						optimalValueFound = true;
					}
					// Comment line
					break;
				case 'v':
					optimalSolution = line.substring(1).trim();
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
