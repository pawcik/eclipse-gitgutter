package com.jedruch.eclipse.gitgutter;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.jedruch.eclipse.gitgutter.ui.EditorTracker;

/**
 * The activator class controls the plug-in life cycle
 */
public class GitGutterPlugin extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "com.jedruch.eclipse.gitgutter"; //$NON-NLS-1$

	// The shared instance
	private static GitGutterPlugin plugin;

	private EditorTracker editorTracker;

	/**
	 * The constructor
	 */
	public GitGutterPlugin() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext
	 * )
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		editorTracker = new EditorTracker(getWorkbench());
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext
	 * )
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		editorTracker.dispose();
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static GitGutterPlugin getDefault() {
		return plugin;
	}

}
