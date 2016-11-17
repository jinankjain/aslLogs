package middleware;
import java.util.Map;
import java.util.TreeMap;
import java.util.SortedMap;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.math.BigInteger;
import java.io.UnsupportedEncodingException;

public class ConsistentHash {
    private static int MAX_REPLICA = 100;
    private SortedMap<BigInteger, String> entries = new TreeMap<>();

    BigInteger numOfServer = new BigInteger("3");

    // public void put(String node) throws NoSuchAlgorithmException,
    //                                        UnsupportedEncodingException{
    //     BigInteger key = hash(node);
    //     entries.put(key, node);

    //     for (int i = 0 ; i < MAX_REPLICA ; i++ ) {
    //         key = hash(node + ":" + i);
    //         entries.put(key, node);
    //     }
    // }

    // private String get(String entry) throws NoSuchAlgorithmException,
    //                                         UnsupportedEncodingException{
    //     assert !entries.isEmpty() : "Handle this case properly";

    //     BigInteger key = hash(entry);

    //     if (!entries.containsKey(key)) {
    //         SortedMap<BigInteger, String> tailMap = entries.tailMap(key);
    //         key = tailMap.isEmpty() ? entries.firstKey() : tailMap.firstKey();
    //     }

    //     return entries.get(key);
    // }

    public BigInteger hash(byte[] node) throws NoSuchAlgorithmException,
                                                UnsupportedEncodingException {
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        byte[] checksum = md5.digest(node);
        return new BigInteger(1, checksum);
    }

    public int getServer(byte[] key) throws NoSuchAlgorithmException,
                                            UnsupportedEncodingException {
        return hash(key).mod(numOfServer).intValue();
    }
}