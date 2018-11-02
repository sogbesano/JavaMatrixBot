public class SendMessageJson {

    private String msgtype;
    private String body;

    public SendMessageJson(String msgtype, String body) {
        this.setMsgtype(msgtype);
        this.setBody(body);
    }

    private void setMsgtype(String msgtype) {
        this.msgtype = msgtype;
    }

    private void setBody(String body) {
        this.body = body;
    }

    public String getMsgType() {
        return this.msgtype;
    }

    public String getBody() {
        return this.body;
    }
}
