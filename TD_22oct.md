# TD SAR : 22 oct

## Questions 

### First step – Static group
- How do processes constitute a group?

### Second step – TOM – Static Group – No failures
- Why does the protocol work? When a process P is about to deliver a message at the top of the queue of received messages, with all the necessary acknowledgments, why is it correct to do so? In other words, how can the process P be sure that it will not received any message in the future that would have a smaller timestamp that the one it is about to deliver?

### Third step – TOM – Process failures – No resurrection
- Assume a perfect fault detector
- Analyze the effect of process failures
- Design a solution to resist process failures while maintaining the multicast atomicity

### Fourth step – TOM – Process failures & resurrections
- Design a solution to resurrect failed processes


## Answers

### First step – Static group
In order to build a static group (size N) in which every process has a communication canal with every other one:
- A Server process is created at the very beginning with the Broker named "Server" (it won't be part of the group). It knows the number of members expected if the group (N). It launched N accept on the port 12.
- Each process is then created and launches a connect to the server (port 12). Each process finds an available port (by trying multiple ones). Once the connection is established, it sends its broker name and the available port number to the server.
- The server sends to each client its unique number in the group (from 0 to N-1) then the client list with for each client :
    - its Broker name
    - its available port
- Each client then connects to members with a bigger number (with their Broker name and port) and accepts on its available port the members with a lower member.


### Second step – TOM – Static Group – No failures
On paper then explained in class.


### Third step – TOM – Process failures – No resurrection
- Case waiting ACK from a dead : ACK is not a counter anymore but a vector (accessible by index) matching the fault-detector results. If a process dies, we don't wait for its ACK anymore.
- Case dying while sending message : some process don't receive message (but they receive ACK from others). If a process reveives ACK but not the message, it waits untils receiving the death notification of the sender (in this case, won't receive the message). In this case, ask for someone who sent the ACK to send the message.
- Case dying while sending ACK : 


