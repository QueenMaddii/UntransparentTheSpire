package untransparentthespire.patches;

import basemod.BaseMod;
import basemod.patches.com.megacrit.cardcrawl.cards.AbstractCard.RenderFixSwitches;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.evacipated.cardcrawl.modthespire.patcher.PatchingException;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import javassist.CannotCompileException;
import javassist.CtBehavior;

import java.util.ArrayList;

public class ModdedEnergyOrbPatches
{
    @SpirePatch(clz= RenderFixSwitches.RenderEnergySwitch.class, method="getEnergyOrb")
    public static class GetEnergyOrb
    {
        @SpireInsertPatch(locator = Locator.class, localvars = {"texture"})
        public static void Insert(AbstractCard c, TextureAtlas.AtlasRegion r, @ByRef Texture[] texture)
        {
//            if (!texture[0].getTextureData().isPrepared()) {
//                texture[0].getTextureData().prepare();
//            }
//            Pixmap pixmap = texture[0].getTextureData().consumePixmap();
//
//            Pixmap cropped = new Pixmap(80,80, pixmap.getFormat());
//
//            cropped.drawPixmap(pixmap, 0 - (80), 0 - 20);
//
//            Pixmap result = new Pixmap(pixmap.getWidth(), pixmap.getHeight(), pixmap.getFormat());
//            result.drawPixmap(cropped, 80, 20);
//            texture[0] = new Texture(result);
//            pixmap.dispose();
//            cropped.dispose();
        }

        public static class Locator extends SpireInsertLocator
        {
            public int[] Locate(CtBehavior ctBehavior) throws PatchingException, CannotCompileException
            {
                Matcher finalMatcher = new Matcher.MethodCallMatcher(BaseMod.class, "saveEnergyOrbTexture");
                return LineFinder.findInOrder(ctBehavior, new ArrayList<Matcher>(), finalMatcher);
            }
        }
    }
}
