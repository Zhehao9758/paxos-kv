import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * The ProposerInterface provides a remote method to initiate a proposal in the Paxos consensus algorithm.
 * It is part of the Paxos distributed consensus protocol, representing the proposing role.
 */
public interface ProposerInterface extends Remote {

  /**
   * Initiates a proposal with the given proposal ID and value.
   *
   * @param proposalId The unique identifier for the proposal.
   */
  Operation prepare(int round, int proposalId) throws RemoteException;
  String propose(int round, int proposalId, Operation operation) throws RemoteException;

  // 0 for success, -1 for not enough nodes, > 0 for promisedNum
  int acceptRequest(int round, int proposalId, Operation operation) throws RemoteException;
}
