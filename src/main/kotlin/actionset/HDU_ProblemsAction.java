package actionset;

import com.sakurawald.silicon.Silicon;
import com.sakurawald.silicon.action.abstracts.ProblemsAction;
import com.sakurawald.silicon.data.beans.Page;
import com.sakurawald.silicon.data.beans.Problem;
import com.sakurawald.silicon.data.beans.request.ProblemsRequest;
import com.sakurawald.silicon.data.beans.response.ProblemsResponse;
import com.sakurawald.silicon.debug.LoggerManager;
import com.sakurawald.silicon.util.HttpUtil;
import com.sakurawald.silicon.util.PluginUtil;
import okhttp3.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HDU_ProblemsAction extends ProblemsAction {

    Pattern pattern = Pattern.compile("p\\((\\d+),(\\d+),(-?\\d+),\"([\\s\\S]*?)\",(\\d+),(\\d+)\\)");

    public ProblemsResponse searchProblem(ProblemsRequest problemsRequest) {
        ArrayList<Problem> problemList = null;

        OkHttpClient client = new OkHttpClient();

        String searchKey = HttpUtil.INSTANCE.encodeURL(problemsRequest.getProblemSearchKey(), "GBK");

        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
        RequestBody body = RequestBody.create(mediaType, "content=" + searchKey + "&searchmode=title");
        Request request = new Request.Builder()
                .url("https://acm.hdu.edu.cn/search.php?action=listproblem")
                .post(body)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .addHeader("Accept", "*/*")
                .addHeader("Cache-Control", "no-cache")
                .addHeader("Host", "acm.hdu.edu.cn")
                .addHeader("Cookie", "PHPSESSID=" + problemsRequest.getRequestAccount().getToken())
                .addHeader("Connection", "keep-alive")
                .build();

        Response response = null;
        try {
            response = client.newCall(request).execute();
            String HTML = response.body().string();
            problemList = PluginUtil.INSTANCE.fastGetProblemList(HTML,
                    "body > table > tbody > tr:nth-child(5) > td > table > tbody > tr:nth-child(2) > td > table > tbody > tr"
                    , 6, 0,
                    0, 1, 4, -1, -1);

        } catch (IOException e) {
            LoggerManager.INSTANCE.reportException(e);
        } finally {
            Objects.requireNonNull(response.body()).close();
        }

        return new ProblemsResponse(problemList);
    }

    public ProblemsResponse problemList(ProblemsRequest problemsRequest) {
        OkHttpClient client = new OkHttpClient();
        String page = problemsRequest.getPage();
        if (Page.HOME_PAGE.equals(page)) page = "1";
        Request request = new Request.Builder()
                .url("http://acm.hdu.edu.cn/listproblem.php?vol=" + page)
                .get()
                .addHeader("Accept", "*/*")
                .addHeader("Cache-Control", "no-cache")
                .addHeader("Cookie", "PHPSESSID=" + problemsRequest.getRequestAccount().getToken())
                .addHeader("Connection", "keep-alive")
                .build();

        Response response = null;
        ArrayList<Problem> problemList = new ArrayList<>();
        try {
            response = client.newCall(request).execute();

            /** HTML Analyse. **/
            String HTML = response.body().string();

            Matcher matcher = pattern.matcher(HTML);
            while (matcher.find()) {

                // Construct Problem.
                Problem problem = new Problem();
                String unknown_field1 = matcher.group(1);
                String problemID = matcher.group(2);
                String problemStatus = matcher.group(3);

                String problemTitle = matcher.group(4);
                int AC = Integer.parseInt(matcher.group(5));
                int submit = Integer.parseInt(matcher.group(6));
                double ratio = (double) AC / submit;

                problem.setProblemID(problemID);
                problem.setProblemTitle(problemTitle);
                problem.setRatio(ratio);
                problem.setAC(AC);
                problem.setSubmit(submit);
                problem.setProblemStatus(Silicon.getCurrentActionSet().getProblemStatus(problemStatus));

                LoggerManager.INSTANCE.logDebug("Get Problem: " + problem);
                problemList.add(problem);
            }


        } catch (IOException e) {
            LoggerManager.INSTANCE.reportException(e);
        } finally {
            Objects.requireNonNull(response.body()).close();
        }

        return new ProblemsResponse(problemList);
    }


    @Override
    public ProblemsResponse execute(ProblemsRequest problemsRequest) {

        LoggerManager.INSTANCE.logDebug("ProblemsAction: request = " + problemsRequest);

        // Is Search Problem ?
        if (problemsRequest.getProblemSearchKey() == null) {
            return problemList(problemsRequest);
        } else {
            return searchProblem(problemsRequest);
        }

    }

    @Override
    public boolean supportProblemSearch() {
        return true;
    }


}
