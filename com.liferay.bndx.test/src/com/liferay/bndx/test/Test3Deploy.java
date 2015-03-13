package com.liferay.bndx.test;

import com.liferay.bndx.bndx;

public class Test3Deploy {

	public static void main(String[] args) throws Exception {
		bndx.main(new String[]{
			"deploy",
			"/lrdev/repos/github/rotty3000/blade/maven/blade.portlet.ds/target/blade.portlet.ds-1.0.0-SNAPSHOT.jar",
		});
	}

}
