package actionset;

import com.sakurawald.silicon.action.abstracts.SubmitAction;
import com.sakurawald.silicon.data.beans.request.SubmitRequest;
import com.sakurawald.silicon.data.beans.response.SubmitResponse;
import com.sakurawald.silicon.debug.LoggerManager;
import com.sakurawald.silicon.util.PluginUtil;
import okhttp3.*;

import java.io.IOException;
import java.util.Objects;

public class HDU_SubmitAction extends SubmitAction {


    @Override
    public SubmitResponse execute(SubmitRequest submitRequest) {

        LoggerManager.INSTANCE.logDebug("SubmitAction: " + submitRequest);

        // Base64 Trans.
        String code = submitRequest.getCode();
        code = PluginUtil.INSTANCE.fastEncodeURL_V2(code, "UTF-8");
        code = PluginUtil.INSTANCE.fastBase64Encode(code);

        OkHttpClient client = new OkHttpClient();
        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
        RequestBody body = RequestBody.create(mediaType, "check=0&_usercode=" + code + "&problemid=" + submitRequest.getProblemID() + "&language=" + submitRequest.getLanguage());
        Request request = new Request.Builder()
                .url("https://acm.hdu.edu.cn/submit.php?action=submit")
                .post(body)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .addHeader("Cookie", "PHPSESSID=" + submitRequest.getSubmitAccount().getToken())
                .addHeader("Accept", "*/*")
                .addHeader("Cache-Control", "no-cache")
                .addHeader("Connection", "keep-alive")
                .build();
        Response response = null;

        try {
            response = client.newCall(request).execute();
        } catch (IOException e) {
            LoggerManager.INSTANCE.reportException(e);
        } finally {
            Objects.requireNonNull(response.body()).close();
        }

        LoggerManager.INSTANCE.logDebug("SubmitAction: response = " + response);

        return new SubmitResponse();
    }


}
