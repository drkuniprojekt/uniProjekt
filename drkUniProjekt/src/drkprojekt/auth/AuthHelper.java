/**
 * 
 */
package drkprojekt.auth;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.SignatureException;
import java.util.Calendar;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.gson.JsonObject;

import net.oauth.jsontoken.JsonToken;
import net.oauth.jsontoken.JsonTokenParser;
import net.oauth.jsontoken.crypto.HmacSHA256Signer;
import net.oauth.jsontoken.crypto.HmacSHA256Verifier;
import net.oauth.jsontoken.crypto.SignatureAlgorithm;
import net.oauth.jsontoken.crypto.Verifier;
import net.oauth.jsontoken.discovery.VerifierProvider;
import net.oauth.jsontoken.discovery.VerifierProviders;


/**
 * Provides static methods for creating and verifying access tokens and such. 
 * @author davidm
 *
 */
public class AuthHelper {

    private static Logger log= LoggerFactory.getLogger(AuthHelper.class);
     
    private static final String AUDIENCE = "ClientForDRKMember";

    private static final String ISSUER = "DRKUniProjekt";

    private static final String SIGNING_KEY = "P-88G[y5NwafMP6BiUz4o)5/~!vNTaJm.d9eU$}x71Yw[C-7U,1<:tE0(eEa";

    /**
     * Creates a json web token which is a digitally signed token that contains a payload (e.g. userId to identify 
     * the user). The signing key is secret. That ensures that the token is authentic and has not been modified.
     * Using a jwt eliminates the need to store authentication session information in a database.
     * @param userId
     * @param durationDays
     * @return
     */
    public static String createJsonWebToken(String userId, String userName, boolean isAdmin, Long durationDays) throws SignatureException   {
        //Current time and signing algorithm
        Calendar cal = Calendar.getInstance();        
        HmacSHA256Signer signer;
        try {
            signer = new HmacSHA256Signer(ISSUER, null, SIGNING_KEY.getBytes());
        } catch (InvalidKeyException e) {
            throw new RuntimeException(e);
        }

        //Configure JSON token
        JsonToken token = new net.oauth.jsontoken.JsonToken(signer);
        token.setAudience(AUDIENCE);
        token.setIssuedAt(new org.joda.time.Instant(cal.getTimeInMillis()));
        token.setExpiration(new org.joda.time.Instant(cal.getTimeInMillis() + 1000L * 60L * 60L * 24L * durationDays));

        //Configure request object, which provides information of the item
        JsonObject request = new JsonObject();
        request.addProperty("userId", userId);
        request.addProperty("userName", userName);
        if(isAdmin) request.addProperty("userRole", "admin");
        else request.addProperty("userRole", "teamMember");
        
        JsonObject payload = token.getPayloadAsJsonObject();
        payload.add("info", request);
        
        return token.serializeAndSign();
    }

    /**
     * Verifies a json web token's validity and extracts the user id and other information from it. 
     * @param token
     * @return
     * @throws SignatureException
     * @throws InvalidKeyException
     */
    private static TokenInfo verifyToken(String token)  
    {
	log.debug("verifyToken");
        try {
            final Verifier hmacVerifier = new HmacSHA256Verifier(SIGNING_KEY.getBytes());

            VerifierProvider hmacLocator = new VerifierProvider() {

                @Override
                public List<Verifier> findVerifier(String id, String key){
                    return Lists.newArrayList(hmacVerifier);
                }
            };
            VerifierProviders locators = new VerifierProviders();
            locators.setVerifierProvider(SignatureAlgorithm.HS256, hmacLocator);
            net.oauth.jsontoken.Checker checker = new net.oauth.jsontoken.Checker(){

                @Override
                public void check(JsonObject payload) throws SignatureException {
                    // don't throw - allow anything
                }

            };
            //Ignore Audience does not mean that the Signature is ignored
            JsonTokenParser parser = new JsonTokenParser(locators,
                    checker);
            JsonToken jt;
            try {
                jt = parser.verifyAndDeserialize(token);
            } catch (SignatureException e) {
        	log.info("Signatur falsch");
                throw new RuntimeException(e);
            }
            JsonObject payload = jt.getPayloadAsJsonObject();
            TokenInfo t = new TokenInfo();
            String issuer = payload.getAsJsonPrimitive("iss").getAsString();
            String userIdString =  payload.getAsJsonObject("info").getAsJsonPrimitive("userId").getAsString();
            String userNameString =  payload.getAsJsonObject("info").getAsJsonPrimitive("userName").getAsString();
            String userRoleString =  payload.getAsJsonObject("info").getAsJsonPrimitive("userRole").getAsString();
            if (issuer.equals(ISSUER) && !StringUtils.isBlank(userIdString))
            {        	
                t.setUserId(userIdString);
                t.setUserRole(userRoleString);
                t.setUserName(userNameString);
                t.setIssued(new DateTime(payload.getAsJsonPrimitive("iat").getAsLong()));
                t.setExpires(new DateTime(payload.getAsJsonPrimitive("exp").getAsLong()));
                return t;
            }
            else
            {
                return null;
            }
        } catch (InvalidKeyException e1) {
            log.info("Invalid Key");
            throw new RuntimeException(e1);
        }
    }
    
    public static boolean isAdmin(HttpServletRequest request){
	log.debug("Proof if Admin");
	if(AuthHelper.isRegistered(request)){
	    return AuthHelper.getToken(request).isAdmin();
	}
	log.debug("Not registered");
	return false;
    }
    public static boolean isRegistered(HttpServletRequest request){
	return isRegistered(request.getHeader("Authorization"));
    }
    public static boolean isRegistered(String token){
	try{
	    //User is not registered, if his token is expired
	    log.debug("Proof of Registration");
	    log.debug("Token: " + token);
	    
	    if(AuthHelper.verifyToken(token).getExpires().getMillis()<System.currentTimeMillis()/1000){
		log.debug("Token is outdated");
		return false;	    
	    }
	}catch(RuntimeException e){
	    //User is not registered, if his token is not valid
	    log.info("No valid Token.");
	    return false;
	}
	return true;
    }
    
    public static boolean userIsRegistered(String token, String login_id){
	if(isRegistered(token) && verifyToken(token).getUserId().equals(login_id)){
	    return true;
	}
	return false;
	
    }
    public static TokenInfo getToken(HttpServletRequest request){
	return AuthHelper.verifyToken(request.getHeader("Authorization"));
    }
    
    public static void assertIsAdmin(HttpServletRequest request) throws SignatureException
    {
    	if(!isAdmin(request))
    		throw new SignatureException("Authorization failed - You are not an Admin!");
    }
}