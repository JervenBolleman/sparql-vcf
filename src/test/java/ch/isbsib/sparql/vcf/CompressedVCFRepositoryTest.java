package ch.isbsib.sparql.vcf;

import java.io.File;

import org.junit.After;
import org.junit.Before;

public class CompressedVCFRepositoryTest extends VCFRepositoryTest {


	@Override
	@Before
	public void setUp() {
		// try {
		newFile = new File(CompressedVCFRepositoryTest.class.getClassLoader()
				.getResource("example.sorted.bed.gz").getFile());
		dataDir = folder.newFolder("data.dir");
		//
		// } catch (IOException e) {
		// fail();
		// }
	}

	@After
	public void tearDown() {

		dataDir.delete();
	}

	
}
