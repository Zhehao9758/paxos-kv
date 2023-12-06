import java.io.Serializable;
import java.util.AbstractMap;

public class PromiseMsg implements Serializable {
    boolean ack;
    Integer promisedNum;
    AbstractMap.SimpleEntry<Integer, Operation> acceptedVal;
    public PromiseMsg(boolean ack, Integer promisedNum, AbstractMap.SimpleEntry<Integer, Operation> acceptedVal){
        this.ack = ack;
        this.promisedNum = promisedNum;
        this.acceptedVal = acceptedVal;
    }
}
