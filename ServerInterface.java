import java.io.File;
import java.io.IOException;
import java.rmi.Remote;import java.rmi.RemoteException;
/**
 * Interface for server
 */
public interface ServerInterface extends Remote
{    
    String Upload(byte[] filedata, String filename, int bytesRead, boolean overwrite) throws IOException, RemoteException;

    String Delete(String filename) throws IOException, RemoteException;
    File[] filesOnServer() throws RemoteException;
}
