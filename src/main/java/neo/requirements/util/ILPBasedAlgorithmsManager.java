package neo.requirements.util;

import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import neo.requirements.cplex.ILPBasedBiobjectiveSolver;

public class ILPBasedAlgorithmsManager {
	private Map<String, ILPBasedBiobjectiveSolver> processes;

	public ILPBasedAlgorithmsManager(String processPackage) {
		processes = new HashMap<>();
		List<Class<? extends ILPBasedBiobjectiveSolver>> res = ClassesDiscovery
				.getClassesForPackageWithSuperclass(processPackage,
						ILPBasedBiobjectiveSolver.class);
		for (Class<? extends ILPBasedBiobjectiveSolver> c : res) {
			int mod = c.getModifiers();
			if (!Modifier.isAbstract(mod) && !Modifier.isInterface(mod)
					&& Modifier.isPublic(mod)) {
				try {
					ILPBasedBiobjectiveSolver e = c.newInstance();

					if (processes.get(e.getCommandLineName()) != null) {
						System.out.println("Duplicate ID in package "
								+ processPackage + ": class " + c.getName()
								+ " will not be loaded");
					}

					processes.put(e.getCommandLineName(), e);
				} catch (Exception e) {
					System.out.println("Class " + c.getName()
							+ " cannot be loaded");
				}
			}
		}
	}

	public String getListOfAlgorithms() {
		String res = "";
		for (String id : processes.keySet()) {
			res += id + " ";
		}
		return res;
	}
	
	public ILPBasedBiobjectiveSolver getSolver(String id) {
		return processes.get(id);
	}
}
