package com.example.newssl;

import android.content.Context;
import android.os.AsyncTask;



class PostAsyncTask extends AsyncTask<Object, Integer, String> {
    private String result = null;
    private boolean done = false;

    PostAsyncTask() {
    }

    public boolean isDone() {
        return this.done;
    }

    public String getResult() {
        return this.result;
    }

    protected String doInBackground(Object... args) {
        this.result = Curl.post((String)args[0], (String)args[1], (String)args[2], (Context)args[3]);
        this.done = true;
        return this.result;
    }

    protected void onProgressUpdate(Integer... progress) {
    }

    protected void onPostExecute(String result) {
        this.result = result;
        this.done = true;
    }
}
