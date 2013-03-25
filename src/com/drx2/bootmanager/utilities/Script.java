package com.drx2.bootmanager.utilities;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;

public class Script {
	Utilities u = new Utilities();
	//TODO decrypt these scripts in with base64 but remove the security checks
	public void vigorScript(Context context){
		extractScript("vigorScript.sh", "/data/local/tmp/edit.sh", context);
	}
	
	public void galaxynexusScript(Context context){
		extractScript("galaxynexusScript.sh", "/data/local/tmp/edit.sh", context);
	}
	
	public void smallInstallScript(Context context){
		extractScript("smallInstallScript.sh", "/data/local/tmp/edit.sh", context);
	}
	
	public void smallInstallScript3d(Context context){
		extractScript("smallInstallScript3d.sh", "/data/local/tmp/edit.sh", context);
	}
	
	public void installScript(Context context){
		extractScript("installScript.sh", "/data/local/tmp/edit.sh", context);
	}
	
	public void installScriptX(Context context){
		extractScript("installScriptX.sh", "/data/local/tmp/edit.sh", context);
	}
	
	public void editStockHijackXScript(Context context){
		extractScript("editStockHijackXScript.sh", "/data/local/tmp/edit.sh", context);
	}
	
	public void editStockHijackRazrScript(Context context){
		extractScript("editStockHijackRazrScript.sh", "/data/local/tmp/edit.sh", context);
	}
	
	public void installScript3d(Context context){
		extractScript("installScript3d.sh", "/data/local/tmp/edit.sh", context);
	}
	
	public void kernelScript(Context context){
		extractScript("kernelScript.sh", "/data/local/tmp/editkernel.sh", context);
	}
	
	private void extractScript(String scriptName, String output, Context context){
		try{
			InputStream in = context.getAssets().open(scriptName);
			OutputStream out = new FileOutputStream(output);
			byte[] buf = new byte[1024];
	    	int len;
	    	while ((len = in.read(buf)) > 0) {
	    		out.write(buf, 0, len);
	    	}
	    	in.close();
	    	out.close();
		} catch(Exception e) {
			System.out.println(e.toString());
		}
	}
	
}