package xyz.angm.game.network;

import com.badlogic.gdx.Gdx;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.util.Enumeration;

/** Searches the local network on all network interfaces for a {@link ServerDiscoveryRunnable}.
 * It will wait for a response indefinitely, so it should be stopped after a few seconds. */
class ClientDiscoveryRunnable implements Runnable {

    private InetAddress address;

    /** @return The address of the server discovery daemon, if any responded. */
    InetAddress getAddress() {
        return address;
    }

    @Override
    public void run() {
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setBroadcast(true);
            byte[] sendData = "DISCOVER_GAME_REQUEST".getBytes();

            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName("255.255.255.255"), Client.PORT);
            socket.send(sendPacket);

            // Broadcast the message over all network interfaces
            Enumeration<java.net.NetworkInterface> interfaces = java.net.NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                java.net.NetworkInterface networkInterface = interfaces.nextElement();
                if (networkInterface.isLoopback() || !networkInterface.isUp()) continue;

                for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
                    InetAddress broadcast = interfaceAddress.getBroadcast();
                    if (broadcast == null) continue;

                    sendPacket = new DatagramPacket(sendData, sendData.length, broadcast, Client.PORT);
                    socket.send(sendPacket);
                }
            }

            // Wait for a response
            byte[] recvBuf = new byte[15000];
            DatagramPacket receivePacket = new DatagramPacket(recvBuf, recvBuf.length);
            socket.receive(receivePacket);

            // Check if the message is correct
            String message = new String(receivePacket.getData()).trim();
            if (message.equals("DISCOVER_GAME_RESPONSE")) address = receivePacket.getAddress();
        } catch (Exception e) {
            Gdx.app.error("Client", "Could not search for servers. Reporting none found.");
        }
    }
}
