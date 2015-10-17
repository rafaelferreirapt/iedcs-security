package player;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.util.HashMap;
import java.util.Map;
import java.io.PrintWriter;
import org.json.*;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;

public class Requests {
    
    private static final String USER_AGENT = "Mozilla/5.0";
    private static final String LOGIN_ENDPOINT = IEDCSPlayer.getBaseUrl() + "api/v1/auth/login/";
      
    public static void login(String email, String password) throws MalformedURLException, ProtocolException, IOException{
        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("email", email);
        parameters.put("password", password);
        post(LOGIN_ENDPOINT, parameters);
    }
    
    public static void setCSRF() throws MalformedURLException, IOException{
        
    }
    
    public static void post(String url, HashMap<String, String> parameters) throws MalformedURLException, ProtocolException, IOException{
        HttpClient client = HttpClientBuilder.create().build();
        HttpPost post = new HttpPost(url);

        // add header
        post.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2228.0 Safari/537.36");
        post.setHeader("Accept-Language", "application/json");
        post.setHeader("Content-Type", "application/json;charset=UTF-8");
        
        // to parse parameters to JSON 
        JSONObject json_obj=new JSONObject();
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            try {
                json_obj.put(key,value);
            } catch (JSONException e) {
                e.printStackTrace();
            }                           
        }
        StringEntity se = new StringEntity(json_obj.toString(), "UTF-8");
        se.setContentType("application/json; charset=UTF-8");
        post.setEntity(se);

        HttpResponse response = client.execute(post);
        System.out.println("\nSending 'POST' request to URL : " + url);
        System.out.println("Post parameters : " + post.getEntity());
        System.out.println("Response Code : " + 
                            response.getStatusLine().getStatusCode());
        
        // print cookies

        System.out.println("Printing Response Header...\n");

        Header[] headers = response.getAllHeaders();
        for (Header header : headers) {
                System.out.println("Key : " + header.getName() 
                           + " ,Value : " + header.getValue());

        }

        System.out.println("\nGet Response Header By Key ...\n");
        String server = response.getFirstHeader("Server").getValue();

        // output file
        BufferedReader rd = new BufferedReader(
                new InputStreamReader(response.getEntity().getContent()));

        StringBuffer result = new StringBuffer();
        String line = "";
        while ((line = rd.readLine()) != null) {
                result.append(line);
        }

        PrintWriter fs = new PrintWriter("output.html");
        fs.print(result.toString());
        fs.close();
    }
}