import java.io.FileNotFoundException;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.List;
import java.rmi.Remote;

public interface MasterServerClient extends Remote {

	List<ReplicaLoc> read(String fileName) throws
			IOException;

	WriteAck write(String fileName) throws IOException;
	

	ReplicaLoc locatePrimaryReplica(String fileName) throws RemoteException;
}
