package middleware;
import java.util.*;
import java.lang.Thread;
import java.lang.Runnable;
import java.util.concurrent.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.io.IOException;


public class WriteQueue {
    private LinkedBlockingQueue<Load> queue;
    private Selector selector;
    private int replicas;
    Load l;

    public class WriteQueueRunnable implements Runnable {
        private Selector selector;
        //public  LinkedBlockingQueue replicationQueue[] = new LinkedBlockingQueue[replicas]; 
        public Queue[] replicationQueue = new Queue[replicas];
        private InetAddress hostAddress;
        private int port;
        private int id;
        LinkedList<MemcachedNode> nodes;
        //ByteBuffer readBuffer = ByteBuffer.allocate(2048);
        HashMap<Integer, Integer> ctq = new HashMap();

        private SocketChannel[] sockChannel = new SocketChannel[replicas];

        public WriteQueueRunnable(LinkedList<MemcachedNode> nodes, InetAddress hostAddress, int port, int id) {
            this.hostAddress = hostAddress;
            this.port = port;
            this.nodes = nodes;
            this.id = id;
            // Initialize Replication Queues:
            for(int i=0; i<replicas; i++) {
                //replicationQueue[i] = new LinkedBlockingQueue<Load>();
                replicationQueue[i] = new LinkedList();
            }

            try{
                this.selector = this.initSelector(nodes);
            }catch(IOException e) {
                e.printStackTrace();
            }
        }

        private Selector initSelector(LinkedList<MemcachedNode> nodes) throws IOException {
            // Create a new selector
            Selector socketSelector = Selector.open();

            int operations = SelectionKey.OP_READ;

            int numServer = nodes.size();
            for(int i=0; i<replicas; i++) {
                ctq.put((this.id + i)%numServer, i);
                this.sockChannel[i] = SocketChannel.open(new InetSocketAddress(this.hostAddress, this.port));
                this.sockChannel[i].finishConnect();
                this.sockChannel[i].configureBlocking(false); 
                this.sockChannel[i].register(socketSelector, operations);
                System.out.println("Replication Server Number: "+(this.id + i)%numServer+" For Server: "+ this.id);
            }
            
            return socketSelector;
        }


        @Override
        public void run() {
            while (true) {
                try {
                    dispatchTask();
                    l.setQueueTime();
                    for(int i=0; i<replicas; i++) {
                        //System.out.println("### Writing into queue!!!");
                        l.setServerTime();
                        sockChannel[i].write(l.getBufferArray());
                        l.rewindBufferArray();
                        replicationQueue[i].add(l);
                    }
                    
                    // Wait for an event one of the registered channels
                    this.selector.select();

                    // Iterate over the set of keys for which events are available
                    Iterator selectedKeys = this.selector.selectedKeys().iterator();
                    while (selectedKeys.hasNext()) {
                        SelectionKey key = (SelectionKey) selectedKeys.next();
                        selectedKeys.remove();

                        if (key.isReadable()) {
                            //System.out.println("fdsfsd");
                            //System.out.println();
                            this.read(key);
                        }
                        // }else if(key.isWritable()) {
                        //     System.out.print("");
                        // }
                    }
                }catch (IOException e) {
                    e.printStackTrace();
                }catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        private void read(SelectionKey key) throws IOException {
            // try {
                SocketChannel socketChannel = (SocketChannel) key.channel();
                ByteBuffer readBuffer = ByteBuffer.allocate(2048);
                InetAddress ipaddr = socketChannel.socket().getInetAddress();
                int port = socketChannel.socket().getPort();
                int index = 0;
                for(int i=0; i<this.nodes.size();i++) {
                    MemcachedNode node = (MemcachedNode) this.nodes.get(i);
                    if(node.ipAddress == ipaddr && node.port == port) {
                        index = ctq.get(node.id);
                        break;
                    }
                }
                //System.out.println(index);
                // The buffer into which we'll read data when it's available
                int numReads = socketChannel.read(readBuffer);
                //System.out.println(numReads);
                // System.out.print(new String(readBuffer.array()));
                //System.out.println();
                if(ipaddr == this.hostAddress) {
                    Load task = (Load) replicationQueue[index].poll();
                    task.setServerTime();
                    readBuffer.flip();
                    task.getClientChannel().write(readBuffer);
                    long time = System.nanoTime();
                    if(task.wanaLog == 1) {
                        System.out.println("Set Request");
                        task.setMiddlewareTime();
                    }
                }
            // }catch(InterruptedException e) {
            //     e.printStackTrace();
            // }
        }

    }

    public WriteQueue(LinkedList<MemcachedNode> nodes, InetAddress hostAddress, int port, int replicas, int id) {
        this.replicas = replicas;
        this.queue = new LinkedBlockingQueue<Load>();
        Thread thread = new Thread(new WriteQueueRunnable(nodes, hostAddress, port, id));
        thread.start();
    }

    public void addWriteTask(Load task) throws InterruptedException{
        queue.add(task);
    }

    public void dispatchTask() throws InterruptedException{
        l = queue.take();
    }
}