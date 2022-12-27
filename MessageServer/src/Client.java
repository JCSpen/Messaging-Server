import org.json.simple.JSONObject;

import java.io.*;
import java.net.Socket;

public class Client implements Runnable {

    private Socket socket;
    private BufferedReader from;
    private PrintWriter to;
    private boolean closed;

    public Channel UserChannel;

    public static void main(String[] args){
        Client client = new Client();
        client.run();
    }
    @Override
    public void run() {
        try{
            socket = new Socket("127.0.0.1",12345);
            to = new PrintWriter(socket.getOutputStream(),true);
            from = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            Handler user = new Handler();
            Thread thread = new Thread(user);
            thread.start();
            String message;
            while((message = from.readLine()) != null && !closed)
            {
                System.out.println(message);
            }
        }catch (IOException e)
        {
            Response response = new Response(true,"Server is not currently running, try again later...");
            System.out.println(response.response);
        }
    }
  public void TerminateConnection()
    {
        closed = true;
        try{
            from.close();
            to.close();
            socket.close();
        }catch (IOException e)
        {
            Response response = new Response(true,"Unable to terminate connection");
            JSONObject errorMessage = response.response;
            System.out.println(errorMessage);
        }
    }

    class Handler implements Runnable{
        private boolean quitLocked;

        public Handler(){
            quitLocked = true;
        }

        @Override
        public void run() {
            try{
                BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
                while(!closed)
                {
                    String message = reader.readLine();
                    if(message.equals("/quit")){
                        if(!quitLocked){
                            reader.close();
                            TerminateConnection();
                        }else {
                            System.out.println("This command is locked, use /disconnect first to exit the server!");
                        }
                    }
                    else{
                        if(message.equals("/disconnect"))
                        {
                            quitLocked = false;
                        }
                        to.println(message);
                    }
                }

        }catch (IOException e)
            {
                TerminateConnection();
            }
    }
}}
