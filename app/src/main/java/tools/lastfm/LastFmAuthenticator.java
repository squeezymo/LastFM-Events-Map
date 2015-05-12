package tools.lastfm;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class LastFmAuthenticator {
    private static final String UTF8 = "UTF8";

    private final byte[] mUsername;
    private final byte[] mPassword;
    private final byte[] mApiKey;
    private final byte[] mApiSecret;
    private final byte[] mApiSig;

    public static class Builder {
        private byte[] mUsername;
        private byte[] mPassword;
        private byte[] mApiKey;
        private byte[] mApiSecret;

        public Builder() {
        }

        public Builder setUsername(String username) {
            this.mUsername = username.getBytes(Charset.forName(UTF8));
            return this;
        }

        public Builder setUsername(byte[] username) {
            this.mUsername = username;
            return this;
        }

        public Builder setPassword(String password) {
            this.mPassword = password.getBytes(Charset.forName(UTF8));
            return this;
        }

        public Builder setPassword(byte[] password) {
            this.mPassword = password;
            return this;
        }

        public Builder setApiKey(String apiKey) {
            this.mApiKey = apiKey.getBytes(Charset.forName(UTF8));
            return this;
        }

        public Builder setApiKey(byte[] apiKey) {
            this.mApiKey = apiKey;
            return this;
        }

        public Builder setApiSecret(String apiSecret) {
            this.mApiSecret = apiSecret.getBytes(Charset.forName(UTF8));
            return this;
        }

        public Builder setApiSecret(byte[] apiSecret) {
            this.mApiSecret = apiSecret;
            return this;
        }

        public LastFmAuthenticator build() {
            if ( mUsername == null ) throw new IllegalStateException("Username must be set");
            if ( mPassword == null ) throw new IllegalStateException("Password must be set");
            if ( mApiKey == null ) throw new IllegalStateException("API Key must be set");
            if ( mApiSecret == null ) throw new IllegalStateException("Secret must be set");

            return new LastFmAuthenticator(this);
        }
    }

    private LastFmAuthenticator(Builder builder) {
        this.mUsername = builder.mUsername.clone();
        this.mPassword = builder.mPassword.clone();
        this.mApiKey = builder.mApiKey.clone();
        this.mApiSecret = builder.mApiSecret.clone();
        this.mApiSig = calcApiSig();
    }

    private byte[] calcApiSig() {
        ByteArrayOutputStream paramsStream;

        try {
            paramsStream = new ByteArrayOutputStream();
            paramsStream.write("api_key".getBytes(Charset.forName(UTF8)));
            paramsStream.write(mApiKey);
            paramsStream.write("method".getBytes());
            paramsStream.write("auth.getMobileSession".getBytes());
            paramsStream.write("password".getBytes(Charset.forName(UTF8)));
            paramsStream.write(mPassword);
            paramsStream.write("username".getBytes(Charset.forName(UTF8)));
            paramsStream.write(mUsername);
            paramsStream.write(mApiSecret);

            MessageDigest md5Digest = MessageDigest.getInstance("MD5");
            md5Digest.reset();
            md5Digest.update(paramsStream.toByteArray());
            return md5Digest.digest();
        }
        catch (IOException e) {
            throw new RuntimeException("Exception writing to byte stream");
        }
        catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("No MD5 implementation found");
        }
    }

    public String getUsername() { return new String(mUsername); }
    public String getPassword() { return new String(mPassword); }
    public String getApiKey() { return new String(mApiKey); }
    public String getSignature() { return String.format("%032x", new BigInteger(1, mApiSig)); }

}
