import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.Scanner;

public class Client {
  private final ServerInterface server;
  public Client(String host, int port) throws Exception {
    String url = "rmi://" + host + ":" + port + "/KVStore";
    server = (ServerInterface) Naming.lookup(url);
  }

  public String performTask(String operation){
    try{
      Operation operationObj = Operation.createOperation(operation);
      if(operationObj == null) return "Invalid Input";
      return server.sendCommand(operationObj);
    } catch (RemoteException e){
      return e.getMessage();
    }
  }


  public static void main(String[] args) throws Exception {
    if(args.length != 2){
      System.err.println("Usage: java KeyValueStoreClient <hostname> <port>");
      System.exit(1);
    }
    String hostName = args[0];
    int port = Integer.parseInt(args[1]);

    Client keyValueStoreClient = new Client(hostName, port);

    Scanner scanner = new Scanner(System.in);
    String operation;
    while (scanner.hasNext()) {
      operation = scanner.nextLine();
      // custom quit command
      if (operation.equals("Q")) {
        break;
      }
      try {
        System.out.println(keyValueStoreClient.performTask(operation));
      } catch (Exception e) {
        System.out.println("Error: " + e);
      }
    }
  }
}

