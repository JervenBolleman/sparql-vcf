package ch.isbsib.sparql.vcf;

import static ch.isbsib.sparql.vcf.VCFFileStoreSchema.FILE;

import org.openrdf.model.Graph;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.util.GraphUtil;
import org.openrdf.model.util.GraphUtilException;
import org.openrdf.sail.config.AbstractSailImplConfig;
import org.openrdf.sail.config.SailConfigException;

public class VCFFileConfig extends AbstractSailImplConfig {

	private String file;

	public VCFFileConfig() {
		super(VCFFileStoreFactory.SAIL_TYPE);
	}

	public String getFile() {
		return file;
	}

	@Override
	public void parse(Graph graph, Resource implNode)
			throws SailConfigException {
		super.parse(graph, implNode);

		try {
			Literal persistValue = GraphUtil.getOptionalObjectLiteral(graph,
					implNode, FILE);
			if (persistValue != null) {
				try {
					setFile((persistValue).stringValue());
				} catch (IllegalArgumentException e) {
					throw new SailConfigException("Boolean value required for "
							+ FILE + " property, found " + persistValue);
				}
			}
		} catch (GraphUtilException e) {
			throw new SailConfigException(e.getMessage(), e);
		}
	}

	private void setFile(String stringValue) {
		this.file = stringValue;

	}
	
	@Override
	public Resource export(Graph graph)
	{
		Resource implNode = super.export(graph);

		if (this.file != null) {
			graph.add(implNode, FILE, graph.getValueFactory().createLiteral(file));
		}

		return implNode;
	}
}
