package ch.isbsib.sparql.vcf;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.broadinstitute.variant.variantcontext.Allele;
import org.broadinstitute.variant.variantcontext.VariantContext;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;

public class VCFToTripleConverter {
	private final boolean rdftype;
	private final boolean faldobegin;
	private final boolean faldoend;

	public VCFToTripleConverter(ValueFactory vf, IRI... preds) {
		super();
		this.vf = vf;
		List<IRI> predList = Arrays.asList(preds);
		boolean tempType = predList.contains(RDF.TYPE);
		boolean tempfaldobegin = predList.contains(FALDO.BEGIN_PREDICATE);
		boolean tempfaldoend = predList.contains(FALDO.END_PREDICATE);

		if (predList.isEmpty() || predList.contains(null)) {
			tempType = true;
			tempfaldobegin = true;
			tempfaldoend = true;
		}
		rdftype = tempType;
		faldobegin = tempfaldobegin;
		faldoend = tempfaldoend;
		// type = predList.contains(RDF.TYPE) || predList.isEmpty() ||
		// predList.contains(null);
	}

	private final ValueFactory vf;

	public List<Statement> convertLineToTriples(String filePath,
			VariantContext feature, long lineNo) {
		List<Statement> stats = new ArrayList<Statement>(28);
		String recordPath = filePath + '/' + lineNo;
		IRI recordId = vf.createIRI(recordPath);
		IRI alignStartId = vf.createIRI(recordPath + "#start");
		IRI alignEndId = vf.createIRI(recordPath + "#end");

		add(stats, recordId, VCF.CHROMOSOME, feature.getChr());

		if (rdftype) {
			rdfTypesForFeature(stats, recordId, alignStartId, alignEndId);
		}
		if (faldobegin) {
			add(stats, recordId, FALDO.BEGIN_PREDICATE, alignStartId);
		}
		add(stats, alignStartId, FALDO.POSTION_PREDICATE, feature.getStart());
		add(stats, alignStartId, FALDO.REFERENCE_PREDICATE, feature.getChr());

		if (faldoend) {
			add(stats, recordId, FALDO.END_PREDICATE, alignEndId);
		}
		add(stats, alignEndId, FALDO.POSTION_PREDICATE, feature.getEnd());
		add(stats, alignEndId, FALDO.REFERENCE_PREDICATE, feature.getChr());
		if (feature instanceof VariantContext) {
			stats.addAll(convertVariantContextToTriples(filePath, (VariantContext) feature,
					lineNo));
		}
		return stats;
	}

	protected void rdfTypesForFeature(List<Statement> stats, IRI recordId,
			IRI alignStartId, IRI alignEndId) {
		add(stats, recordId, RDF.TYPE, VCF.FEATURE_CLASS);
		add(stats, recordId, RDF.TYPE, FALDO.REGION_CLASS);
		add(stats, alignStartId, RDF.TYPE, FALDO.EXACT_POSITION_CLASS);
		add(stats, alignEndId, RDF.TYPE, FALDO.EXACT_POSITION_CLASS);
	}

	private List<Statement> convertVariantContextToTriples(String filePath,
			VariantContext feature, long lineNo) {
		List<Statement> stats = new ArrayList<Statement>(28);
		String recordPath = filePath + '/' + lineNo;
		IRI recordId = vf.createIRI(recordPath);
		for (String label:feature.getFilters()) // name
			add(stats, recordId, RDFS.LABEL, label);
		if (feature.getCalledChrCount() > 0 ) // score
			add(stats, recordId, VCF.CALLED_CHR_COUNT, feature.getCalledChrCount());
		
		for (Allele exon : feature.getAlleles()) {
			convertAllele(feature, stats, recordPath, recordId, exon);
		}
		return stats;
	}

	protected void convertAllele(VariantContext feature, List<Statement> stats,
			String recordPath, IRI recordId, Allele allele) {
		String exonPath = recordPath + "/allele/" + allele.getBaseString();
		IRI exonId = vf.createIRI(exonPath);
		IRI beginId = vf.createIRI(exonPath + "/begin");
		IRI endId = vf.createIRI(exonPath + "/end");
//		add(stats, recordId, VCF.EXON, endId);
		if (rdftype) {
			add(stats, exonId, RDF.TYPE, FALDO.REGION_CLASS);
			add(stats, endId, RDF.TYPE, FALDO.EXACT_POSITION_CLASS);
		}
		if (faldobegin) {
			add(stats, exonId, FALDO.BEGIN_PREDICATE, beginId);
		}
		add(stats, beginId, RDF.TYPE, FALDO.EXACT_POSITION_CLASS);
		if (faldoend) {
			add(stats, exonId, FALDO.END_PREDICATE, endId);
		}
		
		add(stats, endId, FALDO.REFERENCE_PREDICATE, feature.getChr());
	}



	private void add(List<Statement> statements, IRI subject, IRI predicate,
			String string) {
		add(statements, subject, predicate, vf.createLiteral(string));

	}

	private void add(List<Statement> statements, IRI subject, IRI predicate,
			int string) {
		add(statements, subject, predicate, vf.createLiteral(string));

	}

	private void add(List<Statement> statements, Resource subject,
			IRI predicate, Value object) {
		statements.add(vf.createStatement(subject, predicate, object));
	}
}
