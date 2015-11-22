package martinmatko.anatomy;

import android.os.AsyncTask;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

class DownloadFilesTask extends AsyncTask<URL, Integer, DataInputStream> {
    protected DataInputStream doInBackground(URL... urls) {
        int count = urls.length;
        URL u = urls[0];
        try {
            DataInputStream stream = new DataInputStream(u.openStream());
            return stream;
        } catch (IOException e) {
            e.printStackTrace();
        }

//        long totalSize = 0;
//        for (int i = 0; i < count; i++) {
//            totalSize += Downloader.downloadFile(urls[i]);
//            publishProgress((int) ((i / (float) count) * 100));
//            // Escape early if cancel() is called
//            if (isCancelled()) break;
//        }
        return null;
    }

//    protected void onProgressUpdate(Integer... progress) {
//        setProgressPercent(progress[0]);
//    }
//
//    protected void onPostExecute(Long result) {
//        showDialog("Downloaded " + result + " bytes");
//    }
}
