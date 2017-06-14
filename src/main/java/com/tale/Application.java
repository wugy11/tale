package com.tale;

import com.tale.init.TaleLoader;

import com.blade.Blade;

public class Application {

	public static void main(String[] args) throws Exception {
		Blade blade = Blade.me();
		TaleLoader.init(blade);
		blade.start(Application.class);
	}

}