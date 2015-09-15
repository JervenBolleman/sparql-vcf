package ch.isbsib.sparql.http;

import java.io.File;


import org.openrdf.model.impl.SimpleValueFactory;
import org.openrdf.repository.sail.SailRepository;
//import com.sun.net.httpserver.HttpServer;
import ch.isbsib.sparql.vcf.VCFFileStore;

public class SPARQLServer {
	private final SailRepository sr;

	private SPARQLServer(File dataDir, String vcffile) {
		VCFFileStore rep = new VCFFileStore();
		rep.setDataDir(dataDir);
		rep.setBedFile(new File(vcffile));
		rep.setValueFactory(new SimpleValueFactory());
		this.sr = new SailRepository(rep);
		rep.initialize();
		this.sr.initialize();
		
//		HttpServer
	}
	
	
}
