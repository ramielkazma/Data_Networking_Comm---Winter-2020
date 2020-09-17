import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.net.URL;

public class httpcService {
	
	// Constants
	private final static String GET = "GET";
	private final static String POST = "POST";
	
	/*
	 * Builds and sends GET request
	 */
	public void sendGetRequest(URL url, String headers, boolean isVerbose) throws Exception {
		String host = url.getHost();
		int port = url.getPort();
		String path = url.getPath();
		String request = buildRequest(GET, path, headers, null, null);		
				
        try
        {        	
        	httpcUDP client = new httpcUDP();
        	String response = client.execute(new InetSocketAddress(host, port), request);
        	outputResponse(isVerbose, response);        
        }      
        catch (Exception e) 
        {
            System.err.println("Error occured while trying to establish connection to " + host);
            System.exit(1);
        }  
	}
	
	/*
	 * Builds and sends POST request
	 */
	public void sendPostRequest(URL url, String headers, boolean isVerbose, String inlineData, String filePath) throws Exception {		
		String host = url.getHost();
		int port = url.getPort();
		String path = url.getPath();
		String request = buildRequest(POST, path, headers, inlineData, filePath);		
        
        try
        {     
        	httpcUDP client = new httpcUDP();
        	String response = client.execute(new InetSocketAddress(host, port), request);
        	outputResponse(isVerbose, response);        	      
        }
        catch (Exception e) 
        {
            System.err.println("Error occured while trying to establish connection to " + host);
            System.exit(1);
        }     
	}
	
	/*
	 * Builds GET or POST request string
	 */
	private String buildRequest(String protocol, String path, String headers, String inlineData, String filePath) throws Exception {
		StringBuilder request = new StringBuilder();
		String body = null;
		path = !path.isEmpty() ? path : "/";
		request.append(protocol + " " + path + " HTTP/1.0\r\n");
		
		// Extract request body
        if (inlineData != null && !inlineData.isEmpty())  	  	    
        	body = inlineData;
        else if (filePath != null && !filePath.isEmpty())
        	body = readInputFromFile(filePath);
		
		// Append user input headers      
        if (headers != null && !headers.isEmpty()) {
        	String[] keyValues = headers.split(" ");
        	for (String keyValue : keyValues)  		
        		request.append(keyValue.split(":")[0] + ":" + keyValue.split(":")[1]).append("\r\n");        	
        }
        
        // Append headers for POST
        if (protocol.equalsIgnoreCase(POST))        	
        	request.append("Content-Length: " + body.length());
                       
		return protocol.equalsIgnoreCase(POST) ? request.append("\r\n\r\n").append(body).toString() : request.append("\r\n\r\n").toString();
	}
	
	/*
	 * Display HTTP response with or without verbose information
	 */
	private void outputResponse(boolean isVerbose, String response) {
		// Display verbose information and response		
		if(isVerbose) {
			System.out.println(response);
		}			
		
		// Display only response
		else {
			String[] responseLines = response.split("\r\n\r\n");
			for(int i = 1; i < responseLines.length; i++)
				System.out.println(responseLines[i]);
		}		
	} 	 

 	/*
 	 * Reads input string from file
 	 */
 	private String readInputFromFile(String filePath) throws Exception {
 		RandomAccessFile file = new RandomAccessFile(filePath, "r");
 		String str, input = new String();

 		// Read file content
 		while ((str = file.readLine()) != null)
 			input = input.concat(str); 		
 		
 		file.close(); 		
 		return input; 		
 	}
}
