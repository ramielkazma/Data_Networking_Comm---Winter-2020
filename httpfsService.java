import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;

// This class executes the HTTP request on server
public class httpfsService {
	
	// Properties
	String directoryPath;
	
	// Default constructor
	public httpfsService (String directoryPath) {		
		this.directoryPath = directoryPath;
	}	
	
	// Processes the GET request based on the argument for directory or file
	public String processGetRequest(String argument) throws Exception {		
		if (argument.equalsIgnoreCase("/")) {
			return getDirectoryFiles();
		} else {
			return getDirectoryFile(argument);
		}						
	}
	
	// Processes the POST request
	public String processPostRequest(String argument, StringBuilder body) throws Exception {					
		// Handle directory and file creation
		File directory = retrieveDirectory(argument);		
		if(!directory.exists()) {
			String content = directoryPath + " does not exist.";
			return buildResponse(404, content, null);
		}
		
		// Check if access to directory is allowed
		if (!isDirectoryValid(directoryPath)) {
			String content = "Access to " + directoryPath + " is denied.";
			return buildResponse(403, content, null);
 		}
		
		File file = new File(directory, argument.substring(argument.lastIndexOf("/")));				
		
		// Write to file
		PrintWriter printWriter = new PrintWriter(new FileWriter(file, false));			
		printWriter.write(body.toString());
		printWriter.close();
			
		return buildResponse(201, "", null);
	}
	
	// Lists all files in given directory
	public String getDirectoryFiles() throws Exception {		
		File directory = new File(directoryPath);	
		
		// Check existence of directory 
		if(!directory.exists()) {
			String content = directoryPath + " does not exist.";
			return buildResponse(404, content, null);
		}		
		
		// Check if access to directory is allowed
		if (!isDirectoryValid(directoryPath)) {
			String content = "Access to " + directoryPath + " is denied.";
			return buildResponse(403, content, null);
 		}
				
		// List directory items
		File[] items =  directory.listFiles();
		String output = "";		
		for (int i = 0; i < items.length; i++) {
			if (items[i].isFile())
				output += items[i].getName() + " " + items[i].getTotalSpace() + "\r\n";			
			else if (items[i].isDirectory())
				output += "<DIR>" + items[i].getName() + "\r\n";			
		}
	
		return buildResponse(200, output, null);
	}
	
	// Returns content of specified file
	public String getDirectoryFile(String argument) throws Exception {
		File file = new File(directoryPath + argument); 
		
		if (!file.isDirectory()) {
			// Check if access to directory is allowed
			if (!isDirectoryValid(directoryPath)) {
				String content = "Access to " + directoryPath + " is denied.";
				return buildResponse(403, content, null);
	 		}
			
			// Check existence of file
			if (!file.exists()) {
				String content = file.getName() + " does not exist.";
				return buildResponse(404, content, null);
			}
					
	    	BufferedReader bufferedReader = new BufferedReader(new FileReader(file));    	
	    	String output = "", line;
	    	
	    	// Output file content
	    	while((line = bufferedReader.readLine()) != null)     	
	    		output += line + "\r\n";    		    

	    	bufferedReader.close(); 
	    	
			return buildResponse(200, output, file.getName());
		}
		else {
			String content = file.getName() + " is a directory and cannot be accessed as a file.";
			return buildResponse(400, content, null);			
		}
	}
	
	// Checks if directory exists, if not creates it
	public File retrieveDirectory(String argument) {		
		String[] subdirectories = argument.substring(1).split("/");	
		
		if (subdirectories.length > 0) {
			// Ignore file name as last argument								
			for (int i = 0; i < subdirectories.length - 1; i++) {
				File directory = new File(directoryPath + "/" + subdirectories[i]);
				
				// Create subdirectory
				if (!directory.exists())
					directory.mkdir();		       											    	
				
				directoryPath = directory.getPath();
			}																
		}			
		
		return new File(directoryPath);
	}	
	
	// Builds HTTP response
	private String buildResponse(int httpStatusCode, String content, String fileName) throws Exception {
		switch(httpStatusCode) {
			case 200:
				if (fileName != null)			
					return "HTTP/1.0 " + "200 OK" + "\r\n" + getContentTypeDisposition(fileName) + "\r\nContent-length:" + content.length() + "\r\n\r\n" + content;
				else
					return "HTTP/1.0 " + "200 OK" + "\r\nContent-Type:text/html\r\nContent-length:" + content.length() + "\r\n\r\n" + content;										
			case 201:
				content = "Server has successfully created a resource from the POST request.";
				return "HTTP/1.0 " + "201 Created" + "\r\nContent-Type:text/html\r\nContent-length:" + content.length() + "\r\n\r\n" + content;
			case 403:
				return "HTTP/1.0 " + "403 Forbidden" + "\r\nContent-Type:text/html\r\nContent-length:" + content.length() + "\r\n\r\n" + content;
			case 404:
				return "HTTP/1.0 " + "404 Not Found" + "\r\nContent-Type:text/html\r\nContent-length:" + content.length() + "\r\n\r\n" + content;
			default:
				content = content != null && !content.isEmpty() ? content : "Bad request.";		
				return "HTTP/1.0 " + "400 Bad Request" + "\r\nContent-Type:text/html\r\nContent-length:" + content.length() + "\r\n\r\n" + content;				
		}			
	}
	
	// Get Content-Type and Content-Disposition headers for GET /foo
	private String getContentTypeDisposition(String fileName) throws Exception 
	{
		return "Content-Type : text/html; charset=utf-8" + "\r\n" + "Content-Disposition: attachment; filename= " + fileName;    				
	}
	
	// Verifies if user input directory is within the file service application working directory
    public boolean isDirectoryValid(String directory) {  
    	if (!directory.contains(System.getProperty("user.dir")))
    		return false;
    	
    	return true;
    }
}
