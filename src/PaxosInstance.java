import java.io.Serializable;
import java.util.AbstractMap;

// store whole information for current paxos instance
public class PaxosInstance implements Serializable {

    // proposer data
    // instance/epoch
    int round;
    // ballot num/proposalId
    int num;
    // 0 for running, 1 for settled
    int status;
    Operation operation;


    // acceptor data
    int promised;
    AbstractMap.SimpleEntry<Integer, Operation> acceptedValue;


    // learner data
    int learnerCounter = 0;
    Operation learnerVal = null;
    int learnerProposalNum = 0;

    // result for client
    String clientResponse = "";
    public PaxosInstance(int round, int num, int status, Operation operation){
        this.round = round;
        this.num = num;
        this.status = status;
        this.operation = operation;
        promised = 0;
        acceptedValue = null;
    }

    public void increNum(int step){
        num += step;
    }
}
