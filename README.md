# ItemRegistry

ItemRegistry is a small Paper API that maps Adventure [`Key`](https://jd.advntr.dev/key/latest/)s to [`ItemStack`](https://hub.spigotmc.org/javadocs/spigot/org/bukkit/inventory/ItemStack.html) instances through pluggable sources.

## Basics

Use this API the same way in both modes: register sources, then resolve keys.

```java
// Assume you already have a registry instance:
// - Internal mode: your plugin owns it
// - Shared mode: fetched from GlobalItemRegistry.registry()
ItemRegistry registry = ...;

// Register your own custom items.
SimpleItemSource custom = new SimpleItemSource(Key.key("myplugin", "custom_source"));
custom.register(Key.key("myplugin", "magic_wand"), ItemStack.of(Material.STICK));
custom.register(Key.key("myplugin", "power_gem"), ItemStack.of(Material.AMETHYST_SHARD));
registry.registerSource(custom);

// Register a more complex source
registry.registerSource(new mySpecialSource());

// Retrieve items.
ItemStack wand = registry.create(Key.key("myplugin", "magic_wand"));
ItemStack diamond = registry.create(Key.key("minecraft", "diamond"));

// Check without creating.
boolean exists = registry.canResolve(Key.key("myplugin", "magic_wand"));
```

---

## Two Ways To Use This Library

### Internal (embedded, shaded)

Local and isolated mode: your plugin embeds the registry, users install only your plugin, and interoperability is explicit.

You shade `itemregistry` directly into your plugin jar. Your plugin owns the registry completely. No extra jar is required for your users.

**Use when:**
- Your registry is private to your plugin.
- You want zero external dependencies for users.
- You optionally want selected third-party providers to add items (`Internal Extendable` below).

**Drawback:** Third-party plugins that do not explicitly target your plugin cannot integrate. Different plugins that each shade their own copy cannot share registry instances.

**Gradle dependency:**
```kotlin
// In your plugin's build.gradle.kts:
implementation("dev.qheilmann:itemregistry:VERSION") // shaded via Shadow
```

### Internal Extendable (Optional)

Provider plugin can access your internal registry. Two patterns, each with a different load order:

#### Option A — Event-based (provider enables first, listens for `ItemRegistryReadyEvent`)

Provider registers its listener before consumer fires the event, so provider must enable **before** consumer.
Because consumer loads after provider, Paper does not auto-share the consumer's classloader; you must opt in.

```yaml
# consumer paper-plugin.yml — required so classes shaded inside consumer are visible to provider
has-open-classloader: true
```

```yaml
# provider paper-plugin.yml — AFTER means consumer loads/enables after provider
dependencies:
  server:
    YourConsumerPlugin:
      load: AFTER
      required: true
```

Consumer fires the event during `onEnable`:
```java
Bukkit.getPluginManager().callEvent(new ItemRegistryReadyEvent(registry));
```

Provider registers a listener and filters by registry key:
```java
@EventHandler
public void onRegistryReady(ItemRegistryReadyEvent event) {
    if (!MY_REGISTRY_KEY.equals(event.getRegistry().key())) return;
    event.getRegistry().registerSource(mySource);
}
```

#### Option B — Static API (consumer exposes a getter, provider calls it after consumer enables)

Consumer loads and enables first, exposing the registry through a static method.
Provider declares it as a `load: BEFORE` dependency — Paper then auto-shares the consumer's classloader, so **no `has-open-classloader` flag is needed**.

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

Provider calls the consumer API directly in `onEnable`:
```java
ItemRegistry registry = YourConsumerPlugin.getRegistry();
registry.registerSource(mySource);
```

See [`examples/internal-extendable`](examples/internal-extendable) for a full consumer + provider pair.

---

### Shared (external plugin)

Global/common mode: one shared registry in one plugin jar, so unrelated providers and consumers can interoperate.

Install the `ItemRegistry` plugin jar on your server. All plugins that depend on it share a single global registry instance.

**Use when:**
- You want any third-party plugin to be able to add or read items without knowing about each other.
- Broad interoperability across an unknown set of plugins is required.

**Drawback:** Users must install `ItemRegistry` as a separate plugin jar.

**Gradle dependency:**
```kotlin
// In your plugin's build.gradle.kts — no shading needed.
compileOnly("dev.qheilmann:itemregistry:VERSION")
```

**`paper-plugin.yml` dependency:**
```yaml
dependencies:
  server:
    ItemRegistry:
      load: AFTER
      required: true
```

Provider registration example:

```java
@Override
public void onEnable() {
  if (!GlobalItemRegistry.isAvaible()) return; // ItemRegistry plugin not present
  ItemRegistry global = GlobalItemRegistry.registry();

    SimpleItemSource source = new SimpleItemSource(Key.key("myplugin", "source"));
    source.register(Key.key("myplugin", "shared_item"), ItemStack.of(Material.EMERALD));
    global.registerSource(source);
}
```

Consumer read example:

```java
@Override
public void onEnable() {
  if (!GlobalItemRegistry.isAvaible()) return;
  ItemRegistry global = GlobalItemRegistry.registry();

    ItemStack item = global.create(Key.key("myplugin", "shared_item"));
}
```

### Lifecycle and mutability notes (important)

1. There is no universal "all providers finished" moment by default.
2. If your plugin must consume after specific providers, declare explicit dependencies on those providers and run after them.
3. If your plugin is both provider and consumer, register first, then consume when your own plugin is ready.
4. Registry content is mutable. If config references keys like `myplugin:item_a` and `thirdparty:item_b`, validate them at startup/reload and handle missing keys gracefully.
5. For strict coordination, define your own plugin-level ready event/contract between known plugins.

See [`examples/shared`](examples/shared) for a full provider + consumer pair.

---

## Project layout

### Core modules

| Module                                       | Description                                         |
| -------------------------------------------- | --------------------------------------------------- |
| [`itemregistry`](itemregistry)               | API and core registry implementation                |
| [`itemregistry-plugin`](itemregistry-plugin) | Standalone shared plugin owning one global registry |

### Example modules

#### Internal

- [`examples/internal/api`](examples/internal/api): Internal-only example with fully private usage.

#### Internal Extendable

- [`examples/internal-extendable/consumer`](examples/internal-extendable/consumer): Consumer exposing its registry to known providers via event.
- [`examples/internal-extendable/provider`](examples/internal-extendable/provider): Provider explicitly targeting the internal-extendable consumer.

#### Shared

- [`examples/shared/provider`](examples/shared/provider): Provider registering into the shared global registry.
- [`examples/shared/consumer`](examples/shared/consumer): Consumer reading items from the shared global registry.

## License

MIT
