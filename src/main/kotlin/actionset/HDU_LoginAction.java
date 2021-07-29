package actionset;

import com.sakurawald.silicon.action.abstracts.LoginAction;
import com.sakurawald.silicon.data.beans.request.LoginRequest;
import com.sakurawald.silicon.data.beans.response.LoginResponse;
import com.sakurawald.silicon.debug.LoggerManager;
import okhttp3.*;

import java.io.IOException;
import java.util.Objects;

public class HDU_LoginAction extends LoginAction {

    public static void confirmRequest(LoginRequest loginRequest, String token) {

        OkHttpClient client = new OkHttpClient();
        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
        RequestBody body = RequestBody.create(mediaType, "username=" + loginRequest.getUserID() + "&userpass=" + loginRequest.getPassword() + "&login=Sign%20In");
        Request request = new Request.Builder()
                .url("https://acm.hdu.edu.cn/userloginex.php?action=login")
                .post(body)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .addHeader("Accept", "*/*")
                .addHeader("Cache-Control", "no-cache")
                .addHeader("Cookie", "PHPSESSID=" + token)
                .build();
        Response response = null;
        try {
            response = client.newCall(request).execute();
        } catch (IOException e) {
            LoggerManager.INSTANCE.reportException(e);
        } finally {
            Objects.requireNonNull(response.body()).close();
        }

    }

    public LoginResponse execute(LoginRequest loginRequest) {
        LoggerManager.INSTANCE.logDebug("LoginAction: " + loginRequest);
        OkHttpClient client = new OkHttpClient();
        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
        RequestBody body = RequestBody.create(mediaType, "username=" + loginRequest.getUserID() + "&userpass=" + loginRequest.getPassword() + "&login=Sign%20In");
        Request request = new Request.Builder()
                .url("http://acm.hdu.edu.cn/userloginex.php?action=login")
                .post(body)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .addHeader("Accept", "*/*")
                .addHeader("Cache-Control", "no-cache")
                .addHeader("Host", "acm.hdu.edu.cn")
                .build();

        String token = null;
        try {
            Response response = client.newCall(request).execute();
            LoggerManager.INSTANCE.logDebug("HTTP = " + response.body().string());

            token = LoginAction.getLoginToken(response, "PHPSESSID");
            LoggerManager.INSTANCE.logDebug("Get Token: " + token);

        } catch (IOException e) {
            LoggerManager.INSTANCE.reportException(e);
        }

        confirmRequest(loginRequest, token);
        return new LoginResponse(token);
    }

}
