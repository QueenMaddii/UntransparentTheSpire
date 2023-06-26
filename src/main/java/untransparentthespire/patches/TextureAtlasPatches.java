package untransparentthespire.patches;

import basemod.ReflectionHacks;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.ObjectSet;
import com.esotericsoftware.spine.SkeletonJson;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.evacipated.cardcrawl.modthespire.patcher.PatchingException;
import com.megacrit.cardcrawl.core.AbstractCreature;
import javassist.CannotCompileException;
import javassist.CtBehavior;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;

import java.util.ArrayList;

import static untransparentthespire.UntransparentMain.*;
import static untransparentthespire.util.TextureLoader.getTexture;

public class TextureAtlasPatches{
    @SpirePatch(clz=TextureAtlas.class,method="load",paramtypez={TextureAtlas.TextureAtlasData.class})
    public static class TextureAtlasPF
    {
        @SpireInsertPatch(locator=Locator.class,localvars ={"texture"})
        public static void Insert(TextureAtlas __instance, TextureAtlas.TextureAtlasData data, @ByRef Texture[] texture)
        {
            //Texture q = getTexture(resourcePath("morenothing.png"));
            Texture q = cropToTexture(getTexture(resourcePath("morenothing.png")), texture[0]);
            Texture t = combineTextures(texture[0], q);
            texture[0].dispose();
            texture[0] = t;
        }

        public static class Locator extends SpireInsertLocator
        {
            public int[] Locate(CtBehavior ctBehavior) throws PatchingException, CannotCompileException
            {
                Matcher finalMatcher = new Matcher.MethodCallMatcher(ObjectSet.class, "add");
                return LineFinder.findInOrder(ctBehavior, new ArrayList<Matcher>(), finalMatcher);
            }
        }
    }
}