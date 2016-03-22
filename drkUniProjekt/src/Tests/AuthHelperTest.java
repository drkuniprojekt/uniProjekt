/**
 * 
 */
package Tests;

import static org.junit.Assert.*;

import org.junit.Test;

import drkprojekt.auth.AuthHelper;
import drkprojekt.auth.TokenInfo;

/**
 * @author Steffen Terheiden
 *
 */
public class AuthHelperTest {

    @Test
    public void testUser() {
	String token = AuthHelper.createJsonWebToken("User1", "Susi Sorglos", false, (long) 10000);
	System.out.println(token);
	TokenInfo info = AuthHelper.verifyToken(token);
	System.out.println(info.getUserName());
	System.out.println(info.getUserRole());
	System.out.println(info.getUserId());
	System.out.println(info.getIssued());
	assertEquals("UserName","Susi Sorglos", info.getUserName());
	assertEquals("UserRole","teamMember", info.getUserRole());
	assertEquals("UserID","User1", info.getUserId());
    }
}
