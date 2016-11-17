package middleware;
import java.util.*;
import java.lang.Thread;
import java.lang.Runnable;
import java.util.concurrent.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.io.IOException;

public class ReadQueue {
    private LinkedBlockingQueue<Load> queue;
    int numThreads;

    public class ReadQueueRunnable implements Runnable {
        private InetAddress hostAddress;
        private Selector selector;
        private int port;
        SocketChannel sockChannel;

        public ReadQueueRunnable(InetAddress hostAddress, int port) {
            this.hostAddress = hostAddress;
            this.port = port;
            try { 
                this.sockChannel = SocketChannel.open(new InetSocketAddress(this.hostAddress, this.port));
                this.sockChannel.finishConnect();
            }catch(IOException e) {
                e.printStackTrace();
            }
        }

        public void run() {
            while(true) {
                // Thread thread = Thread.currentThread();
                // System.out.println("RunnableJob is being run by " + thread.getName() + " (" + thread.getId() + ")");
                try {

                    Load task = dispatchTask();
                //System.out.println(task.getKey());
            task.setQueueTime();
                    ByteBuffer readBuffer = ByteBuffer.allocate(2048);
                    sockChannel.write(task.getBufferArray());
                    task.setServerTime();
                    int numRead = sockChannel.read(readBuffer);
                    //System.out.println(numRead);
                    readBuffer.flip();
            task.setServerTime();
                    task.getClientChannel().write(readBuffer);

                    long time = System.nanoTime();
                    if(task.wanaLog == 1) {
                        System.out.println("Get Request");
            task.setMiddlewareTime();
                    }

                }catch(IOException e) {
                    e.printStackTrace();
                }catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public ReadQueue(InetAddress hostAddress, int port, int numThreadsPTP) {
        this.numThreads = numThreadsPTP;
        this.queue = new LinkedBlockingQueue<Load>();
        Thread[] threads = new Thread[this.numThreads];
        // System.out.println(this.numThreads);
        for(int i=0;i<this.numThreads;i++) {
            threads[i] = new Thread(new ReadQueueRunnable(hostAddress, port));
            threads[i].start();
        }
    }

    public void addReadTask(Load task) throws InterruptedException {
        queue.add(task);
    }

    public Load dispatchTask() throws InterruptedException {
        return queue.take();
    }

}