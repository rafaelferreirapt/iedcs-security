package player.security;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.KeyStore.ProtectionParameter;
import java.security.KeyStore.SecretKeyEntry;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import player.Player;
import java.io.File;
import java.io.StringWriter;
import java.security.PrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.x509.RSAPublicKeyStructure;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemWriter;
import player.api.Utils;


public class PlayerKeyStore {
    private final static String key = "WvJd8x4b3fpJPAEtFNWd6ptKUEARSpKZEyZDRVq9xJQZAvpbTpKVUhqYDJt8Q3Pxcgfb9r2eHxKQ7N7n28bt6TgUk9wzZbJVANZPWGUfYqttXwpZYetU3zYjmQXGDqET";
    private final static char[] password = key.toCharArray();
    
    // https://docs.oracle.com/javase/7/docs/api/java/security/KeyStore.html
    private static KeyStore ks;
    private static String folder_name = System.getProperty("user.home") +"/iedcs/";
    private static String filename = folder_name + "Player.KeyStore";
    private static ProtectionParameter protParam;
    
    static{
        try{
            // JCEKS allows secret keys
            // JKS keystore can only store private keys and certificates but not secret keys
            // http://docs.oracle.com/javase/7/docs/technotes/guides/security/crypto/CryptoSpec.html#KeystoreImplementation
            ks = KeyStore.getInstance("JCEKS");
            
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(filename);
            } catch (IOException e) {}
            
            try {
                ks.load(fis, password);
            } catch (IOException | CertificateException | NoSuchAlgorithmException ex) {
                Logger.getLogger(PlayerKeyStore.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            protParam =  new KeyStore.PasswordProtection(password);
        } catch (KeyStoreException ex) {
            Logger.getLogger(Player.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static void storeKey(String alias, Key key){
        try {
            // save my secret key
            SecretKeySpec sks = new SecretKeySpec(key.getEncoded(), key.getAlgorithm());
            SecretKey mySecretKey = (SecretKey)sks;
            
            SecretKeyEntry skEntry = new SecretKeyEntry(mySecretKey);
            ks.setEntry(alias, skEntry, protParam);
            
            File folder = new File(folder_name);
                Utils.println(folder_name);
            if(!folder.exists()){
                folder.mkdirs();
            }
            
            // store away the keystore
            FileOutputStream fos = null;
            try {
                fos = new java.io.FileOutputStream(filename);
                ks.store(fos, password);
            } catch (IOException | CertificateException ex) {
                Logger.getLogger(Player.class.getName()).log(Level.SEVERE, null, ex);
                System.exit(0x1);
            } catch (NoSuchAlgorithmException ex) {
                Logger.getLogger(PlayerKeyStore.class.getName()).log(Level.SEVERE, null, ex);
                System.exit(0x1);
            } finally {
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException ex) {
                        System.err.println("Can't close the file!");
                        System.exit(0x1);
                    }
                }
            }
        } catch (KeyStoreException ex) {
            Logger.getLogger(PlayerKeyStore.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(0x1);
        }
    }
    
    public static PublicKey getPublicKey(){
        try {
            // get my public key
            SecretKeyEntry skEntry = (SecretKeyEntry) ks.getEntry("publicKeyDevice", protParam);
            SecretKey myPublicKey = skEntry.getSecretKey();
            
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(myPublicKey.getEncoded());
            KeyFactory keyFactory = KeyFactory.getInstance(myPublicKey.getAlgorithm());
            PublicKey pubKey = keyFactory.generatePublic(keySpec);
            return pubKey;
        } catch (NoSuchAlgorithmException | UnrecoverableEntryException | KeyStoreException | InvalidKeySpecException ex) {
            Logger.getLogger(PlayerPublicKey.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return null;
    }
    
    public static PrivateKey getPrivateKey(){
        try {
            SecretKeySpec sks = (SecretKeySpec)ks.getKey("privateKeyDevice", password);
            PrivateKey privateKey = KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(sks.getEncoded()));
            return privateKey;
        } catch (NoSuchAlgorithmException | UnrecoverableEntryException | KeyStoreException ex) {
            Logger.getLogger(PlayerPublicKey.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvalidKeySpecException ex) {
            Logger.getLogger(PlayerKeyStore.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return null;
    }
    
    public static String getPemPubKey(){
        try {
            RSAPublicKey pub_key = (RSAPublicKey) getPublicKey();
            RSAPublicKeyStructure struct = new RSAPublicKeyStructure(pub_key.getModulus(), pub_key.getPublicExponent());
            ASN1Primitive publicKeyPKCS1ASN1 = struct.toASN1Primitive();
            byte[] publicKeyPKCS1 = publicKeyPKCS1ASN1.getEncoded();
            
            PemObject pemObject = new PemObject("RSA PUBLIC KEY", publicKeyPKCS1);
            StringWriter stringWriter = new StringWriter();
            PemWriter pemWriter = new PemWriter(stringWriter);
            pemWriter.writeObject(pemObject);
            pemWriter.close();
            String pemString = stringWriter.toString();
            
            return pemString;
        } catch (IOException ex) {
            Logger.getLogger(PlayerKeyStore.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "";
    }
    
    
    public static boolean exists(String alias){
        try {
            // get my private key
            SecretKeyEntry skEntry = (SecretKeyEntry) ks.getEntry(alias, protParam);
            SecretKey myPrivateKey = skEntry.getSecretKey();
            
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(myPrivateKey.getEncoded());
            KeyFactory keyFactory = KeyFactory.getInstance(myPrivateKey.getAlgorithm());
            PublicKey pubKey = keyFactory.generatePublic(keySpec);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }
}
