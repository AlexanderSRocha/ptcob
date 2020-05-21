/*
 * Copyright (c) 2012-present, salesforce.com, inc.
 * All rights reserved.
 * Redistribution and use of this software in source and binary forms, with or
 * without modification, are permitted provided that the following conditions
 * are met:
 * - Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * - Neither the name of salesforce.com, inc. nor the names of its contributors
 * may be used to endorse or promote products derived from this software without
 * specific prior written permission of salesforce.com, inc.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package br.com.pentagrupo.pentalog;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.Toolbar;

import androidx.recyclerview.widget.RecyclerView;

import com.salesforce.androidsdk.app.SalesforceSDKManager;
import com.salesforce.androidsdk.mobilesync.app.MobileSyncSDKManager;
import com.salesforce.androidsdk.rest.ApiVersionStrings;
import com.salesforce.androidsdk.rest.RestClient;
import com.salesforce.androidsdk.rest.RestClient.AsyncRequestCallback;
import com.salesforce.androidsdk.rest.RestRequest;
import com.salesforce.androidsdk.rest.RestResponse;
import com.salesforce.androidsdk.ui.SalesforceActivity;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;

import br.com.pentagrupo.pentalog.adapters.ListOperacaoDescargaAdapter;
import br.com.pentagrupo.pentalog.entities.BaseEntity;
import br.com.pentagrupo.pentalog.entities.OperacaoDescarga;
import br.com.pentagrupo.pentalog.objects.OperacaoDescargaObj;
import br.com.pentagrupo.pentalog.requests.BaseRequest;
import br.com.pentagrupo.pentalog.requests.OperacaoDescargaRequest;
import br.com.pentagrupo.pentalog.util.LinearLayoutManagerFixed;
import dmax.dialog.SpotsDialog;

/**
 * Main activity
 */
public class MainActivity extends SalesforceActivity {

    private RestClient client;
    private ListOperacaoDescargaAdapter listAdapter;
    private ArrayList<OperacaoDescarga> listRecord = new ArrayList<OperacaoDescarga>();
	private RecyclerView mRecycleView;
	private LinearLayoutManagerFixed mLayoutManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Setup theme
		boolean isDarkTheme = MobileSyncSDKManager.getInstance().isDarkTheme();
		setTheme(isDarkTheme ? R.style.PLThemeDark : R.style.PLTheme);
		MobileSyncSDKManager.getInstance().setViewNavigationVisibility(this);

		// Setup view
		setContentView(R.layout.main);

		Toolbar myToolbar = (Toolbar) findViewById(R.id.main_toolbar);
		setActionBar(myToolbar);

		View decorView = getWindow().getDecorView();
		decorView.setSystemUiVisibility(decorView.getSystemUiVisibility() & ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
	}
	
	@Override 
	public void onResume() {
		// Hide everything until we are logged in
		findViewById(R.id.root).setVisibility(View.INVISIBLE);

		// Create list adapter
		listAdapter = new ListOperacaoDescargaAdapter(listRecord);
		listAdapter.setOnItemClickListenner(new ListOperacaoDescargaAdapter.OnItemClickListener() {
			@Override
			public void onItemClick(int position, View v, final Object objClicked) {

				AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
				// Add the buttons
				builder.setPositiveButton(R.string.imprimir, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.dismiss();
						Toast.makeText(MainActivity.this, "Operação realizada com sucesso!", Toast.LENGTH_SHORT).show();
						doAtualizarOD((OperacaoDescarga) objClicked);
					}
				});
				builder.setNegativeButton(R.string.voltar, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.dismiss();
					}
				});
				builder.setTitle(((OperacaoDescarga) objClicked).name);
				builder.setMessage("Deseja imprimir o comprovante?");
				// Create the AlertDialog
				AlertDialog dialog = builder.create();
				dialog.show();
			}
		});
//		((ListView) findViewById(R.id.record_list)).setAdapter(listAdapter);

		// Set the adapter
		mRecycleView = (RecyclerView) findViewById(R.id.record_list);
		// use this setting to improve performance if you know that changes
		// in content do not change the layout size of the RecyclerView
		mRecycleView.setHasFixedSize(true);
		//mRecycleView.setItemViewCacheSize(9);

		// use a linear layout manager
		mLayoutManager = new LinearLayoutManagerFixed(this);
		mRecycleView.setLayoutManager(mLayoutManager);

		// specify an adapter (see also next example)
		mRecycleView.setAdapter(listAdapter);

		super.onResume();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main_menu, menu);
		menu.findItem(R.id.action_refresh).getActionView().setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				refreshData();
			}
		});
		return true;
	}


	private void doAtualizarOD(OperacaoDescarga pOperacaoDescarga) {
		pOperacaoDescarga.status = "Finalizado";
		new OperacaoDescargaRequest(client).update(pOperacaoDescarga, new BaseRequest.DMLCallback() {
			@Override
			public void onSuccess(ArrayList<? extends BaseEntity> pListData) {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						refreshData();
					}
				});
			}

			@Override
			public void onError(ArrayList<Exception> pListException) {
				pListException.get(0).printStackTrace();
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						Toast.makeText(MainActivity.this,
								MainActivity.this.getString(R.string.sf__generic_error, "Verifique sua conexão com a internet"),
								Toast.LENGTH_LONG).show();
					}
				});
			}
		});
	}

	@Override
	public void onResume(RestClient client) {
        // Keeping reference to rest client
        this.client = client; 

		// Show everything
		findViewById(R.id.root).setVisibility(View.VISIBLE);

		refreshData();
	}

	private void refreshData() {
		setLoading(true);
		new OperacaoDescargaRequest(client).query("WHERE " + OperacaoDescargaObj.STATUS + " = 'Aguardando Impressão'", new BaseRequest.QueryCallback() {
			@Override
			public void onSuccess(final ArrayList<? extends BaseEntity> pListData) {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						try {
							listAdapter.clear();
							for (int i = 0; i < pListData.size(); i++) {
								listAdapter.add((OperacaoDescarga)pListData.get(i));
							}
							if (pListData.isEmpty())
								Toast.makeText(MainActivity.this, "Não há cobranças pendentes", Toast.LENGTH_LONG).show();
							setLoading(false);
						} catch (Exception e) {
							onError(e);
						}

					}
				});
			}

			@Override
			public void onError(final Exception pException) {
				pException.printStackTrace();
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						Toast.makeText(MainActivity.this,
								MainActivity.this.getString(R.string.sf__generic_error, "Verifique sua conexão com a internet"),
								Toast.LENGTH_LONG).show();
					}
				});
				setLoading(false);
			}
		});
	}


	private void setLoading(boolean pValue) {
		if (pValue) {
			getAlertDialog().show();
//			findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
//			findViewById(R.id.record_list).setVisibility(View.GONE);
		} else {
			getAlertDialog().dismiss();
//			findViewById(R.id.progressBar).setVisibility(View.GONE);
//			findViewById(R.id.record_list).setVisibility(View.VISIBLE);
		}
	}

	private AlertDialog loadingDialog;
	private AlertDialog getAlertDialog() {
		if (loadingDialog == null)
			loadingDialog = new SpotsDialog.Builder().setContext(this).setMessage("Carregando").build();
		return loadingDialog;
	}

	/**
	 * Called when "Logout" button is clicked. 
	 * 
	 * @param v
	 */
	public void onLogoutClick(View v) {
		SalesforceSDKManager.getInstance().logout(this);
	}
	
	/**
	 * Called when "Clear" button is clicked. 
	 * 
	 * @param v
	 */
	public void onClearClick(View v) {
		listAdapter.clear();
	}
}
