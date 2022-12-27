This zip file contains all the files relevant to this project.

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


There are three other txt files in the zip, these consist of:
- MessageLogs (This txt file stores all messages as a JSON Object)
- ServerBackup (When a protocol is used, it is immediately backed up into this file, this allows the sever to recover when killed)
- Channels (Channel names are stored in this file so that they can be used again).

There are Java Class files:
- Server (Consists of server class related attributes and Handler subclass)
- Client (Consists of client class related attributes and Handler subclass)
- Channel (Consists of channel related attributes)
- Requests (As per protocol)
- Responses (Creates and returns responses from server to user, as per protocol).
- Message (Class that returns a JSON encoded message via an internal method, for the server handler, as per protocol)

To compile the server, run the server class via the standard method in an IDE (I Used intelliJ).

To compile the client, run one or multiple instances via an IDE (I Used intelliJ).

***IN ORDER FOR THE PROGRAM TO RUN CORRECTLY, THE SERVER MUST RUN BEFORE THE CLIENT, HOWEVER THIS DOES NOT RETURN AS AN ERROR DUE TO ADDED ROBUSTNESS***
***HOWEVER, YOU WILL FIND THAT THE SERVER FUNCTIONALITY DOES NOT EXIST IF IT IS NOT RUNNING***
***I ALSO FOUND THAT THE PROGRAM DOES NOT RESPOND WELL TO COMMAND LINE EXECUTION, SO I ADVISE THAT THIS IS AVOIDED***