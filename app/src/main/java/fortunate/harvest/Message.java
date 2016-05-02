package fortunate.harvest;

/**
 * Created by Ravi on 13/05/15.
 */
public class Message {
    public  int id;
    public  String title;
    public  String url;
    public  long date_rcvd;
    public  long date_pub;
    public  String description;



    public Message() {
    }

    public Message(int id, String title, String url,long date_rcvd, long date_pub,  String description) {
        this.id = id;
        this.title = title;
        this.url = url;
        this.date_rcvd = date_rcvd;
        this.date_pub = date_pub;
        this.description = description;
    }
}
