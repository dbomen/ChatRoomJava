package Server;

import java.io.*;
import java.net.*;
import java.util.*;

import com.google.gson.Gson;

import GsonTypes.Message;
import GsonTypes.Request;
import GsonTypes.Response;

import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;

public class ChatServer {

    protected static List<String> illegalNames = List.of("PUBLIC", ".gitkeep");
	protected int serverPort = 1234;
	protected List<Socket> clients = new ArrayList<Socket>(); // list of clients
	protected List<String> clientNames = new ArrayList<String>(); // list of client names
	protected String projectPath = "L:/.zrelo obdobje/Programiranje/Projects/ChatRoomJava/src/Server/db";


	public static void main(String[] args) throws Exception {
		new ChatServer();
	}

	public ChatServer() {
		ServerSocket serverSocket = null;

		// create socket
		try {
			serverSocket = new ServerSocket(this.serverPort); // create the ServerSocket
		} catch (Exception e) {
			System.err.println("[system] could not create socket on port " + this.serverPort);
			e.printStackTrace(System.err);
			System.exit(1);
		}

		// start listening for new connections
		System.out.println("[system] listening ...");
		try {
			while (true) {
				Socket newClientSocket = serverSocket.accept(); // wait for a new client connection
				synchronized(this) {
					clients.add(newClientSocket); // add client to the list of clients
				}
				ChatServerConnector conn = new ChatServerConnector(this, newClientSocket); // create a new thread for communication with the new client
				conn.start(); // run the new thread
			}
		} catch (Exception e) {
			System.err.println("[error] Accept failed.");
			e.printStackTrace(System.err);
			System.exit(1);
		}

		// close socket
		System.out.println("[system] closing server socket ...");
		try {
			serverSocket.close();
		} catch (IOException e) {
			e.printStackTrace(System.err);
			System.exit(1);
		}
	}

	// send a message to all clients connected to the server
	public void sendToAllClients(String message, Socket senderSocket) throws Exception {
		Iterator<Socket> i = clients.iterator();
		while (i.hasNext()) { // iterate through the client list
			Socket socket = (Socket) i.next(); // get the socket for communicating with this client
            
            if (senderSocket != null && senderSocket.equals(socket))  continue; // we dont send the message to the client that is sending it

			try {
				DataOutputStream out = new DataOutputStream(socket.getOutputStream()); // create output stream for sending messages to the client
				out.writeUTF(message); // send message to the client
                out.flush();
			} catch (Exception e) {
				System.err.println("[system] could not send message to a client");
				e.printStackTrace(System.err);
			}
		}
	}

	// ko posiljamo clientu x oz. ce je prislo do napake pri privatnem posiljanju
	public void sendToClientX(String message, Socket x) throws Exception {
		try {
			DataOutputStream out = new DataOutputStream(x.getOutputStream()); // create output stream for sending messages to the client
			out.writeUTF(message); // send message to the client
		} catch (Exception e) {
			System.err.println("[system] could not send message to a client");
			e.printStackTrace(System.err);
		}
	}

	public void removeClient(Socket socket, boolean removeMessage) { // v konsoli se izpise koga smo izpisali <ime><port> ter tudi lists size before and after
											  // zato, ker imamo dva lista in s tem preverimo, da ni napake pri sinhronizaciji obeh listov
		synchronized(this) {
            String removingClientName = clientNames.get(clients.indexOf(socket));

			System.out.println("--------------------------------------------");
			System.out.printf("[SYSTEM] REMOVING CLIENT: <%s><%s>\n", clientNames.get(clients.indexOf(socket)), socket.getPort());
			System.out.printf("[SYSTEM] OLD LISTS SIZE: <%s><%s>\n", clientNames.size(), clients.size());
			removeClientsName(socket);
			clients.remove(socket);
			System.out.printf("[SYSTEM] NEW LISTS SIZE: <%s><%s>\n[SYSTEM] REMOVED\n", clientNames.size(), clients.size());
			System.out.println("--------------------------------------------");
            
            // poslje vsem, da je client leaval
            try {
                if (removeMessage && clientNames.size() != 0 && clients.size() != 0)  this.sendToAllClients(new Message().createJson(2, "SYSTEM", null, null, String.format("<%s> has left the chat", removingClientName)), null);
            } catch (Exception e) {
                System.out.println("PROBLEM WITH SENDING TO ALL CLIENTS!");
                e.printStackTrace();
            }
		}
	}

	// returns index, where clients name will be
	public int makeRoomForClientsName() {
		this.clientNames.add(" ");
		return this.clientNames.size() - 1;
	}

	public void addClientsName(String name, int index) {
		this.clientNames.set(index, name);
	}

	public void removeClientsName(Socket socket) {
		int index = this.clients.indexOf(socket);
		this.clientNames.remove(index);
	}

	public void writeNewUser(String name, String password) {
		
		String newUserPath = String.format("%s/users/@%s", this.projectPath, name);
		
		// create necesery folders / files
		this.createNewFolder(newUserPath);
		this.createNewFolder(newUserPath + "/OfflineMessages");
        this.createNewFile(newUserPath + "/OfflineMessages/.gitkeep");

        this.createNewFolder(newUserPath + "/Social");
        this.createNewFile(newUserPath + "/Social/Friends.txt");
        this.createNewFile(newUserPath + "/Social/FriendRequests.txt");

		this.createNewFile(newUserPath + "/Password.txt");

        this.createNewFile(newUserPath + "/SystemLogin.txt");

		// add password, uporabimo lahko kar this.writeToChat metodo, saj isto naredi
		this.writeToChat(newUserPath + "/Password.txt", password);
	}

	public boolean findUser(String name, String password) {

		String path = String.format("%s/users/@%s", this.projectPath, name);
		if (this.fileExsists(path)) {

			String knownPassword = null;
			try {

				BufferedReader reader = new BufferedReader(new FileReader(path + "/Password.txt"));
				knownPassword = reader.readLine();
				reader.close();
			} catch (IOException e) {
				System.out.println("PROBLEM WITH FINDING USER");
				e.printStackTrace();
			}

			if (knownPassword.equals(password))  return true;
		}
		return false;
	}

	public boolean CheckNameAvailibilty(String name) {
		
		return !(this.fileExsists(String.format("%s/users/@%s", this.projectPath, name)));
	}

	public boolean checkRightAscii(String text) {

		for (int i = 0; i < text.length(); i++) {
			if (text.charAt(i) < 33 || text.charAt(i) > 126)  return false;
		}
		return true;
	}

	public boolean isOnline(String user) {

		for (String name : this.clientNames) {
			if (name.equals(user))  return true;
		}
		return false;
	}

	public boolean fileExsists(String fileName) {
		try {
			File tmpF = new File(fileName);
			return tmpF.exists();
		} catch (Exception e) {
			System.out.println("PROBLEM WITH FILE EXSIST");
			e.printStackTrace();
			return false;
		}
	}

	public String realFileName(String fileName1, String fileName2) {
		return (this.fileExsists(fileName1)) ? fileName1 : fileName2;
	}

	// -----
	// SAME FUNCTION, BUT DIFFERENT INPUT
	// if you only have 2 names
	public ArrayList<String> getHistory(String user1, String user2) {
		String fileName1 = String.format("%s/histories/H%s%s.txt", this.projectPath, user1, user2);
		String fileName2 = String.format("%s/histories/H%s%s.txt", this.projectPath, user2, user1);
		if (!this.fileExsists(fileName1) && !this.fileExsists(fileName2))  return null;

		return getHistory(realFileName(fileName1, fileName2)); // the real file name
	}

	// if you already have the path
	public ArrayList<String> getHistory(String fileName) {

        ArrayList<String> historyList = new ArrayList<>();

		try {
			BufferedReader reader = new BufferedReader(new FileReader(fileName));
			String line;
			while ((line = reader.readLine()) != null) {
				historyList.add(line);
			}
			reader.close();
		} catch (IOException e) {
			System.out.println("PROBLEM WITH READER (getHistory)");
			e.printStackTrace();
			return null;
		}	
		return historyList;
	}
	// -----

	public void createNewFolder(String path) {

		new File(path).mkdirs();
	}

	public void createNewFile(String path) {

		try {
			File newChat = new File(path);
			newChat.createNewFile();
		} catch (IOException e) {
			System.out.println("PROBLEM WITH CREATING NEW FILE");
			e.printStackTrace();
		}
	}

	public void removeFile(String path) {

		new File(path).delete();
	}

	public void writeToChat(String chatName, String msg) {

		if (!this.fileExsists(chatName))  return;

		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(chatName, true));
			writer.write(msg + "\n");
			writer.close();
		} catch (IOException e) {
			System.out.println("PROBLEM WITH WRITER (write to chat)");
			e.printStackTrace();
		}
	}

    public void writeToFileAndOverride(String fileName, String line) {

        if (!this.fileExsists(fileName))  return;

		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, false));
			writer.write(line);
			writer.close();
		} catch (IOException e) {
			System.out.println("PROBLEM WITH WRITER (write to chat and override)");
			e.printStackTrace();
		}
    }

	public String getChatName(String user1, String user2) {
		String chatName1 = String.format("%s/histories/H%s%s.txt", this.projectPath, user1, user2);
		String chatName2 = String.format("%s/histories/H%s%s.txt", this.projectPath, user2, user1);
		if (!this.fileExsists(chatName1) && !this.fileExsists(chatName2))  this.createNewFile(chatName1); // if chat does not exsist than we make a new one

		String chatName = this.realFileName(chatName1, chatName2);

		// pogledamo, ce je history == 50, ce je deletamo prvi line
		if (this.historyTooLong(chatName))  this.removeFirstLine(chatName);

		return chatName;
	}

	public int numberOfLines(String fileName) {

		int lines = 0;
		try {
			BufferedReader reader = new BufferedReader(new FileReader(fileName));
			while (reader.readLine() != null)  lines++;
			reader.close();
		} catch (IOException e) {
			System.out.println("PROBLEM WITH READER (historyTooLong)");
			e.printStackTrace();
		}
		return lines;
	}

	// if history == 50 lines return true
	public boolean historyTooLong(String chatName) {

		return (this.numberOfLines(chatName) == 50);
	}

	// it is not so bad that we loop thru the whole file, because it is only max. 50 lines
	public void removeFirstLine(String chatName) {

		StringBuilder history_new = new StringBuilder();

		try {
			BufferedReader reader = new BufferedReader(new FileReader(chatName));
			boolean firstLine = true;
			String line;
			while ((line = reader.readLine()) != null) {
				if (firstLine)  firstLine = false;
				else {
					history_new.append(line + "\n");
				}
			}
			reader.close();
		} catch (IOException e) {
			System.out.println("PROBLEM WITH READER (removeFirstLine)");
			e.printStackTrace();
		}

		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(chatName, false));
			writer.write(history_new.toString());
			writer.close();
		} catch (IOException e) {
			System.out.println("PROBLEM WITH WRITER (removeFirstLine)");
			e.printStackTrace();
		}	
	}

	public void addToOfflineMessages(String chat, String user, String msg) {

		String strippedChatName = chat.substring(chat.indexOf('H'));
		strippedChatName = strippedChatName.substring(0, strippedChatName.indexOf('.'));

		String fileName = String.format("%s/users/@%s/OfflineMessages/%sCNT.txt", this.projectPath, user, strippedChatName);

		// ce file ne obstaja ga naredi
		if (!this.fileExsists(fileName))  this.createNewFile(fileName);

		int cnt = 1;
		if (this.numberOfLines(fileName) == 0) { // ce je file prazen, pustimo cnt na 1
		}
		else { // sicer preberemo cnt in ga povecamo za 1

			// preberemo cnt
			try {

				BufferedReader reader = new BufferedReader(new FileReader(fileName));
				String line = reader.readLine();
				cnt = Integer.valueOf(line.substring(line.length() - 1)) + 1;
				reader.close();
			} catch (IOException e) {
				System.out.println("PROBLEM WITH ADDING OFFLINE MESSAGE, reader");
				e.printStackTrace();
			}
		}

		// napisemo novo
		try {

			BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, false));
			writer.write(String.format("%s.txt, %d", strippedChatName, cnt));
			writer.close();
		} catch (IOException e) {
			System.out.println("PROBLEM WITH ADDING OFFLINE MESSAGE, writer");
			e.printStackTrace();
		}
	}

	// dobimo niz, dveh imen, vrne ime, ki se ne matcha
	public String findNonMatching(String niz, String podniz) {

		int startIx = niz.indexOf(podniz);
		int endIx = startIx + podniz.length();

		if (startIx == 0)  return niz.substring(endIx);
		else 			   return niz.substring(0, startIx);
	}

    // creates a new friendship
    @SuppressWarnings("unchecked")
    public void addFriendship(String user1, String user2, String date) { // happens when you Accept a FriendRequest

        // we remove the friend request
        removeFriendRequest(user2, user1); // removes user2's friend request from user1's friendRequest file

        // we get the current Map that holds users friends
		String fileName1 = String.format("%s/users/@%s/Social/Friends.txt", this.projectPath, user1);
        String fileName2 = String.format("%s/users/@%s/Social/Friends.txt", this.projectPath, user2);
        
        Gson gson = new Gson();
        Map<String, String> map1 = new HashMap<>();
        Map<String, String> map2 = new HashMap<>();

        // reads the Map (JSON) from file
        if (numberOfLines(fileName1) > 0) { // if it is empty (aka no friends, we make a new Map)

            try { // user1

                BufferedReader reader = new BufferedReader(new FileReader(fileName1));
                String line = reader.readLine();

                map1 = gson.fromJson(line, HashMap.class);

                reader.close();
            } catch (IOException e) {
                System.out.println("PROBLEM WITH ADDING FRIENDSHIP, user1");
                e.printStackTrace();
            }
        }

        if (numberOfLines(fileName2) > 0) { // if it is empty (aka no friends, we make a new Map)

            try { // user2

                BufferedReader reader = new BufferedReader(new FileReader(fileName2));
                String line = reader.readLine();

                map2 = gson.fromJson(line, HashMap.class);

                reader.close();
            } catch (IOException e) {
                System.out.println("PROBLEM WITH ADDING FRIENDSHIP, user2");
                e.printStackTrace();
            }
        }

        // we put the new friendship in the new Map
        map1.put(user2, date);
        map2.put(user1, date);

        // we put the map back into the DB
        String json1 = gson.toJson(map1);
        String json2 = gson.toJson(map2);

        writeToFileAndOverride(fileName1, json1);
        writeToFileAndOverride(fileName2, json2);
    }

    // removes friendship
    @SuppressWarnings("unchecked")
    public void removeFriendship(String user1, String user2) {

        // we get the current Map that holds users friends
        String fileName1 = String.format("%s/users/@%s/Social/Friends.txt", this.projectPath, user1);
        String fileName2 = String.format("%s/users/@%s/Social/Friends.txt", this.projectPath, user2);
        
        Gson gson = new Gson();
        Map<String, String> map1 = new HashMap<>();
        Map<String, String> map2 = new HashMap<>();

        // reads the Map (JSON) from file
        try { 

            BufferedReader reader = new BufferedReader(new FileReader(fileName1));
            String line = reader.readLine();

            map1 = gson.fromJson(line, HashMap.class);

            reader.close();
        } catch (IOException e) {
            System.out.println("PROBLEM WITH ADDING FRIENDSHIP, user1");
            e.printStackTrace();
        }

        try {

            BufferedReader reader = new BufferedReader(new FileReader(fileName2));
            String line = reader.readLine();

            map2 = gson.fromJson(line, HashMap.class);

            reader.close();
        } catch (IOException e) {
            System.out.println("PROBLEM WITH ADDING FRIENDSHIP, user2");
            e.printStackTrace();
        }

        // we put the new friendship in the new Map
        map1.remove(user2);
        map2.remove(user1);

        // we put the map back into the DB
        String json1 = gson.toJson(map1);
        String json2 = gson.toJson(map2);

        writeToFileAndOverride(fileName1, json1);
        writeToFileAndOverride(fileName2, json2);
    }

    // removes the friendRequest
    @SuppressWarnings("unchecked")
    public void removeFriendRequest(String requestSender, String requestReciever) { // INFO: sender is the one who sent the friend request

        // we get the current Set that holds recievers friendRequests
		String fileName1 = String.format("%s/users/@%s/Social/FriendRequests.txt", this.projectPath, requestReciever);
        
        Gson gson = new Gson();
        Set<String> fr = new HashSet<>();

        // we read the ArrayList from the file
        try {

            BufferedReader reader = new BufferedReader(new FileReader(fileName1));
            String line = reader.readLine();

            fr = gson.fromJson(line, HashSet.class);

            reader.close();
        } catch (IOException e) {
            System.out.println("PROBLEM WITH REMOVING FRIENDREQUEST");
            e.printStackTrace();
        }

        // we remove the requestSender's friendRequest
        fr.remove(requestSender);

        // we put the set back into the DB
        String json1 = gson.toJson(fr);

        writeToFileAndOverride(fileName1, json1);
    }

    // sends the friendRequest
    @SuppressWarnings("unchecked")
    public void sendFriendRequest(String sender, String reciever) {

        // we get the current Set that holds recievers friendRequests
		String fileName1 = String.format("%s/users/@%s/Social/FriendRequests.txt", this.projectPath, reciever);
        
        Gson gson = new Gson();
        Set<String> fr = new HashSet<>();

        // we read the ArrayList from the file
        if (numberOfLines(fileName1) > 0) { // if the reciever has any friendRequest we have to read them, otherwise we just make a new HashSet

            try {

                BufferedReader reader = new BufferedReader(new FileReader(fileName1));
                String line = reader.readLine();

                fr = gson.fromJson(line, HashSet.class);

                reader.close();
            } catch (IOException e) {
                System.out.println("PROBLEM WITH ADDING FRIENDREQUEST");
                e.printStackTrace();
            }
        }

        // we add the requestSender's friendRequest
        fr.add(sender);

        // we put the set into the DB
        String json1 = gson.toJson(fr);

        writeToFileAndOverride(fileName1, json1);
    }

    @SuppressWarnings("unchecked")
    public HashMap<String, String> getFriendsList(String user) {

        // we get the file path
		String fileName = String.format("%s/users/@%s/Social/Friends.txt", this.projectPath, user);

        // we read the Map
        Gson gson = new Gson();
        Map<String, String> map = new HashMap<>();

        if (numberOfLines(fileName) <= 0)  return null;
        try {

            BufferedReader reader = new BufferedReader(new FileReader(fileName));
            String line = reader.readLine();

            map = gson.fromJson(line, HashMap.class);

            reader.close();
        } catch (IOException e) {
            System.out.println("PROBLEM WITH GETTING FRIENDS LIST");
            e.printStackTrace();
        }

        // we return the map
        return (HashMap<String, String>) map;
    }

    @SuppressWarnings("unchecked")
    public HashSet<String> getFriendRequestList(String user) {

        // we get the file path
		String fileName = String.format("%s/users/@%s/Social/FriendRequests.txt", this.projectPath, user);

        // we read the Set
        Gson gson = new Gson();
        Set<String> set = new HashSet<>();

        if (numberOfLines(fileName) <= 0)  return null;
        try {

            BufferedReader reader = new BufferedReader(new FileReader(fileName));
            String line = reader.readLine();

            set = gson.fromJson(line, HashSet.class);

            reader.close();
        } catch (IOException e) {
            System.out.println("PROBLEM WITH GETTING FRIEND REQUESTS LIST");
            e.printStackTrace();
        }

        return (HashSet<String>) set;
    }

    @SuppressWarnings("unchecked")
    public void addToSystemLoginList(String user, String message) {
    
        // we get the current Set that holds recievers friendRequests
        String fileName = String.format("%s/users/@%s/SystemLogin.txt", this.projectPath, user);
        
        Gson gson = new Gson();
        List<String> list = new ArrayList<>();

        // we read the ArrayList from the file
        if (numberOfLines(fileName) > 0) { // if the user has any other messages we read them

            try {

                BufferedReader reader = new BufferedReader(new FileReader(fileName));
                String line = reader.readLine();

                list = gson.fromJson(line, ArrayList.class);

                reader.close();
            } catch (IOException e) {
                System.out.println("PROBLEM WITH ADDING TO SYSTEM LOGIN LIST");
                e.printStackTrace();
            }
        }

        // we add the system message to the list
        list.add(message);

        // we put the list into the DB
        String json1 = gson.toJson(list);

        writeToFileAndOverride(fileName, json1);
    }

    @SuppressWarnings("unchecked")
    public ArrayList<String> getAndRemoveSystemLoginList(String user) {

        // we get the file path
		String fileName = String.format("%s/users/@%s/SystemLogin.txt", this.projectPath, user);

        // we read the ArrayList
        Gson gson = new Gson();
        List<String> list = new ArrayList<>();

        if (numberOfLines(fileName) <= 0)  return null;
        try {

            BufferedReader reader = new BufferedReader(new FileReader(fileName));
            String line = reader.readLine();

            list = gson.fromJson(line, ArrayList.class);

            reader.close();
        } catch (IOException e) {
            System.out.println("PROBLEM WITH GETTING SYSTEM LOGIN LIST");
            e.printStackTrace();
        }

        writeToFileAndOverride(fileName, gson.toJson(new ArrayList<String>())); // we reset the list back to nothing

        return (ArrayList<String>) list;
    }
}

class ChatServerConnector extends Thread {
	private ChatServer server;
	private Socket socket;
	private String name;

	public ChatServerConnector(ChatServer server, Socket socket) {
		this.server = server;
		this.socket = socket;
	}

	public void run() {
		// za inpute
		DataInputStream in;
		try {
			in = new DataInputStream(this.socket.getInputStream()); // create input stream for listening for incoming messages
		} catch (IOException e) {
			System.err.println("[system] could not open input stream!");
			e.printStackTrace(System.err);
			this.server.removeClient(socket, false);
			return;
		}

		// za outpute
		DataOutputStream out;
		try {
			out = new DataOutputStream(this.socket.getOutputStream()); // create input stream for listening for incoming messages
		} catch (IOException e) {
			System.err.println("[system] could not open output stream!");
			e.printStackTrace(System.err);
			this.server.removeClient(socket, false);
			return;
		}

		// singing in / up
		boolean logedIn = false;
		String name;
		int ixForClientsName = this.server.makeRoomForClientsName();
		// sendMessageToClient("Enter your name or do \"/singup\" to sing up: ", out);
		try {
			while (!logedIn) { // za login ne bom uporabljal JSON, ker je mal drgac UI. Samo, ko si logged in

				String input = this.getMessageFromClient(in);
				if (input == null)  throw new Exception(); // if client closed aplication
				if (input.equals("/singup")) { // if client wants to sing up
	
                    boolean goToLogin = false;
					boolean nameAvailable = false; // checker that name is available
					boolean inputTooLong = true; // checker that input is 20 chars or less
					boolean inputCorrectFormat = false; // checker that input chars are >= 33 && <= 126(ascii) (od <!> naprej)
                    boolean legalName = false; // checker that the name is not from illegalNames
					name = null;
					String password = null;
					while (!nameAvailable || inputTooLong || !inputCorrectFormat) {
						if (input == null) throw new Exception(); // if client closed aplication

						input = this.getMessageFromClient(in);
                        if (input.equals("GOBACK")) { // if the client wants to go back to login mode

                            goToLogin = true;
                            break;
                        }

						name = input.substring(0, input.indexOf(','));
						password = input.substring(input.indexOf(' ') + 1);

						inputTooLong = (name.length() > 20 || password.length() > 20) ? true : false;
						inputCorrectFormat = this.server.checkRightAscii(name) && this.server.checkRightAscii(password);
						nameAvailable = this.server.CheckNameAvailibilty(name);
                        legalName = !(ChatServer.illegalNames.contains(name));

						// name or password too long (max. 20 chars)
						if (inputTooLong)  this.sendMessageToClient("NAME OR PASSWORD TOO LONG, max. size is 20!", out);

						// invisible characters (ascii < 33 || ascii > 126)
						else if (!inputCorrectFormat)  this.sendMessageToClient("FOUND INVISIBLE ASCII CHARACTERS, Try again!", out);

						// name not available
						else if (!nameAvailable)  this.sendMessageToClient(String.format("NAME <%s> NOT AVAILABLE, Try again!", name), out);

                        // illegal name
                        else if (!legalName)  this.sendMessageToClient(String.format("NAME <%s> IS AN INVALID NAME, Try again!", name), out);
					}

                    if (goToLogin) { // if we are going back to login

                        continue;
                    }
	
					this.server.writeNewUser(name, password);
					this.name = name;
					logedIn = true;
				} 
				else { // if client wants to sing in (log in)
	
					name = input.substring(0, input.indexOf(','));
					String password = input.substring(input.indexOf(' ') + 1);
					if (this.server.findUser(name, password)) { // if user in data base, log him in
						this.name = name;
	
						// if user already online give error message saying that
						if (this.server.isOnline(name)) {
							this.sendMessageToClient(String.format("USER <%s> IS ALREADY ONLINE ON ANOTHER DEVICE! Try again!", name), out);
						}
						else  logedIn = true;
					} else {
						this.sendMessageToClient(String.format("USER WITH NAME <%s>, PASSWORD <%s>, DOES NOT EXSIST! Try again!", name, password), out);
					}
				}
			}
			this.sendMessageToClient(String.format("SUCCESS, %s", this.name), out); // posljemo clientu success, da ve da je prijavljen
			server.addClientsName(this.name, ixForClientsName);
		} catch (Exception e) {
			System.out.println("PROBLEM WITH LOGIN IN / SING UP, removing user");
            e.printStackTrace();
			this.server.removeClient(this.socket, false);
			return;
		}

		System.out.println("[system] connected with " + this.socket.getInetAddress().getHostName() + ":" + this.socket.getPort() + " | name: " + this.name);
        
        // poslje vsem message, da se je joinal
        try {
            if (this.server.clientNames.size() != 0 && this.server.clients.size() != 0)  this.server.sendToAllClients(new Message().createJson(2, "SYSTEM", null, null, String.format("<%s> has entered the chat", this.name)), null);
        } catch (Exception e) {
            System.out.println("PROBLEM WITH SENDING TO ALL CLIENTS!");
            e.printStackTrace();
        }

		// ce ima kaksne neprebrane message, ki jih je dobil, ko je bil offline jim mu pokaze in izbrise file iz OfflineMessages
		File dir = new File(String.format("%s/users/@%s/OfflineMessages", this.server.projectPath, this.name));
		File[] offlineMessages = dir.listFiles();
		if (offlineMessages.length > 1) { // ker imamo .gitkeep file, ki je placeholder, da je na githubu

            Map<String, ArrayList<String>> mapOfflineMessagesVsebina = new HashMap<>();

			for (File offlineMessage : offlineMessages) { // gremo cez offlineMessages

                // if .gitkeep we skip
                if (offlineMessage.getName().equals(".gitkeep"))  continue;
	
				StringBuilder content = new StringBuilder();
				// preberemo vsebino
				try {
					
					BufferedReader reader = new BufferedReader(new FileReader(offlineMessage));
					content.append(reader.readLine());
					reader.close();
				} catch (IOException e) {
					System.out.println("PROBLEM WITH READING CONTENT OF OFFLINEMESSAGE FILE");
					e.printStackTrace();
				}
	
                // logika za pridobivanje potrebnih spremenljivk 
				int numberOfMessages = Integer.valueOf(content.toString().substring(content.toString().indexOf(' ') + 1));
				String sender = this.server.findNonMatching(content.toString().substring(1, content.toString().indexOf('.')), this.name);
				String fileToReadFrom = content.toString().substring(0, content.toString().indexOf(','));

                // preberemo vsebino in vzamemo samo zadnjih <numberOfMessages>
                ArrayList<String> mapList = new ArrayList<>();
                ArrayList<String> historyMessages = this.server.getHistory(String.format("%s/histories/%s", this.server.projectPath, fileToReadFrom)); // dobimo cel history
                for (int i = historyMessages.size() - numberOfMessages; i < historyMessages.size(); i++) { // ArrayList dodajamo potrebne messages

                    mapList.add(historyMessages.get(i));
                }

                this.server.removeFile(offlineMessage.getAbsolutePath());
			
                mapOfflineMessagesVsebina.put(sender, mapList);
            }

			this.sendMessageToClient(new Response().createResponse(999, null, mapOfflineMessagesVsebina, this.name, null), out);
		}

        // poslejmo response (998), ki ma noter array list od SYSTEM, noter so messages
        // STEPS: make List, add "you have X friend req." (if X > 0), check the SystemLogin.txt file, add lines from it, DONE. (za future lahko pac dodas se ksn login message ce hocs)
        List<String> loginMessages = new ArrayList<>();

        // we check for number of friend requests
        Set<String> listOfFriendRequests = this.server.getFriendRequestList(this.name);
        if (listOfFriendRequests != null && listOfFriendRequests.size() > 0) { // if not empty

            loginMessages.add(new Message().createJson(2, "SYSTEM", null, null, String.format("YOU HAVE %d PENDING FRIEND REQUESTS", listOfFriendRequests.size())));
        }

        // we check if there is anything in the SystemLogin.txt file
        List<String> systemLoginMessages = this.server.getAndRemoveSystemLoginList(this.name);
        if (systemLoginMessages != null && systemLoginMessages.size() > 0)  loginMessages.addAll(systemLoginMessages);

        // other messages for future logic do here
        // PS: keep in mind that the Client interprets the 998 response as List of Strings (which are JSON's of GsonTypes.Message), so if you want to send something else on login you have to make a revamp:

        // we send the 998 response to client
        this.sendMessageToClient(new Response().createResponse(998, loginMessages, null, this.name, null), out);

        // ---
        // LOGED IN

		while (true) { // infinite loop in which this thread waits for incoming messages and processes them
			String msg_received;
			try {
				msg_received = in.readUTF(); // read the message from the client
			} catch (Exception e) {
				System.err.println("[system] there was a problem while reading message client on port " + this.socket.getPort() + ", removing client");
				// e.printStackTrace(System.err);
				this.server.removeClient(this.socket, true);
				return;
			}

			if (msg_received.length() == 0) // invalid message
				continue;

			// gledamo tip sporocila in kaj bomo z njim naredili

			DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd.MM.yyyy - HH:mm:ss");
			LocalDateTime now = LocalDateTime.now();

            Gson gson = new Gson();
            Message json = gson.fromJson(msg_received, Message.class);
            
            if (json.tipGsona == 1) { // ce je tipa GsonTypes.Message

                if (json.getTip() == 0) { // public sporocilo (broadcast)

                    String msg_send = new Message().createJson(0, json.getSender(), null, dtf.format(now).toString(), String.format("%s", json.getBody()));
                    System.out.println(msg_send);
    
                    try {
                        this.server.sendToAllClients(msg_send, this.socket); // send message to all clients
                    } catch (Exception e) {
                        System.err.println("[system] there was a problem while sending the message to all clients");
                        e.printStackTrace(System.err);
                        continue;
                    }
                }
                else if (json.getTip() == 1) { // private sporocilo

                    // dobimo index od clientNames lista, da dobimo njegov socket
                    int indexC = server.clientNames.indexOf(json.getReciever());

                    String msg_send = "";
                    Socket socketC = this.socket;
    

                    if (indexC == -1 && this.server.CheckNameAvailibilty(json.getReciever())) { // if the client doesnt exist

                        msg_send = new Message().createJson(2, "SYSTEM", null, dtf.format(now).toString(), String.format("Client name <%s> not found! message not sent!", json.getReciever()));
                    }
                    else { // if the client exists (online / offline)

                        String chatName = this.server.getChatName(this.name, json.getReciever());
                        
                        // damo message v history
                        msg_send = new Message().createJson(1, this.name, json.getReciever(), dtf.format(now).toString(), String.format("%s", json.getBody()));
                        this.server.writeToChat(chatName, msg_send); // zapisemo v history (v obeh primerih)
    
                        if (indexC != -1)  socketC = server.clients.get(indexC); // ce je online spremenimo socket na recieverSocket in mu message kasneje posljemo

                        else { // ce je offline, damo v recieverjeve offline messages
                                // spremenimo message, pustimo socket na senderSocket in posljemo senderju nazaj SystemMessage

                            this.server.addToOfflineMessages(this.server.getChatName(this.name, json.getReciever()), json.getReciever(), msg_send);
                            msg_send = new Message().createJson(2, "SYSTEM", null, dtf.format(now).toString(), 
                            String.format("Client name <%s> is offline, they will see the message on log in", json.getReciever()));
                        }             
                    }
    
                    // posljemo sporocilo cilnjemu odjemalcu
                    try {
                        System.out.println(msg_send); // izpis v server
                        this.server.sendToClientX(msg_send, socketC);
                    } catch (Exception e) {
                        System.err.printf("[system] there was a problem while sending the message to client: name: %s | socket: %s", json.getReciever(), socketC);
                        e.printStackTrace(System.err);
                        continue;
                    }
                }

            }
            else if (json.tipGsona == 2) { // GsonTypes.Request

                Request request = gson.fromJson(msg_received, Request.class);
                String msg_send = "";

                boolean sendToOther = false;
                String nameOfOther = "";
                String msgToOther = "";

                if (request.getTip() == 0) { // list of online users request

                    msg_send = new Response().createResponse(0, this.server.clientNames, null, request.getSender(), dtf.format(now).toString());
                }
                else if (request.getTip() == 1) { // history request | "otherInfo"= ime od katerega hoce history

                    ArrayList<String> histories = this.server.getHistory(request.getSender(), request.getOtherInfo());

                    if (histories == null)  msg_send = String.format(new Message().createJson(2, "SYSTEM", null, null, String.format("YOU DO NOT HAVE HISTROY WITH <%s>", request.getOtherInfo())));
                    else {

                        histories.add(0, request.getOtherInfo());
                        msg_send = new Response().createResponse(1, histories, null, request.getSender(), dtf.format(now).toString());
                    }
                }
                else if (request.getTip() == 2) { // friends list request

                    msg_send = new Response().createResponse(2, null, this.server.getFriendsList(request.getSender()), request.getSender(), dtf.format(now).toString());
                }
                else if (request.getTip() == 3) { // friendRequest list request

                    msg_send = new Response().createResponse(3, this.server.getFriendRequestList(request.getSender()), null, request.getSender(), dtf.format(now).toString());
                }
                else if (request.getTip() == 996) { // sendFriendRequest

                    this.server.sendFriendRequest(request.getSender(), request.getOtherInfo());
                    msg_send = new Message().createJson(2, "SYSTEM", null, dtf.format(now).toString(), String.format("YOU SENT A FRIEND REQUEST TO <%s>", request.getOtherInfo()));

                    sendToOther = true;
                    nameOfOther = request.getOtherInfo();
                    msgToOther = new Message().createJson(2, "SYSTEM", null, dtf.format(now).toString(), String.format("<%s> SENT YOU A FRIEND REQUEST", request.getSender()));
                }
                else if (request.getTip() == 997) { // removeFriendRequest Request

                    this.server.removeFriendRequest(request.getOtherInfo(), request.getSender());
                    msg_send = new Message().createJson(2, "SYSTEM", null, dtf.format(now).toString(), String.format("YOU DECLINED <%s>'s FRIEND REQUEST", request.getOtherInfo()));

                    sendToOther = true;
                    nameOfOther = request.getOtherInfo();
                    msgToOther = new Message().createJson(2, "SYSTEM", null, dtf.format(now).toString(), String.format("<%s> DECLINED YOUR FRIEND REQUEST", request.getSender()));
                }
                else if (request.getTip() == 998) { // removeFriend Request

                    this.server.removeFriendship(request.getSender(), request.getOtherInfo());
                    msg_send = new Message().createJson(2, "SYSTEM", null, dtf.format(now).toString(), String.format("YOU UNFRIENDED <%s>", request.getOtherInfo()));
                
                    sendToOther = true;
                    nameOfOther = request.getOtherInfo();
                    msgToOther = new Message().createJson(2, "SYSTEM", null, dtf.format(now).toString(), String.format("<%s> UNFRIENDED YOU", request.getSender()));
                }
                else if (request.getTip() == 999) { // addFriend Request | "otherInfo"= ime user2

                    this.server.addFriendship(request.getSender(), request.getOtherInfo(), request.getTime());
                    msg_send = new Message().createJson(2, "SYSTEM", null, dtf.format(now).toString(), String.format("<%s> IS NOW YOUR FRIEND", request.getOtherInfo()));

                    sendToOther = true;
                    nameOfOther = request.getOtherInfo();
                    msgToOther = new Message().createJson(2, "SYSTEM", null, dtf.format(now).toString(), String.format("<%s> IS NOW YOUR FRIEND", request.getSender()));
                }

                try {
                    System.out.println(msg_send);
                    this.server.sendToClientX(msg_send, socket);

                    if (sendToOther) { // if the request requires we send a message to some other person aswell

                        // we check if the user is online
                        int ix = this.server.clientNames.indexOf(nameOfOther);

                        if (ix != -1) { // if online we send
                            
                            Socket socketOfOther = this.server.clients.get(ix);
                            this.server.sendToClientX(msgToOther, socketOfOther);
                        }
                        else {

                            this.server.addToSystemLoginList(nameOfOther, msgToOther);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            else if (true) { // TODO: ce imamo druge tipe


            }
		}
	}

	public String getNameOfClient() {
		return this.name;
	}

	public void sendMessageToClient(String message, DataOutputStream out) {

        try {
            out.writeUTF(message);
            out.flush();
        } catch (IOException e) {
            System.err.println("[system] problem with sending message to client");
            e.printStackTrace(System.err);
        }
	}

	public String getMessageFromClient(DataInputStream in) {
		String input = null;
		try {
			input = in.readUTF();
		} catch (IOException e) {
			System.err.println("[system] problem with getting message from client");
			e.printStackTrace(System.err);
		}
		return input;
	}
}