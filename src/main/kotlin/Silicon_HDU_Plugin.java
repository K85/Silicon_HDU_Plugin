import actionset.HDU_ActionSet;
import com.sakurawald.silicon.action.actionset.manager.ActionSetManager;
import com.sakurawald.silicon.plugin.SiliconPlugin;


public class Silicon_HDU_Plugin extends SiliconPlugin {

    @Override
    public void onLoad() {
        ActionSetManager.INSTANCE.addActionSet(new HDU_ActionSet());
    }


}
