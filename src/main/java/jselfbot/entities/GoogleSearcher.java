/*
 * Copyright 2016 John Grosh (jagrosh).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jselfbot.entities;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;

import net.dv8tion.jda.core.utils.SimpleLog;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

/**
 * @author John Grosh (jagrosh)
 */
public class GoogleSearcher {

    public ArrayList<String> getDataFromGoogle(String query) {
        String request;
        try {
            request = "https://www.google.com/search?q=" + URLEncoder.encode(query, "UTF-8") + "&num=20";
        } catch (UnsupportedEncodingException ex) {
            System.err.println(ex);
            return null;
        }
        //System.out.println("Sending request..." + request);
        ArrayList<String> result;
        try {

            // need http protocol
            Document doc = Jsoup
                    .connect(request)
                    .userAgent(
                            "Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)")
                    .timeout(5000).get();

            // get all links
            Elements links = doc.select("a[href]");
            result = new ArrayList<>();
            links.stream().map((link) -> link.attr("href")).filter((temp) -> (temp.startsWith("/url?q="))).forEach((temp) -> {
                try {
                    //result.add(temp.substring(7,temp.indexOf("&sa="))+"\n");
                    String rslt = URLDecoder.decode(temp.substring(7, temp.indexOf("&sa=")), "UTF-8");
                    if (!rslt.contains("/settings/ads/preferences") && !rslt.startsWith("http://webcache.googleusercontent.com"))
                        result.add(rslt);
                } catch (UnsupportedEncodingException ex) {
                }
            });
        } catch (IOException e) {
            SimpleLog.getLog("Google").fatal("Search failure: " + e.toString());
            return null;
        }
        return result;
    }
}