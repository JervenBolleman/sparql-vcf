package ch.isbsib.sparql.vcf;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.broad.tribble.AbstractFeatureReader;
import org.broad.tribble.readers.LineIterator;
import org.broadinstitute.variant.variantcontext.VariantContext;
import org.broadinstitute.variant.vcf.VCFCodec;
import org.eclipse.rdf4j.common.iteration.CloseableIteration;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.query.QueryEvaluationException;

public class VCFFileFilterReader implements
		CloseableIteration<Statement, QueryEvaluationException> {

	private final AbstractFeatureReader<VariantContext, LineIterator> reader;
	private Iterator<Statement> currentFeature;
	private final Iterator<VariantContext> variantIter;
	private int lineNo = 0;
	private final String fileName;

	private final VCFToTripleConverter conv;
	private final Resource subj;
	private final IRI pred;
	private final Value obj;

	public VCFFileFilterReader(File file, Resource subj, IRI pred, Value obj,
			Resource[] contexts, ValueFactory valueFactory) throws IOException {
		this.subj = subj;
		this.pred = pred;
		this.obj = obj;
		this.reader = AbstractFeatureReader.getFeatureReader(
				file.getAbsolutePath(), new VCFCodec(), false);
		this.fileName = "file://" + file.getAbsolutePath();
		conv = new VCFToTripleConverter(valueFactory, pred);

		System.err.println("reading file:" + fileName);

		variantIter = reader.iterator();
	}

	@Override
	public boolean hasNext() throws QueryEvaluationException {
		if (currentFeature != null && currentFeature.hasNext())
			return true;

		if (!variantIter.hasNext())
			return false;
		// String filePath = file.getName();

		currentFeature = filter(
				conv.convertLineToTriples(fileName, variantIter.next(),
						lineNo++)).iterator();
		return currentFeature.hasNext();

	}

	@Override
	public Statement next() throws QueryEvaluationException {
		return currentFeature.next();

	}

	@Override
	public void remove() throws QueryEvaluationException {
		throw new QueryEvaluationException("Shizon does not support ");

	}

	@Override
	public void close() throws QueryEvaluationException {
		try {
			this.reader.close();
		} catch (IOException e) {
			throw new QueryEvaluationException(e);
		}
	}

	private List<Statement> filter(List<Statement> statements) {
		List<Statement> filtered = new ArrayList<Statement>();
		for (Statement toFilter : statements) {
			Resource subject = toFilter.getSubject();
			Resource predicate = toFilter.getPredicate();
			Value object = toFilter.getObject();
			if (matches(subject, subj) && matches(predicate, pred)
					&& matches(object, obj)) {
				filtered.add(toFilter);
			}
		}
		return filtered;
	}

	protected boolean matches(Value subject, Value subj) {
		return subject.equals(subj) || subj == null;
	}
}
