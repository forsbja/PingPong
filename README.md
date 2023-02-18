# Summary
This program takes a server and any number of clients to function. When a client is connected to a server, a coin flip is held to determine who the server will be. They then pass an empty "ball" object around to simulate the game being played. Depending on who won the coin flip, the server and client will output the corresponding "ping" or "pong" to the console. THe server is multithreaded to allow it to connect and play "games" with multiple clients at once, and will track which game it is outputting.

This currently supports running as a server, a client, or a servent.

There is no built in end to this program, and requires a kill signal to end the program on any end, killing the server will end any clients attatched.

# Compilation Instructions
This program was written in Kotlin and requires the appropriate compiler. When  ready to compile simply run: <br>
 ### make

# Run Instructions
java -jar PingPong.jar server \<port> <br>
java -jar PingPong.jar client \<host> \<port> <br>
java -jar PingPong.jar servent \<host> <client_port> <svr_port> <br>
