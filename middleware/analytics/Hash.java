import java.util.*;

public class Hash {
	public static void main(String[] args) {
		
		int numberOfServer = 7;

		int cnt[] = {0,0,0,0,0,0,0};


		for(int i=0; i<1000000; i++) {
			byte[] r = new byte[16];
			Random Random = new Random();
			Random.nextBytes(r);
			String s = new String(r);
			cnt[Math.abs(s.hashCode())%numberOfServer]++;
		}

		for(int i = 0; i<numberOfServer; i++) {
			System.out.println(cnt[i]);
		}
	}
}