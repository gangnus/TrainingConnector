package cz.ami.connector.training.data;

import com.evolveum.midpoint.util.logging.Trace;
import com.evolveum.midpoint.util.logging.TraceManager;
import cz.ami.connector.training.model.User;
import cz.ami.connector.training.tools.ProblemsAndErrors;
import org.apache.commons.lang.StringUtils;

import java.util.*;

/**
 * This class contains all data serviced by the connector
 */
public class Users {
    private static final Trace LOG = TraceManager.getTrace(Users.class);
    private static Map<String,User> map = new HashMap<>();

    static public List<User> getAll(){
        List<User> list = new ArrayList<>();
        map.forEach((key, user)
                ->{
            list.add(user);
        });
        return list;
    }
    static public User read(String key) throws Exception {
        if(StringUtils.isBlank(key)){
            ProblemsAndErrors.checkedExcReaction(LOG,"Failed reading. Empty name.");
        }
        User result =  map.get(key);
        if(result == null){
            ProblemsAndErrors.checkedExcReaction(LOG,"Failed getting. User with name: " + key + " doesn't exist");
        }
        return result;
    }
    static public void update(User user) throws Exception {
        String key = user.getName();
        if(StringUtils.isBlank(key)){
            ProblemsAndErrors.checkedExcReaction(LOG,"Failed updating. Empty name.");
        }
        User found = map.get(key);
        if(found == null){
            ProblemsAndErrors.checkedExcReaction(LOG,"Failed updating. User with name: " + key + " doesn't exist");
        }
        map.put(key,user);
    }
    static public void add(User user) throws Exception {
        String key = user.getName();
        if(StringUtils.isBlank(key)){
            ProblemsAndErrors.checkedExcReaction(LOG,"Failed adding. Empty name.");
        }
        User found = map.get(key);
        if(found != null){
            ProblemsAndErrors.checkedExcReaction(LOG,"Failed adding. User with name: " + key + " already exists");
        }
        map.put(key, user);
    }

    static public void delete(String key) throws Exception {
        if(StringUtils.isBlank(key)){
            ProblemsAndErrors.checkedExcReaction(LOG,"Failed deleting. Empty name.");
        }
        map.remove(key);
    }
    static public void clearAll(){
        map = new HashMap<>();
    }

}
