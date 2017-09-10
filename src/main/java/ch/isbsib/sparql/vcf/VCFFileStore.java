package ch.isbsib.sparql.vcf;

import java.io.File;

import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.sail.SailConnection;
import org.eclipse.rdf4j.sail.SailException;
import org.eclipse.rdf4j.sail.helpers.AbstractSail;

public class VCFFileStore extends AbstractSail {
	private File dir;
	private ValueFactory vf;

	@Override
	public boolean isWritable() throws SailException {
		return false;
	}

	@Override
	public ValueFactory getValueFactory() {
		return vf;
	}

	@Override
	protected void shutDownInternal() throws SailException {

	}

	@Override
	protected SailConnection getConnectionInternal() throws SailException {
		return new VCFConnection(dir, getValueFactory());
	}

	public void setValueFactory(ValueFactory vf) {
		this.vf = vf;
	}

	public void setDirectoryWithVCFFiles(File dir) {
		this.dir = dir;
	}
}
