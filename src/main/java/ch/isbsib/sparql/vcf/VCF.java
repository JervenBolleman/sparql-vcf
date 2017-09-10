package ch.isbsib.sparql.vcf;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleNamespace;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

public class VCF {
	public static final String PREFIX = "vcf";
	public static final String NAMESPACE = "http://biohackathon.org/resource/vcf#";
	public static final Namespace NS = new SimpleNamespace(PREFIX, NAMESPACE);
	
	public static final IRI CHROMOSOME;
//	public static final IRI SCORE;
//	public static final IRI EXON;
	public static final IRI FEATURE_CLASS;
	public static final IRI CALLED_CHR_COUNT;
	
	static {
		final ValueFactory f = SimpleValueFactory.getInstance();
		// Properties common to Faldo
		CHROMOSOME = f.createIRI(NAMESPACE, "Chromosome");
		CALLED_CHR_COUNT = f.createIRI(NAMESPACE, "CalledChromosomeCount");
//		SCORE = f.createIRI(NAMESPACE, "score");
//		EXON = f.createIRI(NAMESPACE, "exon");
		FEATURE_CLASS= f.createIRI(NAMESPACE, "Feature");
	}
}
