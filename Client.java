
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.Socket;
import java.util.*;


import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

 class Server { // sever side program..

    static Vector<ClientHandler> activeClients = new Vector<ClientHandler>();
    static int numberOfClients = 0;


    public static void main(String[] args) throws IOException {

        ServerSocket server = new ServerSocket(3456);
        Socket clientSoc = null;
        DataInputStream clientToServerInput = null;
        DataOutputStream severToClientOutput = null;

//      FileOutputStream f1 = new FileOutputStream("D:\\testServer\\data.txt");
//      ObjectOutputStream outputStream = new ObjectOutputStream(f1);

        while (true) {

            try {

                clientSoc = server.accept();
                System.out.println("connection was successful.");

                clientToServerInput = new DataInputStream(clientSoc.getInputStream());
                severToClientOutput = new DataOutputStream(clientSoc.getOutputStream());

                ClientHandler clientHandler = new ClientHandler(clientSoc, clientToServerInput, severToClientOutput);

                Thread handlerThread = new Thread(clientHandler);
                //outputStream.writeObject(clientHandler);

                activeClients.add(clientHandler);
                handlerThread.start();

            }
            catch (IOException e){
                System.out.println(e.getMessage());
            }
        }
    }

}

class ExistingUserException extends Exception{

    ExistingUserException(String massage){
        super(massage);
    }
}

class UnknownUserException extends Exception{

    UnknownUserException(String massge){
        super(massge);
    }

}

class InvalidSessionException extends Exception{

    InvalidSessionException(String massage){
        super(massage);
    }
}

class UnknownPasswordException extends Exception{

    UnknownPasswordException(String massage){
        super(massage);
    }
}

 class ClientHandler extends Thread implements Serializable {

    private final DataInputStream input;
    private final DataOutputStream output;
    private Socket socketClient;
    private String clientUserName = "";
    private String command;
    private String clientPassWord = "";
    private BigInteger session;
    boolean isLogedIn;
    Scanner scanner;
    boolean canRegisterd;

    public BigInteger genarateBigNumber(){

        BigInteger min = new BigInteger("100000000000000000000000005");
        BigInteger bigInteger = new BigInteger("900000000000000000000000000000");
        BigInteger bigIntegerTemp = bigInteger.subtract(min);

        int maxNumBitLength = bigInteger.bitLength();

        Random rnd = new Random();
        BigInteger randomB;

        randomB = new BigInteger(maxNumBitLength, rnd);
        if (randomB.compareTo(min) < 0)
            randomB = randomB.add(min);

        if (randomB.compareTo(bigInteger) >= 0)
            randomB = randomB.mod(bigIntegerTemp).add(min);

        return randomB;
    }

    public  boolean checkSignedIn(String username, String password){  // can be foreach.

        for (int i = 0; i < Server.activeClients.size(); i++){
            if (Server.activeClients.get(i).clientUserName.equals(username) && Server.activeClients.get(i).clientPassWord.equals(password))
                return true;
        }
        return false;

    }

    public boolean isValidSession(BigInteger session){

        for(ClientHandler clientHandler : Server.activeClients){
            if (clientHandler.session.equals(session)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isInList(String username, String password){

        for (int i = 0; i < Server.activeClients.size(); i++){
            if (Server.activeClients.get(i).getClientUserName().equals(username) && Server.activeClients.get(i).getClientPassWord().equals(password)){
                return true;
            }
        }
        return false;
    }

    public String getClientUserName(){
        return clientUserName;
    }

    public String getClientPassWord(){
        return clientPassWord;
    }

    ClientHandler(Socket socketClient, DataInputStream input, DataOutputStream output){
        this.output = output;
        this.input = input;
        this.socketClient = socketClient;

        Server.numberOfClients++;

        isLogedIn = true;
    }

    @Override
    public void run() {
        /// sever works goes here.
        String received;  // this received String is a 3 Strings 1-comand  2-client username 3-client password.

        scanner = new Scanner(System.in);
        while (true) {

            try {

                received = input.readUTF();
                String[] splitStr = received.split("-"); // splited with space.
                System.out.println(splitStr[0]);


                if (splitStr[0].equals("register")) {

                    canRegisterd = true;

                    for (ClientHandler clientHandler : Server.activeClients) {

                        if (clientHandler.clientUserName.equals(splitStr[1])) {
                            canRegisterd = false;
                            output.writeUTF("this user name is existing!");
                            throw new ExistingUserException("this user name is existing!");
                        }
                    }

                    if (canRegisterd) {
                        output.writeUTF("you successfully registered");
                        output.flush();
                        this.clientUserName = splitStr[1];
                        this.clientPassWord = splitStr[2];

                        canRegisterd = false;
                    }
                    else {

                        throw new ExistingUserException("this username has already in the system\n");
                    }
                }

                else if (splitStr[0].equals("login")){

                    if (isInList(splitStr[1], splitStr[2])){

                        isLogedIn = true;

                        System.out.println("the session for new client has been made");
                        session = genarateBigNumber();
                        output.writeUTF(this.clientUserName + " your active session from server: " + session);
                        output.flush();

                    }

                    else {
                        throw new UnknownUserException("this user is not in the system");
                    }
                }

                else if (splitStr[0].equals("logout")){

                    if (this.isLogedIn && !Server.activeClients.isEmpty()){
                        BigInteger num = new BigInteger(splitStr[1]);

                        if (this.isValidSession(num)) {

                            this.isLogedIn = false;
                            // this.session = null;
                            output.writeUTF("your session logout successfully");
                            output.flush();
                        }
                        else
                            throw new InvalidSessionException("InvalidSessionException!");
                    }
                }

            }

            catch (Exception e){
                System.out.println(e.getMessage());

            }
        }

    }
}


public class Client {

    public static void main(String[] args) throws IOException {

        Socket clientSocket = new Socket("localhost", 3456);
        Scanner consoleInput = new Scanner(System.in);
        DataInputStream input = new DataInputStream(clientSocket.getInputStream());
        DataOutputStream output = new DataOutputStream(clientSocket.getOutputStream());

        System.out.println("Enter in this format only enter the commands:");
        System.out.println("1-register-username-password");
        System.out.println("2-login-username-password");  // i splited String with (-)
        System.out.println("3-logout-session");
        // enter the commands

        Thread sendMassage = new Thread(() ->{


            while (true) {
                String massage = consoleInput.next();
                try {

                    output.writeUTF(massage);
                    output.flush();
                } catch (IOException e) {
                    System.out.println(e.getMessage());

                }
            }

        });

        Thread readMassage = new Thread(() ->{

            while (true) {
                try {

                    String massage = input.readUTF();
                    System.out.println(massage + "\n");

                } catch (IOException e) {
                    System.out.println(e.getMessage());
                }

            }

        });

        sendMassage.start();
        readMassage.start();
    }
}
