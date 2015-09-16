package ch.isbsib.sparql.vcf;

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.openrdf.model.impl.SimpleValueFactory;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.query.Binding;
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.SailException;

import ch.isbsib.sparql.vcf.VCF;
import ch.isbsib.sparql.vcf.VCFFileStore;
import ch.isbsib.sparql.vcf.FALDO;

public class VCFRepositoryTest extends TestCase {
	@Rule
	public TemporaryFolder folder = new TemporaryFolder();
	protected File newFile = null;
	protected File dataDir = null;

	@Before
	public void setUp() {
		// try {
		newFile = new File(VCFRepositoryTest.class.getClassLoader()
				.getResource("example.vcf").getFile());
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

	String query1 = "PREFIX vcf:<" + VCF.NAMESPACE
			+ "> SELECT DISTINCT ?read WHERE {?s vcf:Chromosome ?read}";

	@Test
	public void testRecordNumber() throws IOException,
			QueryEvaluationException, MalformedQueryException,
			RepositoryException, SailException {

		assertTrue(newFile.exists());
		VCFFileStore rep = new VCFFileStore();
		rep.setDataDir(dataDir);
		rep.setDirectoryWithVCFFiles(newFile.getParentFile());
		rep.setValueFactory(new SimpleValueFactory());
		SailRepository sr = new SailRepository(rep);
		rep.initialize();
		TupleQuery pTQ = sr.getConnection().prepareTupleQuery(
				QueryLanguage.SPARQL, query1);
		TupleQueryResult eval = pTQ.evaluate();
		for (int i = 0; i < 1; i++) {
			assertTrue(eval.hasNext());
			assertNotNull(eval.next());
		}
		assertFalse(eval.hasNext());
	}

	String query2 = "PREFIX vcf:<"
			+ VCF.NAMESPACE
			+ "> SELECT (COUNT(?score) AS ?countScore) WHERE {?s vcf:score ?score}";

//	@Test
//	public void testRecordNumberViaCount() throws IOException,
//			QueryEvaluationException, MalformedQueryException,
//			RepositoryException, SailException {
//
//		assertTrue(newFile.exists());
//		VCFFileStore rep = new VCFFileStore();
//		rep.setDataDir(dataDir);
//		rep.setBedFile(newFile);
//		rep.setValueFactory(new SimpleValueFactory());
//		SailRepository sr = new SailRepository(rep);
//		rep.initialize();
//		TupleQuery pTQ = sr.getConnection().prepareTupleQuery(
//				QueryLanguage.SPARQL, query2);
//		TupleQueryResult eval = pTQ.evaluate();
//
//		assertTrue(eval.hasNext());
//		BindingSet next = eval.next();
//		assertNotNull(next);
//		assertEquals("9", next.getBinding("countScore").getValue().stringValue());
//	}

	String query3 = "PREFIX vcf:<"
			+ VCF.NAMESPACE
			+ ">\n"
			+ "PREFIX rdf:<"
			+ RDF.NAMESPACE
			+ ">\n"
			+ "PREFIX faldo:<"
			+ FALDO.NAMESPACE
			+ ">\n"
			+ "SELECT (AVG(?length) as ?avgLength) \n"
			+ " WHERE {?s faldo:begin ?b ; faldo:end ?e . ?b faldo:position ?begin . ?e faldo:position ?end . BIND(abs(?end - ?begin) as ?length)} GROUP BY ?s";

//	@Test
//	public void testAverageReadLengthNumber() throws IOException,
//			QueryEvaluationException, MalformedQueryException,
//			RepositoryException, SailException {
//
//		assertTrue(newFile.exists());
//		VCFFileStore rep = new VCFFileStore();
//		rep.setDataDir(dataDir);
//		rep.setBedFile(newFile);
//		rep.setValueFactory(new SimpleValueFactory());
//		SailRepository sr = new SailRepository(rep);
//		rep.initialize();
//		TupleQuery pTQ = sr.getConnection().prepareTupleQuery(
//				QueryLanguage.SPARQL, query3);
//		TupleQueryResult eval = pTQ.evaluate();
//
//		assertTrue(eval.hasNext());
//		BindingSet next = eval.next();
//		assertNotNull(next);
//		Binding lb = next.getBinding("avgLength");
//		assertEquals("1166", lb.getValue()
//				.stringValue());
//	}
}
