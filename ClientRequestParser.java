// Used args4j external library for command line arguments parsing

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.ParserProperties;

// This class parses the request parameters sent to the server
public class ClientRequestParser {
	@Argument (required = false, index = 0, usage = "Protocol [GET|POST]")
	private String protocol;
	@Argument (required = false, index = 1, usage = "Argument")
	private String argument;
	@Argument (required = false, index = 2, usage = "HTTP protocol version")
	private String httpProtocol;
	
	// Default constructor: parses the arguments
	public ClientRequestParser(String[] args) throws CmdLineException {		
		CmdLineParser parser = new CmdLineParser(this, ParserProperties.defaults());
    	parser.parseArgument(args); 	
	}
	
	// Returns GET or POST
	public String getProtocol() {
		return this.protocol;
	}
	
	// Returns argument to directory or file
	public String getArgument() {
		return this.argument;
	}
}
