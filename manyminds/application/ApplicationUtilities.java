/* ApplicationUtilities.java
 * Created on Jan 30, 2004
 *  
 * Copyright (C) 1998-2003 Regents of the University of California
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
package manyminds.application;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.prefs.Preferences;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.ProgressMonitorInputStream;

public class ApplicationUtilities {

    private String myPackage = "manyminds";
    private static ApplicationUtilities mySingle = null;
    private Preferences myPrefs;
    
    public static void initializeUtilities(String s) {
        mySingle = new ApplicationUtilities(s);
    }
    
    public static ApplicationUtilities getUtilities() {
        return mySingle;
    }
    
    public ApplicationUtilities(String s) {
        if (s != null) {
            myPackage = s;
        }
        myPrefs = Preferences.userRoot().node(myPackage);
    }
    
    public File getApplicationRoot() {
        String s = myPrefs.get("applicationRoot", "");
        if (!"".equals(s)) {
            return new File(s);
        } else {
            return null;
        }
    }
    
    public void createApplicationRoot(String rootName, boolean prompt) {
        File f = new File(System.getProperty("user.home"));
        if (prompt) {
            JOptionPane.showMessageDialog(null, "Please select a location for data storage.");
            JFileChooser chooser = new JFileChooser(f);
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            while (chooser.showOpenDialog(null) != JFileChooser.APPROVE_OPTION);
            f = chooser.getSelectedFile();
        }
        String appRootString = f.getAbsolutePath()+"/"+"rootName";
        setPreference("applicationRoot",appRootString);
        initializeApplicationFolder(null);
    }
    
    public String getPreference(String s) {
        return myPrefs.get(s,null);
    }

    public void setPreference(String k, String v) {
        myPrefs.put(k, v);
    }
    
    public void initializeApplicationFolder(String folder) {
        File f = getApplicationRoot();
        if (folder != null) {
            f = new File(f,folder);
        }
        if (!f.exists()) {
            f.mkdirs();
        }
    }
    
    public URL resolveApplicationResource(String resourcePath) throws MalformedURLException {
        if (resourcePath.startsWith("classpath://")) {
            resourcePath = resourcePath.substring(12);
            return getClass().getClassLoader().getResource(resourcePath);
        } else {
            return new URL(resourcePath);
        }
    }
    
    public void loadPlugin(URL plugin) {
        URL[] params = {plugin};
        URLClassLoader.newInstance(params);
    }
    
    public void initializeApplicationFile(String folder, URL resource, boolean overwrite) throws IOException {
        File f = getApplicationRoot();
        if (folder != null) {
            f = new File(f,folder);
        }
        if (!f.exists() || overwrite) {
            BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(f));
            BufferedInputStream in = new BufferedInputStream(new ProgressMonitorInputStream(null,"Loading "+resource,resource.openStream()));
            byte[] b = new byte[512];
            int len;
            while ((len = in.read(b, 0, b.length)) != -1) {
                out.write(b, 0, len);
            }
            try {
                out.close();
                in.close();
            } catch (Throwable t) {}
        }
    }
    
}
