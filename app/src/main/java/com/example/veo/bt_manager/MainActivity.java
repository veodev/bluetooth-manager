package com.example.veo.bt_manager;

import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.lang.reflect.Method;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {

    BluetoothAdapter bluetoothAdapter = null;
    AudioManager audioManager = null;
    ListView bluetoothDevicesListView = null;
    ArrayList<String> bluetoothDevices = new ArrayList<String>();
    String currentMacAddress = "";
    MediaPlayer mediaPlayer = null;
    BluetoothA2dp bluetoothA2dp = null;

    private static void resetAudioManager(AudioManager audioManager) {
        if (audioManager != null) {
            audioManager.setMode(AudioManager.MODE_NORMAL);
            audioManager.stopBluetoothSco();
            audioManager.setBluetoothScoOn(false);
            audioManager.setSpeakerphoneOn(false);
            audioManager.setWiredHeadsetOn(false);
        }
    }

    public static void connectEarpiece(AudioManager audioManager) {
        resetAudioManager(audioManager);
        audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
    }

    public static void connectSpeaker(AudioManager audioManager) {
        resetAudioManager(audioManager);
        audioManager.setSpeakerphoneOn(true);
    }

    public static void connectHeadphones(AudioManager audioManager) {
        resetAudioManager(audioManager);
        audioManager.setWiredHeadsetOn(true);
    }

    public static void connectBluetooth(AudioManager audioManager) {
        resetAudioManager(audioManager);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        bluetoothDevicesListView = findViewById(R.id.listView);
        final ArrayAdapter<String> listViewAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        bluetoothDevicesListView.setAdapter(listViewAdapter);
        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource("/sdcard/Music/Synthya - Be Free.mp3");
            mediaPlayer.prepare();
        }
        catch (Exception e) {

        }

        bluetoothDevicesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                view.setSelected(true);
                currentMacAddress = bluetoothDevices.get(position);
                System.out.println("ITEM CLICKED: " + currentMacAddress);
            }
        });

        if (!bluetoothAdapter.isEnabled()) {
            bluetoothAdapter.enable();
        }

        final BluetoothProfile.ServiceListener btServiceListener = new BluetoothProfile.ServiceListener() {
            @Override
            public void onServiceConnected(int profile, BluetoothProfile proxy) {
                if (profile == BluetoothProfile.A2DP) {
                    System.out.println("A2DP SERVICE CONNECTED!!!");
                    bluetoothA2dp = (BluetoothA2dp) proxy;
                    resetAudioManager(audioManager);
                }
            }

            @Override
            public void onServiceDisconnected(int profile) {
                if (profile == BluetoothProfile.A2DP) {
                    System.out.println("A2DP SERVICE DISCONNECTED!!!");
                    bluetoothA2dp = null;
                }
            }
        };

        BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                BluetoothDevice bluetoothDevice;
                if (action.equals(BluetoothDevice.ACTION_FOUND)) {
                    bluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    String deviceName = bluetoothDevice.getName();
                    String deviceMacAddress = bluetoothDevice.getAddress();
                    System.out.println(deviceName + " " + deviceMacAddress);
                    listViewAdapter.add(deviceName + " " + deviceMacAddress);
                    bluetoothDevices.add(deviceMacAddress);
                }
                else if (action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
                    bluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    System.out.println("PAIRED: " + bluetoothDevice.getAddress());

                    bluetoothAdapter.getProfileProxy(MainActivity.this, btServiceListener, BluetoothProfile.A2DP);
                    Method connect;
                    try {
                        connect = BluetoothA2dp.class.getDeclaredMethod("connect", BluetoothDevice.class);
                        connect.invoke(bluetoothA2dp, bluetoothDevice);
                    }
                    catch (Exception e) {

                    }
                }
            }
        };

        IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(broadcastReceiver, intentFilter);


        final Button scanButton = findViewById(R.id.scanButton);
        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bluetoothAdapter.isDiscovering()) {
                    bluetoothAdapter.cancelDiscovery();
                }
                listViewAdapter.clear();
                bluetoothDevices.clear();
                bluetoothAdapter.startDiscovery();
            }
        });

        final Button pairButton = findViewById(R.id.pairButton);
        pairButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bluetoothAdapter.isDiscovering()) {
                    bluetoothAdapter.cancelDiscovery();
                }
                System.out.println("CURRENT MAC: " + currentMacAddress);
                bluetoothAdapter.getRemoteDevice(currentMacAddress).createBond();
            }
        });

        final Button playButton = findViewById(R.id.playButton);
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.start();
            }
        });

        final Button stopButton = findViewById(R.id.stopButton);
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.pause();
            }
        });

        final Button speakerphoneButton = findViewById(R.id.speakerphoneButton);
        speakerphoneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                audioManager.setMode(AudioManager.MODE_NORMAL);
                audioManager.stopBluetoothSco();
                audioManager.setBluetoothScoOn(false);
                audioManager.setSpeakerphoneOn(true);


            }
        });

        final Button earphonesButton = findViewById(R.id.earphonesButton);
        earphonesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
                audioManager.stopBluetoothSco();
                audioManager.setBluetoothScoOn(false);
                audioManager.setSpeakerphoneOn(false);
            }
        });

        final Button headsetButton = findViewById(R.id.headsetButton);
        headsetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
                audioManager.startBluetoothSco();
                audioManager.setBluetoothScoOn(true);

            }
        });

        final Button statusButton = findViewById(R.id.statusButton);
        statusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("SPEAKERPHONE: " + audioManager.isSpeakerphoneOn());
                System.out.println("BT SCO: " + audioManager.isBluetoothScoOn());
                System.out.println("BT A2DP: " + audioManager.isBluetoothA2dpOn());
                System.out.println("WIRED: " + audioManager.isWiredHeadsetOn());
                System.out.println("AUDIO MANAGER MODE: " + audioManager.getMode());
//                startActivity(new Intent(Settings.ACTION_BLUETOOTH_SETTINGS));

            }
        });

        final Button connectEarpieceButton = findViewById(R.id.connectEarpieceButton);
        connectEarpieceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetAudioManager(audioManager);
                audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
            }
        });

        final Button connectSpeakerButton = findViewById(R.id.connectSpeakerButton);
        connectSpeakerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetAudioManager(audioManager);
                audioManager.setSpeakerphoneOn(true);
            }
        });

        final Button connectHeadphonesButton = findViewById(R.id.connectHeadphonesbutton);
        connectHeadphonesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetAudioManager(audioManager);
                audioManager.setWiredHeadsetOn(true);
            }
        });

        final Button connectBluetoothButton = findViewById(R.id.connectBluetoothButton);
        connectBluetoothButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetAudioManager(audioManager);
            }
        });

    }
}
