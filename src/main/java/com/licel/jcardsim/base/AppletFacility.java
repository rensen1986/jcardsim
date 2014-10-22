package com.licel.jcardsim.base;

import java.lang.reflect.Method;
import java.net.URL;
import java.text.MessageFormat;

import at.fhj.ase.globalplatform.SecurityDomain;
import javacard.framework.AID;
import javacard.framework.Applet;
import javacard.framework.SystemException;

//import com.licel.jcardsim.base.Simulator.AppletClassLoader;
//import com.licel.jcardsim.io.JavaCardInterface;

//import java.net.URLClassLoader;
public class AppletFacility extends AppletClassLoader{
	// default ATR - NXP JCOP 31/36K
    static final String DEFAULT_ATR = "3BFA1800008131FE454A434F5033315632333298";
    // ATR system property name
    static final String ATR_SYSTEM_PROPERTY = "com.licel.jcardsim.card.ATR";
    static final String PROPERTY_PREFIX = "com.licel.jcardsim.card.applet.";
    static final String OLD_PROPERTY_PREFIX = "com.licel.jcardsim.smartcardio.applet.";
    // Applet AID system property template
    static final MessageFormat AID_SP_TEMPLATE = new MessageFormat("{0}.AID");
    // Applet ClassName system property template
    static final MessageFormat APPLET_CLASS_SP_TEMPLATE = new MessageFormat("{0}.Class");
	public static byte[] atr = null;
	protected final AppletClassLoader cl;
	public static Class<? extends Simulator> urls;
	public static void setATR(byte[] newAtr) {
	    atr = newAtr;
	} 

	public AID loadApplet(AID aid, String appletClassName, byte[] appletJarContents)
			throws SystemException {
			    // simple method, but emulate real card login
			    // download data
			    byte[] aidData = new byte[16];
			    aid.getBytes(aidData, (short) 0);
			    Class appletClass = null;
			    try {
			        cl.addAppletContents(appletJarContents);
			        appletClass = cl.loadClass(appletClassName);
			    } catch (Exception e) {
			        SystemException.throwIt(SystemException.ILLEGAL_VALUE);
			
			    }
			    if (appletClass != null) {
			        return loadApplet(aid, appletClass);
			    } else {
			        SystemException.throwIt(SystemException.ILLEGAL_VALUE);
			        return null;
			    }
			}

	public AID loadApplet(AID aid, String appletClassName) throws SystemException {
	    Class<?> appletClass = null;
	    	try {
				appletClass = cl.loadClass(appletClassName);
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    if (checkAppletSuperclass(appletClass)) {
	        return loadApplet(aid, appletClass);
	    }
	    SystemException.throwIt(SystemException.ILLEGAL_VALUE);
	    return null;
	}
	/**
	 * Load
	 * <code>Applet</code> into Simulator
	 *
	 * @param aid applet aid
	 * @param appletClass applet class
	 * @return applet <code>AID</code>
	 * @throws SystemException if <code>appletClass</code> not instanceof
	 * <code>javacard.framework.Applet</code>
	 */
	public AID loadApplet(AID aid, Class<?> appletClass) throws SystemException {
	    if (!checkAppletSuperclass(appletClass)) {
	        SystemException.throwIt(SystemException.ILLEGAL_VALUE);
	    }
	    SimulatorSystem.getRuntime().loadApplet(aid, appletClass);
	    return aid;
	}

	public AID createApplet(AID aid, byte bArray[], short bOffset,
			byte bLength) throws SystemException {
			    try {
			        Class<?> appletClass = SimulatorSystem.getRuntime().getAppletClass(aid);
			        if (appletClass == null) {
			            SystemException.throwIt(SystemException.ILLEGAL_AID);
			        }
			        SimulatorSystem.getRuntime().appletInstalling(aid);
			        Method initMethod = appletClass.getMethod("install",
			                new Class[]{byte[].class, short.class, byte.class});
			        initMethod.invoke(null, new Object[]{bArray, new Short(bOffset), new Byte(bLength)});
			    } catch (Exception ex) {
			        SystemException.throwIt(SimulatorSystem.SW_APPLET_CRATION_FAILED);
			    }
			    return aid;
			}

	/**
	 * Install
	 * <code>Applet</code> into Simulator without installing data
	 *
	 * @param aid applet aid or null
	 * @param appletClass applet class
	 * @return applet <code>AID</code>
	 * @throws SystemException if <code>appletClass</code> not instanceof
	 * <code>javacard.framework.Applet</code>
	 */
	public AID installApplet(AID aid, Class<?> appletClass)
			throws SystemException {
			    return installApplet(aid, appletClass, new byte[]{}, (short) 0, (byte) 0);
			}

	/**
	 * Install
	 * <code>Applet</code> into Simulator. This method is equal to:
	 * <code>
	 * loadApplet(...);
	 * createApplet(...);
	 * </code>
	 *
	 * @param aid applet aid or null
	 * @param appletClass applet class
	 * @param bArray the array containing installation parameters
	 * @param bOffset the starting offset in bArray
	 * @param bLength the length in bytes of the parameter data in bArray
	 * @return applet <code>AID</code>
	 * @throws SystemException if <code>appletClass</code> not instanceof
	 * <code>javacard.framework.Applet</code>
	 */
	public AID installApplet(AID aid, Class<?> appletClass, byte bArray[],
			short bOffset, byte bLength) throws SystemException {
			    loadApplet(aid, appletClass);
			    return createApplet(aid, bArray, bOffset, bLength);
			}

	public AID installApplet(AID aid, String appletClassName, byte bArray[],
			short bOffset, byte bLength) throws SystemException {
			    loadApplet(aid, appletClassName);
			    return createApplet(aid, bArray, bOffset, bLength);
			}

	public AID installApplet(AID aid, String appletClassName, byte[] appletContents,
			byte bArray[], short bOffset, byte bLength) throws SystemException {
			    loadApplet(aid, appletClassName, appletContents);
			    return createApplet(aid, bArray, bOffset, bLength);
			}

	/**
	 * Delete an applet
	 * @param aid applet aid
	 */
	public void deleteApplet(AID aid) {
	    SimulatorSystem.getRuntime().deleteApplet(aid);
	}

	public boolean selectApplet(AID aid) throws SystemException {
		byte[] resp = SimulatorSystem.selectAppletWithResult(aid);
		if(resp != null && resp.length > 1) {
	    	int len = resp.length;
	    	if(resp[len - 2] == (byte)0x90 && resp[len - 1] == 0) {
	    		return true;
	    	}
	    }
	    return false;
	}

	public AppletFacility(URL[] urls) {
		super(urls);
		cl = getClassLoader();
	}

	public static AID getCurrentSelectedAppletAID() {
		return SimulatorSystem.getAID();
	}

	// inspect class hierarchy	
	boolean checkAppletSuperclass(Class appletClass) {
        Class parent = appletClass;
        while (parent != Object.class) {
            if (parent == Applet.class) {
                return true;
            }
            parent = parent.getSuperclass();
        }
        return false;
    }

	public static SecurityDomain getAssociatedSecurityDomain(AID currentSelectedApplet) {
		// TODO Implement me
		return null;
	}
}