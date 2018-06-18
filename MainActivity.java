package com.example.minsangkim.myapplication;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.Arrays;


public class MainActivity extends AppCompatActivity {
    int rid = 0;
    static final int getRequest = 0xa0;
    static final int getNextRequest = 0xa1;
    static final int setRequest = 0xa3;
    static final int INTEGER = 0x2;
    static final int STRING = 0x4;
    static final int OID = 0x6;
    static final int NULL = 0x5;
    public static String oid,value,type;
    public static int bufLength,sel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.snmpGET).setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {
                        try {
                            EditText edit1 = (EditText)findViewById(R.id.OID);
                            oid = edit1.getText().toString();
                            sel = 1;
                            DatagramSocket socket;
                            socket = new DatagramSocket();
                            // snmp get
                            TextView tv = (TextView)findViewById(R.id.walkResult);
                            snmpManager snmp = new snmpManager(socket, oid, tv);
                            snmp.start();
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
        );

        findViewById(R.id.snmpSET).setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {
                        try {
                            EditText edit1 = (EditText)findViewById(R.id.OID);
                            EditText edit2 = (EditText)findViewById(R.id.Value);
                            EditText edit3 = (EditText)findViewById(R.id.Type);
                            oid = edit1.getText().toString();
                            value = edit2.getText().toString();
                            type = edit3.getText().toString();
                            sel = 2;
                            DatagramSocket socket;
                            socket = new DatagramSocket();
                            // snmp set
                            TextView tv = (TextView)findViewById(R.id.walkResult);
                            snmpManager snmp = new snmpManager(socket, oid, value, type, tv);
                            snmp.start();
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
        );

        findViewById(R.id.snmpWALK).setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {
                        try {
                            EditText edit1 = (EditText)findViewById(R.id.OID);
                            oid = edit1.getText().toString();
                            sel = 3;
                            DatagramSocket socket;
                            socket = new DatagramSocket();
                            // snmp get
                            TextView tv = (TextView)findViewById(R.id.walkResult);
                            snmpManager snmp = new snmpManager(socket, oid, tv);
                            snmp.start();
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
        );

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public class snmpManager extends Thread {
        public int rid = 0;
        public int[] oid, init, curr;
        public String value;
        public int type;
        DatagramSocket socket;
        String receive = "";
        boolean isMore = true;
        TextView tv;
        public snmpManager (DatagramSocket socket, String oid, TextView tv) {
            this.socket = socket;
            this.oid = toInt(oid);
            this.value = null;
            this.type = NULL;
            this.tv = tv;
        }
        public snmpManager (DatagramSocket socket, String oid, String value, String type, TextView tv) {
            this.socket = socket;
            this.oid = toInt(oid);
            this.value = value;
            this.tv = tv;
            if (type.equals("integer")) this.type = INTEGER;
            else if (type.equals("string")) this.type = STRING;
            else if (type.equals("oid")) this.type = OID;
            else this.type = NULL;
        }
        public void run() {
            try {
                if (sel == 3) {
                    init = this.oid;
                    // snmp walk
                    while (isMore) {
                        Log.d("ms","ms");
                        rid++;
                        InetAddress addr = InetAddress.getByName("kuwiden.iptime.org");
                        int port = 11161;
                        byte[] buf = encodeData(); // encode data
                        DatagramPacket packet = new DatagramPacket(buf, bufLength, addr, port);
                        socket.send(packet);
                        byte[] buf2 = new byte[1024];
                        packet = new DatagramPacket(buf2, buf2.length, addr, port);
                        socket.receive(packet);
                        receive = decodeData(packet.getData()) + "\n";
                        tv.append(receive);
                    }
                    tv.append("end\n");
                } else {
                    // snmp get , set
                    rid++;
                    InetAddress addr = InetAddress.getByName("kuwiden.iptime.org");
                    int port = 11161;
                    byte[] buf = encodeData(); // encode data
                    DatagramPacket packet = new DatagramPacket(buf, bufLength, addr, port);
                    socket.send(packet);
                    byte[] buf2 = new byte[1024];
                    packet = new DatagramPacket(buf2, buf2.length, addr, port);
                    socket.receive(packet);
                    receive = decodeData(packet.getData()) + "\n";
                    tv.append(receive);
                }
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        public byte[] encodeData() {
            if (sel == 1) {
                try {
                    ByteBuffer buf = ByteBuffer.allocate(1024);
                    BEROutputStream os = new BEROutputStream(buf);
                    ByteBuffer temp = ByteBuffer.allocate(1024);
                    BEROutputStream tempOs = new BEROutputStream(temp);
                    BER.encodeOID(tempOs, BER.OID, this.oid);
                    BER.encodeHeader(tempOs, BER.NULL, 0);
                    int varLength = tempOs.getBuffer().position();
                    // for calculate variable length...

                    BER.encodeHeader(os, BER.SEQUENCE, varLength + 26); // sequence
                    BER.encodeInteger(os, BER.INTEGER, 1); // version
                    BER.encodeString(os, BER.OCTETSTRING, "public".getBytes()); // commnunity
                    BER.encodeHeader(os, (byte)getRequest, varLength + 13); // PDU header
                    BER.encodeInteger(os, BER.INTEGER, rid); // Request ID
                    BER.encodeInteger(os, BER.INTEGER, 0); // error status
                    BER.encodeInteger(os, BER.INTEGER, 0); // error index
                    BER.encodeHeader(os, BER.SEQUENCE, varLength+2); // sequence of
                    BER.encodeHeader(os, BER.SEQUENCE, varLength); // sequence
                    BER.encodeOID(os, BER.OID, this.oid); // oid
                    BER.encodeHeader(os, BER.NULL, 0); // value

                    bufLength = 3;
                    for (int i=0;i<oid.length;i++) {
                        if (oid[i] == 0)
                            bufLength++;
                    } // if oid includes 0 values, buffer length should increase
                    for (int i=0; i<1024;i++) {
                        if (buf.get(i) != 0) {
                            bufLength++;
                        }
                        //calculate buffer length
                    }
                    byte[] retBuf = os.getBuffer().array();
                    return retBuf;
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    return null;
                }

            } else if (sel == 2) {
                try {
                    ByteBuffer buf1 = ByteBuffer.allocate(1024);
                    BEROutputStream os1 = new BEROutputStream(buf1);
                    ByteBuffer temp1 = ByteBuffer.allocate(1024);
                    BEROutputStream tempOs1 = new BEROutputStream(temp1);
                    BER.encodeOID(tempOs1, BER.OID, this.oid);
                    if (this.type == INTEGER) BER.encodeInteger(tempOs1, BER.INTEGER, Integer.parseInt(this.value));
                    else if (this.type == STRING) BER.encodeString(tempOs1, BER.OCTETSTRING, this.value.getBytes());
                    else if (this.type == NULL) BER.encodeHeader(tempOs1, BER.NULL, 0);
                    else if (this.type == OID) BER.encodeOID(tempOs1, BER.OID, toInt(this.value));
                    int varLength = tempOs1.getBuffer().position();
                    // for calculate variable length...

                    BER.encodeHeader(os1, BER.SEQUENCE, varLength + 25); // sequence
                    BER.encodeInteger(os1, BER.INTEGER, 1); // version
                    BER.encodeString(os1, BER.OCTETSTRING, "write".getBytes()); // commnunity
                    BER.encodeHeader(os1, (byte)setRequest, varLength + 13); // PDU header
                    BER.encodeInteger(os1, BER.INTEGER, rid); // Request ID
                    BER.encodeInteger(os1, BER.INTEGER, 0); // error status
                    BER.encodeInteger(os1, BER.INTEGER, 0); // error index
                    BER.encodeHeader(os1, BER.SEQUENCE, varLength+2); // sequence of
                    BER.encodeHeader(os1, BER.SEQUENCE, varLength); // sequence
                    BER.encodeOID(os1, BER.OID, this.oid); // oid

                    if (this.type == INTEGER) BER.encodeInteger(os1, BER.INTEGER, Integer.parseInt(this.value));
                    else if (this.type == STRING) BER.encodeString(os1, BER.OCTETSTRING, this.value.getBytes());
                    else if (this.type == OID) BER.encodeOID(os1, BER.OID, toInt(this.value));
                    else BER.encodeHeader(os1, BER.NULL, 0);
                    // value

                    bufLength = 2;
                    for (int i=0; i<1024;i++) {
                        if (buf1.get(i) != 0) {
                            bufLength++;
                        }
                        //calculate buffer length
                    }
                    byte[] retBuf = os1.getBuffer().array();
                    return retBuf;

                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            } else if (sel == 3) {
                try {
                    ByteBuffer buf2 = ByteBuffer.allocate(1024);
                    BEROutputStream os2 = new BEROutputStream(buf2);
                    ByteBuffer temp2 = ByteBuffer.allocate(1024);
                    BEROutputStream tempOs2 = new BEROutputStream(temp2);
                    BER.encodeOID(tempOs2, BER.OID, this.oid);
                    BER.encodeHeader(tempOs2, BER.NULL, 0);
                    int varLength = tempOs2.getBuffer().position();
                    // for calculate variable length...

                    BER.encodeHeader(os2, BER.SEQUENCE, varLength + 26); // sequence
                    BER.encodeInteger(os2, BER.INTEGER, 1); // version
                    BER.encodeString(os2, BER.OCTETSTRING, "public".getBytes()); // commnunity
                    BER.encodeHeader(os2, (byte)getNextRequest, varLength + 13); // PDU header
                    BER.encodeInteger(os2, BER.INTEGER, rid); // Request ID
                    BER.encodeInteger(os2, BER.INTEGER, 0); // error status
                    BER.encodeInteger(os2, BER.INTEGER, 0); // error index
                    BER.encodeHeader(os2, BER.SEQUENCE, varLength+2); // sequence of
                    BER.encodeHeader(os2, BER.SEQUENCE, varLength); // sequence
                    BER.encodeOID(os2, BER.OID, this.oid); // oid
                    BER.encodeHeader(os2, BER.NULL, 0); // value

                    bufLength = 3;
                    for (int i=0;i<oid.length;i++) {
                        if (oid[i] == 0)
                            bufLength++;
                    }
                    // if oid includes 0 values, buffer length should increase
                    for (int i=0; i<1024;i++) {
                        if (buf2.get(i) != 0) {
                            bufLength++;
                        }
                        //calculate buffer length
                    }
                    byte[] retBuf = os2.getBuffer().array();
                    return retBuf;
                }  catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }
            return null;
        }

        public String decodeData(byte[] buf) {
            BER.MutableByte mutData = new BER.MutableByte();
            String oid;
            int tag;
            ByteBuffer bBuf = ByteBuffer.wrap(buf);
            BERInputStream is = new BERInputStream(bBuf);

            if (sel == 3) {
                try {
                    BER.decodeHeader(is, mutData); // decode snmp header
                    BER.decodeInteger(is, mutData); // decode snmp ver
                    BER.decodeString(is, mutData); // decode snmp community
                    BER.decodeHeader(is, mutData); // decode pdu header
                    BER.decodeInteger(is, mutData); // decode rid
                    BER.decodeInteger(is, mutData); // decode error status
                    BER.decodeInteger(is, mutData); // decode error index
                    BER.decodeHeader(is, mutData); // decode sequence of
                    BER.decodeHeader(is, mutData); // decode sequence
                    curr = BER.decodeOID(is, mutData); // decode oid
                    oid = Arrays.toString(curr);
                    tag = is.read();
                    is.getBuffer().position(is.getBuffer().position() - 1);
                    isMore = isPossible(init, curr);
                    if (isMore) {
                        this.oid = curr;
                        switch (tag) {
                            case BER.INTEGER: {
                                return oid + " integer " + Integer.toString(BER.decodeInteger(is, mutData));
                            }
                            case BER.OCTETSTRING: {
                                String temp = new String(BER.decodeString(is, mutData));
                                return oid + " String " + temp;
                            }
                            case BER.OID: {
                                return oid + " OID " + Arrays.toString(BER.decodeOID(is, mutData));
                            }
                            case BER.NOSUCHOBJECT: {
                                return oid + " no such object";
                            }
                            case BER.TIMETICKS: {
                                Integer timeticks = BER.decodeInteger(is, mutData);
                                return oid + " timeticks " + timeticks.toString();
                            }
                            default: {
                                return oid + " type undefined";
                            }
                        }
                    }
                } catch (Exception e) {
                    System.out.println(e);
                    return null;
                }
            } else {
                try {
                    BER.decodeHeader(is, mutData); // decode snmp header
                    BER.decodeInteger(is, mutData); // decode snmp ver
                    BER.decodeString(is, mutData); // decode snmp community
                    BER.decodeHeader(is, mutData); // decode pdu header
                    BER.decodeInteger(is, mutData); // decode rid
                    BER.decodeInteger(is, mutData); // decode error status
                    BER.decodeInteger(is, mutData); // decode error index
                    BER.decodeHeader(is, mutData); // decode sequence of
                    BER.decodeHeader(is, mutData); // decode sequence
                    oid = Arrays.toString(BER.decodeOID(is, mutData)); // decode oid
                    tag = is.read();
                    is.getBuffer().position(is.getBuffer().position() - 1);
                    switch (tag) {
                        case BER.INTEGER: {
                            return oid + " integer " + Integer.toString(BER.decodeInteger(is, mutData));
                        }
                        case BER.OCTETSTRING: {
                            String temp = new String(BER.decodeString(is, mutData));
                            return oid + " String " + temp;
                        }
                        case BER.OID: {
                            return oid + " OID " + Arrays.toString(BER.decodeOID(is, mutData));
                        }
                        case BER.NOSUCHOBJECT: {
                            return oid + " no such object";
                        }
                        case BER.TIMETICKS: {
                            Integer timeticks = BER.decodeInteger(is, mutData);
                            return oid + " timeticks " + timeticks.toString();
                        }
                        default: {
                            return oid + " type undefined";
                        }
                    }

                } catch (Exception e) {
                    System.out.println(e);
                    return null;
                }
            }
            return "end";
        }
    }

    public static boolean isPossible(int[] init, int[] curr){
        for (int i=0; i<init.length; i++) {
            if (init[i] != curr[i]) return false;
        }
        return true;
    }

    public static int[] toInt(String oid) {
        String[] arr = oid.split("\\.");
        int[] ret = new int[arr.length];
        for (int i=0; i<arr.length; i++) {
            ret[i] = Integer.parseInt(arr[i]);
        }
        return ret;
    }
}
