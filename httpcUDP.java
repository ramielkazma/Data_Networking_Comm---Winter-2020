import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.nio.ByteBuffer;
import static java.nio.channels.SelectionKey.OP_READ;

public class httpcUDP {
	
	// Constants
	private final static int DEFAULT_CLIENT_PORT = 41830;
	private final static String DEFAULT_HOST = "localhost";
	private final static int DEFAULT_ROUTER_PORT = 3000;	
	
	// Properties
	private SocketAddress clientAddress;
    private SocketAddress routerAddress;
    private boolean isCommunicationEstablished = false;
    
    // Default constructor
    public httpcUDP() {
        this.clientAddress = new InetSocketAddress(DEFAULT_CLIENT_PORT);
        this.routerAddress = new InetSocketAddress(DEFAULT_HOST, DEFAULT_ROUTER_PORT);
    }

    /*
     * Start UDP transfer
     */
    public String execute(InetSocketAddress serverAddress, String request) throws IOException {
    	String response = null;
    	
    	try (DatagramChannel channel = DatagramChannel.open()) {
    		channel.bind(this.clientAddress);
            long newSequenceNumber = establishCommunication(channel, serverAddress);            
                          
            // Send request if client-server communication has been established
            if(this.isCommunicationEstablished) {
            	System.out.println("Client-server communication has been established. Request will now be transferred...\r\n");
            
            	// Prepare request packet
            	Packet packet = new Packet.Builder()
            			.setType(0)
                        .setSequenceNumber(newSequenceNumber + 1)
                        .setPortNumber(serverAddress.getPort())
                        .setPeerAddress(serverAddress.getAddress())
                        .setPayload(request.getBytes())
                        .create();                                         	
                
                // Send request packet to router                
                channel.send(packet.toBuffer(), routerAddress);                
                setTimeout(channel, packet);

                // Process response from server
                ByteBuffer byteBuffer = ByteBuffer.allocate(Packet.MAX_LEN);
                this.routerAddress = channel.receive(byteBuffer);
                byteBuffer.flip();               
                Packet responsePacket = Packet.fromBuffer(byteBuffer);
                
                if (responsePacket.getType() == 1) {
                    response = new String(responsePacket.getPayload(), StandardCharsets.UTF_8);
                }           
            }                       
    	}
    	catch (Exception e) {
    		System.err.println("Error occured while trying to establish connection to " + serverAddress.toString());
            System.exit(1);            
    	}
    	
		return response;
    }
    
    /*
     * Process to establish client-server communication
     */
    private long establishCommunication(DatagramChannel channel, InetSocketAddress serverAddress) throws IOException {
        String message = "Hi S";
        Packet clientPacket = new Packet.Builder()
                .setType(2)
                .setSequenceNumber(1L)
                .setPortNumber(serverAddress.getPort())
                .setPeerAddress(serverAddress.getAddress())
                .setPayload(message.getBytes())
                .create();
        
        // Send packet to router
        channel.send(clientPacket.toBuffer(), routerAddress);        
        System.out.println("Sending \"" + message + "\" to router at " + routerAddress.toString());

        // Try to receive packet within timeout
        setTimeout(channel, clientPacket);
        
        // Process response from server
        ByteBuffer byteBuffer = ByteBuffer.allocate(Packet.MAX_LEN);
        byteBuffer.clear();
        channel.receive(byteBuffer);
        byteBuffer.flip();
        Packet serverPacket = Packet.fromBuffer(byteBuffer);
        String payload = new String(serverPacket.getPayload(), StandardCharsets.UTF_8);
        System.out.println("Received \"" + payload + "\" from server at " + DEFAULT_HOST + serverPacket.getPeerAddress() + ":" + serverPacket.getPeerPort());                                                
        this.isCommunicationEstablished = true;
                
        return serverPacket.getSequenceNumber();
    }
    
    /*
     * Try to receive a packet within timeout
     */
    private void setTimeout(DatagramChannel channel, Packet packet) throws IOException {
    	channel.configureBlocking(false);
        Selector selector = Selector.open();
        channel.register(selector, OP_READ);
        selector.select(5000);

        Set<SelectionKey> keys = selector.selectedKeys();
        if (keys.isEmpty()) {
        	// Attempt to send packet again if no response after timeout
        	channel.send(packet.toBuffer(), routerAddress);
        	setTimeout(channel, packet);
        }
        
        keys.clear();        
    }
}
