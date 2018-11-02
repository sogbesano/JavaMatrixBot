public class LoginJsonResponse {

    private String access_token;
    private String home_server;
    private String user_id;
    private String device_id;

    @Override
    public String toString() {
        return "access_token: " + this.access_token + "\n" + "home_server: " + this.home_server + "\n" + "user_id: " + this.user_id + "\n" + "device_id: " + this.device_id;
    }

    public String getAccessToken() {
        return this.access_token;
    }

    public String getHomeServer() {
        return this.home_server;
    }

    public String getUserID() {
        return this.user_id;
    }

    public String getDeviceID() {
        return this.device_id;
    }


}
