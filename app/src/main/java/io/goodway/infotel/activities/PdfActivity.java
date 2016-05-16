package io.goodway.infotel.activities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.joanzapata.pdfview.PDFView;
import com.joanzapata.pdfview.listener.OnDrawListener;
import com.joanzapata.pdfview.listener.OnLoadCompleteListener;
import com.joanzapata.pdfview.listener.OnPageChangeListener;

import java.io.IOException;
import java.util.Collection;

import io.goodway.infotel.R;
import io.goodway.infotel.model.communication.Channel;
import io.goodway.infotel.model.communication.Message;
import io.goodway.infotel.sync.HttpRequest;
import io.goodway.infotel.utils.Constants;
import io.goodway.infotel.utils.File;
import okhttp3.Call;
import okhttp3.Response;

/**
 * Created by antoine on 5/11/16.
 */
public class PdfActivity extends AppCompatActivity implements OnDrawListener, OnLoadCompleteListener, OnPageChangeListener {

    // GCM SERVICE
    // Allow communication with server to display notifications to device
    private static final String TAG = "PdfActivity";
    private PDFView pdfView;

    private int attachment_type;
    private String attachment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf);
        attachment = getIntent().getExtras().getString(Constants.ATTACHMENT);

        ProgressDialog mProgressDialog;

        // instantiate it within the onCreate method
        mProgressDialog = new ProgressDialog(PdfActivity.this);
        mProgressDialog.setMessage(getString(R.string.downloading));
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setCancelable(true);


// execute this when the downloader must be fired
        final File.DownloadTask downloadTask = new File.DownloadTask(PdfActivity.this, File.getNameFromUrl(attachment), mProgressDialog, new HttpRequest.Action<java.io.File>() {
            @Override
            public void action(java.io.File file) {
                pdfView.fromFile(file)
                        .pages(0, 2, 1, 3, 3, 3)
                        .defaultPage(1)
                        .showMinimap(false)
                        .enableSwipe(true)
                        .onDraw(PdfActivity.this)
                        .onLoad(PdfActivity.this)
                        .onPageChange(PdfActivity.this)
                        .load();
            }
        });
        downloadTask.execute(attachment);

        mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                downloadTask.cancel(true);
            }
        });
    }

    @Override
    public void onLayerDrawn(Canvas canvas, float pageWidth, float pageHeight, int displayedPage) {

    }

    @Override
    public void loadComplete(int nbPages) {

    }

    @Override
    public void onPageChanged(int page, int pageCount) {

    }
}
