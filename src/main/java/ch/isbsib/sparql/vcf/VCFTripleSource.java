package ch.isbsib.sparql.vcf;

import info.aduna.iteration.CloseableIteration;
import info.aduna.iteration.EmptyIteration;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.openrdf.model.IRI;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.Join;
import org.openrdf.query.algebra.evaluation.TripleSource;

public class VCFTripleSource implements TripleSource {

	private File file;
	private ValueFactory vf;

	private static final Set<IRI> possiblePredicates = new HashSet<>();
	{
		possiblePredicates.add(RDF.TYPE);
		possiblePredicates.add(VCF.CHROMOSOME);
		possiblePredicates.add(VCF.CALLED_CHR_COUNT);
		possiblePredicates.add(FALDO.AFTER_PREDICATE);
		possiblePredicates.add(FALDO.BEFORE_PREDICATE);
		possiblePredicates.add(FALDO.BEGIN_PREDICATE);
		possiblePredicates.add(FALDO.END_PREDICATE);
		possiblePredicates.add(FALDO.POSTION_PREDICATE);
		possiblePredicates.add(FALDO.LOCATION_PREDICATE);
		possiblePredicates.add(FALDO.REFERENCE_PREDICATE);
	}

	public VCFTripleSource(File file, ValueFactory vf) {
		this.file = file;
		this.vf = vf;
	}

	@Override
	public CloseableIteration<? extends Statement, QueryEvaluationException> getStatements(
			Resource subj, IRI pred, Value obj, Resource... contexts)
			throws QueryEvaluationException {
		if (pred == null || possiblePredicates.contains(pred)) {
			return new VCFFileFilterReader(file, subj, pred, obj, contexts,
					getValueFactory());
		} else
			return new EmptyIteration<Statement, QueryEvaluationException>();
	}

	@Override
	public ValueFactory getValueFactory() {
		return vf;
	}

	public CloseableIteration<BindingSet, QueryEvaluationException> getStatements(
			BindingSet bindings, Join join) {
		return new VCFFileBindingReader(file, bindings, join, getValueFactory());

	}

}
