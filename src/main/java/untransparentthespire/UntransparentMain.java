package untransparentthespire;

import basemod.BaseMod;
import basemod.ReflectionHacks;
import basemod.interfaces.EditKeywordsSubscriber;
import basemod.interfaces.EditStringsSubscriber;
import basemod.interfaces.PostInitializeSubscriber;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.evacipated.cardcrawl.modthespire.Loader;
import com.evacipated.cardcrawl.modthespire.ModInfo;
import com.evacipated.cardcrawl.modthespire.Patcher;
import com.evacipated.cardcrawl.modthespire.lib.SpireInitializer;
import com.google.gson.Gson;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.localization.*;
import com.megacrit.cardcrawl.powers.StrengthPower;
import untransparentthespire.util.GeneralUtils;
import untransparentthespire.util.KeywordInfo;
import untransparentthespire.util.TextureLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.scannotation.AnnotationDB;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import static untransparentthespire.util.TextureLoader.getTexture;

@SpireInitializer
public class UntransparentMain implements
        EditStringsSubscriber,
        EditKeywordsSubscriber,
        PostInitializeSubscriber {
    public static ModInfo info;
    public static String modID; //Edit your pom.xml to change this
    static { loadModInfo(); }
    public static final Logger logger = LogManager.getLogger(modID); //Used to output to the console.
    //todo
    private static final String resourcesFolder = "untransparentthespire";

    //This is used to prefix the IDs of various objects like cards and relics,
    //to avoid conflicts between different mods using the same name for things.
    public static String makeID(String id) {
        return modID + ":" + id;
    }

    //This will be called by ModTheSpire because of the @SpireInitializer annotation at the top of the class.
    public static void initialize() {
        new UntransparentMain();
    }

    public UntransparentMain() {
        BaseMod.subscribe(this); //This will make BaseMod trigger all the subscribers at their appropriate times.
        logger.info(modID + " subscribed to BaseMod.");
    }

    @Override
    public void receivePostInitialize() {

        //This loads the image used as an icon in the in-game mods menu.
        Texture badgeTexture = getTexture(resourcePath("badge.png"));
        //Set up the mod information displayed in the in-game mods menu.
        //The information used is taken from your pom.xml file.
        BaseMod.registerModBadge(badgeTexture, info.Name, GeneralUtils.arrToString(info.Authors), info.Description, null);

//        Texture t = ImageMaster.SETTINGS_ICON;
//        Texture u = new Texture(t.getWidth(), t.getHeight(), t.getTextureData().getFormat());
//        Texture q = getTexture(resourcePath("morenothing.png"));
//        Texture l = combineTextures(u, q);
//        ImageMaster.SETTINGS_ICON = combineTextures(l, t);
//        u.dispose();
//        l.dispose();

    }

    //texture 1 is the texture to crop
    //texture 2 provides the texture data to crop into
    public static Texture cropToTexture(Texture texture, Texture data) {
        if (!texture.getTextureData().isPrepared()) {
            texture.getTextureData().prepare();
        }
        Pixmap texturePixmap = texture.getTextureData().consumePixmap();

        if (!data.getTextureData().isPrepared()) {
            data.getTextureData().prepare();
        }
        Pixmap dataPixmap = data.getTextureData().consumePixmap();
        int minX = dataPixmap.getWidth();
        int maxX = 0;
        int minY = dataPixmap.getHeight();
        int maxY = 0;
        for (int x = 0; x < dataPixmap.getWidth(); x++)
        {
            for (int y = 0; y < dataPixmap.getHeight(); y++)
            {
                int c = dataPixmap.getPixel(x, y);
                int alpha = c & 0xFF;
                if (alpha != 0)
                {
                    minX = Math.min(minX, x);
                    maxX = Math.max(maxX, x);
                    minY = Math.min(minY, y);
                    maxY = Math.max(maxY, y);
                }
            }
        }
        if (minX > maxX || minY > maxY) return texture;
        Pixmap cropped = new Pixmap(maxX - minX, maxY - minY, data.getTextureData().getFormat());
        cropped.drawPixmap(texturePixmap, -minX - 1, -minY - 1);
        Pixmap result = new Pixmap(dataPixmap.getWidth(), dataPixmap.getHeight(), dataPixmap.getFormat());
        result.drawPixmap(cropped, minX - 1, minY - 1);
        Texture t = new Texture(result);
        cropped.dispose();
        texturePixmap.dispose();
        texture.dispose();
        return t;
    }
    //texture 1 is target size and foreground
    //texture 2 is the background
    public static Texture combineTextures(Texture texture1, Texture texture2)
    {
        //cropToTexture(texture2, texture1);
        if (!texture1.getTextureData().isPrepared()) {
            texture1.getTextureData().prepare();
        }
        Pixmap foreground = texture1.getTextureData().consumePixmap();

        if (!texture2.getTextureData().isPrepared()) {
            texture2.getTextureData().prepare();
        }
        Pixmap background = texture2.getTextureData().consumePixmap();

        Pixmap result = new Pixmap(foreground.getWidth(), foreground.getHeight(), foreground.getFormat());

        result.drawPixmap(background, 0,0);
//        Color pixelColor = new Color();
//        for (int x=0; x<foreground.getWidth();x++)
//        {
//            for (int y=0;y<foreground.getHeight();y++)
//            {
//                Color.rgba8888ToColor(pixelColor, background.getPixel(x, y));
//                result.setColor(pixelColor);
//                result.drawPixel(x,y);
//            }
//        }
        result.drawPixmap(foreground,0,0);

        Texture textureResult = new Texture(result);
        //result.dispose();
        foreground.dispose();
        background.dispose();
        return textureResult;


//        if (!texture1.getTextureData().isPrepared()) {
//            texture1.getTextureData().prepare();
//        }
//        Pixmap pixmap1 = texture1.getTextureData().consumePixmap();
//

//        Pixmap pixmap2 = texture2.getTextureData().consumePixmap();
//
//        pixmap1.drawPixmap(pixmap2, 0, 0);
//
//        Texture textureResult = new Texture(pixmap1);
////        pixmap1.dispose();
////        pixmap2.dispose();
//
//        return textureResult;

//        if (!smallRoot.getTextureData().isPrepared()) {
//            smallRoot.getTextureData().prepare();
//        }
//        Pixmap pixmapRoot = smallRoot.getTextureData().consumePixmap();
//
//        if (!toGoOnTop.getTextureData().isPrepared()) {
//            toGoOnTop.getTextureData().prepare();
//        }
//        Pixmap pixmapToGoOnTop = toGoOnTop.getTextureData().consumePixmap();
//        //Pixmap pixmap2 = toGoOnTop.getTextureData().consumePixmap();
//        Pixmap resizedOnTop = new Pixmap(smallRoot.getWidth(), smallRoot.getHeight(), smallRoot.getTextureData().getFormat());
//        Color pixelColor = new Color();
//        for (int x=0; x<resizedOnTop.getWidth();x++)
//        {
//            for (int y=0;y<resizedOnTop.getHeight();y++)
//            {
//                Color.rgba8888ToColor(pixelColor, pixmapToGoOnTop.getPixel(x, y));
//                resizedOnTop.setColor(pixelColor);
//                resizedOnTop.drawPixel(x,y);
//            }
//        }
//
//        pixmapRoot.drawPixmap(resizedOnTop, 0, 0);
////
////        if (ReflectionHacks.getPrivate(pixmapRoot, Pixmap.class, "disposed")) {
////            ReflectionHacks.setPrivate(pixmapRoot, Pixmap.class, "disposed", false);
////        }
//        Texture textureResult = new Texture(pixmapRoot);
//        pixmapRoot.dispose();
//        pixmapToGoOnTop.dispose();
//        resizedOnTop.dispose();
//        smallRoot.dispose();
//        toGoOnTop.dispose();
//
//        return textureResult;
    }

    /*----------Localization----------*/

    //This is used to load the appropriate localization files based on language.
    private static String getLangString()
    {
        return Settings.language.name().toLowerCase();
    }
    private static final String defaultLanguage = "eng";

    @Override
    public void receiveEditStrings() {
        /*
            First, load the default localization.
            Then, if the current language is different, attempt to load localization for that language.
            This results in the default localization being used for anything that might be missing.
            The same process is used to load keywords slightly below.
        */
        loadLocalization(defaultLanguage); //no exception catching for default localization; you better have at least one that works.
        if (!defaultLanguage.equals(getLangString())) {
            try {
                loadLocalization(getLangString());
            }
            catch (GdxRuntimeException e) {
                e.printStackTrace();
            }
        }
    }

    private void loadLocalization(String lang) {
        //While this does load every type of localization, most of these files are just outlines so that you can see how they're formatted.
        //Feel free to comment out/delete any that you don't end up using.
        BaseMod.loadCustomStringsFile(CardStrings.class,
                localizationPath(lang, "CardStrings.json"));
        BaseMod.loadCustomStringsFile(CharacterStrings.class,
                localizationPath(lang, "CharacterStrings.json"));
        BaseMod.loadCustomStringsFile(EventStrings.class,
                localizationPath(lang, "EventStrings.json"));
        BaseMod.loadCustomStringsFile(OrbStrings.class,
                localizationPath(lang, "OrbStrings.json"));
        BaseMod.loadCustomStringsFile(PotionStrings.class,
                localizationPath(lang, "PotionStrings.json"));
        BaseMod.loadCustomStringsFile(PowerStrings.class,
                localizationPath(lang, "PowerStrings.json"));
        BaseMod.loadCustomStringsFile(RelicStrings.class,
                localizationPath(lang, "RelicStrings.json"));
        BaseMod.loadCustomStringsFile(UIStrings.class,
                localizationPath(lang, "UIStrings.json"));
    }

    @Override
    public void receiveEditKeywords()
    {
        Gson gson = new Gson();
        String json = Gdx.files.internal(localizationPath(defaultLanguage, "Keywords.json")).readString(String.valueOf(StandardCharsets.UTF_8));
        KeywordInfo[] keywords = gson.fromJson(json, KeywordInfo[].class);
        for (KeywordInfo keyword : keywords) {
            registerKeyword(keyword);
        }

        if (!defaultLanguage.equals(getLangString())) {
            try
            {
                json = Gdx.files.internal(localizationPath(getLangString(), "Keywords.json")).readString(String.valueOf(StandardCharsets.UTF_8));
                keywords = gson.fromJson(json, KeywordInfo[].class);
                for (KeywordInfo keyword : keywords) {
                    registerKeyword(keyword);
                }
            }
            catch (Exception e)
            {
                logger.warn(modID + " does not support " + getLangString() + " keywords.");
            }
        }
    }

    private void registerKeyword(KeywordInfo info) {
        BaseMod.addKeyword(modID.toLowerCase(), info.PROPER_NAME, info.NAMES, info.DESCRIPTION);
    }

    //These methods are used to generate the correct filepaths to various parts of the resources folder.
    public static String localizationPath(String lang, String file) {
        return resourcesFolder + "/localization/" + lang + "/" + file;
    }

    public static String resourcePath(String file) {
        return resourcesFolder + "/" + file;
    }
    public static String characterPath(String file) {
        return resourcesFolder + "/character/" + file;
    }
    public static String powerPath(String file) {
        return resourcesFolder + "/powers/" + file;
    }
    public static String relicPath(String file) {
        return resourcesFolder + "/relics/" + file;
    }


    //This determines the mod's ID based on information stored by ModTheSpire.
    private static void loadModInfo() {
        Optional<ModInfo> infos = Arrays.stream(Loader.MODINFOS).filter((modInfo)->{
            AnnotationDB annotationDB = Patcher.annotationDBMap.get(modInfo.jarURL);
            if (annotationDB == null)
                return false;
            Set<String> initializers = annotationDB.getAnnotationIndex().getOrDefault(SpireInitializer.class.getName(), Collections.emptySet());
            return initializers.contains(UntransparentMain.class.getName());
        }).findFirst();
        if (infos.isPresent()) {
            info = infos.get();
            modID = info.ID;
        }
        else {
            throw new RuntimeException("Failed to determine mod info/ID based on initializer.");
        }
    }
}