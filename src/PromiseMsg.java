import java.io.Serializable;
import java.util.AbstractMap;

public class PromiseMsg implements Serializable {
    // agree or not
    boolean ack;
    // attach for re-propose
    Integer promisedNum;
    AbstractMap.SimpleEntry<Integer, Operation> acceptedVal;
    public PromiseMsg(boolean ack, Integer promisedNum, AbstractMap.SimpleEntry<Integer, Operation> acceptedVal){
        this.ack = ack;
        this.promisedNum = promisedNum;
        this.acceptedVal = acceptedVal;
    }
}
