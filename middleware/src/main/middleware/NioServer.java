package middleware;
import java.io.*;
import java.net.*;
import java.util.*;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.nio.channels.spi.*;   


public class NioServer{
    // The host:port combination to listen on
    private InetAddress hostAddress;
    private int port;
    private EchoWorker worker;
    private int numServers;
    private int replicas;
    private int numThreadsPTP;
    private int count = 0;
    long insertionTime;

    // static Logger logger = Logger.getLogger(LoggingExamples.class.getName());

    // The channel on which we'll accept connections
    private ServerSocketChannel serverChannel;
    LinkedList<MemcachedNode> nodes;

    // The selector we'll be monitoring
    private Selector selector;

    public NioServer(String hostAddress, int port, List<String> mcAddresses, int numThreadsPTP, int writeToCount) throws IOException {
        this.hostAddress = InetAddress.getByName(hostAddress);
        this.port = port;
        this.numThreadsPTP = numThreadsPTP;
        this.replicas = writeToCount;

        this.numServers = mcAddresses.size();

        nodes = new LinkedList();
        for (int i = 0; i < this.numServers; i++) {
            String lines[] = ((String)mcAddresses.get(i)).split(":");
            MemcachedNode mcnode = new MemcachedNode(i, InetAddress.getByName(lines[0]), Integer.parseInt(lines[1]));
            nodes.add(mcnode);
        }

        EchoWorker worker = new EchoWorker(nodes, this.numThreadsPTP, this.replicas, this.numServers);
        new Thread(worker).start();
        this.worker = worker;

        this.selector = this.initSelector();
    }

    private Selector initSelector() throws IOException {
        // Create a new selector
        Selector socketSelector = SelectorProvider.provider().openSelector();

        // Create a new non-blocking server socket channel
        this.serverChannel = ServerSocketChannel.open();
        serverChannel.configureBlocking(false);

        // Bind the server socket to the specified address and port
        InetSocketAddress isa = new InetSocketAddress(this.hostAddress, this.port);
        serverChannel.socket().bind(isa);

        // Register the server socket channel, indicating an interest in 
        // accepting new connections
        serverChannel.register(socketSelector, SelectionKey.OP_ACCEPT);

        return socketSelector;
    }

    public void run() {
        while (true) {
            try {
                // Wait for an event one of the registered channels
                this.selector.selectNow();

                // Iterate over the set of keys for which events are available
                Iterator selectedKeys = this.selector.selectedKeys().iterator();
                while (selectedKeys.hasNext()) {
                    SelectionKey key = (SelectionKey) selectedKeys.next();
                    selectedKeys.remove();

                    if (!key.isValid()) {
                        continue;
                    }

                    // Check what event is available and deal with it
                    if (key.isAcceptable()) {
                        this.accept(key);
                    }else if (key.isReadable()) {
                        this.read(key);
                    }else if (key.isWritable()) {
                        System.out.print("");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void accept(SelectionKey key) throws IOException {
        // For an accept to be pending the channel must be a server socket channel
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();

        // Accept the connection and make it non-blocking
        SocketChannel socketChannel = serverSocketChannel.accept();
        socketChannel.configureBlocking(false);

        // Register the new SocketChannel with our Selector, indicating
        // we'd like to be notified when there's data waiting to be read
        socketChannel.register(this.selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
    }

    private void read(SelectionKey key) throws IOException {
        SocketChannel socketChannel = (SocketChannel) key.channel();
        // The buffer into which we'll read data when it's available
        ByteBuffer readBuffer = ByteBuffer.allocate(2048);

        readBuffer.clear();

        int numRead;
        try {
            count++;
            insertionTime = System.nanoTime();
            numRead = socketChannel.read(readBuffer);
        } catch (IOException e) {
          // The remote forcibly closed the connection, cancel
          // the selection key and close the channel.
            key.cancel();
            socketChannel.close();
            return;
        }

        if (numRead == -1) {
          // Remote entity shut the socket down cleanly. Do the
          // same from our end and cancel the channel.
          key.channel().close();
          key.cancel();
          return;
        }
        if(count%100 == 0) {
            this.worker.processData(socketChannel, readBuffer, numRead, insertionTime, 1); 
        }else {
            this.worker.processData(socketChannel, readBuffer, numRead, insertionTime, 0);
        }
    }

}

