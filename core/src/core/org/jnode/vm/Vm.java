/*
 * $Id$
 */
package org.jnode.vm;

import java.io.PrintStream;

import org.jnode.vm.classmgr.VmStatics;
import org.jnode.vm.compiler.HotMethodManager;
import org.jnode.vm.memmgr.VmHeapManager;

/**
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class Vm extends VmSystemObject {

	/** The single instance */
	private static Vm instance;
	/** Are will in bootimage building phase? */
	private transient boolean bootstrap;
	/** The current architecture */
	private final VmArchitecture arch;
	/** The heap manager */
	private final VmHeapManager heapManager;
	/** The hot method manager */
	private HotMethodManager hotMethodManager;
	/** Set this boolean to turn the hot method manager on/off */
	private final boolean runHotMethodManager = false;

	/**
	 * Initialize a new instance
	 * 
	 * @param arch
	 */
	public Vm(VmArchitecture arch, VmHeapManager heapManager) {
		instance = this;
		this.bootstrap = true;
		this.arch = arch;
		this.heapManager = heapManager;
	}

	/**
	 * @return Returns the bootstrap.
	 */
	public final boolean isBootstrap() {
		return this.bootstrap;
	}

	/**
	 * @return Returns the arch.
	 */
	public final VmArchitecture getArch() {
		return this.arch;
	}

	/**
	 * @return Returns the instance.
	 */
	public static final Vm getVm() {
		return instance;
	}

	/**
	 * @return Returns the heapManager.
	 */
	public final VmHeapManager getHeapManager() {
		return this.heapManager;
	}

	/**
	 * Start the hot method compiler.
	 *  
	 */
	final void startHotMethodManager() {
		if (runHotMethodManager) {
			final VmStatics statics = Unsafe.getCurrentProcessor().getStatics();
			this.hotMethodManager = new HotMethodManager(arch, statics);
			hotMethodManager.start();
		}
	}

	/**
	 * Show VM info.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		final Vm vm = getVm();
		if ((vm != null) && !vm.isBootstrap()) {
			final PrintStream out = System.out;
			Unsafe.getCurrentProcessor().getStatics().dumpStatistics(out);
			if (vm.hotMethodManager != null) {
				vm.hotMethodManager.dumpStatistics(out);
			}
			vm.heapManager.dumpStatistics(out);
		}
	}
}
