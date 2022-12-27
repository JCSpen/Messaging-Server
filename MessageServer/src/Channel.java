import org.json.simple.JSONObject;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Channel{
    public String name;
    public ArrayList<Server.Handler> Connections;
    private List<String> MessageLog;


    public Channel(String channelName)
    {
        name = channelName;
        Connections = new ArrayList<>();
        MessageLog = new ArrayList<>();
    }

    private List<String> RetrieveLogs(){
        Request request = new Request("SERVER","GET MESSAGE LOGS",new Channel("Server"));
        List<String> logs = request.Get(false,0);
        return logs;
    }

    public void AddConnection(Server.Handler User)
    {

        Connections.add(User);
    }

    public void AddMessage(String message)
    {

        MessageLog.add(message);
    }

    public void GetMessages(){
        MessageLog = RetrieveLogs();
    }

    public List<String> FetchLogs(){
        return MessageLog;
    }

}
