package cz.ami.connector.daktela.http;

import com.google.gson.annotations.Expose;
import com.google.gson.reflect.TypeToken;
import lombok.Setter;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class DaktelaItem {
    @Expose(serialize = false, deserialize = false)
    @Setter
    private String uriSource;

    static public void setUriSources(final String uriSource, List<? extends DaktelaItem> items){
        items.forEach(item -> item.setUriSource(uriSource));
    }
}
