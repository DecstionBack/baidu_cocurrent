import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by lenovo on 2016/8/8.
 */
public class test {
    public static String getTrueUrl(String url){
        String realUrl = null;
        try{
            URL weburl = new URL(url);
            HttpURLConnection conn2=(HttpURLConnection)weburl.openConnection();
            conn2.setConnectTimeout(10000);
            conn2.getResponseCode();
            System.out.println(conn2.getURL());
            realUrl=conn2.getURL().toString();
            conn2.disconnect();
            System.out.println("真实URL:"+realUrl);
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
        return realUrl;

    }
    public static void main(String[] args){
        String trueurl= getTrueUrl("http://www.baidu.com/link?url=1O87WA3BjdSH4OPxmjduMHgRvIo9-9JA9PPmP2f3mRMZYr8stWU7jkfCgp2NJn5SmslNER42R9JcZUuEiALV1_");
        System.out.println(trueurl);


    }
}
