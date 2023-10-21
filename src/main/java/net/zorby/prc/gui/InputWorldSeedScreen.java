package net.zorby.prc.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

@SuppressWarnings("ConstantConditions")
public class InputWorldSeedScreen extends Screen {

    private static final Text TITLE = Text.translatable("input_world_seed.screen.title");
    private static final Text INPUT_LABEL = Text.translatable("input_world_seed.screen.input.label");
    private static final Text INPUT_PLACEHOLDER = Text.translatable("input_world_seed.screen.input.placeholder");
    private TextFieldWidget inputField;
    private final Screen parent;
    private final InputWorldSeedScreenCallback callback;

    public interface InputWorldSeedScreenCallback {
        void run(long seed);
    }

    public InputWorldSeedScreen(Screen parent, InputWorldSeedScreenCallback callback) {
        super(TITLE);
        this.parent = parent;
        this.callback = callback;
    }

    public void tick() {
        this.inputField.tick();
    }

    protected void init() {
        this.inputField = new TextFieldWidget(this.textRenderer, this.width / 2 - 100, 116, 200, 20, INPUT_PLACEHOLDER);
        this.inputField.setMaxLength(128);
        this.inputField.setText(this.client.options.lastServer);

        this.addSelectableChild(this.inputField);

        this.addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, (button) -> {
            this.close();
        }).dimensions(this.width / 2 - 100, this.height / 4 + 120 + 12, 200, 20).build());

        this.setInitialFocus(this.inputField);
    }

    public void resize(MinecraftClient client, int width, int height) {
        String string = this.inputField.getText();
        this.init(client, width, height);
        this.inputField.setText(string);
    }

    public void close() {
        this.callback.run(Long.parseLong(this.inputField.getText()));
        this.client.setScreen(this.parent);
    }

    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context);

        context.drawCenteredTextWithShadow(this.textRenderer, (Text)this.title, this.width / 2, 20, 16777215);

        context.drawTextWithShadow(this.textRenderer, (Text)INPUT_LABEL, this.width / 2 - 100, 100, 10526880);

        this.inputField.render(context, mouseX, mouseY, delta);

        super.render(context, mouseX, mouseY, delta);
    }
}
