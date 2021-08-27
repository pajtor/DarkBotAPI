package eu.darkbot.impl.config;

import eu.darkbot.api.config.ConfigSetting;
import eu.darkbot.api.config.util.ValueHandler;
import eu.darkbot.util.ReflectionUtils;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;

public class DefaultHandler<T> implements ValueHandler<T> {

    protected final @Nullable Field field;

    public DefaultHandler() {
        this(null);
    }

    public DefaultHandler(@Nullable Field field) {
        this.field = field;
    }

    @Override
    public T validate(T t) {
        return t;
    }

    @Override
    public void updateParent(ConfigSetting<T> setting) {
        ConfigSetting.Parent<?> parent = setting.getParent();
        if (parent != null && field != null) {
            Object parentObj = parent.getValue();
            if (parentObj != null) ReflectionUtils.set(field, parentObj, setting.getValue());
        }

        if (setting instanceof ConfigSetting.Parent) {
            ConfigSetting.Parent<T> current = (ConfigSetting.Parent<T>) setting;
            current.getChildren().forEach(this::updateChild);
        }
    }

    @Override
    public void updateChildren(ConfigSetting<T> setting) {
        if (field == null) return;

        ConfigSetting.Parent<?> parent = setting.getParent();
        if (parent == null)
            throw new IllegalStateException("Cannot call to update children on a node without a parent!");

        Object parentObj = parent.getValue();
        setting.setValue(parentObj == null ? null :
                ReflectionUtils.get(field, parentObj, setting.getType()));
    }

    private <C> void updateChild(String key, ConfigSetting<C> child) {
        child.getHandler().updateChildren(child);
    }

}
