import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.DatagramChannel;
import static java.nio.charset.StandardCharsets.UTF_8;

public class httpfsUDP {
	
	// Constants
	private final static String GET = "GET";
	private final static String POST = "POST";
	private final static String DEFAULT_HOST = "localhost";
	
	// Default constructor
    public httpfsUDP(String host, int port, String directory, boolean isVerbose) throws Exception {
    	startServer(host, port, directory, isVerbose);
    }
    
    // Start UDP server to listen and process requests
	public void startServer(String host, int port, String directory, boolean isVerbose) throws Exception {
        try (DatagramChannel channel = DatagramChannel.open()) {
        	SocketAddress serverAddress = new InetSocketAddress(host, port);        	
            channel.bind(serverAddress);
            ByteBuffer byteBuffer = ByteBuffer
                    .allocate(Packet.MAX_LEN)
                    .order(ByteOrder.BIG_ENDIAN);            
            System.out.println("httpfs is ready to accept requests @" + host + ":" + port + "\n");            

            while (true) {
            	byteBuffer.clear();
                SocketAddress router = channel.receive(byteBuffer);

                // Parse a packet from the received raw data.
                byteBuffer.flip();
                Packet packet = Packet.fromBuffer(byteBuffer);
                byteBuffer.flip();
                                
                // Process file server request packet
                if (packet.getType() == 0) {                	
                    packet.toBuilder().setType(1);
                    String payload = new String(packet.getPayload(), UTF_8);                   
                    
                    // Print verbose information if active
                    if (isVerbose) {                          	
                        System.out.println("\r\n" + payload);                       
                    }                    
                                                      
                    // Collect HTTP request details               
            		StringBuilder body = new StringBuilder();            		
            		body = extractBody(body, payload);
            		
                    String commandLineInput = payload.split("\r\n")[0];                                                           
                    ClientRequestParser parser = new ClientRequestParser(commandLineInput.split(" "));
                    
                    httpfsService fileServerService = new httpfsService(directory);   
                    String result = null;
                    
                    // Process GET or POST request
                    if (parser.getProtocol().equalsIgnoreCase(GET))
                    	result = fileServerService.processGetRequest(parser.getArgument());	                    
                    else if (parser.getProtocol().equalsIgnoreCase(POST))
                    	result = fileServerService.processPostRequest(parser.getArgument(), body);
                    
                    // Send response packet to router in order to reach client
                    Packet response = packet.toBuilder()
                    		.setSequenceNumber(packet.getSequenceNumber() + 1)
                    		.setType(1)
                    		.setPayload(result.getBytes())
                    		.create();                    
                    channel.send(response.toBuffer(), router);
                } else {
                	establishCommunication(packet, channel, router);
                	System.out.println("Client-server communication has been established. Request will now be processed...\r\n");
                }                                      
            }
        }
    }

	/*
     * Process to establish client-server communication
     */
	private void establishCommunication(Packet packet, DatagramChannel channel, SocketAddress routerAddress) throws IOException {        
        // Display what has been received
		String payload = new String(packet.getPayload(), UTF_8);
		System.out.println("Received \"" + payload + "\" from router at " + DEFAULT_HOST + packet.getPeerAddress() + ":" + packet.getPeerPort()); 
		
		// Reply with message
        String message = "Hi";
        Packet response = packet.toBuilder()
        		.setSequenceNumber(packet.getSequenceNumber() + 1)
        		.setType(3)
        		.setPayload(message.getBytes())
        		.create();
        
        channel.send(response.toBuffer(), routerAddress);
        System.out.println("Sending \"" + message + "\" to router at " + DEFAULT_HOST + routerAddress.toString());
    }
	
	/*
	 * Extracts the request body from request pay load
	 */
	private StringBuilder extractBody(StringBuilder body, String payload) {
		String[] payloadLines = payload.split("\r\n\r\n");
		for(int i = 1; i < payloadLines.length; i++)
			body.append(payloadLines[i]);          
		
		return body;
	}
}
