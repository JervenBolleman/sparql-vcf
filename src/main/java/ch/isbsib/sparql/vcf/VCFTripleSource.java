package ch.isbsib.sparql.vcf;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.rdf4j.common.iteration.CloseableIteration;
import org.eclipse.rdf4j.common.iteration.EmptyIteration;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.algebra.Join;
import org.eclipse.rdf4j.query.algebra.evaluation.TripleSource;

public class VCFTripleSource implements TripleSource {

	private File dir;
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

	public VCFTripleSource(File dir, ValueFactory vf) {
		this.dir = dir;
		this.vf = vf;
	}

	@Override
	public CloseableIteration<? extends Statement, QueryEvaluationException> getStatements(
			Resource subj, IRI pred, Value obj, Resource... contexts)
			throws QueryEvaluationException {
		if (pred == null || possiblePredicates.contains(pred)) {
			List<CloseableIteration<Statement, QueryEvaluationException>> li = new ArrayList<>();
			for (File f : findVCFilesInDir()) {
				try {
					li.add(new VCFFileFilterReader(f, subj, pred, obj,
							contexts, getValueFactory()));
				} catch (IOException e) {
					throw new QueryEvaluationException(e);
				}
			}
			return new ConcatenatingCloseabelIterator<Statement>(li.iterator());
		} else
			return new EmptyIteration<Statement, QueryEvaluationException>();
	}

	private File[] findVCFilesInDir() {
		return dir.listFiles(new FilenameFilter() {

			@Override
			public boolean accept(File file, String name) {
				return name.endsWith(".vcf") || name.endsWith(".vcf.gz");
			}
		});
	}

	@Override
	public ValueFactory getValueFactory() {
		return vf;
	}

	public CloseableIteration<BindingSet, QueryEvaluationException> getStatements(
			BindingSet bindings, Join join) {
		return new VCFFileBindingReader(dir, bindings, join, getValueFactory());

	}

}
