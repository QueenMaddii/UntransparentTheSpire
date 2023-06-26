package untransparentthespire.patches;

import basemod.BaseMod;
import basemod.ReflectionHacks;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.evacipated.cardcrawl.modthespire.patcher.PatchingException;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.helpers.Prefs;
import com.megacrit.cardcrawl.unlock.UnlockTracker;
import javassist.CannotCompileException;
import javassist.CtBehavior;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;

import java.util.ArrayList;

public class AbstractCardPatches
{
    @SpirePatch(clz= AbstractCard.class,method="renderImage")
    public static class RenderOrderFix
    {
        @SpireInsertPatch(locator=BeforeImageLocator.class)
        public static void Insert(AbstractCard __instance, SpriteBatch sb, boolean hovered, boolean selected)
        {
            ReflectionHacks.privateMethod(AbstractCard.class, "renderPortraitFrame", SpriteBatch.class, float.class, float.class)
                    .invoke(__instance, sb, __instance.current_x, __instance.current_y);
            ReflectionHacks.privateMethod(AbstractCard.class, "renderBannerImage", SpriteBatch.class, float.class, float.class)
                    .invoke(__instance, sb, __instance.current_x, __instance.current_y);
        }

        @SpireInsertPatch(locator= EarlyReturnLocator.class)
        public static SpireReturn<Void> Insert2(AbstractCard __instance, SpriteBatch sb, boolean hovered, boolean selected)
        {
            return SpireReturn.Return();
        }

        public static class BeforeImageLocator extends SpireInsertLocator
        {
            public int[] Locate(CtBehavior ctBehavior) throws PatchingException, CannotCompileException
            {
                Matcher finalMatcher = new Matcher.MethodCallMatcher(Prefs.class, "getBoolean");
                return LineFinder.findInOrder(ctBehavior, new ArrayList<Matcher>(), finalMatcher);
            }
        }
        public static class EarlyReturnLocator extends SpireInsertLocator
        {
            public int[] Locate(CtBehavior ctBehavior) throws PatchingException, CannotCompileException
            {
                Matcher finalMatcher = new Matcher.MethodCallMatcher(AbstractCard.class, "renderPortraitFrame");
                return LineFinder.findInOrder(ctBehavior, new ArrayList<Matcher>(), finalMatcher);
            }
        }
    }

}
