package com.example.backgroundtask;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity{
    ImageView _image1;
    ImageView _image2;
    ImageView _image3;

    Button _startStopButton;

    boolean _isRolling = false;

    ArrayList<String> imageUrl = new ArrayList<>();

    SlotGame _slotGame1, _slotGame2, _slotGame3;
    ExecutorService _rollExecService, _imageExecService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        _image1 = findViewById(R.id.image_1);
        _image2 = findViewById(R.id.image_2);
        _image3 = findViewById(R.id.image_3);

        _startStopButton = findViewById(R.id.start_stop_btn);

        _imageExecService = Executors.newSingleThreadExecutor();

        _rollExecService = Executors.newFixedThreadPool(3);

        _slotGame1 = new SlotGame(_image1);
        _slotGame2 = new SlotGame(_image2);
        _slotGame3 = new SlotGame(_image3);

        _imageExecService.execute(new Runnable() {
            @Override
            public void run() {
                try{
                    final String txt = loadStringFromNetwork("https://mocki.io/v1/821f1b13-fa9a-43aa-ba9a-9e328df8270e");
                    try{
                        JSONArray jsonArray = new JSONArray(txt);
                        for(int i = 0; i < jsonArray.length(); i++){
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            imageUrl.add(jsonObject.getString("url"));
                        }
                    }catch (JSONException e){
                        e.printStackTrace();
                    }
                }catch (IOException e){
                    e.printStackTrace();
                };
            }
        });

        _startStopButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(v.getId() == _startStopButton.getId()){
                    if(!_isRolling){
                        _slotGame1._rolling = true;
                        _slotGame2._rolling = true;
                        _slotGame3._rolling = true;

                        _rollExecService.execute(_slotGame1);
                        _rollExecService.execute(_slotGame2);
                        _rollExecService.execute(_slotGame3);

                        _startStopButton.setText("Stop");

                    } else {
                        _slotGame1._rolling = false;
                        _slotGame2._rolling = false;
                        _slotGame3._rolling = false;

                        _startStopButton.setText("Start");
                    }
                    _isRolling = !_isRolling;
                }
            }
        });
    }

    private String loadStringFromNetwork(String s) throws IOException {
        final URL myUrl = new URL(s);
        final InputStream in = myUrl.openStream();

        final StringBuilder out = new StringBuilder();
        final byte[] buffer = new byte[1024];

        try{
            for(int ctr; (ctr = in.read(buffer)) != -1; ){
                out.append(new String(buffer, 0, ctr));
            }
        } catch (IOException e){
            throw new RuntimeException("Gagal mendapatkan text", e);
        }

        return out.toString();
    }

    class SlotGame implements Runnable {
        Handler handler = new Handler(Looper.getMainLooper());
        ImageView _slotImage;
        Random _randomNumber = new Random();
        public boolean _rolling;
        int i;

        public SlotGame(ImageView _slotImage){
            this._slotImage = _slotImage;
            i = 0;
            _rolling = true;
        }

        @Override
        public void run() {
            while(_rolling){
                i = _randomNumber.nextInt(3);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(MainActivity.this).load(imageUrl.get(i)).into(_slotImage);
                    }
                });

                try{
                    Thread.sleep(_randomNumber.nextInt(500));
                } catch(InterruptedException e){
                    e.printStackTrace();
                }
            }
        }
    }
}