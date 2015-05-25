/*
 * silvertunnel.org Netlib - Java library to easily access anonymity networks
 * Copyright (c) 2009-2012 silvertunnel.org
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see <http://www.gnu.org/licenses/>.
 */

package org.silvertunnel_ng.netlib.layer.tor.api;

import org.silvertunnel_ng.netlib.api.util.TcpipNetAddress;
import org.silvertunnel_ng.netlib.layer.tor.directory.RouterFlags;
import org.silvertunnel_ng.netlib.layer.tor.directory.RouterStatusDescription;
import org.silvertunnel_ng.netlib.util.ConvenientStreamWriter;

import java.io.IOException;
import java.net.InetAddress;
import java.security.interfaces.RSAPublicKey;
import java.util.Set;

/**
 * a compound data structure that keeps track of the static informations we have
 * about a single Tor server.
 *
 * @author hapke
 */
public interface Router {
    String getNickname();

    String getHostname();

    InetAddress getAddress();

    String getCountryCode();

    String toLongString();

    TcpipNetAddress getDirAddress();

    void updateServerStatus(final RouterStatusDescription statusDescription);

    int getOrPort();

    int getSocksPort();

    int getDirPort();

    int getBandwidthAvg();

    int getBandwidthBurst();

    int getBandwidthObserved();

    String getPlatform();

    long getPublished();

    Fingerprint getFingerprint();

    int getUptime();

    RSAPublicKey getOnionKey();

    RSAPublicKey getSigningKey();

    String getContact();

    Set<Fingerprint> getFamily();

    // TODO : implement check for FamilyNames (or convert Family names to fingerprints and add them to the old list)
    Set<String> getFamilyNames();

    long getValidUntil();

    long getLastUpdate();

    boolean isDirv2Authority();

    boolean isDirv2Exit();

    boolean isDirv2Fast();

    boolean isDirv2Guard();

    boolean isDirv2Named();

    boolean isDirv2Stable();

    boolean isDirv2Running();

    boolean isDirv2Valid();

    boolean isValid();

    boolean isDirv2V2dir();

    boolean isDirv2HSDir();

    boolean isExitNode();

    Fingerprint getV3Ident();

    void punishRanking();

    Router cloneReliable();

    boolean exitPolicyAccepts(final InetAddress addr, final int port);

    void save(ConvenientStreamWriter convenientStreamWriter) throws IOException;

    float getRefinedRankingIndex(final float p);

    TcpipNetAddress getOrAddress();

    /**
     * Get the routerflags of a router (stable, valid, fast, etc).
     *
     * @return a {@link RouterFlags} object
     */
    RouterFlags getRouterFlags();

    float getRankingIndex();
}
