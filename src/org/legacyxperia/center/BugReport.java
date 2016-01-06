/*
 * Copyright (C) 2013 Slimroms http://www.slimroms.net
 * Copyright (C) 2014 LegacyXperia Project
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.legacyxperia.center;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.Toast;

public class BugReport extends Fragment {

    private static final String LOG_TAG = "LegacyXperiaCenter";

    private View view;
    private LinearLayout bugreport;
    private LinearLayout bugtracker;

    private String mStrDevice;
    private static final String FILENAME_PROC_VERSION = "/proc/version";

    public File path;
    public String zipfile;
    public String logfile;
    public String last_kmsgfile;
    public String kmsgfile;
    public String radiofile;
    public String systemfile;

    boolean zipCreated;
    byte[] buf = new byte[1024];

    private final View.OnClickListener mActionLayouts = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v == bugreport) {
                preBugReport();
            } else if (v == bugtracker) {
                launchUrl("https://github.com/LegacyXperia/local_manifests/issues");
            }
        }
    };

    private void launchUrl(String url) {
        final Uri uriUrl = Uri.parse(url);
        final Intent openUrl = new Intent(Intent.ACTION_VIEW, uriUrl);
        getActivity().startActivity(openUrl);
    }

    private void preBugReport() {
        bugReport();
        dialog(zipCreated);
    }

    private void bugReport() {
        try {
            // Collect system information
            FileInputStream fstream = new FileInputStream("/system/build.prop");
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String strLine;

            while ((strLine = br.readLine()) != null) {
                String[] line = strLine.split("=");
                if (line[0].equalsIgnoreCase("ro.modversion")) {
                    mStrDevice = line[1];
                }
            }

            in.close();
        } catch (Exception e) {
             Toast.makeText(getActivity(), getString(R.string.system_prop_error),
                     Toast.LENGTH_LONG).show();
             e.printStackTrace();
        }

        String kernel = getFormattedKernelVersion();

        // Initialize log files
        File extdir = Environment.getExternalStorageDirectory();
        path = new File(extdir + "/LX/bugreport");
        File savefile = new File(path + "/system.log");
        File logcat = new File(path + "/logcat.log");
        File last_kmsg = new File(path + "/last_kmsg.log");
        File kmsg = new File(path + "/kmsg.log");
        File radio = new File(path + "/radio.log");
        File zip = new File(extdir + "/LX/bugreport.zip");

        systemfile = savefile.toString();
        logfile = logcat.toString();
        last_kmsgfile = last_kmsg.toString();
        kmsgfile = kmsg.toString();
        radiofile = radio.toString();
        zipfile = zip.toString();

        // Cleanup old logs
        if (sdAvailable() == 2) {
            try {
                // Create directory if it doesn't exist
                if (!path.exists()) {
                    path.mkdirs();
                }
                if (savefile.exists()) {
                    savefile.delete();
                }
                if (logcat.exists()) {
                    logcat.delete();
                }
                if (zip.exists()) {
                    zip.delete();
                }
                if (last_kmsg.exists()) {
                    last_kmsg.delete();
                }
                if (kmsg.exists()) {
                    kmsg.delete();
                }
                if (radio.exists()) {
                    radio.delete();
                }

                // Create system.log and output device info to it
                FileWriter outstream = new FileWriter(savefile);
                BufferedWriter save = new BufferedWriter(outstream);
                save.write("Device: " + mStrDevice + '\n' + "Kernel: " + kernel);
                save.close();
                outstream.close();

                // Get system logs and write them to files
                getLogs("logcat -d -f " + logcat + " *:V");
                getLogs("timeout -t 5 cat /proc/last_kmsg > " + last_kmsgfile);
                getLogs("timeout -t 5 cat /proc/kmsg > " + kmsgfile);
                getLogs("logcat -b radio -d -f " + radio);

                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                // Create zip file
                if (savefile.exists() && logcat.exists() && last_kmsg.exists() &&
                        kmsg.exists() && radio.exists()) {
                    zipCreated = zip();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(getActivity(), getString(R.string.sizer_message_sdnowrite),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private short sdAvailable() {
        // Check if SD card is available
        // Taken from developer.android.com
        short mExternalStorageAvailable;
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            // We can read and write the media
            mExternalStorageAvailable = 2;
        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            // We can only read the media
            mExternalStorageAvailable = 1;
        } else {
            // Something else is wrong. It may be one of many other states, but
            // all we need to know is we can neither read nor write
            mExternalStorageAvailable = 0;
        }
        return mExternalStorageAvailable;
    }

    private static String getFormattedKernelVersion() {
        try {
            return formatKernelVersion(readLine(FILENAME_PROC_VERSION));
        } catch (IOException e) {
            Log.e(LOG_TAG, "IO Exception when getting kernel version", e);
            return "Unavailable";
        }
    }

    public static String formatKernelVersion(String rawKernelVersion) {
        final String PROC_VERSION_REGEX =
            "Linux version (\\S+) " + /* group 1: "3.0.31-g6fb96c9" */
            "\\((\\S+?)\\) " +        /* group 2: "x@y.com" (kernel builder) */
            "(?:\\(gcc.+? \\)) " +    /* ignore: GCC version information */
            "(#\\d+) " +              /* group 3: "#1" */
            "(?:.*?)?" +              /* ignore: optional SMP, PREEMPT, and any CONFIG_FLAGS */
            "((Sun|Mon|Tue|Wed|Thu|Fri|Sat).+)"; /* group 4: "Thu Jun 28 11:02:39 PDT 2012" */

        Matcher m = Pattern.compile(PROC_VERSION_REGEX).matcher(rawKernelVersion);
        if (!m.matches()) {
            Log.e(LOG_TAG, "Regex did not match on /proc/version: " + rawKernelVersion);
            return "Unavailable";
        } else if (m.groupCount() < 4) {
            Log.e(LOG_TAG, "Regex match on /proc/version only returned " +
                    m.groupCount() + " groups");
            return "Unavailable";
        }
        return m.group(1) + " " + m.group(2) + " " + m.group(3);
    }

    private static String readLine(String filename) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(filename), 256);
        try {
            return reader.readLine();
        } finally {
            reader.close();
        }
    }

    private boolean zip () {
        String[] source = {systemfile, logfile, last_kmsgfile, kmsgfile, radiofile};
        try {
            ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipfile));
            for (String log : source) {
                String file = log.substring(log.lastIndexOf("/") + 1, log.length());
                FileInputStream in = new FileInputStream(log);
                out.putNextEntry(new ZipEntry(file));
                int len;
                while((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                out.closeEntry();
                in.close();
            }
            out.close();
        } catch(Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private void getLogs(String command) {
        try {
            Process process = Runtime.getRuntime().exec("su");
            DataOutputStream os = new DataOutputStream(process.getOutputStream());
            os.writeBytes(command + "\n");
            os.writeBytes("exit\n");
            os.flush();
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void dialog (boolean success) {
        final AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
        if (success) {
            alert.setMessage(R.string.report_infosuccess);
        } else {
            alert.setMessage(R.string.report_infofail);
        }
        alert.setPositiveButton(R.string.ok,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // action for ok
                        dialog.cancel();
                    }
                });
        alert.setCancelable(false);
        AlertDialog alertDialog = alert.create();
        alertDialog.show();
        keepDialog(alertDialog);
    }

    private void keepDialog(Dialog dialog){
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.copyFrom(dialog.getWindow().getAttributes());
        layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        dialog.getWindow().setAttributes(layoutParams);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        bugreport = (LinearLayout) view.findViewById(R.id.lx_bugreport);
        bugreport.setOnClickListener(mActionLayouts);

        bugtracker = (LinearLayout) view.findViewById(R.id.lx_bugtracker);
        bugtracker.setOnClickListener(mActionLayouts);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.lx_bugreport, container, false);
        return view;
    }
}
