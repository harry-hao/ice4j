/*
 * ice4j, the OpenSource Java Solution for NAT and Firewall Traversal. Copyright @ 2015 Atlassian Pty Ltd Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or
 * agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under the License.
 */
package org.ice4j.socket;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.List;

import org.ice4j.socket.filter.DatagramPacketFilter;

/**
 * Represents a DatagramSocket which receives DatagramPackets
 * selected by a DatagramPacketFilter from a
 * MultiplexingDatagramSocket. The associated
 * MultiplexingDatagramSocket is the actual DatagramSocket
 * which reads the DatagramPackets from the network. The
 * DatagramPackets received through the
 * MultiplexedDatagramSocket will not be received through the
 * associated MultiplexingDatagramSocket.
 *
 * @author Lyubomir Marinov
 */
public class MultiplexedDatagramSocket implements MultiplexedXXXSocket {
    /**
     * The DatagramPacketFilter which determines which
     * DatagramPackets read from the network by {@link #multiplexing}
     * are to be received through this instance.
     */
    private final DatagramPacketFilter filter;

    /**
     * The MultiplexingDatagramSocket which does the actual reading
     * from the network and which forwards DatagramPackets accepted by
     * {@link #filter} for receipt to this instance.
     */
    private final MultiplexingDatagramSocket multiplexing;

    /**
     * The list of DatagramPackets to be received through this
     * DatagramSocket i.e. accepted by {@link #filter}.
     */
    final List<DatagramPacket> received = new SocketReceiveBuffer() {
        private static final long serialVersionUID = -5763976093759762087L;

        @Override
        public int getReceiveBufferSize() throws SocketException {
            return MultiplexedDatagramSocket.this.getReceiveBufferSize();
        }
    };

    /**
     * Initializes a new MultiplexedDatagramSocket which is unbound and
     * filters DatagramPackets away from a specific
     * MultiplexingDatagramSocket using a specific
     * DatagramPacketFilter.
     *
     * @param multiplexing the MultiplexingDatagramSocket which does
     * the actual reading from the network and which forwards
     * DatagramPackets accepted by the specified filter to the
     * new instance
     * @param filter the DatagramPacketFilter which determines which
     * DatagramPackets read from the network by the specified
     * multiplexing are to be received through the new instance
     * @throws SocketException if the socket could not be opened
     */
    MultiplexedDatagramSocket(MultiplexingDatagramSocket multiplexing, DatagramPacketFilter filter) throws SocketException {
        /*
         * Even if MultiplexingDatagramSocket allows MultiplexedDatagramSocket to perform bind, binding in the super will not execute correctly this early in the construction
         * because the multiplexing field is not set yet. That is why MultiplexedDatagramSocket does not currently support bind at construction time.
         */
        if (multiplexing == null){
            throw new NullPointerException("multiplexing");
        }
        this.multiplexing = multiplexing;
        this.filter = filter;
    }

    /**
     * Closes this datagram socket.
     * <p>
     * Any thread currently blocked in {@link #receive(DatagramPacket)} upon
     * this socket will throw a {@link SocketException}.
     * </p>
     *
     * @see DatagramSocket#close()
     */
    public void close() {
        multiplexing.close(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DatagramPacketFilter getFilter() {
        return filter;
    }

    /**
     * Receives a datagram packet from this socket. When this method returns,
     * the DatagramPacket's buffer is filled with the data received.
     * The datagram packet also contains the sender's IP address, and the port
     * number on the sender's machine.
     * <p>
     * This method blocks until a datagram is received. The length
     * field of the datagram packet object contains the length of the received
     * message. If the message is longer than the packet's length, the message
     * is truncated.
     * </p>
     * <p>
     * If there is a security manager, a packet cannot be received if the
     * security manager's checkAccept method does not allow it.
     * </p>
     *
     * @param p the DatagramPacket into which to place the incoming
     * data
     * @throws IOException if an I/O error occurs
     * @see DatagramSocket#receive(DatagramPacket)
     */
    public void receive(DatagramPacket p) throws IOException {
        multiplexing.receive(this, p);
    }
}
