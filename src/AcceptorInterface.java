import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * The AcceptorInterface defines the remote methods to be implemented by the acceptors in the Paxos
 * consensus algorithm. It includes methods for preparing and accepting proposals.
 */
public interface AcceptorInterface extends Remote {


  PromiseMsg promise(int round, int proposalId) throws RemoteException;

  AcceptReply accept(int round, int proposalId, Operation proposalValue) throws RemoteException;
}
