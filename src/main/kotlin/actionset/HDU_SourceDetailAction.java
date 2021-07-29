package actionset;

import com.sakurawald.silicon.Silicon;
import com.sakurawald.silicon.action.abstracts.SourceDetailAction;
import com.sakurawald.silicon.data.beans.SubmitResult;
import com.sakurawald.silicon.data.beans.request.SourceDetailRequest;
import com.sakurawald.silicon.data.beans.response.SourceDetailResponse;
import com.sakurawald.silicon.debug.LoggerManager;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.Objects;

public class HDU_SourceDetailAction extends SourceDetailAction {

    @Override
    public SourceDetailResponse execute(SourceDetailRequest sourceDetailRequest) {

        LoggerManager.INSTANCE.logDebug("SourceDetailAction: request = " + sourceDetailRequest);

        SourceDetailResponse sourceDetailResponse = null;

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("https://acm.hdu.edu.cn/viewcode.php?rid=" + sourceDetailRequest.getRunID())
                .get()
                .addHeader("Cookie", "PHPSESSID=" + sourceDetailRequest.getSubmitAccount().getToken())
                .addHeader("Accept", "*/*")
                .addHeader("Cache-Control", "no-cache")
                .addHeader("Connection", "keep-alive")
                .build();

        Response response = null;
        try {
            response = client.newCall(request).execute();

            Document doc = Jsoup.parse(response.body().string());


            /** Get SubmitResult. **/
            Elements headInfo_E = doc.select("body > table > tbody > tr:nth-child(6) > td");
            headInfo_E.select("div > div:nth-child(4)").remove();

            Elements head = doc.select("head");
            head.append("<script type=\"text/javascript\" src=\"/js/cpp_coderender.js\"></script>");
            head.append("<script type=\"text/javascript\" src=\"/js/utility.js\"></script>");
            SubmitResult submitResult = Silicon.getCurrentActionSet().getSubmitResult(headInfo_E.html());

            /** Get Source HTML. **/
            String sourceHTML = head.outerHtml() + headInfo_E.outerHtml();

            // BaseURLTrans.
            sourceHTML = Silicon.getCurrentActionSet().transferBaseURL(sourceHTML);
            String source = doc.select("body > table > tbody > tr:nth-child(6) > td > div > div:nth-child(3) > pre").text();

            if (headInfo_E.isEmpty()) {
                sourceHTML = "NO PERMISSION.";
            }

            sourceDetailResponse = new SourceDetailResponse(sourceDetailRequest.getSubmitAccount(), sourceDetailRequest.getRunID(), sourceHTML, submitResult, source);
            LoggerManager.INSTANCE.logDebug("SourceDetailAction: response = " + response);
            return sourceDetailResponse;
        } catch (IOException e) {
            LoggerManager.INSTANCE.reportException(e);
        } finally {
            Objects.requireNonNull(response.body()).close();
        }

        throw new RuntimeException("HDU_SourceDetailAction Failed.");
    }

    @Override
    public boolean supportAccountClone() {
        return false;
    }

}
