package cz.ami.connector.training.tools;

import com.evolveum.midpoint.util.logging.Trace;
import com.evolveum.midpoint.util.logging.TraceManager;
import org.identityconnectors.framework.common.objects.Attribute;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This class is a collection of functions for sets of Attributes and DeltaAttributes
 */
public abstract class AttributesSets {
    private static final Trace LOG = TraceManager.getTrace(AttributesSets.class);
    /**
     * the function removes an attribute with a special name from the set
     * and returns the value of the found attribute.
     * The found attribute must have single value
     * @param set
     * @param nameToExtract
     * @return null if nothing is found
     */
    static public String extractSingle(Set<Attribute> set, String nameToExtract) throws Exception {
        List<Attribute> streamOfFound =  set.stream().filter(element -> element.getName().equals(nameToExtract)).collect(Collectors.toList());
        long numberOfFound = streamOfFound.size();
        // not found
        if(numberOfFound==0){
            return null;
        }
        // found more than one
        if(numberOfFound>1){
            ProblemsAndErrors.checkedExcReaction(LOG, "the set contains " + numberOfFound +" attributes with the same name = " + nameToExtract);
        }
        // exactly one attribute is found
        Attribute attribute = streamOfFound.get(0);
        String value = attribute.getValue().get(0).toString();
        set.remove(attribute);
        return value;
    }
}
