package middleware;

import java.io.*;
import java.net.*;

public class MemcachedNode {
   public int id;
   public InetAddress ipAddress;
   public int port;

   public MemcachedNode(int id, InetAddress ipAddress, int port) {
       this.id = id;
       this.ipAddress = ipAddress;
       this.port      = port;
   }
}