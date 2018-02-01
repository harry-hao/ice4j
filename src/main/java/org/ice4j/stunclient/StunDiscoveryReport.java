/*
 * ice4j, the OpenSource Java Solution for NAT and Firewall Traversal. Copyright @ 2015 Atlassian Pty Ltd Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or
 * agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under the License.
 */
package org.ice4j.stunclient;

import org.ice4j.*;

/**
 * The class is used to deliver results from a STUN Discovery Process. It contains information about the NAT Server (or firewall) this client is behind,
 * and a mapped address value (if discovered)
 *
 * @author Emil Ivov
 */

public class StunDiscoveryReport {
    /**
     * Indicates that NAT detection has failed or not yet initiated.
     */
    public static final String UNKNOWN = "Unknown Network Configuration";

    /**
     * Means, there's no NAT or firewall.
     */
    public static final String OPEN_INTERNET = "Open Internet Configuration";

    /**
     * Indicates that UDP communication is not possible.
     */
    public static final String UDP_BLOCKING_FIREWALL = "UDP Blocking Firewall";

    /**
     * Means we are behind a symmetric udp firewall.
     */
    public static final String SYMMETRIC_UDP_FIREWALL = "Symmetric UDP Firewall";

    /**
     * NAT type is full cone.
     */
    public static final String FULL_CONE_NAT = "Full Cone NAT";

    /**
     * We are behind a symmetric nat.
     */
    public static final String SYMMETRIC_NAT = "Symmetric NAT";

    /**
     * NAT type is Restricted Cone.
     */
    public static final String RESTRICTED_CONE_NAT = "Restricted Cone NAT";

    /**
     * NAT type is port restricted cone.
     */
    public static final String PORT_RESTRICTED_CONE_NAT = "Port Restricted Cone NAT";

    private String natType = UNKNOWN;

    private TransportAddress publicAddress;

    /**
     * Creates a discovery report with natType = UNKNOWN and a null public
     * address.
     */
    StunDiscoveryReport() {
    }

    /**
     * Returns the type of the NAT described in the report.
     * @return the type of the NAT that this report is about.
     */
    public String getNatType() {
        return natType;
    }

    /**
     * Sets the type of the NAT indicated by the report.
     * @param natType the type of the NAT.
     */
    void setNatType(String natType) {
        this.natType = natType;
    }

    /**
     * Returns the public addressed discovered by a discovery process.
     * @return an internet address for public use.
     */
    public TransportAddress getPublicAddress() {
        return publicAddress;
    }

    /**
     * Sets a public address.
     * @param stunAddress An address that's accessible from everywhere.
     */
    void setPublicAddress(TransportAddress stunAddress) {
        this.publicAddress = stunAddress;
    }

    /**
     * Compares this object with obj. Two reports are considered equal if and
     * only if both have the same nat type and their public addresses are
     * equal or are both null.
     * @param obj the object to compare against.
     * @return true if the two objects are equal and false otherwise.
     */
    public boolean equals(Object obj) {
        if (!(obj instanceof StunDiscoveryReport))
            return false;

        if (obj == this)
            return true;

        StunDiscoveryReport target = (StunDiscoveryReport) obj;

        return (target.getNatType() == getNatType() && (getPublicAddress() == null && target.getPublicAddress() == null || target.getPublicAddress().equals(getPublicAddress())));
    }

    /**
     * Returns a readable representation of the report.
     * @return a readable representation of the report.
     */
    public String toString() {
        return "The detected network configuration is: " + getNatType() + "\n" + "Your mapped public address is: " + getPublicAddress();
    }

}
