package bean;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.inject.Inject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Singleton
@Startup
public class StartupBean {
    @Inject
    UserBean userBean;
    @Inject
    TaskBean taskBean;

    private static final Logger LOGGER = LogManager.getLogger(StartupBean.class);

    @PostConstruct
    public void init() {

        userBean.createDefaultUsers();
        userBean.createInitialTimeOut();
        taskBean.createDefaultCategories();
        taskBean.createDefaultTasks();
        LOGGER.info("Default users, categories and tasks created");
    }
}