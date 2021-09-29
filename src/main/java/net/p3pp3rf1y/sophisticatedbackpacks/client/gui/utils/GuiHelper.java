package net.p3pp3rf1y.sophisticatedbackpacks.client.gui.utils;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix4f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import net.minecraftforge.fmlclient.gui.GuiUtils;
import net.p3pp3rf1y.sophisticatedbackpacks.SophisticatedBackpacks;
import net.p3pp3rf1y.sophisticatedbackpacks.client.gui.controls.ToggleButton;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GuiHelper {
	public static final ResourceLocation GUI_CONTROLS = new ResourceLocation(SophisticatedBackpacks.MOD_ID, "textures/gui/gui_controls.png");
	public static final TextureBlitData BAR_BACKGROUND_BOTTOM = new TextureBlitData(GUI_CONTROLS, Dimension.SQUARE_256, new UV(29, 66), Dimension.SQUARE_18);
	public static final TextureBlitData BAR_BACKGROUND_MIDDLE = new TextureBlitData(GUI_CONTROLS, Dimension.SQUARE_256, new UV(29, 48), Dimension.SQUARE_18);
	public static final TextureBlitData BAR_BACKGROUND_TOP = new TextureBlitData(GUI_CONTROLS, Dimension.SQUARE_256, new UV(29, 30), Dimension.SQUARE_18);
	public static final ResourceLocation ICONS = new ResourceLocation(SophisticatedBackpacks.MOD_ID, "textures/gui/icons.png");
	public static final TextureBlitData CRAFTING_RESULT_SLOT = new TextureBlitData(GUI_CONTROLS, new UV(71, 216), new Dimension(26, 26));
	public static final TextureBlitData DEFAULT_BUTTON_HOVERED_BACKGROUND = new TextureBlitData(GUI_CONTROLS, new UV(47, 0), Dimension.SQUARE_18);
	public static final TextureBlitData DEFAULT_BUTTON_BACKGROUND = new TextureBlitData(GUI_CONTROLS, new UV(29, 0), Dimension.SQUARE_18);
	public static final TextureBlitData SMALL_BUTTON_BACKGROUND = new TextureBlitData(GuiHelper.GUI_CONTROLS, Dimension.SQUARE_256, new UV(29, 18), Dimension.SQUARE_12);
	public static final TextureBlitData SMALL_BUTTON_HOVERED_BACKGROUND = new TextureBlitData(GuiHelper.GUI_CONTROLS, Dimension.SQUARE_256, new UV(41, 18), Dimension.SQUARE_12);
	public static final ResourceLocation SLOTS_BACKGROUND = new ResourceLocation(SophisticatedBackpacks.MOD_ID, "textures/gui/slots_background.png");

	private static final Map<Integer, TextureBlitData> SLOTS_BACKGROUNDS = new HashMap<>();

	private GuiHelper() {}

	public static void renderItemInGUI(PoseStack matrixStack, Minecraft minecraft, ItemStack stack, int xPosition, int yPosition) {
		renderItemInGUI(matrixStack, minecraft, stack, xPosition, yPosition, false);
	}

	public static void renderSlotsBackground(Minecraft minecraft, PoseStack matrixStack, int x, int y, int slotWidth, int slotHeight) {
		int key = getSlotsBackgroundKey(slotWidth, slotHeight);
		blit(minecraft, matrixStack, x, y, SLOTS_BACKGROUNDS.computeIfAbsent(key, k ->
				new TextureBlitData(SLOTS_BACKGROUND, Dimension.SQUARE_256, new UV(0, 0), new Dimension(slotWidth * 18, slotHeight * 18))
		));
	}

	private static int getSlotsBackgroundKey(int slotWidth, int slotHeight) {
		return slotWidth * 31 + slotHeight;
	}

	public static void renderItemInGUI(PoseStack matrixStack, Minecraft minecraft, ItemStack stack, int xPosition, int yPosition, boolean renderOverlay) {
		renderItemInGUI(matrixStack, minecraft, stack, xPosition, yPosition, renderOverlay, null);
	}

	public static void renderItemInGUI(PoseStack matrixStack, Minecraft minecraft, ItemStack stack, int xPosition, int yPosition, boolean renderOverlay,
			@Nullable String countText) {
		ItemRenderer itemRenderer = minecraft.getItemRenderer();
		float originalZLevel = itemRenderer.blitOffset;
		itemRenderer.blitOffset += getZOffset(matrixStack);
		itemRenderer.renderAndDecorateItem(stack, xPosition, yPosition);
		if (renderOverlay) {
			itemRenderer.renderGuiItemDecorations(minecraft.font, stack, xPosition, yPosition, countText);
		}
		itemRenderer.blitOffset = originalZLevel;
	}

	private static int getZOffset(PoseStack matrixStack) {
		Float zOffset = ObfuscationReflectionHelper.getPrivateValue(Matrix4f.class, matrixStack.last().pose(), "f_27614_");
		return zOffset == null ? 0 : zOffset.intValue();
	}

	public static void blit(Minecraft minecraft, PoseStack matrixStack, int x, int y, TextureBlitData texData) {
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderTexture(0, texData.getTextureName());
		GuiComponent.blit(matrixStack, x + texData.getXOffset(), y + texData.getYOffset(), texData.getU(), texData.getV(), texData.getWidth(), texData.getHeight(), texData.getTextureWidth(), texData.getTextureHeight());
	}

	public static void coloredBlit(Matrix4f matrix, int x, int y, TextureBlitData texData, int color) {
		float red = (color >> 16 & 255) / 255F;
		float green = (color >> 8 & 255) / 255F;
		float blue = (color & 255) / 255F;
		float alpha = (color >> 24 & 255) / 255F;

		int xMin = x + texData.getXOffset();
		int yMin = y + texData.getYOffset();
		int xMax = xMin + texData.getWidth();
		int yMax = yMin + texData.getHeight();

		float minU = (float) texData.getU() / texData.getTextureWidth();
		float maxU = minU + ((float) texData.getWidth() / texData.getTextureWidth());
		float minV = (float) texData.getV() / texData.getTextureHeight();
		float maxV = minV + ((float) texData.getHeight() / texData.getTextureWidth());

		RenderSystem.setShader(GameRenderer::getPositionColorTexShader);
		BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
		bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX);
		bufferbuilder.vertex(matrix, xMin, yMax, 0).color(red, green, blue, alpha).uv(minU, maxV).endVertex();
		bufferbuilder.vertex(matrix, xMax, yMax, 0).color(red, green, blue, alpha).uv(maxU, maxV).endVertex();
		bufferbuilder.vertex(matrix, xMax, yMin, 0).color(red, green, blue, alpha).uv(maxU, minV).endVertex();
		bufferbuilder.vertex(matrix, xMin, yMin, 0).color(red, green, blue, alpha).uv(minU, minV).endVertex();
		bufferbuilder.end();
		BufferUploader.end(bufferbuilder);
	}

	private static List<? extends FormattedText> tooltipToRender = Collections.emptyList();

	public static void setTooltipToRender(List<? extends FormattedText> tooltip) {
		tooltipToRender = tooltip;
	}

	public static void renderTooltip(Minecraft minecraft, PoseStack matrixStack, int mouseX, int mouseY) {
		if (tooltipToRender.isEmpty()) {
			return;
		}

		renderTooltip(minecraft, matrixStack, tooltipToRender, mouseX, mouseY, ITooltipRenderPart.EMPTY, null, ItemStack.EMPTY, 150);
		tooltipToRender = Collections.emptyList();
	}

	public static void renderTooltip(Minecraft minecraft, PoseStack matrixStack, List<? extends FormattedText> textLines, int mouseX, int mouseY,
			ITooltipRenderPart additionalRender, @Nullable Font tooltipRenderFont, ItemStack stack) {
		renderTooltip(minecraft, matrixStack, textLines, mouseX, mouseY, additionalRender, tooltipRenderFont, stack, 0);
	}

	public static void renderTooltip(Minecraft minecraft, PoseStack matrixStack, List<? extends FormattedText> textLines, int mouseX, int mouseY,
			ITooltipRenderPart additionalRender, @Nullable Font tooltipRenderFont, ItemStack stack, int maxTextWidth) {

		Font font = tooltipRenderFont == null ? minecraft.font : tooltipRenderFont;

		int windowWidth = minecraft.getWindow().getGuiScaledWidth();
		int windowHeight = minecraft.getWindow().getGuiScaledHeight();

		int tooltipWidth = getMaxLineWidth(textLines, font);
		tooltipWidth = Math.max(tooltipWidth, additionalRender.getWidth());

		boolean needsWrap = false;

		if (maxTextWidth > 0 && tooltipWidth > maxTextWidth) {
			tooltipWidth = maxTextWidth;
			needsWrap = true;
		}

		if (needsWrap) {
			int wrappedTooltipWidth = 0;
			List<FormattedText> wrappedTextLines = new ArrayList<>();
			for (FormattedText textLine : textLines) {
				List<FormattedText> wrappedLine = font.getSplitter().splitLines(textLine, tooltipWidth, Style.EMPTY);

				for (FormattedText line : wrappedLine) {
					int lineWidth = font.width(line);
					if (lineWidth > wrappedTooltipWidth) {wrappedTooltipWidth = lineWidth;}
					wrappedTextLines.add(line);
				}
			}
			tooltipWidth = wrappedTooltipWidth;
			textLines = wrappedTextLines;
		}

		int leftX = mouseX + 12;
		if (leftX + tooltipWidth > windowWidth) {
			leftX -= 28 + tooltipWidth;
		}

		int topY = mouseY - 12;
		int tooltipHeight = 8;
		if (textLines.size() > 1) {
			tooltipHeight += 2 + (textLines.size() - 1) * 10;
		}
		tooltipHeight += additionalRender.getHeight();

		if (topY + tooltipHeight + 6 > windowHeight) {
			topY = windowHeight - tooltipHeight - 6;
		}

		int backgroundColor = GuiUtils.DEFAULT_BACKGROUND_COLOR;
		int borderColorStart = GuiUtils.DEFAULT_BORDER_COLOR_START;
		int borderColorEnd = GuiUtils.DEFAULT_BORDER_COLOR_END;
		RenderTooltipEvent.Color colorEvent = new RenderTooltipEvent.Color(stack, textLines, matrixStack, leftX, topY, font, backgroundColor, borderColorStart, borderColorEnd);
		MinecraftForge.EVENT_BUS.post(colorEvent);
		backgroundColor = colorEvent.getBackground();
		borderColorStart = colorEvent.getBorderStart();
		borderColorEnd = colorEvent.getBorderEnd();

		matrixStack.pushPose();
		Matrix4f matrix4f = matrixStack.last().pose();
		renderTooltipBackground(matrix4f, tooltipWidth, leftX, topY, tooltipHeight, backgroundColor, borderColorStart, borderColorEnd);

		MinecraftForge.EVENT_BUS.post(new RenderTooltipEvent.PostBackground(stack, textLines, matrixStack, leftX, topY, font, tooltipWidth, tooltipHeight));

		MultiBufferSource.BufferSource renderTypeBuffer = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
		matrixStack.translate(0.0D, 0.0D, 400.0D);

		topY = writeTooltipLines(textLines, font, (float) leftX, topY, matrix4f, renderTypeBuffer, -1);

		renderTypeBuffer.endBatch();
		additionalRender.render(matrixStack, leftX, topY, font);
		matrixStack.popPose();

		MinecraftForge.EVENT_BUS.post(new RenderTooltipEvent.PostText(stack, textLines, matrixStack, leftX, topY, font, tooltipWidth, tooltipHeight));
	}

	public static void renderTooltipBackground(Matrix4f matrix4f, int tooltipWidth, int leftX, int topY, int tooltipHeight, int backgroundColor, int borderColorStart, int borderColorEnd) {
		Tesselator tessellator = Tesselator.getInstance();
		BufferBuilder bufferbuilder = tessellator.getBuilder();
		RenderSystem.setShader(GameRenderer::getPositionColorShader);
		bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

		fillGradient(matrix4f, bufferbuilder, leftX - 3, topY - 4, leftX + tooltipWidth + 3, topY - 3, 400, backgroundColor, backgroundColor);
		fillGradient(matrix4f, bufferbuilder, leftX - 3, topY + tooltipHeight + 3, leftX + tooltipWidth + 3, topY + tooltipHeight + 4, 400, backgroundColor, backgroundColor);
		fillGradient(matrix4f, bufferbuilder, leftX - 3, topY - 3, leftX + tooltipWidth + 3, topY + tooltipHeight + 3, 400, backgroundColor, backgroundColor);
		fillGradient(matrix4f, bufferbuilder, leftX - 4, topY - 3, leftX - 3, topY + tooltipHeight + 3, 400, backgroundColor, backgroundColor);
		fillGradient(matrix4f, bufferbuilder, leftX + tooltipWidth + 3, topY - 3, leftX + tooltipWidth + 4, topY + tooltipHeight + 3, 400, backgroundColor, backgroundColor);
		fillGradient(matrix4f, bufferbuilder, leftX - 3, topY - 3 + 1, leftX - 3 + 1, topY + tooltipHeight + 3 - 1, 400, borderColorStart, borderColorEnd);
		fillGradient(matrix4f, bufferbuilder, leftX + tooltipWidth + 2, topY - 3 + 1, leftX + tooltipWidth + 3, topY + tooltipHeight + 3 - 1, 400, borderColorStart, borderColorEnd);
		fillGradient(matrix4f, bufferbuilder, leftX - 3, topY - 3, leftX + tooltipWidth + 3, topY - 3 + 1, 400, borderColorStart, borderColorStart);
		fillGradient(matrix4f, bufferbuilder, leftX - 3, topY + tooltipHeight + 2, leftX + tooltipWidth + 3, topY + tooltipHeight + 3, 400, borderColorEnd, borderColorEnd);
		RenderSystem.enableDepthTest();
		RenderSystem.disableTexture();
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		bufferbuilder.end();
		BufferUploader.end(bufferbuilder);
		RenderSystem.disableBlend();
		RenderSystem.enableTexture();
	}

	private static int getMaxLineWidth(List<? extends FormattedText> tooltips, Font font) {
		int maxLineWidth = 0;
		for (FormattedText line : tooltips) {
			int lineWidth = font.width(line);
			if (lineWidth > maxLineWidth) {
				maxLineWidth = lineWidth;
			}
		}
		return maxLineWidth;
	}

	public static int writeTooltipLines(List<? extends FormattedText> textLines, Font font, float leftX, int topY, Matrix4f matrix4f, MultiBufferSource.BufferSource renderTypeBuffer, int color) {
		for (int i = 0; i < textLines.size(); ++i) {
			FormattedText line = textLines.get(i);
			if (line != null) {
				font.drawInBatch(Language.getInstance().getVisualOrder(line), leftX, (float) topY, color, true, matrix4f, renderTypeBuffer, false, 0, 15728880);
			}

			if (i == 0) {
				topY += 2;
			}

			topY += 10;
		}
		return topY;
	}

	private static void fillGradient(Matrix4f matrix, BufferBuilder builder, int x1, int y1, int x2, int y2, int z, int colorA, int colorB) {
		float f = (float) (colorA >> 24 & 255) / 255.0F;
		float f1 = (float) (colorA >> 16 & 255) / 255.0F;
		float f2 = (float) (colorA >> 8 & 255) / 255.0F;
		float f3 = (float) (colorA & 255) / 255.0F;
		float f4 = (float) (colorB >> 24 & 255) / 255.0F;
		float f5 = (float) (colorB >> 16 & 255) / 255.0F;
		float f6 = (float) (colorB >> 8 & 255) / 255.0F;
		float f7 = (float) (colorB & 255) / 255.0F;
		builder.vertex(matrix, (float) x2, (float) y1, (float) z).color(f1, f2, f3, f).endVertex();
		builder.vertex(matrix, (float) x1, (float) y1, (float) z).color(f1, f2, f3, f).endVertex();
		builder.vertex(matrix, (float) x1, (float) y2, (float) z).color(f5, f6, f7, f4).endVertex();
		builder.vertex(matrix, (float) x2, (float) y2, (float) z).color(f5, f6, f7, f4).endVertex();
	}

	public static ToggleButton.StateData getButtonStateData(UV uv, Dimension dimension, Position offset, Component... tooltip) {
		return getButtonStateData(uv, dimension, offset, Arrays.asList(tooltip));
	}

	public static ToggleButton.StateData getButtonStateData(UV uv, String tooltip, Dimension dimension) {
		return getButtonStateData(uv, tooltip, dimension, new Position(0, 0));
	}

	public static ToggleButton.StateData getButtonStateData(UV uv, String tooltip, Dimension dimension, Position offset) {
		return new ToggleButton.StateData(new TextureBlitData(ICONS, offset, Dimension.SQUARE_256, uv, dimension),
				new TranslatableComponent(tooltip)
		);
	}

	public static ToggleButton.StateData getButtonStateData(UV uv, Dimension dimension, Position offset, List<? extends Component> tooltip) {
		return new ToggleButton.StateData(new TextureBlitData(ICONS, offset, Dimension.SQUARE_256, uv, dimension), tooltip);
	}

	public static void renderSlotsBackground(Minecraft minecraft, PoseStack matrixStack, int x, int y, int slotsInRow, int fullSlotRows, int extraRowSlots) {
		renderSlotsBackground(minecraft, matrixStack, x, y, slotsInRow, fullSlotRows);
		if (extraRowSlots > 0) {
			renderSlotsBackground(minecraft, matrixStack, x, y + fullSlotRows * 18, extraRowSlots, 1);
		}
	}

	public interface ITooltipRenderPart {
		ITooltipRenderPart EMPTY = new ITooltipRenderPart() {
			@Override
			public int getWidth() {
				return 0;
			}

			@Override
			public int getHeight() {
				return 0;
			}

			@Override
			public void render(PoseStack matrixStack, int leftX, int topY, Font font) {
				//noop
			}
		};

		int getWidth();

		int getHeight();

		void render(PoseStack matrixStack, int leftX, int topY, Font font);
	}
}
