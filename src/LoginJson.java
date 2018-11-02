
public class LoginJson {

	String type;
	String user;
	String password;
	
	public LoginJson(String type, String user, String password) {
		this.setType(type);
		this.setUser(user);
		this.setPassword(password);
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	public void setUser(String user) {
		this.user = user;
	}
	
	public void setPassword(String password) {
		this.password = password;
	}
	
}
