import java.io.Serializable;

public class AcceptReply implements Serializable {
    boolean ack;
    int promisedNum;
    public AcceptReply(boolean ack, int promisedNum){
        this.ack = ack;
        this.promisedNum = promisedNum;
    }
}
