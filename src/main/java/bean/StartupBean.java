package bean;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.inject.Inject;

@Singleton
@Startup
public class StartupBean {
    @Inject
    UserBean userBean;
    @Inject
    TaskBean taskBean;


    @PostConstruct
    public void init() {

        userBean.createDefaultUsers();
        taskBean.createDefaultCategories();
        taskBean.createDefaultTasks();
    }
}