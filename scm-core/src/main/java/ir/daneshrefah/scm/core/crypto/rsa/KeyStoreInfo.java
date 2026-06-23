package ir.daneshrefah.scm.core.crypto.rsa;


public interface KeyStoreInfo {

    //name of the keystore in the classpath
    String name = "config/tls/keystore-cipher.p12";
    //password used to access the key
    String password = "password";
    //name of the alias to fetch
    String alias = "selfsigned";

}
