package gui.menu;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Background;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import utils.Music;
import utils.Resources;

public final class Settings extends Menu {
    private HBox bgMusicVolumeBox;
    private HBox effectsVolumeBox;
    private final int spacingSize = 20;
    private static final double FONT_VALUE = 0.6;
    public static final Settings INSTANCE = new Settings();

    private Settings() {
    }

    @Override
    public void start(final Stage primaryStage) {
    }

    /**
     * Affiche le menu Settings.
     */
    public void toSettings() {
        setScene();
        getMainStage().setScene(getMenuScene());
    }

    @Override
    protected void addOptionsToMenu() {
        final int titleSize = 100;

        setTitleText(createText("Settings", titleSize));
        HBox title = createHBox(0, Pos.TOP_CENTER, getTitleText());

        setupVolumeControl();

        VBox mainBox = new VBox(spacingSize);
        mainBox.setAlignment(Pos.CENTER);

        mainBox.getChildren().addAll(bgMusicVolumeBox, effectsVolumeBox);

        BorderPane.setMargin(title, new Insets(spacingSize, 0, spacingSize, 0));
        BorderPane.setMargin(mainBox, new Insets(spacingSize));

        getMenuPane().setTop(title);
        getMenuPane().setCenter(mainBox);

        setupBottomBackButton();
    }

    /**
     * Configure les contrôles de volume pour la musique de fond et les effets sonores.
     */
    private void setupVolumeControl() {
        //CHECKSTYLE:OFF
        Slider bgMusicVolumeSlider = new Slider(0, 100, Music.getBgMusicVolume() * 100);
        Slider effectsVolumeSlider = new Slider(0, 100, Music.getEffectsVolume() * 100);

        bgMusicVolumeSlider.setMajorTickUnit(25);
        bgMusicVolumeSlider.setMinorTickCount(5);
        bgMusicVolumeSlider.setBlockIncrement(1);

        effectsVolumeSlider.setMajorTickUnit(25);
        effectsVolumeSlider.setMinorTickCount(5);
        effectsVolumeSlider.setBlockIncrement(1);

        Text selectBg = createText(">", getFontSize().intValue());
        selectBg.setVisible(false);
        Text bgMusicTitle = createText(
            "Background Music Volume :", getFontSize().doubleValue() * FONT_VALUE
        );

        Text selectSfx = createText(">", getFontSize().intValue());
        selectSfx.setVisible(false);
        Text effectsTitle = createText(
            "Sound Effects Volume :", getFontSize().doubleValue() * FONT_VALUE
        );

        Text bgValue = createText("50", getFontSize().doubleValue() * FONT_VALUE);
        Text sfxValue = createText("50", getFontSize().doubleValue() * FONT_VALUE);

        bgMusicVolumeSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            Music.setBgMusicVolume(newValue.floatValue() / 100);
            bgValue.setText(newValue.intValue() + "");
        });
        effectsVolumeSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            Music.setEffectsVolume(newValue.floatValue() / 100);
            sfxValue.setText(newValue.intValue() + "");
        });
        bgMusicVolumeSlider.focusedProperty().addListener(
            e -> selectBg.setVisible(bgMusicVolumeSlider.isFocused())
        );
        effectsVolumeSlider.focusedProperty().addListener(
            e -> selectSfx.setVisible(effectsVolumeSlider.isFocused())
        );
        //CHECKSTYLE:ON

        bgMusicVolumeBox = createHBox(
            spacingSize, Pos.CENTER, selectBg, bgMusicTitle, bgMusicVolumeSlider, bgValue
        );
        effectsVolumeBox = createHBox(
            spacingSize, Pos.CENTER, selectSfx, effectsTitle, effectsVolumeSlider, sfxValue
        );
    }

    /**
     * Configure le bouton de retour en bas de l'écran.
     */
    private void setupBottomBackButton() {
        Text selectback = createText(">", getFontSize().intValue());
        selectback.setVisible(false);
        Button backButton = new Button("Back");
        backButton.setTextFill(Color.WHITE);
        backButton.setBackground(Background.EMPTY);
        backButton.setFont(
            Font.loadFont(Resources.getPathOrContent("small_pixel-7"), getFontSize().doubleValue())
        );

        backButton.setOnMouseClicked(event -> MainMenu.INSTANCE.toMenu());
        backButton.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                MainMenu.INSTANCE.toMenu();
            }
        });
        backButton.focusedProperty().addListener(
            e -> selectback.setVisible(backButton.isFocused())
        );

        HBox backButtonBox = createHBox(-spacingSize, Pos.BASELINE_LEFT, selectback, backButton);

        getMenuPane().setBottom(backButtonBox);
    }
}
