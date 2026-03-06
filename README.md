# ItemRegistry

A small Paper library that unifies multiple item sources into a single registry API. Map Adventure [`Key`](https://jd.advntr.dev/key/latest/)s to [`ItemStack`](https://hub.spigotmc.org/javadocs/spigot/org/bukkit/inventory/ItemStack.html) instances from vanilla Minecraft, common custom item plugins, and yours item.

## Usage

```java
ItemRegistry registry = new ItemRegistry();

// Register vanilla Minecraft items
registry.registerSource(new VanillaItemSource());

// Create vanilla items
ItemStack diamond = registry.create(Key.key("minecraft", "diamond"));

// Register custom items
SimpleItemSource custom = new SimpleItemSource(Key.key("myplugin", "custom"));
ItemStack wand = new ItemStack(Material.STICK);
wand.editMeta(meta -> meta.displayName(Component.text("Magic Wand")));
custom.register(Key.key("myplugin", "magic_wand"), wand);
registry.registerSource(custom);

// Create custom items
ItemStack myWand = registry.create(Key.key("myplugin", "magic_wand"));
```

## Modules

- **`itemregistry`** — the library itself
- **`itemregistry-playground`** — a Paper plugin for demonstration and testing purposes

## Notes

Mojang plans to make items fully data-driven in future versions, allowing them to be added directly to the Minecraft registry. Until then, this library provides a convenient way to retrieve and manage vanilla-modified ItemStacks using custom keys.

## License

MIT
