package com.example.newssl;

import android.content.Context;
import android.os.AsyncTask;



class GetAsyncTask extends AsyncTask<Object, Integer, String> {
    private String result = null;
    private boolean done = false;

    GetAsyncTask() {
    }

    public boolean isDone() {
        return this.done;
    }

    public String getResult() {
        return this.result;
    }

    protected String doInBackground(Object... args) {
        this.result = Curl.get((String)args[0], (Context)args[1]);
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
