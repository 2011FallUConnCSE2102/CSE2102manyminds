package manyminds.datamodel;

import java.io.File;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.TimerTask;

import javax.swing.DefaultListModel;
import javax.swing.ListModel;

/**
 * @author eric
 *
 * Utility class for finding other ManyMinds instances in the local network (using a broadcast datagram)
 * and downloading interesting files from those instances.
 *
 */
public class
DataCollector {

    private DatagramSocket broadcastSocket;
    private File myRoot;
    private HashMap myCollectors;
    private boolean catchingReplies = true;
    private ReplyCatcher myReplyCatcher = new ReplyCatcher();
    private PeepSweeper myPeepSweeper = new PeepSweeper();
    private DefaultListModel myListModel = new DefaultListModel();
    private byte[] joinMessage;
    
    /**
     * @author eric
     * Pings the local network, all ManyMinds instances will be listening for this ping.
     */
    public class
    PeepSweeper
    extends TimerTask {
        public void
        run() {
            try {
                InetAddress group = InetAddress.getByName("230.0.0.1");
                DatagramPacket packet = new DatagramPacket(joinMessage, joinMessage.length, group, 4442);
                broadcastSocket.send(packet);
              } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }

    /**
     * @author eric
     * This thread sits and listens on the reply port.  When a ManyMinds gets a ping from the PeepSweeper
     * it will send its contact info to this machine, port 4443.  This will add its info to the list of 
     * found ManyMinds systems.
     */
    public class
    ReplyCatcher
    extends Thread {
        public void
        run() {
            while (catchingReplies) {
                try {
                    byte[] buf = new byte[1024];
                    DatagramPacket packet = new DatagramPacket(buf, buf.length);
                    broadcastSocket.receive(packet);
                    String reply = new String(packet.getData());
                    System.err.println(reply);
                    if (reply.startsWith("ManyMinds-DataServerDispatch")) {
                        String foreignName = reply.substring("ManyMinds-DataServerDispatch".length(),reply.indexOf(":")).trim();
                        if (foreignName.indexOf(".") >= 0) {
                            foreignName = foreignName.substring(0,foreignName.indexOf("."));
                        }
                        String foreignAddress = reply.substring(reply.indexOf(":")+1).trim();
                        addRemoteManyMinds(foreignName, foreignAddress);
                    }
                } catch (Throwable ie) {
                }
            }
        }
    }
    
    
    
    /**
     * Usually only called from within the ReplyCatcher, but anybody can add a remote ManyMinds if they really
     * want to.
     * 
     * @param name The logical name of the ManyMinds (computer name, whatever)
     * @param address The IP address (dotted quad) in base 10 of the remote ManyMinds.
     */
    public void
    addRemoteManyMinds(String name, String address) {
        if (!myCollectors.containsKey(address)) {
            myCollectors.put(address, new DataCollectorThread(name, address, myRoot));
            myListModel.addElement(myCollectors.get(address));
        }
    }
    
    
    
    /**
     * Use this method to get an updating list of the remote ManyMinds systems found by the PeepSweeper
     * @return A ListModel, each elemot of which is a DataCollectorThread.
     */
    public ListModel
    getListModel() {
        return myListModel;
    }
    
    
    
    /**
     * Empty out the list of found remote ManyMinds 
     */
    public void
    clear() {
        myCollectors.clear();
        myListModel.clear();
    }
    
    
    
    /**
     * 
     * @param root The folder into which you want to put downloaded data.
     */
    public
    DataCollector(File root) {
        try {
            joinMessage = InetAddress.getLocalHost().getHostAddress().getBytes();
            broadcastSocket = new DatagramSocket(4443);
            myCollectors = new HashMap();
            myRoot = root;
            myReplyCatcher.start();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
    
    
    
    /**
     * Starts up the whole deal.
     */
    public void
    startSweep() {
        myPeepSweeper.run();
    }
}