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
    ImageView image1;
    ImageView image2;
    ImageView image3;

    Button startStopButton;

    boolean isRolling = false;

    ArrayList<String> imageUrl = new ArrayList<>();

    SlotGame slotGame1, slotGame2, slotGame3;
    ExecutorService rollExecService, imageExecService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        image1 = findViewById(R.id.image_1);
        image2 = findViewById(R.id.image_2);
        image3 = findViewById(R.id.image_3);

        startStopButton = findViewById(R.id.start_stop_btn);

        imageExecService = Executors.newSingleThreadExecutor();

        rollExecService = Executors.newFixedThreadPool(3);

        slotGame1 = new SlotGame(image1);
        slotGame2 = new SlotGame(image2);
        slotGame3 = new SlotGame(image3);

        if(imageUrl.isEmpty()){
            imageExecService.execute(new Runnable() {
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
        }

        startStopButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(v.getId() == startStopButton.getId()){
                    if(!isRolling){
                        slotGame1.isRolling = true;
                        slotGame2.isRolling = true;
                        slotGame3.isRolling = true;

                        rollExecService.execute(slotGame1);
                        rollExecService.execute(slotGame2);
                        rollExecService.execute(slotGame3);

                        startStopButton.setText("Stop");

                    } else {
                        slotGame1.isRolling = false;
                        slotGame2.isRolling = false;
                        slotGame3.isRolling = false;

                        startStopButton.setText("Start");
                    }
                    isRolling = !isRolling;
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
        ImageView slotImage;
        Random randomNumber = new Random();
        public boolean isRolling = true;
        int i;

        public SlotGame(ImageView slotImage){
            this.slotImage = slotImage;
            i = 0;
            isRolling = true;
        }

        @Override
        public void run() {
            while(isRolling){
                i = randomNumber.nextInt(3);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(MainActivity.this).load(imageUrl.get(i)).into(slotImage);
                    }
                });

                try{
                    Thread.sleep(randomNumber.nextInt(500));
                } catch(InterruptedException e){
                    e.printStackTrace();
                }
            }
        }
    }
}