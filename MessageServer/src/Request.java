import org.json.simple.JSONObject;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class Request {
    private String name;
    private String messageContents;
    private Channel channel;

    private String currentDirectory;

    public Request(String username,String message,Channel channel){
        name = username;
        messageContents = message;
        this.channel = channel;
    }

    public JSONObject UpdateLog(String message)
    {
        JSONObject log = new JSONObject();
        log.put("_class", "log");
        log.put("message", message);
        Backup(log,true);
        return log;
    }


    public void SendError(JSONObject toLog)
    {
        currentDirectory = System.getProperty("user.dir");
        currentDirectory = currentDirectory + "\\src\\ServerBackup.txt";
        Path fileName = Path.of(currentDirectory);
        String logText = toLog.toJSONString();
        try {
            Files.writeString(fileName, '\n'+ logText,StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void Backup(JSONObject toLog, Boolean fromServer){
        currentDirectory = System.getProperty("user.dir");
        if(fromServer) currentDirectory = currentDirectory + "\\src\\ServerBackup.txt";
        else currentDirectory = currentDirectory + "\\src\\MessageLog.txt";
        Path fileName = Path.of(currentDirectory);
        String logText = toLog.toJSONString();
        try {
            Files.writeString(fileName,logText + "\n",StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public void OpenChannel()
    {
        currentDirectory = System.getProperty("user.dir");
        currentDirectory = currentDirectory + "\\src\\ServerChannels.txt";
        Path fileName = Path.of(currentDirectory);
        String logText = channel.name;
        try {
            Files.writeString(fileName,logText + "\n",StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public JSONObject Open(boolean fromServer){
        JSONObject OpenRequest = new JSONObject();
        OpenRequest.put("_class","OpenRequest");
        OpenRequest.put("Identity",name);
        Backup(OpenRequest,fromServer);
        OpenChannel();
        return OpenRequest;

    }
    public JSONObject Publish(boolean fromServer,JSONObject PublishRequest){
        Backup(PublishRequest,fromServer);
        return PublishRequest;
    }
    public JSONObject Subscribe(boolean fromServer){
        JSONObject SubscribeRequest = new JSONObject();
        SubscribeRequest.put("_class","SubscribeRequest");
        SubscribeRequest.put("Identity",name);
        SubscribeRequest.put("Channel",channel.name);
        Backup(SubscribeRequest,fromServer);
        return SubscribeRequest;
    }
    public JSONObject Unsubscribe(boolean fromServer){
        JSONObject UnsubscribeRequest = new JSONObject();
        UnsubscribeRequest.put("_class","UnsubscribeRequest");
        UnsubscribeRequest.put("Identity",name);
        UnsubscribeRequest.put("Channel",channel.name);
        Backup(UnsubscribeRequest,fromServer);
        return UnsubscribeRequest;
    }

    public List<String> getMessages(int from,List<String>channels){
        JSONObject GetRequest = new JSONObject();
        List<String> messageLog = new ArrayList<>();
        GetRequest.put("_class","GetRequest");
        GetRequest.put("Identity",name);
        int index = 0;
        Backup(GetRequest,true);
        currentDirectory = System.getProperty("user.dir");
        currentDirectory = currentDirectory + "\\src\\MessageLog.txt";
        try (BufferedReader br = new BufferedReader(new FileReader(currentDirectory))) {
            for(String line; (line = br.readLine()) != null; ) {
                if(index>=from){
                    for (String channel:channels) {
                        if(line.contains("\"where"+"\":"+"\"" + channel + "\"")){
                            messageLog.add(line);
                        }

                    }
                }
                index++;

            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return messageLog;
    }
    public List<String> Get(boolean forServer,int from){
        JSONObject GetRequest = new JSONObject();
        List<String> messageLog = new ArrayList<>();
        GetRequest.put("_class","GetRequest");
        GetRequest.put("Identity",name);
        int index = 0;
        Backup(GetRequest,true);
        currentDirectory = System.getProperty("user.dir");
        if(forServer) currentDirectory = currentDirectory + "\\src\\ServerBackup.txt";
        else currentDirectory = currentDirectory + "\\src\\MessageLog.txt";
        try (BufferedReader br = new BufferedReader(new FileReader(currentDirectory))) {
            for(String line; (line = br.readLine()) != null; ) {
                if(index>=from){
                    messageLog.add(line);
                }
                index++;

            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return messageLog;
    }

    public List<String> getChannels()
    {
        List<String> Channels = new ArrayList<>();
        currentDirectory = System.getProperty("user.dir");
        currentDirectory = currentDirectory + "\\src\\ServerChannels.txt";
        try (BufferedReader br = new BufferedReader(new FileReader(currentDirectory))) {
            for(String line; (line = br.readLine()) != null; ) {
                Channels.add(line);
            }
    } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return Channels;
    }}
