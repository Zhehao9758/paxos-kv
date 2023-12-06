import java.io.Serializable;
import java.util.AbstractMap;

public class PaxosInstance implements Serializable {
    int round;
    int num;
    int status;
    Operation operation;

    int promised;
    AbstractMap.SimpleEntry<Integer, Operation> acceptedValue;

    int learnerCounter = 0;
    Operation learnerVal = null;
    int learnerProposalNum = 0;
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
