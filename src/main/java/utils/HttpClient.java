package utils;

import burp.BurpExtender;
import burp.IHttpRequestResponse;
import entity.HttpService;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class HttpClient {
    private String url;
    private String protocol;
    private String method;
    private String httpversion;
    private String host;
    private int port;
    private String path;
    private HttpService service;
    private Map<String,String> headers = new HashMap<String, String>();
    private String data;
    private String raw;
    private byte[] byteImg;

    public HttpClient(String url,String raw,byte[] byteImg){
        this.url = url;
        this.raw = raw;
        this.byteImg = byteImg;
        parseLabel();
        parser();

    }

    public String getHttpService(){
        return service.toString();
    }


    private void parser(){
        if(Util.isURL(this.url)){
            service = new HttpService(this.url);
            String[] rawsArray = this.raw.split(System.lineSeparator());
            try {
                for (int i = 0; i < rawsArray.length; i++) {
                    if (i == 0) {
                        this.method = rawsArray[0].split(" ")[0];
                        this.path = rawsArray[0].split(" ")[1];
                        this.httpversion = rawsArray[0].split(" ")[2];
                    } else if (this.method.equals("POST") && i == rawsArray.length - 1) {
                        this.data = rawsArray[i].trim();
                    } else {
                        if (rawsArray[i].indexOf(": ") > 0) {
                            String key = rawsArray[i].split(": ")[0];
                            String value = rawsArray[i].split(": ")[1];
                            this.headers.put(key.trim(), value.trim());
                        }
                    }
                }
            }catch (Exception e){
                BurpExtender.stdout.println(e.getMessage());
            }
        }
    }

    private String buildRequstPackget(){
        if(method.equals("POST")) {
            int length = data.length();
            headers.put("Content-Length", String.valueOf(length));
        }
        String reqLine = String.format("%s %s %s",method,path,httpversion);
        reqLine += System.lineSeparator();
        for(Map.Entry<String,String> header:headers.entrySet()){
            String line = String.format("%s: %s",header.getKey(),header.getValue());
            reqLine += line;
            reqLine += System.lineSeparator();
        }

        reqLine += System.lineSeparator();
        reqLine += data;
        return reqLine;
    }

    /**
     * 解析标签，可以参考下
     */
    public void parseLabel(){
        if(raw.indexOf("<urlencode><base64>{IMG_RAW}</base64></urlencode>")>0){
            String base64Img = Util.base64Encode(byteImg);
            String strImg = Util.URLEncode(base64Img);
            raw = raw.replace("<urlencode><base64>{IMG_RAW}</base64></urlencode>",strImg);
        }
    }

    public byte[] doReust(){
        byte[] req = buildRequstPackget().getBytes();
        try {
            IHttpRequestResponse reqrsp = BurpExtender.callbacks.makeHttpRequest(service, req);
            byte[] response = reqrsp.getResponse();
            return response;
        }catch (Exception e){
            e.printStackTrace();
            BurpExtender.stderr.println(e);
        }
        return null;

    }
}