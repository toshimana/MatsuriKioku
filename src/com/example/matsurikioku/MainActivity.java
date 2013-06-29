package com.example.matsurikioku;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	private List<KiokuItem> kiokuList;
	private KiokuArrayAdapter adapter;
	private String festival = "青葉祭り";
	private static final String miraiKiokuUrl = "http://www.miraikioku.com/api/search/kioku";
	private static final String BR = System.getProperty("line.separator");
	private ProgressDialog progressDialog;
	private static final int maxResults = 10;
	
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        kiokuList = new ArrayList<KiokuItem>();
        adapter = new KiokuArrayAdapter(getApplicationContext(), 0, kiokuList);

        TextView text = (TextView)findViewById(R.id.festival_name);
        text.setText(festival);

        Gallery gallery = (Gallery)findViewById(R.id.gallery1);
        gallery.setAdapter(adapter);
         
        gallery.setOnItemClickListener(new Gallery.OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				KiokuItem item = (KiokuItem)arg0.getItemAtPosition(arg2);
//				View view = (View)findViewById(R.id.imageView1);
//				Activity activity = (Activity)view.getContext();
//				ImageView imageView = new ImageView(MainActivity.this);
//				activity.setContentView(imageView);
//				ImageView imageView = (ImageView)findViewById(R.id.imageView1);
				final String title = item.title;
//				final String imageUrl = item.imageUrl;
				final String desc = item.desc;

				final String toastTitle = "【 Title : " + title + " 】" + BR;
				Toast.makeText(MainActivity.this, toastTitle + desc, Toast.LENGTH_LONG).show();
				/*
				Bitmap b = ImageMap.getImage(imageUrl);
				if (b != null) {
					imageView.setImageBitmap(b);
				} else {
					imageView.setImageDrawable(null);
					new SetImageTask(imageUrl, imageView).execute((Void)null);
				}
				imageView.setClickable(true);
				imageView.setOnClickListener(new ImageView.OnClickListener(){

					@Override
					public void onClick(View arg0) {
						final String toastTitle = "【 Title : " + title + " 】" + BR;
						Toast.makeText(MainActivity.this, toastTitle + desc, Toast.LENGTH_LONG).show();
					}

				});
			*/
			}
        });

        progressDialog = new ProgressDialog(this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setMessage("Getting data from server...");
        progressDialog.setCancelable(true);
        progressDialog.show();
        getData();
        
//        View view = (View)findViewById(R.id.imageView1);
//        Activity activity = (Activity)view.getContext();
//        activity.setContentView(new CameraView(this));

        LinearLayout layout = (LinearLayout)findViewById(R.id.layout1);
        CameraView camera1 = new CameraView(this);
        LinearLayout.LayoutParams param1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
//        param1.height = 400;
        camera1.setLayoutParams(param1);
        layout.addView(camera1);
    }

    
    private void getData() {
    	// API アクセスのための url を文字列として組み立てます。
    	// ここでは type と event-date のパラメータを指定しています。
    	// http://www.miraikioku.com/docs/api/search_kioku を参照して
    	// いろいろなパラメータを設定して試してみて下さい。
    	final String queryFestival = "q=" + festival;
    	final String queryMaxResults = "max-results=" + maxResults;
//    	String apiUrl = miraiKiokuUrl + "?" + "type=photo" + "&" + "event-date=20080805";
    	String apiUrl = miraiKiokuUrl + "?" + "type=photo" + '&' + queryFestival + '&' + queryMaxResults;
    	new AccessAPItask().execute(apiUrl);
    }
    
 
    private class KiokuArrayAdapter extends ArrayAdapter<KiokuItem> {
    	private Context context; 
    	
		public KiokuArrayAdapter(Context context, int textViewResourceId,
				List<KiokuItem> objects) {
			super(context, textViewResourceId, objects);
			this.context = context;
//			inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}
/*
		private class ViewHolder {
			TextView title;
			ImageView thumbnail;
		}
	*/	
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ImageView imageView = new ImageView(context);
			KiokuItem item = getItem(position);
			String thumbUrl = item.thumbUrl;
			imageView.setTag(thumbUrl);
			Bitmap b = ImageMap.getImage(thumbUrl);
			if(b != null) {
				imageView.setImageBitmap(b);
			} else {
				imageView.setImageDrawable(null);
				new SetImageTask(thumbUrl, imageView).execute((Void)null);
			}
			imageView.setScaleType(ImageView.ScaleType.FIT_XY);
			imageView.setLayoutParams(new Gallery.LayoutParams(150, 120));
			return imageView;
		}
    }    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
  
    private class AccessAPItask extends AsyncTask<String, Void, JSONObject> {
    	private DefaultHttpClient httpClient;
    	
    	public AccessAPItask() {
    		httpClient = new DefaultHttpClient();
    	}

		@Override
		protected JSONObject doInBackground(String... args) {
			execAPI(args[0]);
			return null;
		}
		
		@Override
		protected void onPostExecute(JSONObject result) {
			progressDialog.dismiss();
			adapter.notifyDataSetChanged();
		}
		
		private void execAPI(String url) {
			try {
		    	Log.d("MiraiKiokuAPIsample", "execAPI=" + url);
		    	// 文字列として組み立てた url で http の GET リクエストをサーバーに送ります。
		    	// これが「API を呼び出す」ことになります。
				HttpGet request = new HttpGet(url);
				HttpResponse response = executeRequest(request);
				
				// サーバーからのステータスを取得します。
				int statusCode = response.getStatusLine().getStatusCode();
				StringBuilder buf = new StringBuilder();
				InputStream in = response.getEntity().getContent();
				BufferedReader reader = new BufferedReader(new InputStreamReader(in));
				String l = null;
				
				// サーバーからのレスポンスを行単位に読み込みます。
				while((l = reader.readLine()) != null) {
					buf.append(l);
//					Log.d("MiraiKiokuAPISample", l);
				}
				if(statusCode == 200) {
					// ステータスが成功ならレスポンスのパース（解析）を行います。
					parseResponse(buf.toString());
				}
			} catch(IOException e) {
				Log.e("MiraiKiokuAPISample", "IO error", e);
			} catch(JSONException e) {
				Log.e("MiraiKiokuAPISample", "JSON error", e);
			}
		}
		
		private void parseResponse(String buf) throws JSONException {
			// レスポンスは JSON フォーマットとしてパースします。
			JSONObject rootObj = new JSONObject(buf);
			// アイテムの件数を取得
			int count = rootObj.getInt("count");
			Log.d("MiraiKiokuAPISample", String.valueOf(count));
			
			// アイテムを配列として取得
			JSONArray results = rootObj.getJSONArray("results");
			for(int i = 0; i < count; i++) {
				JSONObject item = results.getJSONObject(i);
				Log.d("MiraiKiokuAPISample", item.getString("title"));
				Log.d("MiraiKiokuAPISample", item.getString("url"));
				Log.d("MiraiKiokuAPISample", item.getString("thumb-url"));
				Log.d("MiraiKiokuAPISample", item.getString("image-url"));
				Log.d("MiraiKiokuAPISample", item.getString("desc"));
				
				// ListView に表示するためのアイテムとして登録します。
				KiokuItem kioku = new KiokuItem();
				kioku.title = item.getString("title");
				kioku.thumbUrl = item.getString("thumb-url");
				kioku.imageUrl = item.getString("image-url");
				kioku.desc= item.getString("desc");
				kiokuList.add(kioku);
			}
		}
		
		private HttpResponse executeRequest(HttpRequestBase base) throws IOException {
			try {
				return httpClient.execute(base);
			} catch(IOException e) {
				base.abort();
				throw e;
			}
		}
    }
}
