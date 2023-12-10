import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ServerInterface extends Remote {
    // client input command string and return result
    String sendCommand(Operation operation) throws RemoteException;
}
