package edu.harvard.cs50.pokedex;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;


public class PokemonActivity extends AppCompatActivity {
    private TextView nameTextView;
    private TextView numberTextView;
    private TextView type1TextView;
    private TextView type2TextView;
    private TextView Description;
    private String url;
    private RequestQueue requestQueue;
    private Button button;
    private String Released;
    private String Caught;
    private String Catch;
    private String Release;
    private String name;
    private String Id;
    private String spriteUrl;
    private ImageView imageView;
    private String description = "";
    private String descriptionURL = "https://pokeapi.co/api/v2/pokemon-species/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pokemon);

        requestQueue = Volley.newRequestQueue(getApplicationContext());
        url = getIntent().getStringExtra("url");
        nameTextView = findViewById(R.id.pokemon_name);
        numberTextView = findViewById(R.id.pokemon_number);
        type1TextView = findViewById(R.id.pokemon_type1);
        type2TextView = findViewById(R.id.pokemon_type2);
        button = findViewById(R.id.button);
        Description = findViewById(R.id.pokemon_description);
        Released = "Released";
        Release = "Release";
        Caught = "Caught";
        Catch = "Catch";
        name = "pokemonCaught";
        load();

    }

    public void load() {
        type1TextView.setText("");
        type2TextView.setText("");

        final JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @SuppressLint("DefaultLocale")
            @Override
            public void onResponse(final JSONObject response) {
                try {
                    nameTextView.setText(response.getString("name"));
                    JSONObject sprites = response.getJSONObject("sprites");
                    spriteUrl = sprites.getString("front_default");
                    new DownloadSpriteTask().execute(spriteUrl);
                    SharedPreferences sharedPreferences = getSharedPreferences(name,MODE_PRIVATE);
                    if (Objects.equals(sharedPreferences.getString(nameTextView.getText().toString(), Released), Caught)) {
                        button.setText(Release);
                    }
                    else {
                        button.setText(Catch);
                    }
                    numberTextView.setText(String.format("#%03d", response.getInt("id")));
                    loadDescription();
                    JSONArray typeEntries = response.getJSONArray("types");
                    for (int i = 0; i < typeEntries.length(); i++) {
                        JSONObject typeEntry = typeEntries.getJSONObject(i);
                        int slot = typeEntry.getInt("slot");
                        String type = typeEntry.getJSONObject("type").getString("name");

                        if (slot == 1) {
                            type1TextView.setText(type);
                        }
                        else if (slot == 2) {
                            type2TextView.setText(type);
                        }
                    }
                } catch (JSONException e) {
                    Log.e("cs50", "Pokemon json error", e);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("cs50", "Pokemon details error", error);
            }
        });

        requestQueue.add(request);
    }


    public void loadDescription() {
        Id = numberTextView.getText().toString();
        Id = Integer.toString(Integer.parseInt(Id.substring(1)));
        descriptionURL += Id;
        final JsonObjectRequest descriptionRequest = new JsonObjectRequest(Request.Method.GET, descriptionURL, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray textEntries = response.getJSONArray("flavor_text_entries");
                    for (int i = 0; i < textEntries.length(); i++) {
                        JSONObject textEntry = textEntries.getJSONObject(i);
                        if (textEntry.getJSONObject("language").getString("name").equals("en")) {
                            description += textEntry.getString("flavor_text");
                            break;
                        }
                    }

                    Description.setText(description);
                }

                catch (JSONException e) {
                    e.printStackTrace();
                    Log.e("Description", "onResponse: Description reading failed" );
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Description", "onErrorResponse: Description Read error");
            }
        });

        requestQueue.add(descriptionRequest);
    }

    private class DownloadSpriteTask extends AsyncTask<String, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(String... strings) {
            try {
                URL url = new URL(strings[0]);
                return BitmapFactory.decodeStream(url.openStream());
            }
            catch (IOException e) {
                Log.e("cs50", "Download sprite error", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            // load the bitmap into the ImageView!
            imageView = findViewById(R.id.sprite);
            imageView.setImageBitmap(bitmap);
        }
    }


    public void toggleCatch(View view) {
        SharedPreferences sharedPreferences = getSharedPreferences(name,MODE_PRIVATE);
        if (button.getText().equals("Catch")) {
            sharedPreferences.edit().putString(nameTextView.getText().toString(),Caught).apply();
            button.setText(Release);
        }
        else {
            Log.d("button", "loadButton: " + nameTextView.getText().toString());
            sharedPreferences.edit().putString(nameTextView.getText().toString(),Released).apply();
            button.setText(Catch);
        }
    }
}
