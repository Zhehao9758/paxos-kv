import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * The ProposerInterface provides a remote method to initiate a proposal in the Paxos consensus algorithm.
 * It is part of the Paxos distributed consensus protocol, representing the proposing role.
 */
public interface ProposerInterface extends Remote {

  // start a complete paxos instance, run whole process from propose to accept, including re-propose
  String propose(int round, int proposalId, Operation operation) throws RemoteException;

  // phase 1 communicate with acceptors and return operation for phase 2
  Operation prepare(int round, int proposalId) throws RemoteException;


  // 0 for success, -1 for not enough nodes, > 0 for promisedNum
  int acceptRequest(int round, int proposalId, Operation operation) throws RemoteException;
}
