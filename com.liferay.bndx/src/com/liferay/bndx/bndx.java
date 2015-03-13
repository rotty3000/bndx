package com.liferay.bndx;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import aQute.bnd.osgi.Jar;
import aQute.lib.consoleapp.AbstractConsoleApp;
import aQute.lib.getopt.Arguments;
import aQute.lib.getopt.Description;
import aQute.lib.getopt.Options;

import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;

/**
 * @author Gregory Amerson
 */
public class bndx extends AbstractConsoleApp {

	private final String FRAMEWORK = ":type=framework,*";
	private final String LOCAL_CONNECTOR_ADDRESS =
		"com.sun.management.jmxremote.localConnectorAddress";

	public bndx() throws UnsupportedEncodingException {
		super();
	}

	public bndx(Object target) throws UnsupportedEncodingException {
		super(target);
	}

	public static void main(String[] args) throws Exception {
		final bndx bndx = new bndx();
		bndx.run(args);
	}

	@Description("Deploys a bundle into OSGi framework over JMX")
	@Arguments(arg = "<bundle-path>")
	interface deployOptions extends Options {
		@Description("A regex pattern to match the id of the OSGi framework "
			+ "in JMX registry")
		String id();

		@Description("The JMX port to connect to the OSGi framework")
		int port();
	}

	@Description("Deploys a bundle into OSGi framework over JMX")
	public void _deploy(deployOptions options) throws Exception {
		String bundlePath = options._().get(0);

		File bundleFile = new File(bundlePath);

		if (!bundleFile.exists() && !bundleFile.isAbsolute()) {
			bundleFile = new File(this.getBase(), bundlePath);
		}

		if (!bundleFile.exists()) {
			addError("Deploy", "Unable to find specified bundle file " +
				bundleFile.getAbsolutePath());
			return;
		}

		final BundleDeployer bundleDeployer = getBundleDeployer(options);

		String bsn = null;

		try (Jar jar = new Jar(bundleFile)) {
			bsn = jar.getBsn();
		}

		if (bsn == null) {
			addError("deploy", "Unable to determine bsn for file " +
				bundleFile.getAbsolutePath());
			return;
		}

		final long bundleId = bundleDeployer.deployBundle(bsn, bundleFile);

		this.out.println("Installed or updated bundle " + bundleId);
	}

	@Description("Uninstalls a bundle in a OSGi framework over JMX")
	@Arguments(arg = "<id or bsn>")
	interface uninstallOptions extends deployOptions {
	}

	@Description("Uninstalls a bundle in a OSGi framework over JMX")
	public void _uninstall(uninstallOptions options) throws Exception {
		String idOrBsn = options._().get(0);

		long id = -1;

		try {
			id = Long.parseLong(idOrBsn);
		} catch (Exception e1) {
		}

		final BundleDeployer bundleDeployer = getBundleDeployer(options);

		if (id > 0) {
			bundleDeployer.uninstallBundle(id);
			this.out.println("Uninstalled bundle " + id);
		}
		else {
			bundleDeployer.uninstallBundle(idOrBsn);
			this.out.println("Uninstalled bundle " + idOrBsn);
		}
	}

	private BundleDeployer getBundleDeployer(deployOptions options) {
		String id = options.id();

		if (id == null) {
			id = "osgi.core";
		}

		int port = options.port();

		BundleDeployer bundleDeployer = null;

		try {
			if (port > 0) {
				bundleDeployer = new BundleDeployer(id, port);
			}
			else {
				String serviceURL = searchForServiceURL(id);

				bundleDeployer = new BundleDeployer(serviceURL);
			}
		}
		catch (IllegalArgumentException e) {
			addError("Deploy",
				"Unable to connect to OSGi framework for deployment");
		}

		return bundleDeployer;
	}

	private String searchForServiceURL(String id) {
		// search for port using JDK Attach API
		final List<VirtualMachineDescriptor> virtualMachineDescriptors =
			VirtualMachine.list();

		for (VirtualMachineDescriptor virtualMachineDescriptor :
				virtualMachineDescriptors) {

			try {
				final VirtualMachine virtualMachine = VirtualMachine.attach(
					virtualMachineDescriptor.id());

				String localConnectorAddress =
					virtualMachine.getAgentProperties().getProperty(
						LOCAL_CONNECTOR_ADDRESS);

				if (localConnectorAddress == null) {
					final String agent =
						virtualMachine.getSystemProperties()
							.getProperty("java.home")
								+ File.separator
								+ "lib"
								+ File.separator
								+ "management-agent.jar";

					virtualMachine.loadAgent(agent);

					localConnectorAddress =
						virtualMachine.getAgentProperties()
							.getProperty(LOCAL_CONNECTOR_ADDRESS);
				}

				if (localConnectorAddress != null) {
					final JMXServiceURL jmxServiceUrl =
						new JMXServiceURL(localConnectorAddress);
					final JMXConnector jmxConnector =
						JMXConnectorFactory.connect(jmxServiceUrl, null);

					final MBeanServerConnection mBeanServerConnection =
						jmxConnector.getMBeanServerConnection();

					if (mBeanServerConnection != null) {
						final ObjectName framework =
							getFramework(mBeanServerConnection, id, FRAMEWORK);

						if (framework != null) {
							return localConnectorAddress;
						}
					}
				}
			}
			catch (Exception e) {
			}
		}

		return null;
	}

	private ObjectName getFramework(
			MBeanServerConnection mBeanServerConnection, String mBeanRegex,
			String framework)
		throws MalformedObjectNameException, IOException {

		final Set<ObjectName> objectNames =
			mBeanServerConnection.queryNames(
				new ObjectName(mBeanRegex + framework), null);

		if (objectNames != null && objectNames.size() > 0) {
			return objectNames.iterator().next();
		}

		return null;
	}

	private void addError(String prefix, String msg) {
		this.addErrors(prefix, Collections.singleton(msg));
	}
}