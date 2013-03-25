package com.drx2.bootmanager.utilities;




import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.drx2.bootmanager.MainActivity;
import com.drx2.bootmanager.R;


 
/**
 * 
 * Create custom Dialog windows for your application
 * Custom dialogs rely on custom layouts wich allow you to 
 * create and use your own look & feel.
 * 
 * Under GPL v3 : http://www.gnu.org/licenses/gpl-3.0.html
 * 
 * @author antoine vianey
 *
 */
public class CustomDialog extends Dialog {
 
    public CustomDialog(Context context, int theme) {
        super(context, theme);
    }
 
    public CustomDialog(Context context) {
        super(context);
    }
 
    
    /**
     * Helper class for creating a custom dialog
     */
    public static class Builder {
 
        private Context context;
        private String title;
        private String message;
        private String positiveButtonText;
        private String negativeButtonText;
        private String neutralButtonText;
        private View contentView;
        private TextView textview;
        private Boolean cancelable=true;
        private Boolean icon=false;
        private static ListView lst;
        private int iconId;
        private int position;
        private boolean[] states;
        private CharSequence[] items;
        private CharSequence[] listitems;
        private CharSequence[] multiitems;
        private DialogInterface.OnClickListener itemsClickListener;
        private DialogInterface.OnMultiChoiceClickListener onMultichoiceListener;
        private DialogInterface.OnClickListener 
        				positiveButtonClickListener,
                        negativeButtonClickListener,
        				neutralButtonClickListener;
        private DialogInterface.OnCancelListener onCancel;
 
        public Builder(Context context) {
            this.context = context;
        }
 
        /**
         * Set the Dialog message from String
         * @param title
         * @return
         */
        public Builder setMessage(String message) {
            this.message = message;
            return this;
        }
 
        /**
         * Set the Dialog message from resource
         * @param title
         * @return
         */
        public Builder setMessage(int message) {
            this.message = (String) context.getText(message);
            return this;
        }
        
  
 
        
        /**
         * Set the Dialog title from resource
         * @param title
         * @return
         */
        public Builder setTitle(int title) {
            this.title = (String) context.getText(title);
            return this;
        }
 
        /**
         * Set the Dialog title from String
         * @param title
         * @return
         */
        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }
 
        /**
         * Set a custom content view for the Dialog.
         * If a message is set, the contentView is not
         * added to the Dialog...
         * @param v
         * @return
         */
        public Builder setContentView(View v) {
            this.contentView = v;
            return this;
        }
        
        /**
         * Set a custom content view for the Dialog.
         * If a message is set, the View is not
         * added to the Dialog...
         * @param v
         * @return
         */
        public Builder setView(TextView v, Boolean isEditTextBox) {
            v.setTextColor(R.color.black);
            if(isEditTextBox){
            	v.setBackgroundResource(R.drawable.editbox);
            	v.setHighlightColor(context.getResources().getColor(R.color.red2));
            }
        	this.textview = v;
            return this;
        }
 
        public Builder setItems(CharSequence[] items,
                OnClickListener listener) {
            this.items = items;
            this.itemsClickListener = listener;
            return this;
        }
        
        public Builder setMultiChoiceItems(CharSequence[] items, boolean[] states2, DialogInterface.OnMultiChoiceClickListener listener){
			this.multiitems=items;
			this.states=states2;
			this.onMultichoiceListener=listener;
        	return this;
        }
        
        public Builder setSingleChoiceItems(CharSequence[] items, int selected,
                OnClickListener listener) {
            this.listitems = items;
            this.position = selected;
            this.itemsClickListener = listener;
            return this;
        }
        
        public Builder setOnCancelListener(OnCancelListener onCancelListener){
			this.onCancel=onCancelListener;
        	return this;
        	
        }
        /**
         * Set the positive button resource and it's listener
         * @param positiveButtonText
         * @param listener
         * @return
         */
        public Builder setPositiveButton(int positiveButtonText,
                DialogInterface.OnClickListener listener) {
            this.positiveButtonText = (String) context
                    .getText(positiveButtonText);
            this.positiveButtonClickListener = listener;
            return this;
        }
 
        /**
         * Set the positive button text and it's listener
         * @param positiveButtonText
         * @param listener
         * @return
         */
        public Builder setPositiveButton(String positiveButtonText,
                DialogInterface.OnClickListener listener) {
            this.positiveButtonText = positiveButtonText;
            this.positiveButtonClickListener = listener;
            return this;
        }
 
        /**
         * Set the Neutral button resource and it's listener
         * @param NeutralButtonText
         * @param listener
         * @return
         */
        public Builder setNeutralButton(int negativeButtonText,
                DialogInterface.OnClickListener listener) {
            this.neutralButtonText = (String) context
                    .getText(negativeButtonText);
            this.neutralButtonClickListener = listener;
            return this;
        }
 
        /**
         * Set the Neutral button text and it's listener
         * @param NeutralButtonText
         * @param listener
         * @return
         */
        public Builder setNeutralButton(String negativeButtonText,
                DialogInterface.OnClickListener listener) {
            this.neutralButtonText = negativeButtonText;
            this.neutralButtonClickListener = listener;
            return this;
        }
        
        
        /**
         * Set the negative button resource and it's listener
         * @param negativeButtonText
         * @param listener
         * @return
         */
        public Builder setNegativeButton(int negativeButtonText,
                DialogInterface.OnClickListener listener) {
            this.negativeButtonText = (String) context
                    .getText(negativeButtonText);
            this.negativeButtonClickListener = listener;
            return this;
        }
 
        /**
         * Set the negative button text and it's listener
         * @param negativeButtonText
         * @param listener
         * @return
         */
        public Builder setNegativeButton(String negativeButtonText,
                DialogInterface.OnClickListener listener) {
            this.negativeButtonText = negativeButtonText;
            this.negativeButtonClickListener = listener;
            return this;
        }
        
        /** Set whether the app icon displays in the Dialog*/
        public Builder setIcon(int iconId, Boolean icon){
        	this.iconId=iconId;
        	this.icon=icon;
			return this;
        }
        
        /** Set cancellable */
        public Builder setCancelable(Boolean cancelable) {
			this.cancelable=cancelable;
        	return this;
        
        }
        
        /** Show the dialog */
        public Builder show(){
			this.create().show();
        	return null;
        }
        
        private static final String PREFS_DEVICE = "DeviceInfo";
 
        /**
         * Create the custom dialog
         */
        public CustomDialog create() {
        	
        	//Get the custom color and draw the border here
        	SharedPreferences colors = context.getSharedPreferences(PREFS_DEVICE, 0);
        	final float[] roundedCorners = new float[] { 10, 10, 10, 10, 10, 10, 10, 10 };
   		 	ShapeDrawable boardershape = new ShapeDrawable(new RoundRectShape(roundedCorners, null, roundedCorners));
   		 	boardershape.getPaint().setColor(colors.getInt("actionbarStart", context.getResources().getColor(R.color.actionbar_background_start)));
   		 	int startcolor = colors.getInt("actionbarStart", context.getResources().getColor(R.color.actionbar_background_start));
   		 	int endcolor = colors.getInt("actionbarEnd", context.getResources().getColor(R.color.actionbar_background_end));
    		int[] color = {startcolor, endcolor};
    		GradientDrawable d = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, color);
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            // instantiate the dialog with the custom Theme
            final CustomDialog dialog = new CustomDialog(context, R.style.Dialog);
            dialog.setCancelable(cancelable);
            SharedPreferences shared = PreferenceManager.getDefaultSharedPreferences(context);
    		
            View layout;
            if(shared.getBoolean("themePref", false) == true){
            	layout = inflater.inflate(R.layout.custom_dialog, null);
            }else{
    	    	layout = inflater.inflate(R.layout.custom_dialog_test, null);
            }
            dialog.addContentView(layout, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
            //Set custom button colors
       	 	((Button) layout.findViewById(R.id.ok)).setBackgroundDrawable(MainActivity.buttonState(context));
       	 	((Button) layout.findViewById(R.id.neutral)).setBackgroundDrawable(MainActivity.buttonState(context));
       	 	((Button) layout.findViewById(R.id.cancel)).setBackgroundDrawable(MainActivity.buttonState(context));
        	((Button) layout.findViewById(R.id.ok)).setTextColor(colors.getInt("buttonText", context.getResources().getColor(R.color.white)));
       	 	((Button) layout.findViewById(R.id.neutral)).setTextColor(colors.getInt("buttonText", context.getResources().getColor(R.color.white)));
       	 	((Button) layout.findViewById(R.id.cancel)).setTextColor(colors.getInt("buttonText", context.getResources().getColor(R.color.white)));
       	 	//Border colors set here...we draw the outside border above...
            ((LinearLayout) layout.findViewById(R.id.border)).setBackgroundDrawable(boardershape);
            ((LinearLayout) layout.findViewById(R.id.horizontalLine)).setBackgroundColor(colors.getInt("actionbarStart", context.getResources().getColor(R.color.actionbar_background_start)));
            // set the dialog title
            if(onCancel!=null){
	            dialog.setOnCancelListener(new OnCancelListener(){
	            	@Override
					public void onCancel(DialogInterface arg0) {
						onCancel.onCancel(dialog);
					}
	            });
            }
            if(title!=null){
            	((TextView) layout.findViewById(R.id.title)).setText(title);
            } else {
                layout.findViewById(R.id.title).setVisibility(View.GONE);
                layout.findViewById(R.id.horizontalLine).setVisibility(View.GONE);
            }
            // set the confirm button
            if (positiveButtonText != null) {
                ((Button) layout.findViewById(R.id.ok))
                        .setText(positiveButtonText);
                if (positiveButtonClickListener != null) {
                    ((Button) layout.findViewById(R.id.ok))
                            .setOnClickListener(new View.OnClickListener() {
                                public void onClick(View v) {
                                    positiveButtonClickListener.onClick(dialog, DialogInterface.BUTTON_POSITIVE);
                                    dialog.dismiss();
                                }
                            });
                }
            } else {
                // if no confirm button just set the visibility to GONE
                layout.findViewById(R.id.ok).setVisibility(
                        View.GONE);
            }
            // set the cancel button
            if (negativeButtonText != null) {
                ((Button) layout.findViewById(R.id.cancel))
                        .setText(negativeButtonText);
                if (negativeButtonClickListener != null) {
                    ((Button) layout.findViewById(R.id.cancel))
                            .setOnClickListener(new View.OnClickListener() {
                                public void onClick(View v) {
                                    negativeButtonClickListener.onClick(dialog, DialogInterface.BUTTON_NEGATIVE);
                                    dialog.dismiss();
                                }
                            });
                }
            } else {
                // if no confirm button just set the visibility to GONE
                layout.findViewById(R.id.cancel).setVisibility(
                        View.GONE);
            }
         // set the nuetral button
            if (neutralButtonText != null) {
                ((Button) layout.findViewById(R.id.neutral))
                        .setText(neutralButtonText);
                if (neutralButtonClickListener != null) {
                    ((Button) layout.findViewById(R.id.neutral))
                            .setOnClickListener(new View.OnClickListener() {
                                public void onClick(View v) {
                                	neutralButtonClickListener.onClick(dialog, DialogInterface.BUTTON_NEGATIVE);
                                }
                            });
                }
            } else {
                // if no confirm button just set the visibility to GONE
                layout.findViewById(R.id.neutral).setVisibility(
                        View.GONE);
            }
            if (items!=null) {
            	((TextView) layout.findViewById(R.id.message)).setVisibility(View.GONE);
            	ListView lst = (ListView) layout.findViewById(R.id.listView);
            	lst.setSelector(d);
            	ListAdapter adapter = new ArrayAdapter<CharSequence>(context ,android.R.layout.simple_list_item_1, items);
            	lst.setAdapter(adapter); 
            	if (itemsClickListener != null) {
                    ((ListView) layout.findViewById(R.id.listView))
                            .setOnItemClickListener(new OnItemClickListener() {
                                public void onItemClick(AdapterView<?> arg0,
										View arg1, int arg2, long arg3) {
									itemsClickListener.onClick(dialog, arg2);
									dialog.dismiss();
								}
                            });
                }
            }
            if (listitems!=null) {
            	((TextView) layout.findViewById(R.id.message)).setVisibility(View.GONE);
            	ListView lst = (ListView) layout.findViewById(R.id.listView);
            	lst.setSelector(d);
            	ListAdapter adapter = new ArrayAdapter<CharSequence>(context ,R.layout.custom_list_singlechoice, listitems);
            	lst.setAdapter(adapter); 
            	lst.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
            	lst.setItemChecked(position, true);
            	if (itemsClickListener != null) {
                    ((ListView) layout.findViewById(R.id.listView))
                            .setOnItemClickListener(new OnItemClickListener() {
                                public void onItemClick(AdapterView<?> arg0,
										View arg1, int arg2, long arg3) {
									itemsClickListener.onClick(dialog, arg2);
									
								}
                            });
                }
            }
            if (multiitems!=null) {
            	((TextView) layout.findViewById(R.id.message)).setVisibility(View.GONE);
            	lst = (ListView) layout.findViewById(R.id.listView);
            	lst.setSelector(d);
            	ListAdapter adapter = new ArrayAdapter<CharSequence>(context ,R.layout.custom_list_checkbox, multiitems);
            	lst.setAdapter(adapter); 
            	lst.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
            	for (int i = 0; i < states.length; i++) {
            		lst.setItemChecked(i, states[i]);
            	}
            	if (onMultichoiceListener != null) {
            		((ListView) layout.findViewById(R.id.listView))
            			.setOnItemSelectedListener(new OnItemSelectedListener(){
            				public void onItemSelected(AdapterView<?> arg0,
									View arg1, int arg2, long arg3) {
            						boolean state = false;
									onMultichoiceListener.onClick(dialog, arg2, state);
            				}
            				public void onNothingSelected(AdapterView<?> arg0) {
            					dialog.dismiss();
							}
            				
            			});
                }
            }
            //Set whether icon is visible
            if(icon) {
            	layout.findViewById(R.id.icon).setBackgroundResource(iconId);
            } else {
            	layout.findViewById(R.id.icon).setVisibility(
                        View.GONE);
            }
            // set the content message
            if (message != null) {
                ((TextView) layout.findViewById(R.id.message)).setText(message);
            } else if (contentView != null) {
                // if no message set
                // add the contentView to the dialog body
                ((LinearLayout) layout.findViewById(R.id.linearLayout2))
                        .removeAllViews();
                ((LinearLayout) layout.findViewById(R.id.linearLayout2))
                        .addView(contentView, 
                                new LayoutParams(
                                        LayoutParams.WRAP_CONTENT, 
                                        LayoutParams.WRAP_CONTENT));
            } else if (textview != null) {
            	((LinearLayout) layout.findViewById(R.id.linearLayout2))
                .removeAllViews();
 /*       ((LinearLayout) layout.findViewById(R.id.linearLayout2))
        		.setLayoutParams(new LayoutParams(
                                LayoutParams.MATCH_PARENT, 
                                LayoutParams.WRAP_CONTENT));*/
        ((LinearLayout) layout.findViewById(R.id.linearLayout2))
                .addView(textview, 
                        new LayoutParams(
                                LayoutParams.FILL_PARENT, 
                                LayoutParams.WRAP_CONTENT)); 
            }
            
            dialog.setContentView(layout);
            
            return dialog;
        }

		public static ListView getListView() {
			return lst;
		}
 
    }


	public ListView getListView() {
		ListView lst = Builder.getListView();
		return lst;
	}

 
}
