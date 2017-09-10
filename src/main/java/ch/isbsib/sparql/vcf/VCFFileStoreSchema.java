package ch.isbsib.sparql.vcf;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

public class VCFFileStoreSchema {
	/**
	 * The BEDFileStore schema namespace
	 * (<tt>http://www..org/config/sail/memory#</tt>).
	 */
	public static final String NAMESPACE = "https://github.com/JervenBolleman/sparql-vcf/config/sail/vcffile#";

	public final static IRI FILE;

	static {
		ValueFactory factory = SimpleValueFactory.getInstance();
		FILE = factory.createIRI(NAMESPACE, "file");
	}
}
