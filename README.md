# ItemRegistry

![JitPack](https://jitpack.io/v/qheilmann/ItemRegistry.svg)

ItemRegistry is a small Paper API that resolves Adventure [Key](https://jd.advntr.dev/key/latest/net/kyori/adventure/key/Key.html) values to Bukkit [ItemStack](https://hub.spigotmc.org/javadocs/spigot/org/bukkit/inventory/ItemStack.html) using pluggable item sources.

## Goal

Make item lookup easier and easy to share across plugins, without hard dependencies or custom data formats.

- Providers register items by key with pluggable item sources.
- Consumers request items by the same key.

Multiple registries can exist at the same time. A registry can be private to one plugin or shared across plugins.

## Basic Usage

```java
// Suppose you already have a registry instance.
ItemRegistry registry = ...;

// Register your own simple item source.
PdcItemSource custom = new PdcItemSource(Key.key("myplugin", "custom_source"));
custom.register(Key.key("myplugin", "magic_wand"), ItemStack.of(Material.STICK));
custom.register(Key.key("myplugin", "power_gem"), ItemStack.of(Material.AMETHYST_SHARD));
registry.registerSource(custom);

// Register a more complex source.
registry.registerSource(new MySpecialSource());

// Resolve.
ItemStack wand = registry.createItem(Key.key("myplugin", "magic_wand"));
boolean exists = registry.canResolve(Key.key("myplugin", "power_gem"));

// Reverse lookup (item -> key).
Key itemKey = registry.resolveKey(wand);
```

## Dependency Setup

### Gradle (Kotlin DSL)

```kotlin
repositories {
    maven("https://jitpack.io")
}

dependencies {
    // Shared mode (standalone ItemRegistry plugin at runtime):
    compileOnly("com.github.qheilmann.ItemRegistry:itemregistry:VERSION")
    // Internal mode (embedded / shaded runtime):
    implementation("com.github.qheilmann.ItemRegistry:itemregistry:VERSION")
}
```

### Maven

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependency>
    <groupId>com.github.qheilmann.ItemRegistry</groupId>
    <artifactId>itemregistry</artifactId>
    <version>VERSION</version>
    <!-- Shared mode (standalone plugin at runtime) -->
    <scope>provided</scope>
    <!-- Internal mode (embedded / shaded runtime) -->
    <scope>compile</scope>
</dependency>
```

> Replace `VERSION` with a release tag. Choose `compileOnly`/`provided` for Shared mode, and `implementation`/`compile` for Internal mode. Available versions are listed on [JitPack](https://jitpack.io/#qheilmann/ItemRegistry).


## Two Main Usage Patterns

- Shared: A standalone ItemRegistry plugin provides one global shared registry for all plugins.
- Internal: A plugin embeds and owns its own registry instance, which can stay private or be intentionally exposed.

### Shared (standalone ItemRegistry plugin)

In shared mode, the ItemRegistry plugin jar provides the runtime implementation and creates the global shared registry.
Provider and consumer plugins only compile against the API.


Use this when:
- You want broad interoperability between unrelated plugins.
- Providers and consumers should not need direct awareness of each other.

Tradeoff:
- Server owners must install the ItemRegistry plugin jar.

Gradle dependency for providers/consumers:
```kotlin
compileOnly("com.github.qheilmann.ItemRegistry:itemregistry:VERSION")
```

`paper-plugin.yml` dependency (matches examples):
```yaml
dependencies:
  server:
    ItemRegistry:
      load: BEFORE
      required: true
```

Provider example:
```java
@Override
public void onEnable() {
    if (!GlobalItemRegistry.isAvailable()) {
        getSLF4JLogger().error("Global item registry is unavailable. Ensure the ItemRegistry plugin is enabled. Disabling plugin.");
        getServer().getPluginManager().disablePlugin(this);
        return;
    }

    ItemRegistry global = GlobalItemRegistry.registry();
    
    PdcItemSource source = new PdcItemSource(Key.key("myplugin", "source"));
    source.register(Key.key("myplugin", "shared_item"), ItemStack.of(Material.EMERALD));
    global.registerSource(source);
}
```

Consumer example:
```java
@Override
public void onEnable() {
    if (!GlobalItemRegistry.isAvailable()) {
        getSLF4JLogger().error("Global item registry is unavailable. Ensure the ItemRegistry plugin is enabled. Disabling plugin.");
        getServer().getPluginManager().disablePlugin(this);
        return;
    }

    ItemRegistry global = GlobalItemRegistry.registry();
    
    ItemStack item = global.createItem(Key.key("myplugin", "shared_item"));
    Key resolved = global.resolveKey(item);
}
```

See examples:
- [examples/shared/provider](examples/shared/provider)
- [examples/shared/consumer](examples/shared/consumer)

### Internal (embedded / shaded)

In internal mode, your plugin owns its own registry instance and provides the runtime implementation by shading ItemRegistry into its own jar.



Use this when:
- You want no extra plugin jar for server owners.
- The registry is private to your plugin.

Tradeoff:
- Other plugins cannot integrate unless you intentionally expose your registry. (see [Internal Extended](#3-internal-extended-consumer-exposes-its-internal-registry) below)

Gradle dependency:
```kotlin
implementation("com.github.qheilmann.ItemRegistry:itemregistry:VERSION")
```

See example: [examples/internal/api](examples/internal/api)

### Internal Extended (consumer exposes its internal registry)

This is still internal ownership, but third-party provider plugins can explicitly register into an internal-owned registry.

There is many ways to implement this pattern, but here are two possible approaches.

#### Internal Extended With Static Getter

Consumer loads first and exposes a static getter. Provider depends on consumer with `load: BEFORE`, then reads the registry in `onEnable()`.

Consumer example:
```java
public final class YourConsumerPlugin extends JavaPlugin {
    private static ItemRegistry registry;

    public static ItemRegistry getRegistry() {
        return registry;
    }

    @Override
    public void onEnable() {
        registry = new ItemRegistry(Key.key("consumer_example", "main"));
        registry.registerSource(ItemSource.VANILLA_SOURCE);
    }
}
```

Provider dependency and usage:
```kotlin
// provider build.gradle.kts
compileOnly(project(":your-consumer-plugin"))
```

```yaml
# provider paper-plugin.yml — BEFORE means consumer loads/enables before provider
dependencies:
  server:
    YourConsumerPlugin:
      load: BEFORE
      required: true
```

```java
@Override
public void onEnable() {
    ItemRegistry registry = YourConsumerPlugin.getRegistry();
    registry.registerSource(mySource);
}
```

#### Internal Extended With Event Based

Provider loads first and listens for a ready event. Consumer fires `ItemRegistryReadyEvent` when its registry is ready.

Consumer example:
```java
public final class YourConsumerPlugin extends JavaPlugin {
    private final ItemRegistry registry = new ItemRegistry(Key.key("consumer_example", "main"));

    @Override
    public void onEnable() {
        registry.registerSource(ItemSource.VANILLA_SOURCE);
        Bukkit.getPluginManager().callEvent(new ItemRegistryReadyEvent(registry));
    }
}
```

Consumer `paper-plugin.yml` in this pattern needs to declare `has-open-classloader: true` to allow providers to listen for the event:
```yaml
has-open-classloader: true
```

Provider dependency and listener:
```yaml
# provider paper-plugin.yml — AFTER means consumer loads/enables after provider
dependencies:
  server:
    YourConsumerPlugin:
      load: AFTER
      required: true
```

```java
@EventHandler
public void onRegistryReady(ItemRegistryReadyEvent event) {
    if (!TARGET_REGISTRY_KEY.equals(event.getRegistry().key())) return;
    event.getRegistry().registerSource(mySource);
}
```

Consumer fire the event when the registry is ready (for example in `onEnable()`):
```java
Bukkit.getPluginManager().callEvent(new ItemRegistryReadyEvent(registry));
```

See examples (event-based implementation):
- [examples/internal-extendable/consumer](examples/internal-extendable/consumer)
- [examples/internal-extendable/provider](examples/internal-extendable/provider)

## Lifecycle Notes

- There is no automatic "all providers finished" phase.
- If order matters, declare plugin dependencies explicitly.
- Registry content is mutable during lifecycle and often depends on plugin enable/load order.
- In shared mode, `GlobalItemRegistry.registry()` throws if the global registry is not ready yet. Check `GlobalItemRegistry.isAvailable()` first.

## ItemSource Strategies

**PdcItemSource (Recommended):** Stores item keys in ItemStack metadata (PDC). O(1) reverse lookup, survives item modifications (anvil renames, enchantment changes). Best for owned item creation.

**StrictItemSource:** Compares items using `ItemStack#isSimilar`. Works with external items, but O(n) lookup and breaks on property changes. Use for external item identification.

See [examples/shared/consumer](examples/shared/consumer) for both strategies in action.

## License

MIT
