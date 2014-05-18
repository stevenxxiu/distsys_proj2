package simplestream;

import org.json.JSONException;
import org.json.JSONObject;

class ServerStatus {
    public boolean isLocal;
    public int numClients;
    public boolean hasRateLimiting;
    public boolean hasHandOver;

    ServerStatus(){}

    ServerStatus(boolean isLocal, int numClients, boolean hasRateLimiting, boolean hasHandOver) {
        this.isLocal = isLocal;
        this.numClients = numClients;
        this.hasRateLimiting = hasRateLimiting;
        this.hasHandOver = hasHandOver;
    }

    static ServerStatus fromJSON(JSONObject response) throws JSONException {
        ServerStatus res = new ServerStatus();
        if (response.getString("streaming").equals("local")) {
            res.isLocal = true;
        } else if(response.getString("streaming").equals("remote")) {
            res.isLocal = false;
        } else {
            throw new JSONException("streaming");
        }
        res.numClients = response.getInt("clients");
        if (response.getString("ratelimiting").equals("yes")) {
            res.isLocal = true;
        } else if(response.getString("ratelimiting").equals("no")) {
            res.isLocal = false;
        } else {
            throw new JSONException("ratelimiting");
        }
        if (response.getString("handover").equals("yes")) {
            res.isLocal = true;
        } else if(response.getString("handover").equals("no")) {
            res.isLocal = false;
        } else {
            throw new JSONException("handover");
        }
        return res;
    }

    JSONObject toJSON() throws JSONException {
        JSONObject res = new JSONObject();
        res.put("streaming", isLocal?"local":"remote");
        res.put("clients", this.numClients);
        res.put("ratelimiting", this.hasRateLimiting?"yes":"no");
        res.put("handover", this.hasHandOver?"yes":"no");
        return res;
    }
}
