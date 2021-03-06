// Stub class generated by rmic, do not edit.
// Contents subject to change without notice.

package manyminds.datamodel;

public final class DataServer_Stub
    extends java.rmi.server.RemoteStub
    implements manyminds.datamodel.RemoteDataServer, java.rmi.Remote
{
    private static final long serialVersionUID = 2;
    
    private static java.lang.reflect.Method $method_downstreamConnect_0;
    private static java.lang.reflect.Method $method_downstreamDisconnect_1;
    private static java.lang.reflect.Method $method_listEntries_2;
    private static java.lang.reflect.Method $method_peerAddData_3;
    private static java.lang.reflect.Method $method_peerAddResource_4;
    private static java.lang.reflect.Method $method_peerGetData_5;
    private static java.lang.reflect.Method $method_peerGetResource_6;
    private static java.lang.reflect.Method $method_peerListResources_7;
    private static java.lang.reflect.Method $method_peerRemoveData_8;
    private static java.lang.reflect.Method $method_peerRemoveResource_9;
    private static java.lang.reflect.Method $method_upstreamConnect_10;
    
    static {
	try {
	    $method_downstreamConnect_0 = manyminds.datamodel.RemoteDataServer.class.getMethod("downstreamConnect", new java.lang.Class[] {manyminds.datamodel.RemoteDataServer.class});
	    $method_downstreamDisconnect_1 = manyminds.datamodel.RemoteDataServer.class.getMethod("downstreamDisconnect", new java.lang.Class[] {manyminds.datamodel.RemoteDataServer.class});
	    $method_listEntries_2 = manyminds.datamodel.RemoteDataServer.class.getMethod("listEntries", new java.lang.Class[] {});
	    $method_peerAddData_3 = manyminds.datamodel.RemoteDataServer.class.getMethod("peerAddData", new java.lang.Class[] {java.lang.String.class, manyminds.datamodel.RemoteData.class});
	    $method_peerAddResource_4 = manyminds.datamodel.RemoteDataServer.class.getMethod("peerAddResource", new java.lang.Class[] {java.lang.String.class, byte[].class});
	    $method_peerGetData_5 = manyminds.datamodel.RemoteDataServer.class.getMethod("peerGetData", new java.lang.Class[] {java.lang.String.class});
	    $method_peerGetResource_6 = manyminds.datamodel.RemoteDataServer.class.getMethod("peerGetResource", new java.lang.Class[] {java.lang.String.class});
	    $method_peerListResources_7 = manyminds.datamodel.RemoteDataServer.class.getMethod("peerListResources", new java.lang.Class[] {});
	    $method_peerRemoveData_8 = manyminds.datamodel.RemoteDataServer.class.getMethod("peerRemoveData", new java.lang.Class[] {java.lang.String.class});
	    $method_peerRemoveResource_9 = manyminds.datamodel.RemoteDataServer.class.getMethod("peerRemoveResource", new java.lang.Class[] {java.lang.String.class});
	    $method_upstreamConnect_10 = manyminds.datamodel.RemoteDataServer.class.getMethod("upstreamConnect", new java.lang.Class[] {manyminds.datamodel.RemoteDataServer.class});
	} catch (java.lang.NoSuchMethodException e) {
	    throw new java.lang.NoSuchMethodError(
		"stub class initialization failed");
	}
    }
    
    // constructors
    public DataServer_Stub(java.rmi.server.RemoteRef ref) {
	super(ref);
    }
    
    // methods from remote interfaces
    
    // implementation of downstreamConnect(RemoteDataServer)
    public void downstreamConnect(manyminds.datamodel.RemoteDataServer $param_RemoteDataServer_1)
	throws java.rmi.RemoteException
    {
	try {
	    ref.invoke(this, $method_downstreamConnect_0, new java.lang.Object[] {$param_RemoteDataServer_1}, -8230633720480406744L);
	} catch (java.lang.RuntimeException e) {
	    throw e;
	} catch (java.rmi.RemoteException e) {
	    throw e;
	} catch (java.lang.Exception e) {
	    throw new java.rmi.UnexpectedException("undeclared checked exception", e);
	}
    }
    
    // implementation of downstreamDisconnect(RemoteDataServer)
    public void downstreamDisconnect(manyminds.datamodel.RemoteDataServer $param_RemoteDataServer_1)
	throws java.rmi.RemoteException
    {
	try {
	    ref.invoke(this, $method_downstreamDisconnect_1, new java.lang.Object[] {$param_RemoteDataServer_1}, -412244271420707667L);
	} catch (java.lang.RuntimeException e) {
	    throw e;
	} catch (java.rmi.RemoteException e) {
	    throw e;
	} catch (java.lang.Exception e) {
	    throw new java.rmi.UnexpectedException("undeclared checked exception", e);
	}
    }
    
    // implementation of listEntries()
    public java.lang.String[] listEntries()
	throws java.rmi.RemoteException
    {
	try {
	    Object $result = ref.invoke(this, $method_listEntries_2, null, 6914855471000258262L);
	    return ((java.lang.String[]) $result);
	} catch (java.lang.RuntimeException e) {
	    throw e;
	} catch (java.rmi.RemoteException e) {
	    throw e;
	} catch (java.lang.Exception e) {
	    throw new java.rmi.UnexpectedException("undeclared checked exception", e);
	}
    }
    
    // implementation of peerAddData(String, RemoteData)
    public void peerAddData(java.lang.String $param_String_1, manyminds.datamodel.RemoteData $param_RemoteData_2)
	throws java.rmi.RemoteException
    {
	try {
	    ref.invoke(this, $method_peerAddData_3, new java.lang.Object[] {$param_String_1, $param_RemoteData_2}, 2796971433640894112L);
	} catch (java.lang.RuntimeException e) {
	    throw e;
	} catch (java.rmi.RemoteException e) {
	    throw e;
	} catch (java.lang.Exception e) {
	    throw new java.rmi.UnexpectedException("undeclared checked exception", e);
	}
    }
    
    // implementation of peerAddResource(String, byte[])
    public void peerAddResource(java.lang.String $param_String_1, byte[] $param_arrayOf_byte_2)
	throws java.rmi.RemoteException
    {
	try {
	    ref.invoke(this, $method_peerAddResource_4, new java.lang.Object[] {$param_String_1, $param_arrayOf_byte_2}, 3117996055039128334L);
	} catch (java.lang.RuntimeException e) {
	    throw e;
	} catch (java.rmi.RemoteException e) {
	    throw e;
	} catch (java.lang.Exception e) {
	    throw new java.rmi.UnexpectedException("undeclared checked exception", e);
	}
    }
    
    // implementation of peerGetData(String)
    public manyminds.datamodel.RemoteData peerGetData(java.lang.String $param_String_1)
	throws java.rmi.RemoteException
    {
	try {
	    Object $result = ref.invoke(this, $method_peerGetData_5, new java.lang.Object[] {$param_String_1}, 3433331470923572926L);
	    return ((manyminds.datamodel.RemoteData) $result);
	} catch (java.lang.RuntimeException e) {
	    throw e;
	} catch (java.rmi.RemoteException e) {
	    throw e;
	} catch (java.lang.Exception e) {
	    throw new java.rmi.UnexpectedException("undeclared checked exception", e);
	}
    }
    
    // implementation of peerGetResource(String)
    public byte[] peerGetResource(java.lang.String $param_String_1)
	throws java.rmi.RemoteException
    {
	try {
	    Object $result = ref.invoke(this, $method_peerGetResource_6, new java.lang.Object[] {$param_String_1}, 59768001229252361L);
	    return ((byte[]) $result);
	} catch (java.lang.RuntimeException e) {
	    throw e;
	} catch (java.rmi.RemoteException e) {
	    throw e;
	} catch (java.lang.Exception e) {
	    throw new java.rmi.UnexpectedException("undeclared checked exception", e);
	}
    }
    
    // implementation of peerListResources()
    public java.lang.String[] peerListResources()
	throws java.rmi.RemoteException
    {
	try {
	    Object $result = ref.invoke(this, $method_peerListResources_7, null, 6304233542051681520L);
	    return ((java.lang.String[]) $result);
	} catch (java.lang.RuntimeException e) {
	    throw e;
	} catch (java.rmi.RemoteException e) {
	    throw e;
	} catch (java.lang.Exception e) {
	    throw new java.rmi.UnexpectedException("undeclared checked exception", e);
	}
    }
    
    // implementation of peerRemoveData(String)
    public void peerRemoveData(java.lang.String $param_String_1)
	throws java.rmi.RemoteException
    {
	try {
	    ref.invoke(this, $method_peerRemoveData_8, new java.lang.Object[] {$param_String_1}, 6431421026153312239L);
	} catch (java.lang.RuntimeException e) {
	    throw e;
	} catch (java.rmi.RemoteException e) {
	    throw e;
	} catch (java.lang.Exception e) {
	    throw new java.rmi.UnexpectedException("undeclared checked exception", e);
	}
    }
    
    // implementation of peerRemoveResource(String)
    public void peerRemoveResource(java.lang.String $param_String_1)
	throws java.rmi.RemoteException
    {
	try {
	    ref.invoke(this, $method_peerRemoveResource_9, new java.lang.Object[] {$param_String_1}, 3820683310850410014L);
	} catch (java.lang.RuntimeException e) {
	    throw e;
	} catch (java.rmi.RemoteException e) {
	    throw e;
	} catch (java.lang.Exception e) {
	    throw new java.rmi.UnexpectedException("undeclared checked exception", e);
	}
    }
    
    // implementation of upstreamConnect(RemoteDataServer)
    public void upstreamConnect(manyminds.datamodel.RemoteDataServer $param_RemoteDataServer_1)
	throws java.rmi.RemoteException
    {
	try {
	    ref.invoke(this, $method_upstreamConnect_10, new java.lang.Object[] {$param_RemoteDataServer_1}, -4251829066798565930L);
	} catch (java.lang.RuntimeException e) {
	    throw e;
	} catch (java.rmi.RemoteException e) {
	    throw e;
	} catch (java.lang.Exception e) {
	    throw new java.rmi.UnexpectedException("undeclared checked exception", e);
	}
    }
}
