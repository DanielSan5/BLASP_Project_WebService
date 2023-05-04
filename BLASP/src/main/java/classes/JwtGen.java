package classes;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.sql.Date;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator.Builder;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;

public class JwtGen{

	public JwtGen() {
		Security.addProvider(new BouncyCastleProvider());
	}
	public String generateJwt(Map<String, String> payload) throws IllegalArgumentException, JWTCreationException, NoSuchAlgorithmException, InvalidKeySpecException, IOException {
		
		Builder tokenBuilder = JWT.create()
                .withIssuer("https://blasp.mooo.com")
                .withClaim("jti", UUID.randomUUID().toString())
                .withExpiresAt(Date.from(Instant.now().plusSeconds(86400)))
                .withIssuedAt(Date.from(Instant.now()));
		
		//inserisco i claims del jwt (ovvero le informazioni sull'utente)
        payload.entrySet().forEach(action -> tokenBuilder.withClaim(action.getKey(), action.getValue()));
       
        //generazione del token con le chiavi di rsa
        String token = tokenBuilder.sign(Algorithm.RSA256(getPublicKey(), getPrivateKey()));
        return  token;
	}
	
	
	private RSAPublicKey getPublicKey() throws NoSuchAlgorithmException, IOException, InvalidKeySpecException {
		
		//ottengo la chiave pubblica dalla variabile di ambiente PUBLIC_KEY
		String path = System.getenv("PUBLIC_KEY");
		File filePub = new File(path);
		String key = new String(Files.readAllBytes(filePub.toPath()), Charset.defaultCharset());
		
		String publicKeyPEM = key
			      .replace("-----BEGIN PUBLIC KEY-----", "")
			      .replaceAll(System.lineSeparator(), "")
			      .replace("-----END PUBLIC KEY-----", "");
		
		
	    byte[] encoded = Base64.getDecoder().decode(publicKeyPEM);

	    KeyFactory keyFactory = KeyFactory.getInstance("RSA");
	    X509EncodedKeySpec keySpec = new X509EncodedKeySpec(encoded);
	    return (RSAPublicKey) keyFactory.generatePublic(keySpec);
	}
	
	
	private RSAPrivateKey getPrivateKey() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
		
		//ottengo la chiave privata dalla variabile di ambiente PRIVATE_KEY
		String path = System.getenv("PRIVATE_KEY");
		File filePub = new File(path);
		String key = new String(Files.readAllBytes(filePub.toPath()), Charset.defaultCharset());
		
		String privateKeyPEM = key
			      .replace("-----BEGIN RSA PRIVATE KEY-----", "")
			      .replaceAll(System.lineSeparator(), "")
			      .replace("-----END RSA PRIVATE KEY-----", "");
		
		
	    byte[] encoded = Base64.getDecoder().decode(privateKeyPEM);
	    //bouncy castle provider usato per poter utilizzare la chiave in formato pkcs#1 per generare una keySpec in formato pkcs#8, senza convertire il file
	    KeyFactory keyFactory = KeyFactory.getInstance("RSA");
	    PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
	    return (RSAPrivateKey) keyFactory.generatePrivate(keySpec);
	}
	
	

}
