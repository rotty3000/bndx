package com.liferay.bndx.test;

import com.liferay.bndx.bndx;

public class Test2BadPort {

	public static void main(String[] args) throws Exception {
		bndx.main(new String[]{
			"deploy",
			"-p",
			"110101",
			"/lrdev/repos/github/rotty3000/blade/maven/blade.portlet.ds/target/blade.portlet.ds-1.0.0-SNAPSHOT.jar",
		});
	}

}
