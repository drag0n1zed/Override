# SpruceUI Usage Guide

This document provides a comprehensive guide to using SpruceUI, based on the library's testmod.

-   **Target Platform:** NeoForge 1.21.1, Java 21
-   **Scope:** This guide covers APIs and patterns demonstrated in the testmod (`src/testmod`). It focuses on end-to-end examples and common usage patterns.

---

## Setup

To use SpruceUI in your NeoForge mod, you need to add it as a dependency and configure it to be embedded into your mod's JAR file.

### 1. `gradle.properties`

Add the SpruceUI version to your `gradle.properties` file.

```properties
spruceui_version=6.2.2+1.21
```

### 2. `build.gradle`

Add the repository, dependency, and Jar-in-Jar configuration to your `build.gradle` file.

```groovy
repositories {
    // Add the Gegy Maven repository to resolve SpruceUI
    maven {
        name = "Gegy"
        url = uri("https://maven.gegy.dev")
    }
}

dependencies {
    // Add the SpruceUI dependency
    // The capabilities block is required to request the Mojmap-named artifact.
    implementation("dev.lambdaurora:spruceui:${project.spruceui_version}") {
        capabilities {
            requireCapability("dev.lambdaurora:spruceui-mojmap")
        }
    }
}

// Configure the jar-in-jar plugin to embed SpruceUI.
// This ensures users do not need to download SpruceUI separately.
// The pin ensures a specific version range is embedded.
jarJar {
    pin("dev.lambdaurora:spruceui", "[6.2.2,7.0.0)")
}
```

---

## Core Concepts

### Screens

Screens are the top-level containers for a GUI. SpruceUI provides a base class, `SpruceScreen`, that simplifies widget management and rendering.

-   **Base Class**: Extend `dev.lambdaurora.spruceui.screen.SpruceScreen` to build Spruce-based screens.
-   **Constructor**: The constructor requires a `net.minecraft.network.chat.Component` for the screen title (e.g., `Text.literal("My Screen")`).
-   **Initialization**: All widgets should be created and added within the `init()` method.

**Title Rendering**
You can override `renderTitle` to draw a custom title. The testmod uses a standard pattern:

```java
@Override
public void renderTitle(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
    guiGraphics.drawCenteredShadowedText(this.font, this.title, this.width / 2, 8, 16777215);
}
```

**Navigation**
Screen navigation is handled using the vanilla `client.setScreen(...)` method, typically from within a button's press action.

```java
// Navigate to a new screen
btn -> this.client.setScreen(new SpruceOptionScreen(this))

// Return to the previous screen
btn -> this.client.setScreen(this.parent)
```

**Adding Widgets**
Use `addRenderableWidget(...)` to register Spruce widgets with the screen. This makes them visible and interactive.

```java
// Add a button
this.addRenderableWidget(new SpruceButtonWidget(Position.of(this, ...), ...));

// Add a container widget
this.addRenderableWidget(this.list);
```

### Widgets

SpruceUI provides a rich set of widgets for building user interfaces.

#### 1. Buttons (`SpruceButtonWidget`)

-   **Class**: `dev.lambdaurora.spruceui.widget.SpruceButtonWidget`
-   **Construction**:
    -   Screen-relative: `new SpruceButtonWidget(Position.of(this, this.width / 2 - 100, y), 200, 20, text, callback)`
    -   With predefined text: `new SpruceButtonWidget(Position.of(this, ...), 150, 20, SpruceTexts.GUI_DONE, callback)`
-   **Callback**: The last argument is a `PressAction` lambda that executes when the button is clicked.

#### 2. Labels (`SpruceLabelWidget`)

-   **Class**: `dev.lambdaurora.spruceui.widget.SpruceLabelWidget`
-   **Usage**: Used to display static or multi-line text. It supports automatic wrapping and centering.
-   **Construction**: `new SpruceLabelWidget(Position.of(x, y), text, maxWidth, true)` (the final `true` enables shadow).

#### 3. Text Area (`SpruceTextAreaWidget`)

-   **Class**: `dev.lambdaurora.spruceui.widget.text.SpruceTextAreaWidget`
-   **Usage**: A multi-line text editor with support for selection, copy/paste, and cursor navigation.
-   **API Methods**:
    -   `setLines(List<String>)`: Populates the text area.
    -   `getText()`: Retrieves the full content as a single string.
    -   `setCursorToStart()`: Moves the cursor to the beginning.

#### 4. Containers (`SpruceContainerWidget`)

-   **Class**: `dev.lambdaurora.spruceui.widget.container.SpruceContainerWidget`
-   **Usage**: A generic container that can hold other widgets. Useful for grouping elements and managing layouts.
-   **Adding Children**:
    -   Directly: `container.addChild(widget)`
    -   Via layout callback: `container.addChildren((width, height, adder) -> { adder.accept(newWidget); });`

#### 5. Tabbed Widget (`SpruceTabbedWidget`)

-   **Class**: `dev.lambdaurora.spruceui.widget.container.tabbed.SpruceTabbedWidget`
-   **Usage**: Creates a multi-tab layout with a navigation list on the left and a content panel on the right.
-   **API Methods**:
    -   `addTabEntry(title, subtitle, factory)`: Adds a new tab. The factory is a `BiFunction<Integer, Integer, SpruceWidget>` that creates the content for the tab.
    -   `addSeparatorEntry(label)`: Adds a non-interactive separator to the tab list.

#### 6. Option List Widget (`SpruceOptionListWidget`)

-   **Class**: `dev.lambdaurora.spruceui.widget.container.SpruceOptionListWidget`
-   **Usage**: A specialized container for displaying rows of `SpruceOption` instances, perfect for configuration screens.
-   **Adding Entries**:
    -   `addOptionEntry(option1, option2)`: Adds a row with two options, side-by-side.
    -   `addSingleOptionEntry(option)`: Adds a row with a single option that spans the full width.

### Options

The `dev.lambdaurora.spruceui.option` package provides a powerful API for creating configurable settings. Options are typically displayed within a `SpruceOptionListWidget`.

-   **Boolean Options**:
    -   `SpruceBooleanOption`: A button that toggles between "ON" and "OFF".
    -   `SpruceCheckboxBooleanOption`: A standard checkbox.
    -   `SpruceToggleBooleanOption`: A modern toggle switch.
-   **Value Options**:
    -   `SpruceDoubleOption`: A slider for `double` values.
    -   `SpruceIntegerInputOption`, `SpruceFloatInputOption`, `SpruceDoubleInputOption`: Text fields for numeric input.
-   **Choice/Action Options**:
    -   `SpruceCyclingOption`: A button that cycles through a set of values (e.g., an enum).
    -   `SpruceSimpleActionOption`: A button that performs a custom action. Includes a `reset` factory for creating reset buttons.
-   **Other**:
    -   `SpruceSeparatorOption`: A visual separator with a label.

All options are constructed with a key (for localization), getters/setters (or an action), and an optional tooltip.

### Utilities and Helpers

#### 1. Position Helper (`Position`)

-   **Class**: `dev.lambdaurora.spruceui.Position`
-   **Usage**: A helper for defining and managing widget positions, with built-in support for anchoring to a screen.
-   **Patterns**:
    -   **Absolute**: `Position.of(x, y)`
    -   **Screen-Relative**: `Position.of(this, x, y)` (anchors to the screen, responsive to resizing).
    -   **Origin**: `Position.origin()` (used in factories where position is relative to the container).

#### 2. Predefined Texts (`SpruceTexts`)

-   **Class**: `dev.lambdaurora.spruceui.SpruceTexts`
-   **Usage**: Provides common, translatable `Component` instances.
-   **Example**: `SpruceTexts.GUI_DONE` for a "Done" button.

#### 3. Vanilla Interop (`asVanilla()`)

-   **Usage**: Some Spruce widgets (like `SpruceButtonWidget`) have an `.asVanilla()` method. This converts them into a vanilla `Widget` instance, allowing them to be added to vanilla-managed lists or screens (e.g., via a Mixin to `TitleScreen`).

---

## Full Examples

These are complete, compilable-style snippets derived from the testmod that demonstrate how to implement typical UI flows.

### Example 1: Main Menu with Navigation

This screen acts as a central hub, with buttons to navigate to other test screens.

**File**: `src/testmod/java/dev/lambdaurora/spruceui/test/gui/SpruceMainMenuScreen.java`
```java
import dev.lambdaurora.spruceui.Position;
import dev.lambdaurora.spruceui.SpruceTexts;
import dev.lambdaurora.spruceui.screen.SpruceScreen;
import dev.lambdaurora.spruceui.widget.SpruceButtonWidget;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

public class SpruceMainMenuScreen extends SpruceScreen {
    private final Screen parent;

    public SpruceMainMenuScreen(@Nullable Screen parent) {
        super(Component.literal("SpruceUI Test Main Menu"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();

        int startY = this.height / 4 + 48;
        this.addRenderableWidget(new SpruceButtonWidget(Position.of(this, this.width / 2 - 100, startY), 200, 20, Component.literal("Option Test"),
                btn -> this.client.setScreen(new SpruceOptionScreen(this))));
        this.addRenderableWidget(new SpruceButtonWidget(Position.of(this, this.width / 2 - 100, startY += 25), 200, 20, Component.literal("Text Area Test"),
                btn -> this.client.setScreen(new SpruceTextAreaScreen(this))));
        this.addRenderableWidget(new SpruceButtonWidget(Position.of(this, this.width / 2 - 100, startY += 25), 200, 20, Component.literal("Tabbed Screen Test"),
                btn -> this.client.setScreen(new SpruceTabbedTestScreen(this))));

        // Add done button.
        this.addRenderableWidget(new SpruceButtonWidget(Position.of(this, this.width / 2 - 75, this.height - 29), 150, 20, SpruceTexts.GUI_DONE,
                btn -> this.client.setScreen(this.parent)));
    }

    @Override
    public void renderTitle(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        guiGraphics.drawCenteredShadowedText(this.font, this.title, this.width / 2, 8, 16777215);
    }
}
```

### Example 2: Options Screen with `SpruceOptionListWidget`

This screen demonstrates how to use a `SpruceOptionListWidget` populated with various options from a shared builder.

**File**: `src/testmod/java/dev/lambdaurora/spruceui/test/gui/SpruceOptionScreen.java`
```java
import dev.lambdaurora.spruceui.Position;
import dev.lambdaurora.spruceui.SpruceTexts;
import dev.lambdaurora.spruceui.screen.SpruceScreen;
import dev.lambdaurora.spruceui.test.SpruceUITest;
import dev.lambdaurora.spruceui.widget.SpruceButtonWidget;
import dev.lambdaurora.spruceui.widget.container.SpruceOptionListWidget;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

public class SpruceOptionScreen extends SpruceScreen {
    private final Screen parent;
    private SpruceOptionListWidget list;

    public SpruceOptionScreen(@Nullable Screen parent) {
        super(Component.literal("SpruceUI Test Option Menu"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();

        this.list = SpruceUITest.get().buildOptionList(Position.of(0, 22), this.width, this.height - 35 - 22);

        // This consumer is used by the Reset button in the option list to rebuild the screen.
        SpruceUITest.get().resetConsumer = btn -> {
            this.init(this.client, this.client.getWindow().getGuiScaledWidth(), this.client.getWindow().getGuiScaledHeight());
        };

        this.addRenderableWidget(this.list);

        // Done button
        this.addRenderableWidget(new SpruceButtonWidget(Position.of(this, this.width / 2 - 155 + 160, this.height - 29), 150, 20, SpruceTexts.GUI_DONE,
                btn -> this.client.setScreen(this.parent)).asVanilla());
    }

    @Override
    public void renderTitle(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        guiGraphics.drawCenteredShadowedText(this.font, this.title, this.width / 2, 8, 16777215);
    }
}
```

### Example 3: Tabbed Screen

This example shows how to use a `SpruceTabbedWidget` to create a complex layout with different content panes, including labels, an option list, and a text area.

**File**: `src/testmod/java/dev/lambdaurora/spruceui/test/gui/SpruceTabbedTestScreen.java`
```java
import dev.lambdaurora.spruceui.Position;
import dev.lambdaurora.spruceui.SpruceTexts;
import dev.lambdaurora.spruceui.screen.SpruceScreen;
import dev.lambdaurora.spruceui.test.SpruceUITest;
import dev.lambdaurora.spruceui.widget.SpruceButtonWidget;
import dev.lambdaurora.spruceui.widget.SpruceLabelWidget;
import dev.lambdaurora.spruceui.widget.container.SpruceContainerWidget;
import dev.lambdaurora.spruceui.widget.container.tabbed.SpruceTabbedWidget;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

public class SpruceTabbedTestScreen extends SpruceScreen {
    private final Screen parent;
    private SpruceTabbedWidget tabbedWidget;

    protected SpruceTabbedTestScreen(@Nullable Screen parent) {
        super(Component.literal("Tabbed Screen Test"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();
        this.tabbedWidget = new SpruceTabbedWidget(Position.of(this, 0, 4), this.width, this.height - 35 - 4, this.title);

        // Tab 1: Simple labels
        this.tabbedWidget.addTabEntry(Component.literal("Hello World"), null, (width, height) -> {
            var container = new SpruceContainerWidget(Position.origin(), width, height);
            container.addChildren((containerWidth, containerHeight, widgetAdder) -> {
                widgetAdder.accept(new SpruceLabelWidget(Position.of(0, 16),
                        Component.literal("Hello World!").withStyle(ChatFormatting.WHITE),
                        containerWidth, true));
                widgetAdder.accept(new SpruceLabelWidget(Position.of(0, 48),
                        Component.literal("This is a tabbed widget. You can switch tabs by using the list on the left.")
                                .withStyle(ChatFormatting.WHITE),
                        containerWidth, true));
            });
            return container;
        });

        this.tabbedWidget.addSeparatorEntry(Component.literal("Separator"));

        // Tab 2: Re-uses the option list builder
        this.tabbedWidget.addTabEntry(Component.literal("Option Test"), Component.literal("useful for config stuff.").withStyle(ChatFormatting.GRAY),
                (width, height) -> SpruceUITest.get().buildOptionList(Position.origin(), width, height));

        // Tab 3: Re-uses the text area builder
        this.tabbedWidget.addTabEntry(Component.literal("Text Area"), Component.literal("to edit stuff on multiple lines.").withStyle(ChatFormatting.GRAY),
                (width, height) -> SpruceUITest.buildTextAreaContainer(Position.origin(), width, height,
                        textArea -> {}, null));

        this.addRenderableWidget(this.tabbedWidget);

        // Add done button.
        this.addRenderableWidget(new SpruceButtonWidget(Position.of(this, this.width / 2 - 75, this.height - 29), 150, 20, SpruceTexts.GUI_DONE,
                btn -> this.client.setScreen(this.parent)).asVanilla());
    }
}
```

### Example 4: Injecting a Menu Entry via Mixin

This mixin adds a button to Minecraft's title screen to open the SpruceUI test menu. It demonstrates using `.asVanilla()` for compatibility with vanilla screens.

**File**: `src/testmod/java/dev/lambdaurora/spruceui/test/mixin/TitleScreenMixin.java`
```java
import dev.lambdaurora.spruceui.Position;
import dev.lambdaurora.spruceui.test.gui.SpruceMainMenuScreen;
import dev.lambdaurora.spruceui.widget.SpruceButtonWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public class TitleScreenMixin extends Screen {
    protected TitleScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "init", at = @At("RETURN"))
    private void onInit(CallbackInfo ci) {
        this.addRenderableWidget(new SpruceButtonWidget(Position.of(0, 0), 150, 20, Component.literal("SpruceUI Test Menu"),
                btn -> this.client.setScreen(new SpruceMainMenuScreen(this))).asVanilla());
    }
}
```

### Example 5: Shared Widget Builders

The testmod centralizes the creation of complex widget groups into helper methods. This promotes reuse and keeps screen classes cleaner.

**File**: `src/testmod/java/dev/lambdaurora/spruceui/test/SpruceUITest.java`

#### 5.a) Building the Option List

This method constructs and populates a `SpruceOptionListWidget` with various options.

```java
// Inside SpruceUITest.java
public SpruceOptionListWidget buildOptionList(Position position, int width, int height) {
    var list = new SpruceOptionListWidget(position, width, height);

    list.addOptionEntry(this.booleanOption, this.checkboxOption);
    list.addOptionEntry(this.toggleSwitchOption, null);
    // ... more entries for scrolling ...
    list.addSingleOptionEntry(this.separatorOption);
    list.addSingleOptionEntry(this.doubleOption);
    list.addSingleOptionEntry(this.intInputOption);
    list.addOptionEntry(this.actionOption, this.cyclingOption);

    return list;
}
```

#### 5.b) Building the Text Area Container

This static method builds a `SpruceContainerWidget` containing a `SpruceTextAreaWidget` and associated control buttons.

```java
// Inside SpruceUITest.java
public static SpruceContainerWidget buildTextAreaContainer(Position position, int width, int height,
        Consumer<SpruceTextAreaWidget> textAreaConsumer,
        @Nullable SpruceButtonWidget.PressAction doneButtonAction) {

    int textFieldWidth = (int) (width * (3.0 / 4.0));
    var textArea = new SpruceTextAreaWidget(Position.of(width / 2 - textFieldWidth / 2, 0), textFieldWidth, height - 50,
            Component.literal("Text Area"));
    textArea.setLines(Arrays.asList(
            "Hello world,",
            "",
            "This widget can be very useful in some cases."));
    textAreaConsumer.accept(textArea);
    textArea.setCursorToStart();

    var container = new SpruceContainerWidget(position, width, height);
    container.addChild(textArea);

    int printToConsoleX = width / 2 - (doneButtonAction == null ? 75 : 155);
    container.addChild(new SpruceButtonWidget(Position.of(printToConsoleX, height - 29), 150, 20, Component.literal("Print to console"),
            btn -> {
                System.out.println("########################## START TEXT AREA CONTENT ##########################");
                System.out.println(textArea.getText());
                System.out.println("##########################  END TEXT AREA CONTENT  ##########################");
            }));

    if (doneButtonAction != null) {
        container.addChild(new SpruceButtonWidget(Position.of(width / 2 - 155 + 160, height - 29), 150, 20, SpruceTexts.GUI_DONE,
                doneButtonAction));
    }

    return container;
}
```