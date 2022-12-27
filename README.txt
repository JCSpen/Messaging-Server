This readme file previews the system.

Persistence and chat searching extensions have been implemented.
Persistence:
- Messages and ServerLogs are backed up to seperate txt files.
- Upon loading, messages and serverlogs are retrived and outputted to the correct destinations.
- Sever logs are outputed server side, and messages are shown when a user subscribes to the channel said messages are in.
- Sever can recover gracefully when killed, no data is lost regardless of method of killing.

Chat Searching:
- User can use "/search" command to search by a specific value.
- This can be of any value in the message JSONObject, ie body, name, timestamp etc.
- The user is prompted to enter a value to search by, this value is compared to every message in the channel the user is subscribed to.
- If the value is similar to any value in the JSON encoded message object, the message is outputed in the clients terminal.
- If not, an error message is displayed accordingly.


There are three other txt files in the repository, these consist of:
- MessageLogs (This txt file stores all messages as a JSON Object)
- ServerBackup (When a protocol is used, it is immediately backed up into this file, this allows the sever to recover when killed)
- Channels (Channel names are stored in this file so that they can be used again).

There are Java Class files:
- These are all found in the initial src folder, note the src in the JarFiles is strictly for directory purposes.
- Server (Consists of server class related attributes and Handler subclass)
- Client (Consists of client class related attributes and Handler subclass)
- Channel (Consists of channel related attributes)
- Requests (As per protocol)
- Responses (Creates and returns responses from server to user, as per protocol).
- Message (Class that returns a JSON encoded message via an internal method, for the server handler, as per protocol)

HOW TO COMPILE:
**ENSURE YOUR JAVA DOWNLOAD SUPPORTS JAVA 61.0, I USED JAVA 17 FOR THIS.**

Command Line: 
Ensure jdk is up to date.
This program can be ran in command line, however it will use different txt files than if it was ran in an IDE.
I have copied the files into the correct directory for this, however note that it will differ from when ran in an IDE.
Use java -jar "FILELOCATION/MessageSever/out/artifacts/JarFiles/Server.jar" to run server, then swap Server.jar for Client.jar to run clients.

IDE:
To run in an IDE, it is advised that you use the jar files, however using the class files in the src folder will work.
Ensure the jdk is up to date.
Run the server before the client, instructions vary depending on what IDE is used.
I used IntelliJ so just select which file to run and select the green button.
As mentioned, IDE altered server files will differ from command line files, so messages most likely will differ as a result.

***IN ORDER FOR THE PROGRAM TO RUN CORRECTLY, THE SERVER MUST RUN BEFORE THE CLIENT, HOWEVER THIS DOES NOT RETURN AS AN ERROR DUE TO ADDED ROBUSTNESS***
***HOWEVER, YOU WILL FIND THAT THE SERVER FUNCTIONALITY DOES NOT EXIST IF IT IS NOT RUNNING***
