import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;


public interface ReplicaReplica extends ReplicaInterface {
	
	boolean reflectUpdate(long txnID, String fileName, ArrayList<byte[]> data) throws RemoteException, IOException;
	
	void releaseLock(String fileName)throws RemoteException;
}
