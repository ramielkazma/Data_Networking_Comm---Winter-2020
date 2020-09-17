// Used args4j external library for command line arguments parsing
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.ParserProperties;
import java.net.URL;

public class httpc {		
	
	// Constants
	private final static String GET = "get";
	private final static String POST = "post";
	
	// Command line parser declarations
	@Option (name = "-help", help = true, usage = "Help menu")
	private boolean isHelp = false;
	@Argument (required = false, index = 0, usage = "Protocol [GET|POST]")
	private String protocol = "";
	@Option (name = "-v", usage = "Verbose [ON|OFF]" )
	private boolean isVerbose = false;
	@Option(name = "-h")
	private String headers = "";	
	@Argument (required = false, index = 1, usage = "URL")
	private String URL;
	@Option(name = "-d", forbids = {"-f"})
	private String inlineData = "";	
	@Option(name = "-f", forbids = {"-d"})
	private String filePath = "";
	
	// Services
	httpcService service = new httpcService();	
		
    public static void main(String[] args) throws Exception {
    	new httpc().execute(args);
    }  

    /*
     * Process the request
     */
    public void execute(String[] args) throws Exception {    	
    	processCommandLineInput(cleanseCommandLineInput(args));
    	
    	try {
    		if (isHelp) {
    			processHelp();
    		} else {    			
    			if (protocol.equalsIgnoreCase(GET)) {    		
    				service.sendGetRequest(new URL(cleanseURL(URL)), cleanseHeaders(headers), isVerbose);    				
    			}
    			else if (protocol.equalsIgnoreCase(POST)) {
    				service.sendPostRequest(new URL(cleanseURL(URL)), cleanseHeaders(headers), isVerbose, inlineData, filePath);
    			}
    		}    	       	
    	} catch (Exception ex) {
    		System.out.println(ex);
    	}    	
    }
    
    /*
     * Parses command line input using 3rd party library args4j
     */
    public void processCommandLineInput(String[] args) throws CmdLineException {
    	CmdLineParser parser = new CmdLineParser(this, ParserProperties.defaults());
    	parser.parseArgument(args);       
    }    
    
    /*
     * Format "://" to "=//" to satisfy 3rd party library for command line arguments parsing
     */
    public String[] cleanseCommandLineInput(String[] args) {
    	for(int i = 0; i < args.length; i++)
		{
			args[i] = args[i].replaceAll("://", "=//");
		}
    	
    	return args;
    }
    
    /*
     * Format "=//" to "://" to revert back to original after parsing
     */
    public String cleanseURL(String URL) {
    	return URL.replaceAll("=//", "://");
    }
    
    /*
     * Format "=" to ":" to revert back to original after parsing
     */
    public String cleanseHeaders(String headers) {
    	return headers.replaceAll("=", ":");
    }
    
    /*
     * Determines the type of help requested
     */
    public void processHelp() {
    	if (!protocol.isEmpty()) {
    		if (protocol.equalsIgnoreCase(GET)) {
    			displayHelp(GET); 	
    		}
    		else if (protocol.equalsIgnoreCase(POST) ) {
    			displayHelp(POST); 	
    		}
    	} else {
    		displayHelp("");
    	}    		   
    }
    
    /*
     * Displays help menu corresponding to type
     */
    public void displayHelp(String type) {
    	switch (type.toLowerCase()) {
    		case GET:
    			System.out.println("httpc help get");
    			System.out.println("usage: httpc get [-v] [-h key:value] URL" + "\n");
    			System.out.println("Get executes a HTTP GET request for a given URL." + "\n");
    			System.out.println("\t" + "-v" + "\t\t" + "Prints the detail of the response such as protocol, status, and headers.");
    			System.out.println("\t" + "-h key:value" + "\t" + "Associates headers to HTTP Request with the format 'key:value'." + "\n");
    			break;
    		case POST:
    			System.out.println("httpc help post");
    			System.out.println("usage: httpc post [-v] [-h key:value] [-d inline-data] [-f file] URL" + "\n");
    			System.out.println("Post executes a HTTP POST request for a given URL with inline data or from file." + "\n");
    			System.out.println("\t" + "-v" + "\t\t" + "Prints the detail of the response such as protocol, status, and headers.");
    			System.out.println("\t" + "-h key:value" + "\t" + "Associates headers to HTTP Request with the format 'key:value'.");
    			System.out.println("\t" + "-d string" + "\t" + "Associates an inline data to the body HTTP POST request.");
    			System.out.println("\t" + "-f file" + "\t\t" + "Associates the content of a file to the body HTTP POST request." + "\n");
    			System.out.println("Either [-d] or [-f] can be used but not both." + "\n");
    			break;
    		default:
    			System.out.println("httpc help" + "\n");
    	    	System.out.println("httpc is a curl-like application but supports HTTP protocol only.");
    	    	System.out.println("Usage:");   
    	    	System.out.println("\t" + "httpc command [arguments]");
    	    	System.out.println("The commands are:");
    	    	System.out.println("\t" + "get" + "\t" + "executes a HTTP GET request and prints the response.");
    	    	System.out.println("\t" + "post" + "\t" + "executes a HTTP POST request and prints the response.");
    	    	System.out.println("\t" + "help" + "\t" + "prints this screen." + "\n");
    	    	System.out.println("Use \"httpc help [command]\" for more information about a command." + "\n");
    			break;    	
    	}    	    	
    }      
}