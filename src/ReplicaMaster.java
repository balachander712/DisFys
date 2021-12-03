import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.List;
import java.rmi.Remote;


public interface ReplicaMasterInterface extends ReplicaInterface{
	

	void createFile(String fileName) throws IOException;
	

	void takeCharge(String fileName, List<ReplicaLoc> slaveReplicas) throws RemoteException, NotBoundException ;

	boolean isAlive() throws RemoteException;
	
}
