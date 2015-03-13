package com.liferay.bndx.test;

import com.liferay.bndx.bndx;

public class Test1BundleFileNotFound {

	public static void main(String[] args) throws Exception {
		bndx.main(new String[]{"-b", "/lrdev/dev repos/com.liferay.osgi/com.liferay.bndx", "deploy", "blade.portlet.ds-1.0.0-SNAPSHOT.jar"});
	}

}
