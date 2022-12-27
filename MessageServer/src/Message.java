import org.json.simple.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class Message {
    private int timeStamp;
    private String body;
    private String channel;
    private String name;

    public Message(String messageContent,String channelName, String username){

        timeStamp = getTimestamp();
        body = messageContent;
        channel = channelName;
        name = username;
    }

    private int getTimestamp()
    {
        int num = 0;
        Request req = new Request("SERVER","RETRIEVE TIMESTAMP",new Channel("N/A"));
        List<String>messages = req.Get(false,0); //False retrieves messages, server logs are the "true" case
        for (String str: messages) {
            num++;
        }
        return num;
    }

    public Object GetMessage(){
        JSONObject newMessage = new JSONObject();
        newMessage.put("_class","Message");
        newMessage.put("from",name);
        newMessage.put("when",timeStamp);
        newMessage.put("where",channel);
        newMessage.put("body",body);
        return newMessage;
    }
}
