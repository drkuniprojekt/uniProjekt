/**
 * 
 */
package drkprojekt.auth;

import org.bson.types.ObjectId;
import org.joda.time.DateTime;

public class TokenInfo {
    private ObjectId userId;
    private DateTime issued;
    private DateTime expires;
    private String userRole;
    private String userName;
    
    public Boolean getAdminrole() {
       if(userRole.equals("admin")) return true;
       else return false;
    }
    public String getUserRole() {
        return userRole;
    }

    public void setUserRole(String userRole) {
        this.userRole = userRole;
    }
    public String getUserName() {
        return userName;
    }
    public void setUserName(String userName) {
        this.userName = userName;
    }
    public ObjectId getUserId() {
        return userId;
    }
    public void setUserId(ObjectId userId) {
        this.userId = userId;
    }
    public DateTime getIssued() {
        return issued;
    }
    public void setIssued(DateTime issued) {
        this.issued = issued;
    }
    public DateTime getExpires() {
        return expires;
    }
    public void setExpires(DateTime expires) {
        this.expires = expires;
    }
}