package search.impl;

import main.SearchManager;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import search.FileHandler;
import search.Parser;
import search.WebSpider;
import vo.Program;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 请在此类中完成自己对象初始化工作，并注册
 */
public class Manager {
//    public static FileHandler fileHandler;
//    public static Parser parser;
//    public static WebSpider webSpider;
//    static String sendGet(String url)
//    { // 定义一个字符串用来存储网页内容
//        String result = "";
//        // 定义一个缓冲字符输入流
//        BufferedReader in = null;
//        try
//        {
//
//            URL realUrl = new URL(url);
//
//            URLConnection connection = realUrl.openConnection();
//
//            connection.connect();
//
//            in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
//
//            String line;
//            while ((line = in.readLine()) != null)
//            {
//                // 遍历抓取到的每一行并将其存储到result里面
//                result += line;
//            }
//        } catch (Exception e)
//        {
//            System.out.println("发送GET请求出现异常！" + e);
//            e.printStackTrace();
//        } // 使用finally来关闭输入流
//        finally
//        {
//            try
//            {
//                if (in != null)
//                {
//                    in.close();
//                }
//            } catch (Exception e2)
//            {
//                e2.printStackTrace();
//            }
//        }
//        return result;
//    }

    static{
        // TODO:在此初始化所需组件，并将其注册到SearchManager中供主函数使用
        // SearchManager.registFileHandler(new yourFileHandler());
        // SearchManager.registSpider(new yourSpider());

        SearchManager.registFileHandler(new FileHandler() {
            @Override
            public int program2File(List<Program> programList) {



                    try {
                        PrintWriter printText = new PrintWriter(new FileWriter("programList.txt"));
                        for(Program program : programList) {
                            printText.print("ID:" + program.getId() + "\n");
                            printText.print("Country:" + program.getCountry() + "\n");
                            printText.print("University:" + program.getUniversity() + "\n");
                            printText.print("School:" + program.getSchool() + "\n");
                            printText.print("ProgramName:" + program.getProgramName() + "\n");
                            printText.print("Degree:" + program.getDegree() + "\n");
                            printText.print("Homepage:" + program.getHomepage() + "\n");
                            printText.print("Address:" + program.getLocation() + "\n");
                            printText.print("E-mail:" + program.getEmail() + "\n");
                            printText.print("Phone:" + program.getPhoneNumber() + "\n");
                            printText.print("DDLWithAid:" + program.getDeadlineWithAid() + "\n");
                            printText.print("DDLWithoutAid:" + program.getDeadlineWithoutAid() + "\n");
                            printText.print("\n");
                        }
                        printText.close();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }



                return 0;
            }
        });


        SearchManager.registSpider(new WebSpider() {
            @Override
            public Parser getParser() {
                Parser parser = new Parser() {
                    @Override
                    public Program parseHtml(String html) {
                        List<String> moreLink = new LinkedList<String>();
                        Program programInfo = new Program();
                        programInfo.setCountry("America");
                        programInfo.setUniversity("Colorado State University");
                        try {
                            Document nextPage = Jsoup.connect(html).userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:52.0) Gecko/20100101 Firefox/52.0)").get();
                            Elements programName = nextPage.select("#slide-header ").select("div").select("div").select("div").select("h3");
                            Elements school = nextPage.select("#main").select("article").select("div").select("div").select("ul:nth-child(3)").select("li:nth-child(1)");
                            Elements liLink = nextPage.select("#main").select("article").select("div").select("div").select("ul:nth-child(6)");
                            String lisLink = liLink.toString();
                            String nameStr = programName.text();
                            String schoolStr = school.text();
                            programInfo.setSchool(school.text());
                            programInfo.setProgramName(programName.text());
                            System.out.println(nameStr + schoolStr);
                            Pattern moreDetails = Pattern.compile("<li><a.*?href=\"(.*?)\">(.*?)</a>.*?</li>");
                            Matcher moreMatcher = moreDetails.matcher(lisLink);
                            while (moreMatcher.find()) {
                                moreLink.add(moreMatcher.toMatchResult().group(1));
                                programInfo.setDegree(moreMatcher.toMatchResult().group(2) + "\n");
                                System.out.println(moreMatcher.toMatchResult().group(0));
                            }
                            if (moreLink.size() > 0) {
                                Document detailPage = Jsoup.connect(moreLink.get(0)).userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:52.0) Gecko/20100101 Firefox/52.0)").get();
                                Elements EMail = detailPage.select("#sidebar").select("aside").select("div:nth-child(3)").select("div.program-info-ul").select("ul").select("li.email-icon").select("a");
                                Elements phone = detailPage.select("#sidebar").select("aside").select("div:nth-child(3)").select("div.program-info-ul").select("ul").select("li.phone-icon").select("a");
                                Elements homePage = detailPage.select("#centered-icons ").select("li.globe-icon").select("a");
                                Elements location = detailPage.select("#centered-icons").select("li.pin-icon").select("a").select("span").select("strong");
                                Elements deadLineA = detailPage.select("#main").select("article").select("div").select("div").select("ul:nth-child(16)");
                                Elements deadLine = deadLineA.select("#main").select("article").select("div").select("div").select("ul:nth-child(17)");

                                programInfo.setLocation(location.text());
                                programInfo.setHomepage(homePage.attr("abs:href"));
                                programInfo.setPhoneNumber(phone.text());
                                programInfo.setEmail(EMail.attr("abs:href"));
                                programInfo.setDeadlineWithoutAid(deadLine.text());
                                programInfo.setDeadlineWithAid(deadLineA.text());

                            } else {
                                programInfo.setLocation(null);
                                programInfo.setHomepage(null);
                                programInfo.setPhoneNumber(null);
                                programInfo.setEmail(null);
                                programInfo.setDeadlineWithoutAid(null);
                                programInfo.setDeadlineWithAid(null);
                            }
                            System.out.println(lisLink);

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return programInfo;
                    }

                };
                return parser;
            }

            @Override
            public List<String> getHtmlFromWeb() {
                List<String> programList = new LinkedList<String>();
                List<Program> programs = new LinkedList<Program>();
                // 定义即将访问的链接

               try {
                Document document = Jsoup.connect("http://graduateschool.colostate.edu/programs/").userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:52.0) Gecko/20100101 Firefox/52.0)").get();
                Elements mainPage[] = new Elements[64];
                for(int i =0 ;i<64;i++){
                    int t=i+1;
                    mainPage[i] = document.select("#main").select("article").select("div:nth-child("+t+")").select("a");
                }
                for(int i =0; i<64;i++){
                    for(Element url:mainPage[i]){
                        String URL = url.attr("abs:href");
                        programList.add(URL);
                        System.out.println(i+URL);
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

                return programList;
            }
        });



    }
}
