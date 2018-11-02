import com.google.gson.JsonObject;

public class MessageJson {

    private String sender;
    private String event_id;
    private JsonObject content;

    public String getSender() {
        return this.sender;
    }

    public String getEventID() {
        return this.event_id;
    }

    public String getBody() {
        if(content.get("body") == null) {//don't crash on nick changes
            return "";
        }
        String body = content.get("body").toString();
        return body.substring(1, body.length() - 1);//remove beginning and end "
    }

    @Override
    public String toString() {
        return "sender: " + this.sender + "\n" + "event id: " + this.event_id + "\n" + "body: " + this.getBody();
    }

}
