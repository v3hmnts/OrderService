package orderService.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Component
public class CustomKeyProvider {
    @Value("${custom.jwt.rsa256.public-key}")
    private String publicKey;

    private RSAPublicKey parsePublicKey() throws NoSuchAlgorithmException, InvalidKeySpecException {
        String publicKeyPEM = publicKey.replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");

        byte[] encoded = Base64.getDecoder().decode(publicKeyPEM);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(encoded);
        return (RSAPublicKey) keyFactory.generatePublic(keySpec);
    }

    public RSAPublicKey getPublicKey() throws NoSuchAlgorithmException, InvalidKeySpecException {
        return parsePublicKey();
    }


}
