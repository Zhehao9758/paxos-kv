import java.io.Serializable;

public class AcceptReply implements Serializable {

    // accept or not
    boolean ack;

    // if not, attach higher num for re-propose
    int promisedNum;
    public AcceptReply(boolean ack, int promisedNum){
        this.ack = ack;
        this.promisedNum = promisedNum;
    }
}
