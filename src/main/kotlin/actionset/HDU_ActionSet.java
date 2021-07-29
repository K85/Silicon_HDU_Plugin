package actionset;

import com.sakurawald.silicon.action.abstracts.*;
import com.sakurawald.silicon.action.actionset.abstracts.ActionSet;
import com.sakurawald.silicon.data.beans.Language;
import com.sakurawald.silicon.data.beans.ProblemStatus;

import java.util.ArrayList;

public class HDU_ActionSet extends ActionSet {

    @Override
    public SubmitAction getSubmitAction() {
        return new HDU_SubmitAction();
    }

    @Override
    public LoginAction getLoginAction() {
        return new HDU_LoginAction();
    }

    @Override
    public StatusAction getStatusAction() {
        return new HDU_StatusAction();
    }

    @Override
    public ProblemsAction getProblemsAction() {
        return new HDU_ProblemsAction();
    }

    @Override
    public ProblemDetailAction getProblemDetailAction() {
        return new HDU_ProblemDetailAction();
    }

    @Override
    public SourceDetailAction getSourceDetailAction() {
        return new HDU_SourceDetailAction();
    }

    @Override
    public CompileDetailAction getCompileDetailAction() {
        return new HDU_CompileDetailAction();
    }

    @Override
    public String getBaseURL() {
        return "http://acm.hdu.edu.cn/";
    }

    @Override
    public ProblemStatus getProblemStatus(String problemStatus) {
        if (problemStatus.equals("5")) return ProblemStatus.ACCEPTED;
        if (problemStatus.equals("6")) return ProblemStatus.WRONG;
        return ProblemStatus.NEVER_TRY;
    }

    @Override
    public String getActionSetName() {
        return "HDU OJ";
    }

    @Override
    public ArrayList<Language> getSupportLanguages() {
        ArrayList<Language> languages = new ArrayList<>();
        languages.add(new Language("G++", "0"));
        languages.add(new Language("GCC", "1"));
        languages.add(new Language("C++", "2"));
        languages.add(new Language("C", "3"));
        languages.add(new Language("Pascal", "4"));
        languages.add(new Language("Java", "5"));
        languages.add(new Language("C#", "6"));
        return languages;
    }

}
