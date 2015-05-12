package tools;

import com.loopj.android.http.Base64;
import java.nio.charset.Charset;

public class Obfuscator {
    
    public static String encode(String in) {
        return new String(Base64.encode(in.getBytes(Charset.forName("UTF8")), Base64.DEFAULT));
    }
    
    public static String decode(String in) {
        return new String(Base64.decode(in.getBytes(Charset.forName("UTF8")), Base64.DEFAULT));
    }
}
