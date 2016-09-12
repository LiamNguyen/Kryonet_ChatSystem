import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import packets.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Main {
    public static void main(String[] args) throws IOException {

        final HashMap<String, Connection> clients = new HashMap<String, Connection>();

        final Server server = new Server();
        server.start();
        server.bind(23110, 23111);

        server.addListener(new Listener() {
            @Override
            public void received(Connection connection, Object object) {
                if (object instanceof Packet) {
                    if (object instanceof PacketConnect) {
                        PacketConnect connectingPacket = (PacketConnect) object;
                        clients.put(connectingPacket.username, connection);
                        PacketClientConnected connectedPacket = new PacketClientConnected();
                        connectedPacket.connectedClientName = connectingPacket.username;
                        server.sendToAllExceptTCP(connection.getID(), connectedPacket);
                        System.out.println("TEsting server");
                    } else if (object instanceof PacketClientDisconnected) {
                        PacketClientDisconnected disconnetedPacket = (PacketClientDisconnected) object;
                        clients.remove(disconnetedPacket.disconnectedClientName);
                    } else if (object instanceof PacketChatDialog) {
                        PacketChatDialog chatPacket = (PacketChatDialog) object;
                        server.sendToAllExceptTCP(connection.getID(), chatPacket);
                    }
                }
            }

            public void disconnected(Connection connection) {
                PacketClientDisconnected disconnectedPacket = new PacketClientDisconnected();
                String disconnetedClientName = "";
                Iterator it = clients.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry pairs = (Map.Entry) it.next();
                    if (pairs.getValue() == connection) {
                        disconnetedClientName = (String) pairs.getKey();
                        break;
                    }
                }

                if (!disconnetedClientName.equalsIgnoreCase("")) {
                    disconnectedPacket.disconnectedClientName = disconnetedClientName;
                    server.sendToAllExceptTCP(connection.getID(), disconnectedPacket);
                }

            }
        });



        server.getKryo().register(Packet.class);
        server.getKryo().register(PacketConnect.class);
        server.getKryo().register(PacketClientConnected.class);
        server.getKryo().register(PacketClientDisconnected.class);
        server.getKryo().register(PacketChatDialog.class);
    }
}
