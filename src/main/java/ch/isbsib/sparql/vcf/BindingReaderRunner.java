package ch.isbsib.sparql.vcf;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingQueue;

import org.broad.tribble.AbstractFeatureReader;
import org.broad.tribble.readers.LineIterator;
import org.broadinstitute.variant.variantcontext.VariantContext;
import org.broadinstitute.variant.vcf.VCFCodec;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.algebra.StatementPattern;
import org.eclipse.rdf4j.query.impl.MapBindingSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class BindingReaderRunner implements Runnable {
	private static final Logger log = LoggerFactory
			.getLogger(BindingReaderRunner.class);
	private final BlockingQueue<BindingSet> queue;
	private final AbstractFeatureReader<VariantContext, LineIterator> reader;
	volatile boolean done = false;
	private final File bedFile;
	private final ValueFactory vf;
	private final StatementPattern left;
	private final StatementPattern right;

	public BindingReaderRunner(File vcfFile, BlockingQueue<BindingSet> queue,
			StatementPattern left, StatementPattern right, ValueFactory vf,
			BindingSet binding) {
		this.vf = vf;

		this.reader = AbstractFeatureReader.getFeatureReader(
				vcfFile.getAbsolutePath(), new VCFCodec(), false);
		this.left = left;
		this.right = right;
		this.queue = queue;
		this.bedFile = vcfFile;
	}

	@Override
	public void run() {
		long lineNo = 0;
		String filePath = "file:///" + bedFile.getAbsolutePath();
		Iterable<VariantContext> iter;
		try {
			
			VCFToTripleConverter conv = new VCFToTripleConverter(vf,
					 findKnownPredicates());
			iter = reader.iterator();
			for (VariantContext feature : iter) {
				List<Statement> statements = conv.convertLineToTriples(
						filePath, feature, lineNo++);
				List<BindingSet> bindings = new ArrayList<BindingSet>();
				filter(statements, left.getPredicateVar().getValue(), right
						.getPredicateVar().getValue());
				while (!statements.isEmpty())
					find(statements, left, right, bindings);
				try {
					for (BindingSet bs : bindings)
						queue.put(bs);
				} catch (InterruptedException e) {
					Thread.interrupted();
				}
			}
		} catch (IOException e) {
			log.error("IO error while reading bed file", e);
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				log.error("IO error while closing bed file", e);
			}
			done = true;
		}
	}

	protected IRI[] findKnownPredicates() {
		List<IRI> preds = new ArrayList<IRI>();
		Value vr = right.getPredicateVar().getValue();
		Value vl = left.getPredicateVar().getValue();
		if (vr instanceof IRI)
			preds.add((IRI) vr);
		else
			preds.add(null);
		if (vl instanceof IRI)
			preds.add((IRI) vl);
		else
			preds.add(null);
		return preds.toArray(new IRI[]{});
	}

	private void find(List<Statement> statements, StatementPattern left2,
			StatementPattern right2, List<BindingSet> bindings) {
		Statement first = statements.remove(0);
		StatementPattern found = null, toFind = null;
		if (first.getPredicate().equals(left2.getPredicateVar().getValue())) {
			found = left2;
			toFind = right2;
		} else {
			found = right2;
			toFind = left2;
		}

		for (Iterator<Statement> iterator = statements.iterator(); iterator
				.hasNext();) {
			Statement second = iterator.next();
			if (second.getPredicate().equals(
					toFind.getPredicateVar().getValue())) {
				Value ss = second.getSubject();

				Value fs = first.getSubject();
				String findSN = toFind.getSubjectVar().getName();
				String foundSN = found.getSubjectVar().getName();
				Value so = second.getObject();
				Value fo = first.getObject();
				String findON = toFind.getObjectVar().getName();
				String foundON = found.getObjectVar().getName();
				MapBindingSet mapBindingSet = new MapBindingSet();

				if (findSN.equals(foundSN) && ss.equals(fs)) {
					mapBindingSet.addBinding(findSN, fs);
					mapBindingSet.addBinding(findON, fo);
					mapBindingSet.addBinding(foundON, so);
					bindings.add(mapBindingSet);
				}

			}
		}

	}

	private void filter(List<Statement> statements, Value lv, Value rv) {
		for (Iterator<Statement> iterator = statements.iterator(); iterator
				.hasNext();) {
			Statement statement = iterator.next();
			IRI predicate = statement.getPredicate();
			if (!(predicate.equals(lv) || predicate.equals(rv)))
				iterator.remove();
		}

	}

	protected boolean matches(Value subject, Value subj) {
		return subject.equals(subj) || subj == null;
	}

}