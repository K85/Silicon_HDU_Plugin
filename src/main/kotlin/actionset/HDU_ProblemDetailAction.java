package actionset;

import com.sakurawald.silicon.Silicon;
import com.sakurawald.silicon.action.abstracts.ProblemDetailAction;
import com.sakurawald.silicon.data.beans.request.ProblemDetailRequest;
import com.sakurawald.silicon.data.beans.response.ProblemDetailResponse;
import com.sakurawald.silicon.debug.LoggerManager;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.Objects;

public class HDU_ProblemDetailAction extends ProblemDetailAction {

    @Override
    public ProblemDetailResponse execute(ProblemDetailRequest problemDetailRequest) {
        LoggerManager.INSTANCE.logDebug("ProblemDetail: request = " + problemDetailRequest);
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("https://acm.hdu.edu.cn/showproblem.php?pid=" + problemDetailRequest.getProblem().getProblemID())
                .get()
                .addHeader("Accept", "*/*")
                .addHeader("Cache-Control", "no-cache")
                .addHeader("Connection", "keep-alive")
                .addHeader("cache-control", "no-cache")
                .build();


        Response response = null;
        try {
            response = client.newCall(request).execute();
        } catch (IOException e) {
            LoggerManager.INSTANCE.reportException(e);
        }

        ProblemDetailResponse problemDetailResponse = null;
        try {

            String problemDetailHtml = response.body().string();
            Document doc = Jsoup.parse(problemDetailHtml);
            Elements head = doc.select("head");
            Elements doc_elements = doc.select("body > table > tbody > tr:nth-child(4) > td");
            doc_elements.select("center").remove();

            problemDetailHtml = head.outerHtml() + doc_elements.outerHtml();

            // BaseURLTrans.
            problemDetailHtml = Silicon.getCurrentActionSet().transferBaseURL(problemDetailHtml);
            problemDetailResponse = new ProblemDetailResponse(problemDetailRequest.getProblem(), problemDetailHtml);
            LoggerManager.INSTANCE.logDebug("ProblemDetailAction: response = " + problemDetailResponse);
            return problemDetailResponse;
        } catch (IOException e) {
            LoggerManager.INSTANCE.reportException(e);
        } finally {
            Objects.requireNonNull(response.body()).close();
        }

        throw new RuntimeException("HDU_ProblemDetailAction Failed.");
    }

}
