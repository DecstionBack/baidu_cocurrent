/**
 * Created by decstionback on 16-8-3.
 */
/**
 * Created by decstionback on 16-8-2.
 */
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

class baidu_jsoup_cocurrent implements Runnable{
    Thread t;
    int start;
    int end;
    List<String> Keywords;
    baidu_jsoup_cocurrent(int start, int end, List<String> Keywords){
        this.start = start;
        this.end = end;
        this.Keywords = Keywords;
        t = new Thread(this, "Demo Thread");
        System.out.println("Child thread: " + t);
        t.start();
    }

    public void run() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String url = "jdbc:postgresql://192.168.140.200:5432/keyword_2.0?character-encoding=UTF-8";
        try {
            Class.forName("org.postgresql.Driver").newInstance();
            Connection conn = DriverManager.getConnection(url, "postgres", "xlive911");
            if (!conn.isClosed())
                System.out.println("Connecting to the Database successfully!");
            Statement insertstatement = conn.createStatement();
            for (int i = start; i < end; i++){
                String keyword = Keywords.get(i).split(":")[1];
                int topicsID = Integer.parseInt(Keywords.get(i).split(":")[0]);
                System.out.println(i + ":" + keyword);
                try {
                    Document doc = Jsoup.connect("http://www.baidu.com/s?wd=\"" + keyword + "\"").get();
                    Elements elements = doc.select("div.c-container");
                    System.out.println(elements.size());
                    String title = null;
                    String titleurl = null;
                    String trueurl = null;
                    String content = null;
                    String sql = null;
                    for (Element element : elements) {
                        try {
                            title = element.select("h3 a").text();
                            titleurl = element.select("h3 a").attr("href");
                            System.out.println(titleurl);
                            content = element.select("div.c-abstract").text();
                            trueurl = getTrueUrl(titleurl);
                            if (trueurl == null)
                                continue;
                            if (trueurl.contains("www.baidu.com"))
                                continue;
                        }catch(Exception e2){
                            e2.printStackTrace();
                            continue;
                        }
                        System.out.println(title);
                        System.out.println(trueurl);
                        System.out.println(content);
                        sql = "insert into sample_urls(topics_id,keyword,url,info_type,title,abstract,updatetime) values("
                                + topicsID + ",'" + keyword + "','" + trueurl + "','百度','" + title + "','" + content + "','" + df.format(new Date()) + "')";
                        try {
                           //insertstatement.execute(sql);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }catch(Exception e){
                    e.printStackTrace();
                    continue;
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    public static String getTrueUrl(String url){
        String realUrl = null;
        try{
            URL weburl = new URL(url);
            HttpURLConnection conn2=(HttpURLConnection)weburl.openConnection();
            conn2.setConnectTimeout(10000);
            conn2.getResponseCode();
            realUrl=conn2.getURL().toString();
            conn2.disconnect();
            System.out.println("真实URL:"+realUrl);
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
        return realUrl;

    }
}

public class baidu_jsoup {
    public static String preurl = "https://www.baidu.com/s?wd=";

    public static List<String> getKeywords(){
        List<String> Keywords =  new ArrayList<String>();
        String url  ="jdbc:postgresql://192.168.140.200:5432/keyword_2.0?character-encoding=UTF-8";
        try{
            Class.forName("org.postgresql.Driver").newInstance();
            Connection conn = DriverManager.getConnection(url, "postgres", "xlive911");
            if (!conn.isClosed())
                System.out.println("Connecting to the Database successfully!");
            Statement statement = conn.createStatement();
            //读取news
            String sql = "select * from sample_keywords order by id desc";
            ResultSet rs = statement.executeQuery(sql);
            while(rs.next()) {
                Keywords.add(rs.getString("sample_topics_id") + ":" + rs.getString("sample_keyword"));
            }

            rs.close();
            statement.close();
            conn.close();
        } catch(Exception e){
            e.printStackTrace();
        }
        return Keywords;
    }
    public static void main(String[] args) throws  Exception{
        List<String> Keywords = getKeywords();
        System.out.println(Keywords.size());
        for (int i = 0; i< 10; i++) {
            new baidu_jsoup_cocurrent(i * (Keywords.size() / 10 + 1) , (i + 1) * (Keywords.size() / 10 + 1), Keywords);
            Thread.sleep(5000);
        }
    }
}
