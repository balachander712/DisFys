import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;


public class LaunchServer {

	static int regPort = Configurations.REG_PORT;

	static Registry registry ;

	static void respawnReplicaServers(Master master)throws IOException{
		System.out.println("[@main] respawning replica servers ");

		BufferedReader br = new BufferedReader(new FileReader("repServers.txt"));
		int n = Integer.parseInt(br.readLine().trim());
		ReplicaLoc replicaLoc;
		String s;

		for (int i = 0; i < n; i++) {
			s = br.readLine().trim();
			replicaLoc = new ReplicaLoc(i, s.substring(0, s.indexOf(':')) , true);
			ReplicaServer rs = new ReplicaServer(i, "./");

			ReplicaMaster stub = (ReplicaMaster) UnicastRemoteObject.exportObject(rs, 0);
			registry.rebind("ReplicaClient"+i, stub);

			master.registerReplicaServer(replicaLoc, stub);

			System.out.println("replica server state [@ main] = "+rs.isAlive());
		}
		br.close();
	}


	public  static void customTest() throws IOException, NotBoundException, MessageNotFoundException{
		Client c = new Client();
		String fileName = "file1";

		char[] ss = "[INITIAL DATA!]".toCharArray(); // len = 15
		byte[] data = new byte[ss.length];
		for (int i = 0; i < ss.length; i++) 
			data[i] = (byte) ss[i];

		c.write(fileName, data);

		c = new Client();
		ss = "Testing File 1 ---> END!!!".toCharArray(); // len = 20
		data = new byte[ss.length];
		for (int i = 0; i < ss.length; i++) 
			data[i] = (byte) ss[i];

		
		byte[] chunk = new byte[Configurations.CHUNK_SIZE];

		int seqN =data.length/Configurations.CHUNK_SIZE;
		int lastChunkLen = Configurations.CHUNK_SIZE;

		if (data.length%Configurations.CHUNK_SIZE > 0) {
			lastChunkLen = data.length%Configurations.CHUNK_SIZE;
			seqN++;
		}
		
		WriteAck ackMsg = c.masterStub.write(fileName);
		ReplicaServerClient stub = (ReplicaServerClient) registry.lookup("ReplicaClient"+ackMsg.getLoc().getId());

		FileContent fileContent;
		@SuppressWarnings("unused")
		ChunkAck chunkAck;
		System.arraycopy(data, 0*Configurations.CHUNK_SIZE, chunk, 0, Configurations.CHUNK_SIZE);
		fileContent = new FileContent(fileName, chunk);
		chunkAck = stub.write(ackMsg.getTransactionId(), 0, fileContent);


		System.arraycopy(data, 1*Configurations.CHUNK_SIZE, chunk, 0, Configurations.CHUNK_SIZE);
		fileContent = new FileContent(fileName, chunk);
		chunkAck = stub.write(ackMsg.getTransactionId(), 1, fileContent);

		// read here 
		List<ReplicaLoc> locations = c.masterStub.read(fileName);
		System.err.println("[@CustomTest] Read1 started ");

		ReplicaLoc replicaLoc = locations.get(0);
		ReplicaServerClient replicaStub = (ReplicaServerClient) registry.lookup("ReplicaClient"+replicaLoc.getId());
		fileContent = replicaStub.read(fileName);
		System.err.println("[@CustomTest] data:");
		System.err.println(new String(fileContent.getData()));


		// continue write 
		for(int i = 2; i < seqN-1; i++){
			System.arraycopy(data, i*Configurations.CHUNK_SIZE, chunk, 0, Configurations.CHUNK_SIZE);
			fileContent = new FileContent(fileName, chunk);
			chunkAck = stub.write(ackMsg.getTransactionId(), i, fileContent);
		}
		// copy the last chuck that might be < CHUNK_SIZE
		System.arraycopy(data, (seqN-1)*Configurations.CHUNK_SIZE, chunk, 0, lastChunkLen);
		fileContent = new FileContent(fileName, chunk);
		chunkAck = stub.write(ackMsg.getTransactionId(), seqN-1, fileContent);

		
		
		//commit
		ReplicaLoc primaryLoc = c.masterStub.locatePrimaryReplica(fileName);
		ReplicaServerClient primaryStub = (ReplicaServerClient) registry.lookup("ReplicaClient"+primaryLoc.getId());
		primaryStub.commit(ackMsg.getTransactionId(), seqN);

		
		// read
		locations = c.masterStub.read(fileName);
		System.err.println("[@CustomTest] Read3 started ");

		replicaLoc = locations.get(0);
		replicaStub = (ReplicaServerClient) registry.lookup("ReplicaClient"+replicaLoc.getId());
		fileContent = replicaStub.read(fileName);
		System.err.println("[@CustomTest] data:");
		System.err.println(new String(fileContent.getData()));

	}

	static Master startMasterServer() throws AccessException, RemoteException{
		Master master = new Master();
		try{
			MasterServerClient stub = (MasterServerClient) UnicastRemoteObject.exportObject(master, 0);
			registry.rebind("MasterServerClientInterface", stub);
			System.err.println("Server ready");
		}
		catch(AccessException a)
		{
			a.printStackTrace();
		}
		catch (RemoteException r)
		{
			r.printStackTrace();
		}

		return master;
	}

	public static void main(String[] args) throws IOException {


		try {
			LocateRegistry.createRegistry(regPort);
			registry = LocateRegistry.getRegistry(regPort);

			Master master = startMasterServer();
			respawnReplicaServers(master);

		} catch (RemoteException   e) {
			System.err.println("Server exception: " + e.toString());
			e.printStackTrace();
		}
	}

}
