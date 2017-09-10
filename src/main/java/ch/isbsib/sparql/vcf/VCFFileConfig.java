package ch.isbsib.sparql.vcf;

import static ch.isbsib.sparql.vcf.VCFFileStoreSchema.FILE;

import java.util.Optional;

import org.eclipse.rdf4j.model.Graph;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.util.GraphUtil;
import org.eclipse.rdf4j.model.util.GraphUtilException;
import org.eclipse.rdf4j.model.util.ModelUtil;
import org.eclipse.rdf4j.model.util.Models;
import org.eclipse.rdf4j.sail.config.AbstractSailImplConfig;
import org.eclipse.rdf4j.sail.config.SailConfigException;

public class VCFFileConfig extends AbstractSailImplConfig {

	private String file;

	public VCFFileConfig() {
		super(VCFFileStoreFactory.SAIL_TYPE);
	}

	public String getFile() {
		return file;
	}

	@Override
	public void parse(Model graph, Resource implNode) throws SailConfigException {
		super.parse(graph, implNode);

		Optional<Literal> persistValue = Models.objectLiteral(graph);
		if (persistValue.isPresent()) {
			try {
				setFile((persistValue.get()).stringValue());
			} catch (IllegalArgumentException e) {
				throw new SailConfigException(
						"Boolean value required for " + FILE + " property, found " + persistValue.get());
			}
		}

	}

	private void setFile(String stringValue) {
		this.file = stringValue;

	}

	@Override
	public Resource export(Model graph) {
		Resource implNode = super.export(graph);

		if (this.file != null) {
			graph.add(implNode, FILE, SimpleValueFactory.getInstance().createLiteral(file));
		}
		return implNode;
	}
}
