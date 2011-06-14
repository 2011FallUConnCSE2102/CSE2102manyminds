/*  Copyright (C) 1998-2002 Regents of the University of California
 *  This file is part of ManyMinds.
 *
 *  ManyMinds is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  
 *  ManyMinds is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License
 *  along with ManyMinds; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
 package manyminds.datamodel;

public interface
RemoteDataServer
extends java.rmi.Remote {
    public void upstreamConnect(RemoteDataServer rds) throws java.rmi.RemoteException;
    public void downstreamConnect(RemoteDataServer rds) throws java.rmi.RemoteException;
    public void downstreamDisconnect(RemoteDataServer rds) throws java.rmi.RemoteException;
    public void peerRemoveData(String tag) throws java.rmi.RemoteException;
    public void peerAddData(String tag, RemoteData rdp) throws java.rmi.RemoteException;
    public RemoteData peerGetData(String tag) throws java.rmi.RemoteException;
    public String[] listEntries() throws java.rmi.RemoteException;
    public String[] peerListResources() throws java.rmi.RemoteException;
    public void peerAddResource(String s, byte[] data) throws java.rmi.RemoteException;
    public void peerRemoveResource(String tag) throws java.rmi.RemoteException;
    public byte[] peerGetResource(String s) throws java.rmi.RemoteException;
}