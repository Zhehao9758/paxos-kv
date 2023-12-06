import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Implementation of a Server class that represents a node in a Paxos distributed consensus system.
 * This server plays the role of Proposer, Acceptor, and Learner in the Paxos algorithm, and it also handles key-value store operations.
 */
public class Server extends UnicastRemoteObject implements ServerInterface, ProposerInterface, AcceptorInterface, LearnerInterface {
    private final ConcurrentHashMap<String, String> kvStore = new ConcurrentHashMap<>();
    private AcceptorInterface[] acceptors;
    private LearnerInterface[] learners;
    private final int numServers;
    private final int serverId;
    private int paxosRound;
    private final int majorityNum;
    private final Map<Integer, PaxosInstance> instances;
    private final ReentrantLock learnLock = new ReentrantLock();
    private final ReentrantLock writeLock = new ReentrantLock();
    public boolean serviceDown;

    /**
     * Constructor to create a Server instance.
     *
     * @param serverId   The unique ID of this server.
     * @param numServers The total number of servers in the system.
     */
    public Server(int serverId, int numServers) throws RemoteException {
        super();
        this.numServers = numServers;
        serviceDown = false;
        // exclude itself
        this.majorityNum = numServers / 2;

        this.serverId = serverId;
        paxosRound = 0;
        instances = new HashMap<>();

    }

    /**
     * Set the acceptors for this server.
     *
     * @param acceptors Array of acceptors.
     */
    public void setAcceptors(AcceptorInterface[] acceptors) {
        this.acceptors = acceptors;
    }

    /**
     * Set the learners for this server.
     *
     * @param learners Array of learners.
     */
    public void setLearners(LearnerInterface[] learners) {
        this.learners = learners;
    }


    /**
     * Propose an operation to be applied.
     *
     * @param operation The operation to be proposed.
     */
    @Override
    public synchronized String sendCommand(Operation operation) throws RemoteException {
        // if it is down, cannot communicate with client
        if(serviceDown) throw new RemoteException("target server is down");
        if (!instances.containsKey(paxosRound)) {
            instances.put(paxosRound, new PaxosInstance(paxosRound, serverId, 0, operation));
        }
        PaxosInstance instance = instances.get(paxosRound);
        instance.increNum(numServers);
        return propose(instance.round, instance.num, operation);
    }

    @Override
    public synchronized String propose(int round, int proposalId, Operation operation) throws RemoteException {
        // if operation is null, means just want to catch up
        PaxosInstance instanceR = instances.get(round);
        Operation val = prepare(round, proposalId);
        if (val == null) {
            return "cannot communicate with majority";
        }
        int res = acceptRequest(round, instanceR.num, val);
        if (res == -1) return "cannot communicate with majority";

            // accept request nack by higher Num
        else if (res > 0) {
            instanceR.num = nextHigherNum(res);
            return propose(round, instanceR.num, operation);
        }

        // success
        // if not the expected value, try another instance later
        if (operation != null && !operation.equals(val)) {
            applyOperation(round, val);
            return sendCommand(operation);
        }
        return applyOperation(round, val);
    }

    //!!! dirty data
    @Override
    public synchronized Operation prepare(int round, int proposalId) throws RemoteException {
        int count = 0;
        int promisedNum = 0;
        Operation acceptedValue = instances.get(round).operation;
        int acceptedNum = 0;
        for (AcceptorInterface acceptor : acceptors) {
            if (acceptor == null) continue;
            try {
                PromiseMsg msg = acceptor.promise(round, proposalId);
                if (msg.acceptedVal != null && msg.acceptedVal.getKey() > acceptedNum) {
                    acceptedNum = msg.acceptedVal.getKey();
                    acceptedValue = msg.acceptedVal.getValue();
                }
                if (msg.ack) {
                    count++;
                } else if (msg.promisedNum > promisedNum) {
                    promisedNum = msg.promisedNum;
                }
            } catch (RemoteException e) {
                System.out.println(e.getMessage());
            }
        }
        // get nack, try higher num
        if (promisedNum != 0) {
            instances.get(round).num = nextHigherNum(promisedNum);
            return prepare(round, instances.get(round).num);
        }
        if (count >= majorityNum) return acceptedValue;
        // cannot communicate with majority, stop
        return null;
    }

    @Override
    public synchronized PromiseMsg promise(int round, int proposalId) throws RemoteException {
        if(serviceDown) throw new RemoteException("Cannot get prepare result from acceptor " + serverId);
        while (paxosRound < round) {
            sendCommand(null);
        }
        if (!instances.containsKey(round)) {
            instances.put(round, new PaxosInstance(round, serverId, 0, null));
        }
        PaxosInstance instance = instances.get(round);
        PromiseMsg reply = new PromiseMsg(false, instance.promised, instance.acceptedValue);
        if (instance.promised < proposalId) {
            instance.promised = proposalId;
            reply.ack = true;
        }
        return reply;
    }


    // 0 for success, -1 for not enough nodes, > 0 for promisedNum
    @Override
    public synchronized int acceptRequest(int round, int proposalId, Operation operation) throws RemoteException {
        // Implement Paxos accept logic here
        int count = 0;
        int promisedNum = -1;
        for (AcceptorInterface acceptor : acceptors) {
            if (acceptor == null) continue;
            try {
                AcceptReply reply = acceptor.accept(round, proposalId, operation);
                if (reply.ack) {
                    count++;
                } else if (reply.promisedNum > promisedNum) {
                    promisedNum = reply.promisedNum;
                }
            } catch (RemoteException e) {
                System.out.println(e.getMessage());
            }
        }
        if (count < majorityNum) {
            return promisedNum;
        }
        return 0;
    }

    @Override
    public synchronized AcceptReply accept(int round, int proposalId, Operation proposalValue) throws RemoteException {
        if(serviceDown) throw new RemoteException("Cannot get accept result from acceptor " + serverId);
        while (paxosRound < round) {
            sendCommand(null);
        }
        PaxosInstance instanceR = instances.get(round);
        if (instanceR.promised > proposalId) return new AcceptReply(false, instanceR.promised);
        instanceR.promised = proposalId;
        instanceR.acceptedValue = new AbstractMap.SimpleEntry<>(proposalId, proposalValue);

        for (LearnerInterface learner : learners) {
            if (learner == null) continue;
            try {
                learner.learn(round, proposalId, proposalValue);
            } catch (RemoteException e) {
                System.out.println(e.getMessage());
            }
        }
        return new AcceptReply(true, proposalId);
    }

    @Override
    public void learn(int round, int proposalId, Operation acceptedValue) throws RemoteException {
        // Implement Paxos learn logic here
        if(serviceDown) throw new RemoteException("Cannot communicate with learner " + serverId);
        learnLock.lock();
        try {
            while (paxosRound < round) {
                System.out.println(paxosRound+ " and " + round);
                sendCommand(null);
            }
            if (!instances.containsKey(round)) {
                instances.put(round, new PaxosInstance(round, serverId, 0, null));
            }
            PaxosInstance instanceR = instances.get(round);
            if (instanceR.learnerProposalNum < proposalId) {
                instanceR.learnerCounter = 1;
                instanceR.learnerVal = acceptedValue;
                instanceR.learnerProposalNum = proposalId;
            } else if (instanceR.learnerProposalNum == proposalId) {
                instanceR.learnerCounter++;
                if (instanceR.learnerCounter >= majorityNum) {
                    applyOperation(round, acceptedValue);
                }
            }
        } finally {
            learnLock.unlock();
        }
    }

    public int nextHigherNum(int promised) {
        return (promised / numServers + 1) * numServers + serverId;
    }

    /**
     * Apply the given operation to the key-value store.
     *
     * @param operation The operation to apply.
     */
    private String applyOperation(int round, Operation operation) throws RemoteException {
        writeLock.lock();
        try {
            if (!instances.containsKey(round) || operation == null) {
                if(operation == null) throw new IllegalStateException("Operation is null");
                throw new IllegalStateException("not have this round " + round + "this round is " + paxosRound);
            }
            PaxosInstance instanceR = instances.get(round);
            if (instanceR.status == 1) return instanceR.clientResponse;
            instanceR.status = 1;
            paxosRound++;
            switch (operation.type) {
                case "PUT" -> {
                    kvStore.put(operation.key, operation.value);
                    instanceR.clientResponse = "Successfully put the key " + operation.key + " of value " + operation.value;
                }
                case "DELETE" -> {
                    String result = kvStore.remove(operation.key);
                    if (result == null) instanceR.clientResponse = "Key " + operation.key + " Not found in the store";
                    else instanceR.clientResponse = "Successfully deleted the key " + operation.key;
                }
                case "GET" -> {
                    String result = kvStore.get(operation.key);
                    if (result == null) instanceR.clientResponse = "Key " + operation.key + " Not found in the store";
                    else instanceR.clientResponse = "The value of key " + operation.key + " is " + result;
                }
                default -> throw new IllegalArgumentException("Unknown operation type: " + operation.type);
            }
            return instanceR.clientResponse;
        } finally {
            writeLock.unlock();
        }

    }


}
