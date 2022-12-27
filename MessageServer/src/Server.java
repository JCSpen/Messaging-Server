import netscape.javascript.JSObject;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.JarURLConnection;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.Checksum;

public class Server implements Runnable {

    public static void main(String[] args) {
        Server server = new Server();
        server.run();
    }




    private ArrayList<Handler> UserConnections;
    private ServerSocket socket;
    private boolean closed;

    private ArrayList<Channel> Channels;
    private ExecutorService threads;

    public Server(){
        System.out.println("SERVER QUIT UNEXPECTEDLY BEFOREHAND, RETRIEVING LOGS....");
        List<String> SeverLogs = GetServerLogs();
        for (String log:SeverLogs) {
            System.out.println(log);
        }
        closed = false;
        Channels = new ArrayList<>();
        UserConnections = new ArrayList<>();
        Request req = new Request("SERVER","Get Channels",new Channel("NONE"));
        for (String channelName: req.getChannels()) {
            Channels.add(new Channel(channelName));
        }
    }
   @Override
    public void run()
   {
       try {
           socket = new ServerSocket(12345);
           threads = Executors.newCachedThreadPool();
           while(!closed) {
           Socket client = socket.accept();
           Handler User = new Handler(client);
           UserConnections.add(User);
           threads.execute(User);
           }
       }catch (IOException e)
       {
           TerminateServer();
       }
   }

   private List<String> GetServerLogs(){
        Request request = new Request("SERVER","GET LOGS",new Channel("Server"));
        List<String> logs = request.Get(true,0);
        return logs;
   }


   public Request UpdateServerLog()
   {
       Request request = new Request("SERVER","AUTOMATIC BACKUP",new Channel("ServerChannel"));
       return request;
   }


   public void TerminateServer()
   {
       try {
           closed = true;
           if (!socket.isClosed()) {
               socket.close();
           }
           for(Handler User: UserConnections)
           {
                User.TerminateClient();
           }
       }catch (IOException e)
       {
           System.out.println("Cannot close server");
       }
   }

   public void MessageToAll(String name, String message,String channel,boolean fromServer){
       Message newMessage = new Message(message,channel,name);
       JSONObject JSONMessage = (JSONObject)newMessage.GetMessage();
       Channel PublishedOn = new Channel(channel);
       for (Channel c: Channels) {
           if(c.name == channel){
               PublishedOn = c;
           }
       }
       Request newRequest = new Request(name,message,PublishedOn);
       newRequest.Publish(fromServer,JSONMessage);
      for (Handler User : UserConnections)
      {
          for (String str: User.SubscribedChannels) {
              if(User != null && channel.equals(str))
              {
                  User.SendMessage(JSONMessage);
              }
          }
      }
   }


   class Handler implements Runnable{

       private Socket client;

       public List<String> SubscribedChannels;
       private BufferedReader from;
       private PrintWriter to;

       private String name;

       private String SelectedChannel;

       private boolean close;
        public Handler(Socket CurrentClient)
       {
           client = CurrentClient;
           closed = false;
           SubscribedChannels = new ArrayList<>();
       }
       @Override
       synchronized public void run()
       {
           try{
               closed = false;
               to = new PrintWriter(client.getOutputStream(), true);
               from = new BufferedReader(new InputStreamReader(client.getInputStream()));
               to.println("Enter Username");
               name = from.readLine();
               Response r = new Response(false,"");
               System.out.println(r.response.toJSONString());
               SelectedChannel = "Default";
               DisplayCommandBox();
               MessageToAll(name , " Has Connected" ,SelectedChannel,true);
               String message;
               while((message = from.readLine()) != null || !closed){
                   if(message.startsWith( "/name")) {
                       String[] messageSplit = message.split(" ", 2);
                       if (messageSplit.length == 2) {
                           MessageToAll(name, " has changed their name to: " + message, SelectedChannel, false);
                           Request req = UpdateServerLog();
                           JSONObject response = req.UpdateLog(name + " Changed Name To: " + message);
                           System.out.println(response);
                           name = messageSplit[1];
                           DisplaySuccessMessage(SelectedChannel);
                           to.println("Your name is now: " + name);
                       } else {
                           DisplayErrorMessage("No name inputted", SelectedChannel);
                       }
                   }else if(message.equals("/disconnect"))
                   {
                       Request req = UpdateServerLog();
                       JSONObject response = req.UpdateLog(name + " Disconnected");
                       System.out.println(response.toJSONString());
                       to.println("You have been disconnected from the server, type /quit to end the program");
                       TerminateClient();
                       MessageToAll(name, " has left the server",SelectedChannel,true);
                   }else if(message.equals("/search"))
                   {
                       to.println("Enter a value to search by");
                       String searchValue = from.readLine();
                       List<String> searchedValues = SearchbyValue(searchValue);
                       if(searchedValues.isEmpty()){
                           DisplayErrorMessage("No messages found with: " + searchValue,SelectedChannel);
                       }
                       else{
                           DisplaySuccessMessage(SelectedChannel);
                           for (String value: searchedValues) {
                               to.println(value);
                           }
                       }
                   }
                   else if(message.equals("/open"))
                   {
                       boolean ChannelExists = false;
                       to.println("Enter new channel name");
                       String input = from.readLine();
                       if(input != null && input != "")
                       {
                           for (Channel c: Channels) {
                               if(c.name.equals(input)) ChannelExists = true;
                           }
                           if(!ChannelExists){
                           Channel newChannel = new Channel(input);
                           Channels.add(newChannel);
                           Request newRequest = new Request(name,null,newChannel);
                           JSONObject obj = newRequest.Open(true);
                           System.out.println(obj.toJSONString());
                           SelectedChannel = newChannel.name;
                           MessageToAll(name,": Has created this Channel: " + newChannel.name,SelectedChannel,false);
                           to.println("You will now publish any messages to: " + SelectedChannel);
                           SubscribedChannels.add(newChannel.name);
                           DisplaySuccessMessage(SelectedChannel);
                           }else {
                               DisplayErrorMessage("This channel already exists",input);
                           }
                       }
                       else{
                           DisplayErrorMessage("No input detected",SelectedChannel);
                       }
                   }
                   else if(message.equals("/join"))
                   {
                       ArrayList<String> ChannelNames = new ArrayList<>();
                       to.println("Available Channels: ");
                       for (Channel c: Channels) {
                           ChannelNames.add(c.name);
                           to.println(c.name);
                       }
                       String input = from.readLine();
                       if(ChannelNames.contains(input))
                           {
                               for (Channel channel:Channels) {
                                   if(channel.name.equals(input)){
                                       if(!SubscribedChannels.contains(input)) {
                                           channel.AddConnection(this);
                                           Subscribe(input);
                                       }
                                       else {
                                           DisplayErrorMessage("You are already subscribed to this channel!",SelectedChannel);
                                       }
                                   }
                               }


                           }else {
                           DisplayErrorMessage("Channel does not exist: " + input,SelectedChannel);
                       }


                   }else if(message.equals("/get"))
                   {
                       to.println("From which timestamp? 0 returns all");
                       String input = from.readLine();
                       try{
                           int timeStamp = Integer.parseInt(input);
                           Request req = new Request(name,"Get Log",new Channel(SelectedChannel));
                           List<String> serverlog = req.getMessages(timeStamp,SubscribedChannels);
                           for (String field:serverlog) {
                               to.println(field);
                           }
                           Request serverReq = new Request("SERVER","UPDATE LOGS",new Channel(SelectedChannel));
                           JSONObject response = serverReq.UpdateLog(name + " Get Request");
                           System.out.println(response.toJSONString());
                           DisplaySuccessMessage(SelectedChannel);
                       }catch (Exception e){
                           DisplayErrorMessage("Non integer character detected: "+input,SelectedChannel);
                       }
                   }
                   else if(message.equals("/select"))
                   {
                       to.println("Your Subscribed Channels: ");
                       for (String str: SubscribedChannels) {
                           to.println(str);
                       }
                       String input = from.readLine();
                       if(SubscribedChannels.contains(input))
                       {
                           DisplaySuccessMessage(SelectedChannel);
                           SelectedChannel = input;
                           to.println("You will now publish any messages to: " + SelectedChannel);
                       }
                       else{
                           DisplayErrorMessage("You are not subscribed to this channel: " + input,SelectedChannel);
                       }
                   }
                   else if(message.equals("/help")){
                       DisplayCommandBox();
                   }
                   else if(message.equals("/leave"))
                   {
                       if(SubscribedChannels.contains(SelectedChannel)){
                       SubscribedChannels.remove(SelectedChannel);
                       to.println("You have unsubscribed from: " + SelectedChannel);
                       Request req = new Request(name,"Unsubscribe",new Channel(SelectedChannel));
                       JSONObject response = req.Unsubscribe(true);
                       to.println("You have not selected a channel, use /select to do so");
                       System.out.println(response.toJSONString());
                       SelectedChannel = "Default";
                       DisplaySuccessMessage(SelectedChannel);
                   }
                   }
               else{
                        if(SelectedChannel != "Default" && !message.startsWith("/")) {
                            for (Channel channel: Channels) {
                                if(channel.equals(SelectedChannel)){
                                    channel.AddMessage(message);
                                }
                            }
                            MessageToAll(name, message, SelectedChannel, false);
                        } else if (message.startsWith("/")) {
                            DisplayErrorMessage("Unrecognised command",SelectedChannel);
                            to.println("Try /help");
                        } else{
                            DisplayErrorMessage("You have not selected a channel to publish in",SelectedChannel);
                        }
                   }
               }

           }catch (IOException e)
           {
               TerminateClient();
           }
       }

       private void Subscribe(String input){
           SelectedChannel = input;
           Request req = new Request(name,"Subscribe",new Channel(SelectedChannel));
           JSONObject response = req.Subscribe(true);
           SubscribedChannels.add(SelectedChannel);
           System.out.println(response.toJSONString());
           DisplaySuccessMessage(SelectedChannel);
           DisplayOldMessages();
       }

       private List<String> SearchbyValue(String value){
            Request req = new Request(name,"Search By",new Channel(""));
            List<String> matchingValues = new ArrayList<>();
            for (String message: req.Get(false,0)) {
               if(message.contains(value)){
                   matchingValues.add(message);
               }
           }
           JSONParser parser = new JSONParser();
            JSONObject object = new JSONObject();
            List<String> returningValues = new ArrayList<>();
           for (String message:matchingValues) {
               try {
                   object = (JSONObject) parser.parse(message);
                   object.remove("_class");
                   object.remove("when");
                   if(SubscribedChannels.contains(object.get("where").toString())){
                       String output = "Published on: " + "\"" + object.get("where") + "\" " + object.get("from").toString() + ": " + object.get("body").toString();
                       returningValues.add(output);
                   }
               } catch (ParseException e) {
                   throw new RuntimeException(e);
               }
           }
           return returningValues;
       }

       public void SendMessage(JSONObject message){
           StringBuilder buildMessage = new StringBuilder();
           buildMessage.append(message.get("from"));
           buildMessage.append(": ");
           buildMessage.append(message.get("body"));
           to.println(buildMessage);
       }

       public void DisplayErrorMessage(String error,String channelName){
           Response response = new Response(true,error);
           JSONObject ErrorMessage = response.response;
           to.println(ErrorMessage.toJSONString());
           Request req = new Request(name,error,new Channel(channelName));
           req.SendError(ErrorMessage);
       }
       public void DisplaySuccessMessage(String channelName){
           Response response = new Response(false,"");
           JSONObject SuccessMessage = response.response;
           to.println(SuccessMessage.toJSONString());
           Request req = new Request(name,"Success",new Channel(channelName));
           req.SendError(SuccessMessage);
       }

       private void DisplayCommandBox()
       {
           to.println("/join to see available channels\n/open to create a new channel\n/get to retrieve messagelog\n/select to select which channel to publish on\n/leave to unsubscribe to a channel\n/name to reset your name\n/disconnect to leave the server\n/help to show commands\n/search to search for messages");
       }

       private void DisplayOldMessages()
       {
           List<String> logs;
           for (Channel channel: Channels) {
               if(channel.name.equals(SelectedChannel)){
                   channel.GetMessages();
                   logs = channel.FetchLogs();
                   if(!logs.isEmpty())
                   {
                       for (String log: logs){
                           if(!log.isEmpty())
                           {JSONParser parser = new JSONParser();
                               try {
                                   JSONObject obj = (JSONObject) parser.parse(log);
                                   String where = obj.get("where").toString();
                                   String from = obj.get("from").toString();
                                   String body = obj.get("body").toString();
                                   if(SelectedChannel.equals(where)) to.println(from + ": " + body);
                       } catch (ParseException e) {
                           throw new RuntimeException(e);
                       }}
                   }
                   }
               }
           }
       }


       synchronized public void TerminateClient()
       {
           closed = true;
           try{
               to.close();
               from.close();
               client.close();
           }catch (IOException e)
           {
               Response response = new Response(true,"Unable to terminate client");
               JSONObject errorMessage = response.response;
               System.out.println(errorMessage);
           }

       }


   }

}