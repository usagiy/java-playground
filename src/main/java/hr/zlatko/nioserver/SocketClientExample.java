package hr.zlatko.nioserver;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

public class SocketClientExample {
 
    
	//zr igra sa selectorom
	private Selector selector;
	
	
	public void startClient()
            throws IOException, InterruptedException {
    	//sacekaj da se digne server
    	Thread.sleep(5000);
    	InetSocketAddress hostAddress = new InetSocketAddress("localhost", 9999);
        SocketChannel client = SocketChannel.open(hostAddress);
        
        //register selector at channel
        client.configureBlocking(false);
        this.selector = Selector.open();
        client.register(this.selector, /*SelectionKey.OP_ACCEPT | SelectionKey.OP_CONNECT | */SelectionKey.OP_READ |  SelectionKey.OP_WRITE);
        
        
        
        System.out.println("Client... started");
        
        String threadName = Thread.currentThread().getName();
 
        // Send messages to server
        String [] messages = new String [] 
        		{threadName + ": test1",threadName + ": test2",threadName + ": test3"};
 
        //for (int i = 0; i < messages.length; i++) {
        for (int i = 0; i < 100; i++) {
            //byte [] message = new String(messages [i]).getBytes();
        	byte [] message = ("test " + String.valueOf(i)).getBytes();
        	ByteBuffer buffer = ByteBuffer.wrap(message);
            client.write(buffer);
            //System.out.println(messages [i]);
            System.out.println("test " + String.valueOf(i));
            buffer.clear();
            Thread.sleep(5000);
        }
        
        //testno slusanje
        while (true) {
            // wait for events
            this.selector.select();
            //work on selected keys
            Iterator keys = this.selector.selectedKeys().iterator();
            
            while (keys.hasNext()) {
                SelectionKey key = (SelectionKey) keys.next();
                System.out.println("Client selection key: " + key.toString());
                System.out.println("Client isValid: " + key.isValid());
                System.out.println("Client readable: " + key.isReadable());
                System.out.println("Client eritable: " + key.isWritable());
                
            }
        }
        
        
        
        //unreachable code
        //client.close();            
    }
}
