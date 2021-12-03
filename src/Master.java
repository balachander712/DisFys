import java.io.FileNotFoundException;
import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeSet;


public class Master implements MasterServerClient, Remote{

	class HeartBeatTask extends TimerTask {

		@Override
		public void run() {
			// check state of replicas
			for (ReplicaLoc replicaLoc : replicaServersLocs) {
				try {
					replicaServersStubs.get(replicaLoc.getId()).isAlive();
				} catch (RemoteException e) {
					replicaLoc.setAlive(false);
					e.printStackTrace();
				}
			}
		}
	}
	
	private int nextTID;
	private int heartBeatRate = Configurations.HEART_BEAT_RATE;
	private int replicationN = Configurations.REPLICATION_FACTOR; // number of file replicas
	private Timer HeartBeatTimer;
	private Random randomGen;

	private Map<String,	 List<ReplicaLoc> > filesLocationMap;
	private Map<String,	 ReplicaLoc> primaryReplicaMap;
	private List<ReplicaLoc> replicaServersLocs;
	private List<ReplicaMaster> replicaServersStubs;


	public Master() {
		filesLocationMap = new HashMap<String, List<ReplicaLoc>>();
		primaryReplicaMap = new HashMap<String, ReplicaLoc>();
		replicaServersLocs = new ArrayList<ReplicaLoc>();
		replicaServersStubs = new ArrayList<ReplicaMaster>();

		nextTID = 0;
		randomGen = new Random();
		
		HeartBeatTimer = new Timer();  //At this line a new Thread will be created
		HeartBeatTimer.scheduleAtFixedRate(new HeartBeatTask(), 0, heartBeatRate); //delay in milliseconds
	}


	private void assignNewMaster(String fileName){
		List<ReplicaLoc> replicas = filesLocationMap.get(fileName);
		boolean newPrimaryAssigned = false;
		for (ReplicaLoc replicaLoc : replicas) {
			if (replicaLoc.isAlive()){
				newPrimaryAssigned = true;
				primaryReplicaMap.put(fileName, replicaLoc);
				try {
					replicaServersStubs.get(replicaLoc.getId()).takeCharge(fileName, filesLocationMap.get(fileName));
				} catch (RemoteException | NotBoundException e) {
					e.printStackTrace();
				}
				break;
			}
		}

		if (!newPrimaryAssigned){
			//TODO a7a ya3ni
		}
	}


	private void createNewFile(String fileName){
		System.out.println("[@Master] Creating new file initiated");
		int luckyReplicas[] = new int[replicationN];
		List<ReplicaLoc> replicas = new ArrayList<ReplicaLoc>();

		Set<Integer> chosenReplicas = new TreeSet<Integer>();

		for (int i = 0; i < luckyReplicas.length; i++) {

			do {
				luckyReplicas[i] = randomGen.nextInt(replicationN);
			} while(!replicaServersLocs.get(luckyReplicas[i]).isAlive() || chosenReplicas.contains(luckyReplicas[i]));


			chosenReplicas.add(luckyReplicas[i]);
			replicas.add(replicaServersLocs.get(luckyReplicas[i]));

			try {
				replicaServersStubs.get(luckyReplicas[i]).createFile(fileName);
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

		int primary = luckyReplicas[0];
		try {
			replicaServersStubs.get(primary).takeCharge(fileName, replicas);
		} catch (RemoteException | NotBoundException e) {
			e.printStackTrace();
		}

		filesLocationMap.put(fileName, replicas);
		primaryReplicaMap.put(fileName, replicaServersLocs.get(primary));

	}

	
	@Override
	public List<ReplicaLoc> read(String fileName) throws FileNotFoundException,
	IOException, RemoteException {
		List<ReplicaLoc> replicaLocs = filesLocationMap.get(fileName);
		if (replicaLocs == null)
			throw new FileNotFoundException();
		return replicaLocs;
	}

	@Override
	public WriteAck write(String fileName) throws RemoteException, IOException {
		System.out.println("[@Master] write request initiated");
		long timeStamp = System.currentTimeMillis();

		List<ReplicaLoc> replicaLocs= filesLocationMap.get(fileName);
		int tid = nextTID++;
		if (replicaLocs == null)	// file not found
			createNewFile(fileName);

		ReplicaLoc primaryReplicaLoc = primaryReplicaMap.get(fileName);

		if (primaryReplicaLoc == null)
			throw new IllegalStateException("No primary replica found");

		if (!primaryReplicaLoc.isAlive()){
			assignNewMaster(fileName);
			primaryReplicaLoc = primaryReplicaMap.get(fileName);
		}

		return new WriteAck(tid, timeStamp,primaryReplicaLoc);
	}

	@Override
	public ReplicaLoc locatePrimaryReplica(String fileName)
			throws RemoteException {
		
		return primaryReplicaMap.get(fileName);
	}
	

	public void registerReplicaServer(ReplicaLoc replicaLoc, ReplicaInterface replicaStub){
		replicaServersLocs.add(replicaLoc);
		replicaServersStubs.add( (ReplicaMasterInterface) replicaStub);
	}


}
