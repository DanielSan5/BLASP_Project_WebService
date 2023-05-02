package classes;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.sql.Date;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator.Builder;
import com.auth0.jwt.algorithms.Algorithm;

public class JwtGen{

	
	private KeyPairGenerator keyPairGenerator;
	private KeyPair keyPair;
	
	public JwtGen() throws NoSuchAlgorithmException {
		
		this.keyPairGenerator =  KeyPairGenerator.getInstance("RSA");
		keyPairGenerator.initialize(2048);
		this.keyPair = keyPairGenerator.generateKeyPair();
	}
	
	public String generateJwt(Map<String, String> payload) {
		
		Builder tokenBuilder = JWT.create()
                .withIssuer("https://keycloak.quadmeup.com/auth/realms/Realm")
                .withClaim("jti", UUID.randomUUID().toString())
                .withExpiresAt(Date.from(Instant.now().plusSeconds(86400)))
                .withIssuedAt(Date.from(Instant.now()));
		
		//inserisco i claims del jwt (ovvero le informazioni sull'utente)
        payload.entrySet().forEach(action -> tokenBuilder.withClaim(action.getKey(), action.getValue()));
       
        //generazione del token con le chiavi di rsa
        return  tokenBuilder.sign(Algorithm.RSA256(((RSAPublicKey) keyPair.getPublic()), ((RSAPrivateKey) keyPair.getPrivate())));
	}
	
	

}
