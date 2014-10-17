package com.licel.jcardsim.base;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;

class AppletClassLoader extends URLClassLoader {

     AppletClassLoader(URL[] urls) {
        super(urls, Simulator.class.getClassLoader());
    }

    void addAppletContents(byte[] appletJarContents) throws IOException {
        File downloadedAppletJar = File.createTempFile("applet", "contents");
        downloadedAppletJar.deleteOnExit();
        FileOutputStream fos = new FileOutputStream(downloadedAppletJar);
        fos.write(appletJarContents);
        fos.close();
        addURL(downloadedAppletJar.toURI().toURL());

    }
    public AppletClassLoader getClassLoader() {
		return this;
	}
}