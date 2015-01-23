package neo.requirements.sat;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.net.URL;

import neo.requirements.sat.NextReleaseProblem.Constraint;
import neo.requirements.sat.NextReleaseProblem.ConstraintType;

import org.junit.Test;

import NRPReaders.FileReader;

public class FileReaderTest {

	@Test
	public void testReadInstance() {
		URL instanceURL = getClass().getResource("/dataset2.dat");
		File instance = new File(instanceURL.getFile());
		FileReader reader = new FileReader(instance);
		NextReleaseProblem nrp = reader.readInstance();
		
		assertEquals(100,nrp.getRequirements());
		assertEquals(5, nrp.getStakeholders());
		assertEquals(19, nrp.getEffortOfRequirement(1));
		assertEquals(3, nrp.getValueOfRequirementForStakeholder(99, 4));
		
		assertEquals(42,nrp.getConstraints().size());
		Constraint constraint = nrp.getConstraints().get(0);
		
		assertEquals(20, constraint.firstRequirement);
		assertEquals(21, constraint.secondRequirement);
		assertEquals(ConstraintType.SIMULTANEOUS, constraint.type);
	}

}
