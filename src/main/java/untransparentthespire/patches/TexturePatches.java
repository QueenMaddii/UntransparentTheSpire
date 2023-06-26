package untransparentthespire.patches;

import basemod.abstracts.CustomPlayer;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.TextureData;
import com.evacipated.cardcrawl.modthespire.Loader;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.evacipated.cardcrawl.modthespire.patcher.PatchingException;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.helpers.Prefs;
import javassist.*;
import javassist.expr.ExprEditor;
import javassist.expr.NewExpr;
import org.clapper.util.classutil.*;

import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

import static untransparentthespire.UntransparentMain.*;
import static untransparentthespire.util.TextureLoader.getTexture;

public class TexturePatches
{
    @SpirePatch(clz=CustomPlayer.class, method="getCustomModeCharacterButtonImage")
    public static class BaseModCompatibilityFix
    {
        @SpireInsertPatch(locator = Locator.class, localvars = {"texture"})
        public static SpireReturn<Texture> Insert(CustomPlayer __instance, Texture texture)
        {
            return SpireReturn.Return(texture);
        }
        public static class Locator extends SpireInsertLocator
        {
            public int[] Locate(CtBehavior ctBehavior) throws CannotCompileException, PatchingException {
                Matcher finalMatcher = new Matcher.MethodCallMatcher(Pixmap.class, "dispose");
                return new int[]{LineFinder.findAllInOrder(ctBehavior, new ArrayList<Matcher>(), finalMatcher)[1]};
            }
        }
    }

    @SpirePatch(clz = CardCrawlGame.class, method = SpirePatch.CONSTRUCTOR)
    public static class ImSorryKio
    {
        @SpireRawPatch
        public static void rawr(CtBehavior ctBehavior) throws CannotCompileException
        {
            ClassFinder finder = new ClassFinder();
            finder.add(new File(Loader.STS_JAR));
            Arrays.stream(Loader.MODINFOS).filter(Objects::nonNull).forEach(i -> {
                try {
                    finder.add(new File(i.jarURL.toURI()));
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            });
            ClassPool pool = ctBehavior.getDeclaringClass().getClassPool();

            ClassFilter justAllOfThem = new AndClassFilter(
                    new NotClassFilter(new InterfaceOnlyClassFilter()),
                    new ClassModifiersClassFilter(java.lang.reflect.Modifier.PUBLIC),
                    new NotClassFilter((classInfo, classFinder) -> classInfo.getClassName().matches(".*megacrit.*")),
                    new NotClassFilter(((classInfo, classFinder) -> classInfo.getClassName().matches("^org\\..*"))),
                    new NotClassFilter(((classInfo, classFinder) -> classInfo.getClassName().matches(".*untransparent.*"))),
                    new NotClassFilter(((classInfo, classFinder) -> classInfo.getClassName().matches(".*gdx.*"))),
                    ((classInfo, classFinder) -> true));
            ArrayList<ClassInfo> allClasses = new ArrayList<>();
            finder.findClasses(allClasses, justAllOfThem);

            allClasses.stream()
                    .map(c ->
                    {
                        try
                        {

                            return pool.get(c.getClassName());
                        } catch (NotFoundException e)
                        {
                            throw new RuntimeException(e);
                        }
                    })
                    .map(CtClass::getDeclaredMethods).flatMap(Arrays::stream)
                    .filter(m -> (!m.isEmpty() && !Modifier.isNative(m.getModifiers())))
                    .forEach(m ->
                    {
                        try
                        {
                            m.instrument(new ExprEditor()
                            {
                                public void edit(NewExpr e) throws CannotCompileException {
                                    if (e.getClassName().equals(Texture.class.getName()))
                                    {
                                        e.replace("{" +
                                                "$_ = untransparentthespire.patches.TexturePatches.ImSorryKio.theThing($proceed($$));" +
                                                "}");
                                    }
                                }
                            });
                        } catch (CannotCompileException e)
                        {
                            throw new RuntimeException(e);
                        }
                    });
        }

        public static Texture theThing(Texture t)
        {
            Texture q = cropToTexture(getTexture(resourcePath("morenothing.png")), t);

            Texture r = combineTextures(t, q);
            t.dispose();
            return r;
            //            Texture q = getTexture(resourcePath("morenothing.png"));
//            Texture t = combineTextures(retVal[0], q);
//            retVal[0].dispose();
//            retVal[0] = t;
        }
    }
}


//    @SpirePatch(clz= Texture.class,method= SpirePatch.CONSTRUCTOR,paramtypez={int.class,int.class, TextureData.class})
//    public static class TextureConstruct
//    {
//        @SpirePostfixPatch
//        public static void Postfix(Texture __instance, int i, int j, @ByRef TextureData[] data)
//        {
//            Texture q = getTexture(resourcePath("morenothing.png"));
//            Texture t = combineTextures(__instance, q);
//            __instance.dispose();
//            __instance = t;
//            //retVal[0].dispose();
//            //retVal[0] = t;
//        }
//    }

