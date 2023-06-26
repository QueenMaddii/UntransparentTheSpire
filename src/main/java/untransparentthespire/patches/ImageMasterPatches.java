package untransparentthespire.patches;

import com.badlogic.gdx.graphics.Texture;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.evacipated.cardcrawl.modthespire.patcher.PatchingException;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import javassist.CannotCompileException;
import javassist.CtBehavior;

import java.util.ArrayList;

import static untransparentthespire.UntransparentMain.*;
import static untransparentthespire.util.TextureLoader.getTexture;

public class ImageMasterPatches
{
    @SpirePatch(clz= ImageMaster.class,method="loadImage",paramtypez = {String.class})
    public static class IMLoadImage
    {
        @SpireInsertPatch(locator= Locator.class,localvars = {"retVal"})
        public static void Insert(String imgUrl, @ByRef Texture[] retVal)
        {
            //Texture q = getTexture(resourcePath("morenothing.png"));
            Texture q = cropToTexture(getTexture(resourcePath("morenothing.png")), retVal[0]);
            Texture t = combineTextures(retVal[0], q);
            retVal[0].dispose();
            retVal[0] = t;
        }
    }

    @SpirePatch(clz= ImageMaster.class,method="loadImage",paramtypez = {String.class, boolean.class})
    public static class IMLoadImage2
    {
        @SpireInsertPatch(locator= Locator.class,localvars = {"retVal"})
        public static void Insert(String imgUrl, boolean unUsed, @ByRef Texture[] retVal)
        {
            Texture q = cropToTexture(getTexture(resourcePath("morenothing.png")), retVal[0]);

            Texture t = combineTextures(retVal[0], q);
            retVal[0].dispose();
            retVal[0] = t;
        }
    }

    public static class Locator extends SpireInsertLocator
    {
        public int[] Locate(CtBehavior ctBehavior) throws PatchingException, CannotCompileException
        {
            Matcher finalMatcher = new Matcher.MethodCallMatcher(Texture.class, "setFilter");
            return LineFinder.findAllInOrder(ctBehavior, new ArrayList<Matcher>(), finalMatcher);
        }
    }
}
