package ch.isbsib.sparql.vcf;

import java.io.File;

import org.openrdf.sail.Sail;
import org.openrdf.sail.config.SailConfigException;
import org.openrdf.sail.config.SailFactory;
import org.openrdf.sail.config.SailImplConfig;

public class VCFFileStoreFactory implements SailFactory {

	/**
	 * The type of repositories that are created by this factory.
	 * 
	 * @see SailFactory#getSailType()
	 */
	public static final String SAIL_TYPE = "isbsib:VCFFileStore";

	/**
	 * Returns the Sail's type: <tt>openrdf:MemoryStore</tt>.
	 */
	@Override
	public String getSailType() {
		return SAIL_TYPE;
	}

	@Override
	public SailImplConfig getConfig() {
		return new VCFFileConfig();
	}

	@Override
	public Sail getSail(SailImplConfig config)
		throws SailConfigException
	{
		if (!SAIL_TYPE.equals(config.getType())) {
			throw new SailConfigException("Invalid Sail type: " + config.getType());
		}

		VCFFileStore memoryStore = new VCFFileStore();

		if (config instanceof VCFFileConfig) {
			VCFFileConfig memConfig = (VCFFileConfig)config;

			memoryStore.setBedFile(new File(memConfig.getFile()));
		}

		return memoryStore;
	}

}
