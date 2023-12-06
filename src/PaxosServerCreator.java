import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.random.RandomGenerator;

/**
 * The PaxosServerCreator class is responsible for creating and binding the Paxos servers
 * within the RMI registry. It also configures the acceptors and learners for each server.
 */
public class PaxosServerCreator {

    /**
     * The main method to launch the creation and binding process of the Paxos servers.
     *
     * @param args Command-line arguments (unused in this context).
     */
    public static void main(String[] args) {
        try {
            int numServers = 5; // Total number of servers
            int basePort = 5000; // Starting port number

            Server[] servers = new Server[numServers];

            // Create and bind servers
            for (int serverId = 0; serverId < numServers; serverId++) {
                int port = basePort + serverId; // Increment port for each server

                // Create server instance
                servers[serverId] = new Server(serverId, numServers);

                // Create and Bind the server to the RMI registry
                Registry registry = LocateRegistry.createRegistry(port);
                registry.rebind("KVStore", servers[serverId]);

                System.out.println("Server " + serverId + " is ready at port " + port);
            }

            // Set acceptors and learners for each server
            for (int serverId = 0; serverId < numServers; serverId++) {
                AcceptorInterface[] acceptors = new AcceptorInterface[numServers];
                LearnerInterface[] learners = new LearnerInterface[numServers];
                for (int i = 0; i < numServers; i++) {
                    if (i != serverId) {
                        acceptors[i] = servers[i];
                        learners[i] = servers[i];
                    }
                }
                servers[serverId].setAcceptors(acceptors);
                servers[serverId].setLearners(learners);
            }
            while(true){
                // random server and random status
                int serverId = RandomGenerator.getDefault().nextInt()%5;
                if(serverId < 0) serverId += numServers;
                servers[serverId].serviceDown = RandomGenerator.getDefault().nextInt()%2==0;
                Thread.sleep(5000);
            }

        } catch (Exception e) {
            System.err.println("Server exception: " + e);
            e.printStackTrace();
        }
    }
}
