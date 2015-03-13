package com.liferay.bndx.test;

import com.liferay.bndx.bndx;

@SuppressWarnings("restriction")
public class Test8UninstallBadBsn {

	public static void main(String[] args) throws Exception {
		bndx.main(new String[]{
			"uninstall",
			"bad",
		});
	}

}
