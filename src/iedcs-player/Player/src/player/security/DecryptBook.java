package player.security;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ProtocolException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.SignatureException;
import java.util.Base64;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;
import player.IEDCSPlayer;
import player.api.BookContent;
import player.api.Requests;
import player.api.Result;
import player.api.Utils;
import java.security.PublicKey;

public class DecryptBook {
    
    private byte[] r2;
    private String book_signed;
    private String book_identifier;
    private byte[] book_ciphered_bytes;
    private byte[] iv_array;
    private static final int max_iterations = 10;
    private long last_byte = 0;
    private long[] last_skyp_bytes = {0};
    private long last_page = 1;
    public String title;
    public boolean isFinal = false;
    private static final long number_of_blocks_per_page = 50;
    final protected static char[] hexArray = "0123456789abcdef".toCharArray();
    
    public DecryptBook(String identifier) throws BookRestricted, SecurityException{
        Result rs = Requests.getBook(identifier);
        BookContent bc = (BookContent) rs.getResult();
        
        for(Header h : bc.getHeaders()){
            switch (h.getName()) {
                case "r2":
                    byte[] f = Base64.getDecoder().decode(h.getValue());
                    byte[] tmp1 = new byte[32];
                    System.arraycopy(f, 0, tmp1, 0, 32);
                    this.iv_array = tmp1;
                    byte[] tmp2 = new byte[f.length-32];
                    System.arraycopy(f, 32, tmp2, 0, f.length-32);
                    this.r2 = tmp2;
                    break;
                case "book_signed":
                    this.book_signed = h.getValue();
                    break;
                case "identifier":
                    this.book_identifier = h.getValue();
                    break;
                case "name":
                    this.title = h.getValue();
                    break;
                case "restriction":
                    throw new BookRestricted(h.getValue());
            }
        }
        
        if(!verify_book_content(bc.getContent().getBytes())){
            throw new SecurityException("Something went wrong, please contact the admin.");
        }
        this.book_ciphered_bytes = bc.getContent().getBytes();
    }
    
    public boolean verify_book_content(byte[] book_content_ciphered){
        try {
            Signature signature1 = Signature.getInstance("SHA256withRSA");
            PublicKey pk = PlayerPublicKey.getKey();
            signature1.initVerify(pk);
            signature1.update(book_content_ciphered);
            boolean result = signature1.verify(Base64.getDecoder().decode(this.book_signed));
            return result;
        } catch (NoSuchAlgorithmException | SignatureException ex) {
            Logger.getLogger(DecryptBook.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvalidKeyException ex) {
            Logger.getLogger(DecryptBook.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }
    
    public String getContent(long page){
        try {
            byte[] r2_encripted = this.r2;
            // k1 = PBKDF2(user.username + "jnpc" + book.identifier, book.identifier).read(32)
            PBKDF2 k1 = new PBKDF2(Requests.getUser().getString("username") + "jnpc" + this.book_identifier,this.book_identifier);
            byte[] key = k1.read(32);
            
            byte[] rd2_bytes = decryptAES(r2_encripted, key);
            String rd2 = new String(rd2_bytes);
            
            int number = 0;
            
            for(char c : rd2.toCharArray()){
                if(Character.isDigit(c)){
                    number += Integer.parseInt(String.valueOf(c));
                }
            }
            
            int n = number % max_iterations;
            
            byte[] rd1 = rd1_process(rd2_bytes, rd2_bytes);
            
            HashMap<String, String> parameters = new HashMap<String, String>();
            parameters.put("book_identifier", this.book_identifier);
            parameters.put("rd1", new String(rd1));
            parameters.put("device_identifier", ComputerDetails.getUniqueIdentifier());
            Result r = Requests.postJSON(Requests.SECURITY_EXCHANGE_R1_R2, parameters);
            
            JSONObject jso = (JSONObject) r.getResult();
            String rd2_server = jso.getString("rd2");
            
            byte[] rd3 = rd3_process(rd2_server.getBytes(), n, rd2_bytes);
            PBKDF2 pbk = new PBKDF2(new String(rd3), new String(rd2_bytes), Requests.fin);
            byte[] filekey = pbk.read(32);
            
            byte[] contentText = decryptStreamAES(filekey, page);
            
            String content = new String(contentText);
            return content;
        } catch (JSONException ex) {
            Logger.getLogger(DecryptBook.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ProtocolException ex) {
            Logger.getLogger(DecryptBook.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(DecryptBook.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "";
    }
    
    private byte[] rd1_process(byte[] rd, byte[] random2){
        try {
            MessageDigest messageDigest224 = MessageDigest.getInstance( "SHA-224");
            messageDigest224.reset();
            messageDigest224.update((Requests.getUser().getString("username") + "fcp" + this.book_identifier).getBytes());
            byte[] sha_tmp = bytesToHex(messageDigest224.digest()).getBytes();
            PBKDF2 pbk = new PBKDF2(new String(sha_tmp), new String(random2));
            byte[] k1 = pbk.read(32);
            
            byte[] iv1 = new byte[16];
            System.arraycopy(this.iv_array, 0, iv1, 0, 16);
            
            byte[] rd1 = encryptAES(rd, k1, iv1);
            byte[] final_rd1 = new byte[iv1.length + rd1.length];
            System.arraycopy(iv1, 0, final_rd1, 0, iv1.length);
            System.arraycopy(rd1, 0, final_rd1, iv1.length, rd1.length);
            
            return Base64.getEncoder().encode(final_rd1);
        } catch (NoSuchAlgorithmException | JSONException ex) {
            Logger.getLogger(DecryptBook.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    private byte[] rd3_process(byte[] rd2, int n, byte[] random2){
        try {
            MessageDigest messageDigest224 = MessageDigest.getInstance( "SHA-224");
            messageDigest224.reset();
            messageDigest224.update((Integer.toString(n) + "ua" + this.book_identifier).getBytes());
            String dsgs = Integer.toString(n) + "ua" + this.book_identifier;
            byte[] sha_tmp = bytesToHex(messageDigest224.digest()).getBytes();
            PBKDF2 pbk = new PBKDF2(new String(sha_tmp), new String(random2));
            byte[] k3 = pbk.read(32);
            
            String rd2_str = new String(rd2);
            String random2_rd3 = new String(random2);
            String sha = new String(sha_tmp);
            String teste_k3 = new String(Base64.getEncoder().encode(k3));
            
            byte[] iv3 = new byte[16];
            System.arraycopy(this.iv_array, 16, iv3, 0, 16);
            
            String teste_iv3 = new String(Base64.getEncoder().encode(iv3));
            
            byte[] rd3 = encryptAES(rd2, k3, iv3);
            String d = new String(rd3);
            
            byte[] final_rd3 = new byte[iv3.length + rd3.length];
            System.arraycopy(iv3, 0, final_rd3, 0, iv3.length);
            System.arraycopy(rd3, 0, final_rd3, iv3.length, rd3.length);
            
            return Base64.getEncoder().encode(final_rd3);
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(DecryptBook.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    private static String bytesToHex(byte[] bytes) {
        // http://stackoverflow.com/questions/9655181/how-to-convert-a-byte-array-to-a-hex-string-in-java
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
    
    private static byte[] encryptAES(byte[] clearText, byte[] key, byte[] iv){
        try {
            Cipher cipher_aes = Cipher.getInstance("AES/CBC/PKCS5Padding");
            
            IvParameterSpec iv_spec = new IvParameterSpec(iv);
            SecretKeySpec sks = new SecretKeySpec(key, "AES");
            
            // Instantiate the cipher
            cipher_aes.init(Cipher.ENCRYPT_MODE, sks, iv_spec);
            Utils.println(cipher_aes.getBlockSize());
            
            return cipher_aes.doFinal(clearText);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException ex) {
            Logger.getLogger(DecryptBook.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    private static byte[] decryptAES(byte[] cipheredText, byte[] key){
        try {
            Cipher cipher_aes = Cipher.getInstance("AES/CBC/PKCS5Padding");
            
            int bs = 16;
            byte[] iv = new byte[bs];
            System.arraycopy(cipheredText, 0, iv, 0, bs);
            IvParameterSpec iv_spec = new IvParameterSpec(iv);
            SecretKeySpec sks = new SecretKeySpec(key, "AES");
            
            byte[] tmp_block = new byte[cipheredText.length-bs];
            System.arraycopy(cipheredText, bs, tmp_block, 0, cipheredText.length-bs);
            
            // Instantiate the cipher
            cipher_aes.init(Cipher.DECRYPT_MODE, sks, iv_spec);
            Utils.println(cipher_aes.getBlockSize());
            
            return cipher_aes.doFinal(tmp_block);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException ex) {
            Logger.getLogger(DecryptBook.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    private byte[] decryptStreamAES(byte[] key, long page){
        try {
            InputStream book_ciphered = Base64.getDecoder().wrap(new ByteArrayInputStream(this.book_ciphered_bytes));
            Cipher cipher_aes = Cipher.getInstance("AES/CBC/PKCS5Padding");
            
            int bs = 16;
            long skyp_bytes;
             
            skyp_bytes = this.last_skyp_bytes[(int) page-1];
            if(skyp_bytes>=bs){
                book_ciphered.skip(skyp_bytes-(page-1)*bs);
            }
            byte[] iv = new byte[bs];
            long teste = book_ciphered.read(iv);
            IvParameterSpec iv_spec = new IvParameterSpec(iv);
            SecretKeySpec sks = new SecretKeySpec(key, "AES");
            
            // Instantiate the cipher
            cipher_aes.init(Cipher.DECRYPT_MODE, sks, iv_spec);
            
            long bytesRead = 0;
            
            byte[] clearText_block;
            String clearText = "";
            
            int counter_linhas = 0;
            
            while (book_ciphered.available() > 0){
              byte[] dataBlock = new byte[bs];
              long read_bytes = book_ciphered.read(dataBlock);
              clearText_block = cipher_aes.update(dataBlock);
              String clearText_str = new String(clearText_block);
              
              String[] number_of_barran = (clearText + clearText_str).split(System.getProperty("line.separator"));
              
              counter_linhas = number_of_barran.length - 1;
              
              if(counter_linhas >= 31 || book_ciphered.available() == 0){ 
                String[] linha = clearText_str.split(System.getProperty("line.separator"));
                    
                if(linha.length>1){
                    clearText += linha[0];
                    
                    this.last_byte = this.last_byte + bytesRead;
                    if(this.last_skyp_bytes.length == page){
                        long[] toAppend = {this.last_byte};
                        long[] tmp = new long[this.last_skyp_bytes.length + toAppend.length];
                        System.arraycopy(this.last_skyp_bytes, 0, tmp, 0, this.last_skyp_bytes.length);
                        System.arraycopy(toAppend, 0, tmp, this.last_skyp_bytes.length, toAppend.length);

                        this.last_skyp_bytes = tmp; 
                    }         
                    break;
                }else{
                    clearText += clearText_str;
                    bytesRead += read_bytes;
                }
              }else{
                if(bytesRead==bs){
                    String[] linha = clearText_str.split(System.getProperty("line.separator"));
                    clearText += clearText_str.replace(linha[0]+System.getProperty("line.separator"), "");
                    bytesRead += read_bytes;
                }else{
                    clearText += clearText_str;
                    bytesRead += read_bytes;
                }
              }
            }
            
            if(book_ciphered.available() == 0){
                clearText_block = cipher_aes.doFinal();
                clearText += new String(clearText_block);
                this.isFinal = true;
            }else{
                this.isFinal = false;
            }
            
            return clearText.getBytes();
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException | IOException ex) {
            Logger.getLogger(DecryptBook.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
}

