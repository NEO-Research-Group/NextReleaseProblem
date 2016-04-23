package neo.requirements.cplex;

import java.io.File;
import java.util.List;
import java.util.Properties;

import neo.requirements.problems.NextReleaseProblem;
import neo.requirements.problems.readers.ClassicInstancesReader;
import neo.requirements.problems.readers.FileReader;
import neo.requirements.problems.readers.InMemoryReader;
import neo.requirements.problems.readers.NextReleaseProblemReader;
import neo.requirements.util.EfficientSolution;
import neo.requirements.util.ILPBasedAlgorithmsManager;
import neo.requirements.util.SingleThreadCPUTimer;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class MainClass {
	
	private static final String CONFIGURATON_OPTION = "O";
	private static final String ALGORITHMS_PACKAGE = "neo.requirements.cplex.algorithms";
	private static final String LISTENER = "listener";
	private static final String PROGRAM_NAME = "MainClass";
	private static final String INSTANCE = "instance";
	private static final String XUAN = "xuan";
	private static final String ALMERIA = "almeria";
	private static final String MEMORY = "memory";
	private static final String ALGORITHM = "algorithm";
	
	private Options options;
	
    private Options getOptions() {
        if (options == null) {
            options = prepareOptions();
        }
        return options;
    }
	
	private Options prepareOptions() {
	    Options options = new Options();
	    
	    Option option = Option.builder(INSTANCE)
	    		.argName("file")
	    		.numberOfArgs(1)
	    		.desc("NRP instance file").build();			    
	    options.addOption(option);
	    
	    option = Option.builder(ALGORITHM)
	    		.argName("alg")
	    		.numberOfArgs(1)
	    		.desc("algorithm used to solve the instance (use ? to see the algorithms)").build();
	    options.addOption(option);
	    
	    options.addOption(XUAN, false, "the problem is the one of Xuan et al.");
	    options.addOption(LISTENER, false, "enables the solution listener");
	    options.addOption(ALMERIA, false, "the problem is the one of del Águila et al.");
	    options.addOption(MEMORY, false, "it reads the sample instance in memory (20 requirements)");
	    
	    option = Option.builder(CONFIGURATON_OPTION)
	    		.argName("property=value")
	    		.numberOfArgs(2)
	    		.valueSeparator()
	    		.desc("use value for given property of the algorithm").build();
	    options.addOption(option);	    
	    
	    return options;
	}

	private void execute(String[] args) {
	    
		if (args.length == 0) {
			HelpFormatter helpFormatter = new HelpFormatter();
			helpFormatter.printHelp(PROGRAM_NAME, getOptions());
			return;
		}
		
		CommandLine commandLine = parseCommandLine(args);
		
		try {

			NextReleaseProblemReader reader = configureReader(commandLine);
			ILPBasedBiobjectiveSolver solver = configureSolver(commandLine);

			NextReleaseProblem problem = reader.readInstance();
			ILPAdaptor adaptor = new NRPCplexILPAdaptor(problem);

			SingleThreadCPUTimer timer = new SingleThreadCPUTimer();
			timer.startTimer();
			System.out.println("Running "+solver.getName());

			List<EfficientSolution> paretoFront = solver.computeParetoFront(adaptor);
			long computationTime = timer.elapsedTimeInMilliseconds();
			
			showResults(paretoFront, computationTime);

		}
		catch (IllegalArgumentException e) {
			System.err.println(e.getMessage());
		}

	}

	protected void showResults(List<EfficientSolution> paretoFront,
			long computationTime) {
		System.out.println("Pareto Front");
		System.out.println("------------");
		for (EfficientSolution solution: paretoFront) {
			System.out.println(solution);
		}
		System.out.println(paretoFront.size()+ " efficient solutions computed");
		
		System.out.println("Time: "+computationTime+ " ms");
	}

	protected ILPBasedBiobjectiveSolver configureSolver(CommandLine commandLine) {
		ILPBasedAlgorithmsManager manager = new ILPBasedAlgorithmsManager(ALGORITHMS_PACKAGE);
		ILPBasedBiobjectiveSolver solver = manager.getSolver(commandLine.getOptionValue(ALGORITHM));
		if (solver == null) {
			throw new IllegalArgumentException("Solver not found, use one of the following IDs: "+manager.getListOfAlgorithms());
		}
		
		if (commandLine.hasOption(LISTENER)) {
			solver.setListener(new ILPSolverListener() {
				@Override
				public void notifyEfficientSolutionFound(EfficientSolution solution) {
					System.out.println("Found: "+solution);

				}

				@Override
				public void info(String key, String value) {
					System.out.println("Info: "+key+"="+value);
					
				}

				@Override
				public void reportInnecesaryRunOfSolver() {
					System.out.println("Innecesary run of ILP solver");
				}
				
			});
		}
		
		Properties properties = readConfiguration(commandLine);
		if (properties != null) {
			solver.setConfiguration(properties);
		}
		
		return solver;
	}

	protected Properties readConfiguration(CommandLine commandLine) {
		Properties properties;
		if (commandLine.hasOption(CONFIGURATON_OPTION)) {
			String [] configuration = commandLine.getOptionValues(CONFIGURATON_OPTION);
			properties = new Properties();
			for (int i=0; i < configuration.length; i+=2) {
				properties.setProperty(configuration[i], configuration[i+1]);
			}
		} else {
			properties=null;
		}
		return properties;
	}

	protected NextReleaseProblemReader configureReader(CommandLine commandLine) {
		NextReleaseProblemReader reader;
		if (commandLine.hasOption(MEMORY)) {
			reader = new InMemoryReader();
		} else {
			File instancia = new File(commandLine.getOptionValue(INSTANCE));

			if (commandLine.hasOption(XUAN)) {
				if (commandLine.hasOption(ALMERIA)) {
					throw new IllegalArgumentException("The instance cannot have two formats: xuan and almeria");
				}
				reader = new ClassicInstancesReader(instancia);

			} else {
				reader = new FileReader(instancia);
			}
		}
		return reader;
	}
	
	private CommandLine parseCommandLine(String[] args) {
		try {
		    CommandLineParser parser = new DefaultParser();
            return parser.parse(getOptions(), args);
        } catch (ParseException e) {
            throw new RuntimeException (e);
        }
    }
	
	public static void main (String[] args) {
		MainClass mainClass = new MainClass();
		mainClass.execute(args);
	}


}
