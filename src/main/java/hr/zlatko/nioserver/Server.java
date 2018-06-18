package hr.zlatko.nioserver;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;




class SocketAccepter implements Runnable{

    private int tcpPort = 0;
    private ServerSocketChannel serverSocket = null;

    private Queue socketQueue = null;

    public SocketAccepter(int tcpPort, Queue socketQueue)  {
        this.tcpPort     = tcpPort;
        this.socketQueue = socketQueue;
    }



    public void run() {
        try{
            this.serverSocket = ServerSocketChannel.open();
            this.serverSocket.bind(new InetSocketAddress(tcpPort));
        } catch(IOException e){
            e.printStackTrace();
            return;
        }

        while(true){
            try{
                SocketChannel socketChannel = this.serverSocket.accept();
                System.out.println("Socket accepted: " + socketChannel);
                //todo check if the queue can even accept more sockets.
                //XXXXX maximum number of sockets
                //this.socketQueue.add(new Socket(socketChannel));
                this.socketQueue.add(socketChannel);
            } catch(IOException e){
                e.printStackTrace();
            }

        }

    }
}



public class Server {
	
	private SocketAccepter  socketAccepter  = null;
    //private SocketProcessor socketProcessor = null;
	
	/*
	TU implementiram non blocking server sa hrpom 
	
	Napravi i Client moze connection pool 
	*/
	
	private final int tcpPort;
	//sto se desi ako alociram vise od 1024 ?
	Queue socketQueue = new ArrayBlockingQueue(1024);
	
	public Server(int tcpPort) {
		this.tcpPort = tcpPort;
		this.socketAccepter  = new SocketAccepter(tcpPort, socketQueue);
			
	}
	
	public void start() {
		Thread accepterThread  = new Thread(this.socketAccepter);
		accepterThread.start();	
	}
	
	
	public static void main(String[] args) {
		int port = 9999;
		System.out.println("---- Starting server ----");
		Server server = new Server(9999);
		server.start();
	}
		
}







