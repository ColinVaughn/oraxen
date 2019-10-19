package io.th0rgal.oraxen.mechanics;

import io.th0rgal.oraxen.items.ItemBuilder;

import org.bukkit.configuration.ConfigurationSection;
import java.util.function.Function;

public abstract class Mechanic {

    private MechanicFactory mechanicFactory;
    private ConfigurationSection section;
    private Function<ItemBuilder, ItemBuilder>[] itemModifiers;
    private String itemID;

    @SafeVarargs
    public Mechanic(MechanicFactory mechanicFactory, ConfigurationSection section, Function<ItemBuilder, ItemBuilder>... modifiers) {
        this.mechanicFactory = mechanicFactory;
        this.section = section;
        this.itemModifiers = modifiers;
        this.itemID = section.getParent().getParent().getName();
    }

    public String getItemID() {
        return itemID;
    }

    public Function<ItemBuilder, ItemBuilder>[] getItemModifiers() {
        return itemModifiers;
    }

    public MechanicFactory getFactory() {
        return mechanicFactory;
    }

    protected ConfigurationSection getSection() {
        return section;
    }

}