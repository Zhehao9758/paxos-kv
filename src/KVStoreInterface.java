import java.rmi.Remote;
import java.rmi.RemoteException;

public interface KVStoreInterface extends Remote {
  void put(String key, String value) throws RemoteException;
  String delete(String key) throws RemoteException;
  String get(String key) throws RemoteException;
}

