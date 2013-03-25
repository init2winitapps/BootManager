package com.drx2.bootmanager.page.adapter;

import android.os.Bundle;

public class PageInfo {

	public final Class<?> clss;
	public final Bundle bundle;
	public final String title;
	
	public PageInfo(Class<?> clss, Bundle args, String title) {
		this.title = title;
		this.bundle = args;
		this.clss = clss;
	}
}