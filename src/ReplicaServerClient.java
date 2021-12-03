import java.io.IOException;
import java.rmi.RemoteException;


public interface ReplicaServerClientInterface extends ReplicaInterface {

	FileContent read(String fileName)
			throws IOException;

	ChunkAck write(long txnID, long msgSeqNum, FileContent data)
			throws IOException;

	boolean commit(long txnID, long numOfMsgs)
			throws MessageNotFoundException, IOException;

	boolean abort(long txnID) throws RemoteException;
}
