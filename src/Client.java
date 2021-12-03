import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;


public class Client {


	MasterServerClientInterface masterStub;
	static Registry registry;
	int regPort = Configurations.REG_PORT;
	String regAddr = Configurations.REG_ADDR;
	int chunkSize = Configurations.CHUNK_SIZE; // in bytes 
	
	public Client() {
		try {
			registry = LocateRegistry.getRegistry(regAddr, regPort);
			masterStub =  (MasterServerClient) registry.lookup("MasterServerClientInterface");
			System.out.println("[@client] Master Stub fetched successfuly");
		} catch (RemoteException | NotBoundException e) {
			// fatal error .. no registry could be linked
			e.printStackTrace();
		}
	}

	public byte[] read(String fileName) throws IOException, NotBoundException{
		List<ReplicaLoc> locations = masterStub.read(fileName);
		System.out.println("[@client] Master Granted read operation");
		
		ReplicaLoc replicaLoc = locations.get(0);

		ReplicaServerClient replicaStub = (ReplicaServerClient) registry.lookup("ReplicaClient"+replicaLoc.getId());
		FileContent fileContent = replicaStub.read(fileName);
		System.out.println("[@client] read operation completed successfuly");
		System.out.println("[@client] data:");
		
		System.out.println(new String(fileContent.getData()));
		return fileContent.getData();
	}


	
	public void write (String fileName, byte[] data) throws IOException, NotBoundException, MessageNotFoundException{
		WriteAck ackMsg = masterStub.write(fileName);
		ReplicaServerClient replicaStub = (ReplicaServerClient) registry.lookup("ReplicaClient"+ackMsg.getLoc().getId());
		
		System.out.println("[@client] Master granted write operation");
		
		int segN = (int) Math.ceil(1.0*data.length/chunkSize);
		FileContent fileContent = new FileContent(fileName);
		ChunkAck chunkAck;
		byte[] chunk = new byte[chunkSize];
		
		for (int i = 0; i < segN-1; i++) {
			System.arraycopy(data, i*chunkSize, chunk, 0, chunkSize);
			fileContent.setData(chunk);
			do { 
				chunkAck = replicaStub.write(ackMsg.getTransactionId(), i, fileContent);
			} while(chunkAck.getSeqNo() != i);
		}

		// Handling last chunk of the file < chunk size
		int lastChunkLen = chunkSize;
		if (data.length%chunkSize > 0)
			lastChunkLen = data.length%chunkSize; 
		chunk = new byte[lastChunkLen];
		System.arraycopy(data, segN-1, chunk, 0, lastChunkLen);
		fileContent.setData(chunk);
		do { 
			chunkAck = replicaStub.write(ackMsg.getTransactionId(), segN-1, fileContent);
		} while(chunkAck.getSeqNo() != segN-1 );
		
		
		System.out.println("[@client] write operation complete");
		replicaStub.commit(ackMsg.getTransactionId(), segN);
		System.out.println("[@client] commit operation complete");
	}
	
	public void commit(String fileName, long txnID, long seqN) throws MessageNotFoundException, IOException, NotBoundException{
		ReplicaLoc primaryLoc = masterStub.locatePrimaryReplica(fileName);
		ReplicaServerClient primaryStub = (ReplicaServerClient) registry.lookup("ReplicaClient"+primaryLoc.getId());
		primaryStub.commit(txnID, seqN);
		System.out.println("[@client] commit operation complete");
	}

	
}
