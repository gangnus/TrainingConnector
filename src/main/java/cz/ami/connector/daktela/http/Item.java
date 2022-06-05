package cz.ami.connector.daktela.http;

import com.google.gson.annotations.Expose;
import lombok.Setter;

import java.util.List;

public class Item {
    @Expose(serialize = false, deserialize = false)
    @Setter
    private String uriSource;

    static public void setUriSources(final String uriSource, List<? extends Item> items){
        items.forEach(item -> item.setUriSource(uriSource));
    }
}
