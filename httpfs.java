// Used args4j external library for command line arguments parsing
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.args4j.ParserProperties;

public class httpfs {	
	
	// Constants
	private final static int DEFAULT_PORT = 8007;
	private final static String DEFAULT_SERVER_ADDRESS = "localhost";
	private final static String DEFAULT_DIRECTORY = System.getProperty("user.dir");
	
	@Argument (required = false, index = 0)
	private String command = "";
	@Option (name = "help", help = true, usage = "Help menu")
	private boolean isHelp = false;
	@Option (name = "-v", usage = "Verbose [ON|OFF]")	
	private boolean isVerbose = false;	
	@Option (name = "-p", usage = "Port #")
	private int port = DEFAULT_PORT;	
	@Option (name = "-d", usage = "Path-to-dir")
	private String directory = DEFAULT_DIRECTORY;	
	
    public static void main(String[] args) throws Exception {
    	new httpfs().execute(args);
    }  

    /*
     * Process the request
     */
    public void execute(String[] args) throws Exception {    	    	           
    	processCommandLineInput(args);
    	
    	try {
    		if (command.equalsIgnoreCase("help")) {
        		displayHelpMessage();
        	} else {
        		verifyDirectory();
        		startServer();
        	}
    	} catch (Exception ex) {
    		System.out.println(ex);
    	}
    }
    
    // Parses command line input using 3rd party library args4j
    public void processCommandLineInput(String[] args) throws CmdLineException {
    	CmdLineParser parser = new CmdLineParser(this, ParserProperties.defaults());
    	parser.parseArgument(args);       
    }    
    
    /*
     * Displays the help message of httpfs
     */
    public void displayHelpMessage() {
    	System.out.println("httpfs is a simple file server." + "\n");
    	System.out.println("usage: httpfs [-v] [-p PORT] [-d PATH-TO-DIR]" + "\n");
    	System.out.println("\t" + "-v" + "\t" + "Prints debugging messages." + "\n");
    	System.out.println("\t" + "-p" + "\t" + "Specifies the port number that the server will listen and serve at. Default is 8080." + "\n");
    	System.out.println("\t" + "-d" + "\t" + "Specifies the directory that the server will use to read/write requested files. Default is the current directory when launching the application.");
    }
    
    /*
     * Verifies if user input directory is within the file service application working directory
     */
    public void verifyDirectory() throws Exception {  
    	if (!directory.contains(System.getProperty("user.dir"))) {
    		throw new Exception("HTTP 403 Forbidden. Access to this directory is denied.");
    	}
    }
   
    /*
     * Starts server for listen and serve functionalities
     */
    public void startServer() throws Exception {        	    	    	    	    	    	    
    	new httpfsUDP(DEFAULT_SERVER_ADDRESS, port, directory, isVerbose);    	    	
    }      	
}