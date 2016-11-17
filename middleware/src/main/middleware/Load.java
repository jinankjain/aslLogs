package middleware;

import java.nio.channels.*;
import java.nio.ByteBuffer;

public class Load {
	private SocketChannel clientChannel;
	private ByteBuffer buff;
	private String key;
	public int wanaLog;

	public long timeMiddleware;
	public long timeQueue;
	public long timeServer;
	public long timeHash;


	public Load(SocketChannel clientChannel, ByteBuffer buff, long insertionTime, int wanaLog) {
		this.clientChannel = clientChannel;
		this.timeMiddleware = insertionTime;
		this.timeQueue = 0;
		this.timeServer = 0;
		this.wanaLog = wanaLog;
		this.buff = buff;
	}

	public SocketChannel getClientChannel() {
		return this.clientChannel;
	}

	public ByteBuffer getBuffer() {
		//this.buff.flip();
		return this.buff;
	}

	public ByteBuffer getBufferArray() {
		this.buff.flip();
		return this.buff;
	}

	public void rewindBufferArray() {
		this.buff.flip();
		this.buff.rewind();
	}

	public void clearBuffer() {
		this.buff.compact();
		this.buff.flip();
	}

	public void buffClear() {
		this.buff.clear();
	}

	public void setKey(String key) {
		this.key = key;
	}

	public void setQueueTime() {
		this.timeQueue = System.nanoTime()-this.timeQueue;
	}

	public void setHashTime(long time) {
		this.timeHash = time;
	}

	public void setServerTime() {
		this.timeServer = System.nanoTime()-this.timeServer;
	}

	public void setMiddlewareTime() {
	this.timeMiddleware = System.nanoTime() - this.timeMiddleware;
        if(this.wanaLog == 1) {
            System.out.println("Time Middleware: "+this.timeMiddleware);
            System.out.println("Time Queue: "+this.timeQueue);
            System.out.println("Time Server: "+this.timeServer);
            System.out.println();
        }	
	}

	public String getKey() {
		return this.key;
	}

}