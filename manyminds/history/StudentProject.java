package manyminds.history;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

public class StudentProject {

	private SortedMap myEpisodes = new TreeMap();

	private SortedMap myResources = new TreeMap();

	public StudentProject() {
	}

	public void addEpisode(String s, StudentEpisode e) {
		myEpisodes.put(s, e);
		update();
	}

	public void removeEpisode(String s) {
		myEpisodes.remove(s);
		update();
	}

	public Collection listEpisodes() {
		return Collections.unmodifiableSet(myEpisodes.keySet());
	}

	public StudentEpisode getEpisode(String s) {
		return (StudentEpisode) myEpisodes.get(s);
	}

	public void addResource(String s, Resource e) {
		myResources.put(s, e);
		update();
	}

	public void removeResource(String s) {
		myResources.remove(s);
		update();
	}

	public Collection listResources() {
		return Collections.unmodifiableSet(myResources.keySet());
	}

	public Resource getResource(String s) {
		return (Resource) myResources.get(s);
	}

	protected void update() {
	}

	public String toXML() {
		StringBuffer retVal = new StringBuffer(
				"<?xml version=\"1.0\" encoding=\"");
		retVal.append(((new OutputStreamWriter(System.err)).getEncoding()));
		retVal.append("\"?>\n");
		retVal
				.append("<!DOCTYPE project-list PUBLIC \"manyminds-DTD\" \"manyminds.dtd\" >\n");
		retVal.append("<history-project>\n");
		Iterator it = myResources.keySet().iterator();
		while (it.hasNext()) {
			String s = it.next().toString();
			Resource r = (Resource) myResources.get(s);
			if (r instanceof VideoResource) {
				retVal.append("     <resource type=\"video\">");
			} else {
				retVal.append("     <resource type=\"picture\">");
			}
			retVal.append(s);
			retVal.append("</resource>\n");
		}
		it = myEpisodes.keySet().iterator();
		while (it.hasNext()) {
			String s = it.next().toString();
			StudentEpisode r = (StudentEpisode) myEpisodes.get(s);
			retVal.append("     <episode name=\"");
			retVal.append(s);
			retVal.append("\">");
			retVal.append("      <summary>");
			try {
				retVal.append(r.getNote().getText(0, r.getNote().getLength()));
			} catch (Throwable t) {
				t.printStackTrace();
			}
			retVal.append("</summary>\n");
			retVal.append("</episode>\n");
		}
		retVal.append("</history-project>\n");
		return retVal.toString();
	}

	public void saveYourself(File f) throws IOException {
		JarOutputStream out = null;
		try {
			out = new JarOutputStream(new BufferedOutputStream(
					new FileOutputStream(f)));
			JarEntry currentJarEntry = new JarEntry("data.xml");
			out.putNextEntry(currentJarEntry);
			String xml = toXML();
			out.write(xml.getBytes());
			Iterator it = myEpisodes.keySet().iterator();
			while (it.hasNext()) {
				String s = it.next().toString();
				StudentEpisode se = (StudentEpisode) myEpisodes.get(s);
				String seCSV = se.toCSV();
				s = "Episodes/" + s;
				currentJarEntry = new JarEntry(s);
				out.putNextEntry(currentJarEntry);
				out.write(seCSV.getBytes());
			}
			it = myResources.keySet().iterator();
			while (it.hasNext()) {
				String s = it.next().toString();
				byte[] bytes = ((Resource) myResources.get(s)).getData();
				s = "Resources/" + s;
				currentJarEntry = new JarEntry(s);
				out.putNextEntry(currentJarEntry);
				out.write(bytes);
			}
			out.close();
		} catch (IOException ioe) {
			if (out != null) {
				out.close();
			}
			throw ioe;
		}
	}

}