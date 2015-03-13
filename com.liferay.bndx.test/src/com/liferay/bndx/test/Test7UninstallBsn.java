package com.liferay.bndx.test;

import com.liferay.bndx.bndx;

@SuppressWarnings("restriction")
public class Test7UninstallBsn {

	public static void main(String[] args) throws Exception {
		bndx.main(new String[]{
			"uninstall",
			"bad",
		});
	}

}
