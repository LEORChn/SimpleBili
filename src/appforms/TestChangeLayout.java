package appforms;

import android.app.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import com.LEORChn.SimpleBili.*;
import android.graphics.drawable.*;

public class TestChangeLayout extends Activity implements View.OnClickListener{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO: Implement this method
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.testchangelayout_1);
		fv(R.id.test_big).setOnClickListener(this);
		fv(R.id.test_change).setOnClickListener(this);
		AnimationDrawable bg=(AnimationDrawable)fv(R.id.test_animview).getBackground();
		bg.start();
	}
	
	View fv(int id){return findViewById(id);}
	boolean cview=false;
	public void onClick(View v){
		switch(v.getId()){
			case R.id.test_big:
				((Button)v).setText("big button");
				((Button)fv(R.id.test_small)).setText("useless small button"); break;
			case R.id.test_change:
				setContentView(
					cview?
					R.layout.testchangelayout_1:
					R.layout.testchangelayout_2
				);
		}
	
	}
}
