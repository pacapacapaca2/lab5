package com.example.lab5;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private EditText journalIdInput;
    private Button downloadButton, viewButton, deleteButton;
    private TextView statusText;
    private File downloadedFile;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        journalIdInput = findViewById(R.id.journalIdEditText);
        downloadButton = findViewById(R.id.downloadButton);
        viewButton = findViewById(R.id.viewButton);
        deleteButton = findViewById(R.id.deleteButton);
        statusText = findViewById(R.id.status_text);

        downloadButton.setOnClickListener(v -> {
            String journalId = journalIdInput.getText().toString();
            if (!journalId.isEmpty()) {
                createDirectoryAndDownload(journalId);
            } else {
                statusText.setText("Пожалуйста, введите номер журнала!");
            }
        });

        viewButton.setOnClickListener(v -> {
            if (downloadedFile != null && downloadedFile.exists()) {
                Uri fileUri = FileProvider.getUriForFile(MainActivity.this,
                        getApplicationContext().getPackageName() + ".provider", downloadedFile);
                Intent viewIntent = new Intent(Intent.ACTION_VIEW);
                viewIntent.setDataAndType(fileUri, "application/pdf");
                viewIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                try {
                    startActivity(Intent.createChooser(viewIntent, "Открыть PDF в приложении"));
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(MainActivity.this, "Не установлено приложение для просмотра PDF-файлов", Toast.LENGTH_SHORT).show();
                }
            }
        });

        deleteButton.setOnClickListener(v -> {
            if (downloadedFile != null && downloadedFile.exists()) {
                if (downloadedFile.delete()) {
                    viewButton.setEnabled(false);
                    deleteButton.setEnabled(false);
                    statusText.setText("Файл удален");
                }
            }
        });
    }

    // Создание директории и запуск загрузки
    private void createDirectoryAndDownload(String journalId) {
        File dir = new File(getExternalFilesDir(null), "JournalDownloads");
        if (!dir.exists()) {
            boolean isCreated = dir.mkdirs();  // Создаем директорию, если ее нет
            if (isCreated) {
                Log.d("Directory", "Directory created successfully");
            } else {
                Log.d("Directory", "Failed to create directory");
                Toast.makeText(this, "Ошибка при создании директории!", Toast.LENGTH_SHORT).show();
                return;  // Выход, если не удалось создать директорию
            }
        }
        // Запуск задачи по загрузке файла
        String fileUrl = "https://ntv.ifmo.ru/file/journal/" + journalId + ".pdf";
        new DownloadFileTask(MainActivity.this).execute(fileUrl, journalId);
    }

    private class DownloadFileTask extends AsyncTask<String, Void, Boolean> {
        private Context context;
        private String errorMessage = "";

        public DownloadFileTask(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            statusText.setText("Файл загружается...");
            viewButton.setEnabled(false);
            deleteButton.setEnabled(false);
        }

        @Override
        protected Boolean doInBackground(String... params) {
            String fileUrl = params[0];
            String journalId = params[1];

            try {
                File dir = new File(context.getExternalFilesDir(null), "JournalDownloads");
                if (!dir.exists()) {
                    dir.mkdirs();
                    if (!dir.mkdir()) {
                        errorMessage = "Ошибка при создании директории!";
                        return false;
                    }
                }

                downloadedFile = new File(dir, journalId);

                URL url = new URL(fileUrl);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.connect();
                int responseCode = urlConnection.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    String contentType = urlConnection.getContentType();
                    if (!"application/pdf".equals(contentType)) {
                        errorMessage = "Журнал не найден!";
                        return false;
                    }

                    InputStream inputStream = urlConnection.getInputStream();
                    FileOutputStream fileOutput = new FileOutputStream(downloadedFile);

                    byte[] buffer = new byte[1024];
                    int bufferLength;

                    while ((bufferLength = inputStream.read(buffer)) > 0) {
                        fileOutput.write(buffer, 0, bufferLength);
                    }
                    try {
                        if (inputStream != null) {
                            inputStream.close();
                        }
                        if (fileOutput != null) {
                            fileOutput.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return true;

                } else {
                    if (responseCode == 404) {
                        errorMessage ="Журнал не найден!";
                        return false;
                    }
                    else {
                        errorMessage = "Ошибка:" + responseCode;
                        return false;
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
                errorMessage = "Ошибка при скачивании файла: " + e.getMessage();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                viewButton.setEnabled(true);
                deleteButton.setEnabled(true);
                statusText.setText("Скачивание файла завершено!");
            } else {
                statusText.setText(errorMessage);
            }
        }
    }
}