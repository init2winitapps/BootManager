package com.drx2.bootmanager.extras;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;

import com.drx2.bootmanager.R;
import com.drx2.bootmanager.utilities.CustomDialog;

public class Tips extends Activity {
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Tip1();
	}
	
	private void Tip1(){
        CustomDialog.Builder builder = new CustomDialog.Builder(Tips.this);
        builder.setTitle("Tip 1")
        	   .setMessage(R.string.tip1)
               .setCancelable(false)
               .setPositiveButton(R.string.previous, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                        Tip7();
                   }
               })
               .setNeutralButton(R.string.okay, new DialogInterface.OnClickListener() {
            	   public void onClick(DialogInterface dialog, int id) {
            		   Tips.this.finish();
            	   }
               })
               .setNegativeButton(R.string.next, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                        Tip2();
                   }
               }).show();
	}
	
	private void Tip2(){
        CustomDialog.Builder builder = new CustomDialog.Builder(Tips.this);
        builder.setTitle("Tip 2")
        	   .setMessage(R.string.tip2)
               .setCancelable(false)
               .setPositiveButton(R.string.previous, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                        Tip1();
                   }
               })
               .setNeutralButton(R.string.okay, new DialogInterface.OnClickListener() {
            	   public void onClick(DialogInterface dialog, int id) {
            		   Tips.this.finish();
            	   }
               })
               .setNegativeButton(R.string.next, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                        Tip3();
                   }
               }).show();
	}
	
	private void Tip3(){
        CustomDialog.Builder builder = new CustomDialog.Builder(Tips.this);
        builder.setTitle("Tip 3")
        	   .setMessage(R.string.tip3)
               .setCancelable(false)
               .setPositiveButton(R.string.previous, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                        Tip2();
                   }
               })
               .setNeutralButton(R.string.okay, new DialogInterface.OnClickListener() {
            	   public void onClick(DialogInterface dialog, int id) {
            		   Tips.this.finish();
            	   }
               })
               .setNegativeButton(R.string.next, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                        Tip4();
                   }
               }).show();
	}
	
	private void Tip4(){
        CustomDialog.Builder builder = new CustomDialog.Builder(Tips.this);
        builder.setTitle("Tip 4")
        	   .setMessage(R.string.tip4)
               .setCancelable(false)
               .setPositiveButton(R.string.previous, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                        Tip3();
                   }
               })
               .setNeutralButton(R.string.okay, new DialogInterface.OnClickListener() {
            	   public void onClick(DialogInterface dialog, int id) {
            		   Tips.this.finish();
            	   }
               })
               .setNegativeButton(R.string.next, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                        Tip5();
                   }
               }).show();
	}
	
	private void Tip5(){
        CustomDialog.Builder builder = new CustomDialog.Builder(Tips.this);
        builder.setTitle("Tip 5")
        	   .setMessage(R.string.tip5)
               .setCancelable(false)
               .setPositiveButton(R.string.previous, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                        Tip4();
                   }
               })
               .setNeutralButton(R.string.okay, new DialogInterface.OnClickListener() {
            	   public void onClick(DialogInterface dialog, int id) {
            		   Tips.this.finish();
            	   }
               })
               .setNegativeButton(R.string.next, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                        Tip6();
                   }
               }).show();
	}
	
	private void Tip6(){
        CustomDialog.Builder builder = new CustomDialog.Builder(Tips.this);
        builder.setTitle("Tip 6")
        	   .setMessage(R.string.tip6)
               .setCancelable(false)
               .setPositiveButton(R.string.previous, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                        Tip5();
                   }
               })
               .setNeutralButton(R.string.okay, new DialogInterface.OnClickListener() {
            	   public void onClick(DialogInterface dialog, int id) {
            		   Tips.this.finish();
            	   }
               })
               .setNegativeButton(R.string.next, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                        Tip7();
                   }
               }).show();
	}
	
	private void Tip7(){
        CustomDialog.Builder builder = new CustomDialog.Builder(Tips.this);
        builder.setTitle("Tip 7")
        	   .setMessage(R.string.tip7)
               .setCancelable(false)
               .setPositiveButton(R.string.previous, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                        Tip6();
                   }
               })
               .setNeutralButton(R.string.okay, new DialogInterface.OnClickListener() {
            	   public void onClick(DialogInterface dialog, int id) {
            		   Tips.this.finish();
            	   }
               })
               .setNegativeButton(R.string.next, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                        Tip1();
                   }
               }).show();
	}
}