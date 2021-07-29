package actionset;

import com.sakurawald.silicon.Silicon;
import com.sakurawald.silicon.action.abstracts.CompileDetailAction;
import com.sakurawald.silicon.data.beans.request.CompileDetailRequest;
import com.sakurawald.silicon.data.beans.response.CompileDetailResponse;
import com.sakurawald.silicon.debug.LoggerManager;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.Objects;

public class HDU_CompileDetailAction extends CompileDetailAction {

    @Override
    public CompileDetailResponse execute(CompileDetailRequest compileDetailRequest) {

        LoggerManager.INSTANCE.logDebug("CompileDetailAction: request = " + compileDetailRequest);
        CompileDetailResponse compileDetailResponse;
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("https://acm.hdu.edu.cn/viewerror.php?rid=" + compileDetailRequest.getRunID())
                .get()
                .addHeader("Cookie", "PHPSESSID=" + compileDetailRequest.getSubmitAccount().getToken())
                .addHeader("Accept", "*/*")
                .addHeader("Cache-Control", "no-cache")
                .addHeader("Connection", "keep-alive")
                .build();
        Response response = null;
        try {
            response = client.newCall(request).execute();
            Document doc = Jsoup.parse(response.body().string());
            String sourceHTML = doc.select("body > table > tbody > tr:nth-child(4) > td > table > tbody > tr > td").html();

            // BaseURLTrans.
            sourceHTML = Silicon.getCurrentActionSet().transferBaseURL(sourceHTML);

            if (sourceHTML.contains("No such error message.")) {
                sourceHTML = "COMPILE SUCCESSFULLY!";
            }
            compileDetailResponse = new CompileDetailResponse(compileDetailRequest.getSubmitAccount(), compileDetailRequest.getRunID(), sourceHTML);
            LoggerManager.INSTANCE.logDebug("CompileDetailAction: response = " + response);
            return compileDetailResponse;
        } catch (IOException e) {
            LoggerManager.INSTANCE.reportException(e);
        } finally {
            Objects.requireNonNull(response.body()).close();
        }

        throw new RuntimeException("HDU_CompileDetailAction Failed.");
    }

}
