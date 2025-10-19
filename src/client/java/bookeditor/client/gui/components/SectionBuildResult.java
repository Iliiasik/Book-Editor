package bookeditor.client.gui.components;

import java.util.HashMap;
import java.util.Map;

public class SectionBuildResult {
    public final ToolbarSection section;
    public final Map<String, Object> handles = new HashMap<>();

    public SectionBuildResult(ToolbarSection section) {
        this.section = section;
    }

    public SectionBuildResult with(String key, Object value) {
        handles.put(key, value);
        return this;
    }

    public Object get(String key) {
        return handles.get(key);
    }
}

