package fortunate.harvest;

/**
 * Created by Ravi on 13/05/15.
 */
public class Message {
    public  int id;
    public  String title;
    public  String url;
    public  String date_rcvd;
    public  String date_pub;
    public  String description;
    public   int read;



    public Message() {
    }

    public Message(int id, String title, String url,String date_rcvd, String date_pub,  String description, int read) {
        this.id = id;
        this.title = title;
        this.url = url;
        this.date_rcvd = date_rcvd;
        this.date_pub = date_pub;
        this.description = description;
        this.read = read;
    }

}
