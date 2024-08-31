## General info
Project contains a Java-based web application that communicates with two artificial servers.

It is responsible for booking tire replacement times from the two servers. 
It also provides a way to get available time slots and filter them by their characteristics.

The two artificial servers are from "https://github.com/Surmus/tire-change-workshop".

## Usage
1. Run application binaries for backend servers:
     ```sh
     london-server.exe
     manchester-server.exe
     ```
2. The backend should be accessible from:
     Manchester tire workshop - http://localhost:9003/swagger/index.html
     London tire workshop - http://localhost:9004/swagger/index.html
3. Open tire-change-middleman as a Java Spring boot project.
4. Run TireChangeMiddlemanApplication.java
5. The application should be accessible from http://localhost:8080/interface.html
