package middleware;
import java.io.*;
import java.net.*;
import java.util.*;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.nio.channels.spi.*; 

public class EchoWorker implements Runnable {
    private Queue queue = new LinkedList();
    private int numOfServers;
    private int numThreadsPTP;
    private int replicas;
    
    ReadQueue[] readQueueArray;
    WriteQueue[] writeQueueArray;
    int serverNumber = 0;
    
    ByteBuffer buff = ByteBuffer.allocate(2048);
    
    Load l;
    
    String keyString;

    public EchoWorker(LinkedList<MemcachedNode> nodes, int numThreadsPTP, int replicas, int numOfServers) {
        this.numOfServers = numOfServers;
        this.numThreadsPTP = numThreadsPTP;
        this.replicas = replicas;

        writeQueueArray = new WriteQueue[this.numOfServers];
        readQueueArray =  new ReadQueue[this.numOfServers];

        for(int i=0;i<this.numOfServers;i++)
        {
            // try {
                MemcachedNode tempNode = (MemcachedNode) nodes.get(i);
                readQueueArray[i] = new ReadQueue(tempNode.ipAddress, tempNode.port, this.numThreadsPTP);
                writeQueueArray[i] = new WriteQueue(nodes, tempNode.ipAddress, tempNode.port, this.replicas, tempNode.id);
            // }catch(UnknownHostException e) {
            //     e.printStackTrace();
            // }
        }
    }

    public void processData(SocketChannel socketChannel, ByteBuffer buf, int count, long insertionTime, int wanaLog) {
        
        synchronized(queue) {
            queue.add(new Load(socketChannel, buf, insertionTime, wanaLog));
            queue.notify();
        }
    }

    public void run() {

        while(true) {
            // Wait for data to become available
            synchronized(queue) {
                while(queue.isEmpty()) {
                    try {
                        queue.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                synchronized (queue) {
                    l = (Load) queue.poll();
                    queue.notify();
                    buff = l.getBuffer().duplicate();
                    
                        // String v = new String(buff.array());
                        // System.out.println(v);
                    buff.flip();
                    // Step 1: Read from buffer type of Operation
                    int opcode;
                    char op = (char) buff.get();
                    
                    if(op == 's') {
                        opcode = 0;
                    }else if(op == 'g') {
                        opcode = 1;
                    }else {
                        opcode = 2;
                    }
                    //System.out.println(op);
                    if(opcode <2){
                        // Step 2: Move buffer 3 steps ahead so that we get key
                        for(int i=0; i<3 ;i++) {
                            buff.get();
                            // System.out.print((char) buff.get());
                        }

                        // Step 3: Build key by readinf next 16 bytes of information
                        byte[] key = new byte[16]; 
                        for(int i=0; i<16; i++) {
                            key[i] = buff.get();
                        }
                        buff.rewind();
                        buff.flip();
                        //buff.compact();
                        
                        try {
                            this.keyString = new String(key, "UTF-8");
                        }catch(UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                        l.setKey(this.keyString);
                        //System.out.println("This is keyString!!!!  " + this.keyString);

                        // String v = new String(buff.array());
                        // System.out.println(v);
                        // Step 4: Get the server number which we need to send this query
                        // try {
                        serverNumber = Math.abs(this.keyString.hashCode())%numOfServers;  

                        try {
                            // // Set Operation
                            l.setQueueTime();
                            
                            if (opcode == 0) {
                                //System.out.println("#####################WriteQueue enters");
                                writeQueueArray[serverNumber].addWriteTask(l);               
                            }else if (opcode == 1) {
                                // Call hash on the key and find it's server
                                readQueueArray[serverNumber].addReadTask(l);
                            }
                        }catch(InterruptedException e) {
                            e.printStackTrace();
                        }
                    }else {
                        // Step 2: Move buffer 3 steps ahead so that we delete key
                        for(int i=0; i<5 ;i++) {
                            buff.get();
                            // System.out.print((char) buff.get());
                        }

                        // Step 3: Build key by readinf next 16 bytes of information
                        byte[] key = new byte[16]; 
                        for(int i=0; i<16; i++) {
                            key[i] = buff.get();
                        }
                        buff.rewind();
                        buff.flip();
                        //buff.compact();
                        
                        try {
                            this.keyString = new String(key, "UTF-8");
                        }catch(UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                        l.setKey(this.keyString);
                        //System.out.println("This is keyString!!!!  " + this.keyString);

                        // String v = new String(buff.array());
                        // System.out.println(v);
                        // Step 4: Get the server number which we need to send this query
                        // try {
                        serverNumber = Math.abs(this.keyString.hashCode())%numOfServers;  

                        try {
                            // // Set Operation
                             l.setQueueTime();
                            writeQueueArray[serverNumber].addWriteTask(l);               
                        }catch(InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    } 
}