package actionset;

import com.sakurawald.silicon.action.abstracts.StatusAction;
import com.sakurawald.silicon.data.beans.Page;
import com.sakurawald.silicon.data.beans.request.StatusRequest;
import com.sakurawald.silicon.data.beans.response.StatusResponse;
import com.sakurawald.silicon.data.beans.response.SubmitResponse;
import com.sakurawald.silicon.debug.LoggerManager;
import com.sakurawald.silicon.util.HttpUtil;
import com.sakurawald.silicon.util.PluginUtil;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

public class HDU_StatusAction extends StatusAction {

    /**
     * 存储Prev Page 和 Next Page的参数.
     **/
    private static String prevPageParam = null;
    private static String nextPageParam = null;


    @Override
    public StatusResponse execute(StatusRequest requestBean) {

        LoggerManager.INSTANCE.logDebug("StatusAction: request = " + requestBean);

        ArrayList<SubmitResponse> submitResponseList = null;

        /** Calc UserID & ProblemID. **/
        if (requestBean.getUserID() == null) requestBean.setUserID("");
        if (requestBean.getProblemID() == null) requestBean.setProblemID("");

        /** Calc Top. **/
        String pageControl = requestBean.getPage();
        if (pageControl.equals(Page.HOME_PAGE)) pageControl = "";
        if (pageControl.equals(Page.PREV_PAGE) && prevPageParam != null)
            pageControl = "last=" + prevPageParam;
        if (pageControl.equals(Page.NEXT_PAGE) && nextPageParam != null) pageControl = "first=" + nextPageParam;


        OkHttpClient client = new OkHttpClient();
        String URL = "https://acm.hdu.edu.cn/status.php?" + pageControl + "&pid=" + requestBean.getProblemID() + "&user=" + requestBean.getUserID() + "&lang=0&status=0";
        Request request = new Request.Builder()
                .url(URL)
                .get()
                .addHeader("Accept", "*/*")
                .addHeader("Cache-Control", "no-cache")
                .addHeader("Host", "acm.hdu.edu.cn")
                .addHeader("Connection", "keep-alive")
                .addHeader("Cookie", "PHPSESSID=" + requestBean.getRequestAccount().getToken())
                .build();

        Response response = null;
        try {
            response = client.newCall(request).execute();

            /** HTML Analyse. **/
            String HTML = response.body().string();
            Document doc = Jsoup.parse(HTML);

            /** PrevPageButton & NextPageButton. **/
            Elements pageButtons = doc.select("#fixed_table > p");
            Elements prevButton = pageButtons.select("a:nth-child(2)");
            Elements nextButton = pageButtons.select("a:nth-child(3)");
            String prevButtonString = prevButton.attr("href");
            String nextButtonString = nextButton.attr("href");

//            System.out.println("prevButtonString = " + prevButtonString);
//            System.out.println("nextButtonString = " + nextButtonString);

            try {
                prevPageParam = HttpUtil.INSTANCE.betweenString(prevButtonString, "last=", "&");
            } catch (Exception e) {
                prevPageParam = "";
            }

            try {
                nextPageParam = HttpUtil.INSTANCE.betweenString(nextButtonString, "first=", "&");
            } catch (Exception e) {
                nextPageParam = "";
            }

            /** Status. **/
            submitResponseList = PluginUtil.INSTANCE.fastGetSubmitResponseList(HTML,
                    "#fixed_table > table > tbody > tr",
                    0, 8, 3,
                    2, 5, 4, 7
                    , 6, 1);


        } catch (IOException e) {
            LoggerManager.INSTANCE.reportException(e);
        } finally {
            Objects.requireNonNull(response.body()).close();
        }

        LoggerManager.INSTANCE.logDebug("StatusAction: response = " + response);
        return new StatusResponse(submitResponseList);
    }

    @Override
    public boolean supportStatusPageSkip() {
        return false;
    }

}
