import java.io.Serializable;

// parse the client command input string
public class Operation implements Serializable {
    String type;
    String key;
    String value;

    public static Operation createOperation(String operation){
        Operation res = new Operation();
        String[] parts = operation.split(" ");
        if(parts.length < 2 || parts.length > 3) return null;
        switch (parts[0]){
            case "GET":
            case "DELETE":
                if(parts.length != 2) return null;
                break;
            case "PUT":
                if(parts.length != 3) return null;
                res.value = parts[2];
                break;
            default:
                return null;
        }
        res.type = parts[0];
        res.key = parts[1];
        return res;
    }

}