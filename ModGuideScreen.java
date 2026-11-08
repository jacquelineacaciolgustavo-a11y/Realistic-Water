package me.skaffy.client.water;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class ModGuideScreen extends Screen {
    private final Screen lastScreen;

    public ModGuideScreen(Screen lastScreen) {
        super(Component.literal("Realistic Fluids - Guia Completo"));
        this.lastScreen = lastScreen;
    }

    @Override
    protected void init() {
        super.init();
        int btnWidth = 140;
        int btnHeight = 20;
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> this.onClose())
            .bounds(this.width / 2 - btnWidth / 2, this.height - 28, btnWidth, btnHeight)
            .build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);

        int centerX = this.width / 2;
        int boxWidth = 430;
        int boxHeight = this.height - 40;
        int leftX = centerX - boxWidth / 2;
        int rightCol = centerX + 15;

        graphics.fill(leftX - 10, 10, leftX + boxWidth + 10, 10 + boxHeight, 0xDD0D1117);
        graphics.renderOutline(leftX - 10, 10, boxWidth + 20, boxHeight, 0xFF388BFD);

        graphics.drawCenteredString(this.font, "§b§lREALISTIC FLUIDS - GUIA COMPLETO", centerX, 18, 0xFFFFFF);

        int y1 = 36;
        graphics.drawString(this.font, "§e§l[ TECLAS DE ATALHO ]", leftX, y1, 0xFFFF55);
        y1 += 12;
        graphics.drawString(this.font, "§6[B] §fFecha e desliga todas as fontes no mundo", leftX, y1, 0xFFFFFF);
        y1 += 11;
        graphics.drawString(this.font, "§6[N] §fAbre as fontes que aparecem na sua tela", leftX, y1, 0xFFFFFF);
        y1 += 11;
        graphics.drawString(this.font, "§6[G] §fConfigura fonte à distância (olhe e aperte)", leftX, y1, 0xFFFFFF);
        y1 += 11;
        graphics.drawString(this.font, "§6[/agua configurar] §fQualidade, espuma e respingos", leftX, y1, 0xFFFFFF);
        y1 += 11;
        graphics.drawString(this.font, "§6[J] §c§lBOTÃO PÂNICO: §fLimpa fluidos e tira lag", leftX, y1, 0xFFFFFF);

        y1 += 16;
        graphics.drawString(this.font, "§a§l[ BLOCOS DE FONTE (SOURCES) ]", leftX, y1, 0x55FF55);
        y1 += 12;
        graphics.drawString(this.font, "§f• Fontes de: §bÁgua§f, §6Lava§f, §2Slime§f, §8Lama§f, §fLeite§f e §4Coca", leftX, y1, 0xFFFFFF);
        y1 += 11;
        graphics.drawString(this.font, "§f• Clique Direito: Liga/desliga saída da face.", leftX, y1, 0xFFFFFF);
        y1 += 11;
        graphics.drawString(this.font, "§f• Shift + Clique: Ajusta vazão e ativa alagamento.", leftX, y1, 0xFFFFFF);
        y1 += 11;
        graphics.drawString(this.font, "§f• §3Modo Alagamento: §fFluxo massivo gelatinoso que", leftX, y1, 0xFFFFFF);
        y1 += 10;
        graphics.drawString(this.font, "  acumula altura em vez de escorrer fino.", leftX, y1, 0xAAAAAA);

        y1 += 16;
        graphics.drawString(this.font, "§d§l[ PROJÉTEIS REALISTAS ]", leftX, y1, 0xFF55FF);
        y1 += 12;
        graphics.drawString(this.font, "§f• §bBola de Neve: §fEspatifa pó de neve no chão.", leftX, y1, 0xFFFFFF);
        y1 += 11;
        graphics.drawString(this.font, "  §cReação: §fNa §4Coca-Cola §fgera geiser explosivo!", leftX, y1, 0xFFAAAA);
        y1 += 11;
        graphics.drawString(this.font, "§f• §2Bolas de Slime & Mel: §fGrudam em paredes", leftX, y1, 0xFFFFFF);
        y1 += 10;
        graphics.drawString(this.font, "  verticais e escorrem devagar até sumir.", leftX, y1, 0xAAAAAA);

        int y2 = 36;
        graphics.drawString(this.font, "§9§l[ FLUIDOS E BALDES ]", rightCol, y2, 0x55FFFF);
        y2 += 12;
        graphics.drawString(this.font, "§f• §bÁgua Realista: §fFluido limpo (sem bolhas).", rightCol, y2, 0xFFFFFF);
        y2 += 11;
        graphics.drawString(this.font, "§f• §6Lava Realista: §fTranslúcida com calor/brilho.", rightCol, y2, 0xFFFFFF);
        y2 += 11;
        graphics.drawString(this.font, "§f• §2Slime Realista: §fGelatina verde translúcida.", rightCol, y2, 0xFFFFFF);
        y2 += 11;
        graphics.drawString(this.font, "§f• §8Lama Realista: §fFluido denso marrom.", rightCol, y2, 0xFFFFFF);
        y2 += 11;
        graphics.drawString(this.font, "§f• §fLeite Realista: §fLíquido branco opaco suave.", rightCol, y2, 0xFFFFFF);
        y2 += 11;
        graphics.drawString(this.font, "§f• §4Coca-Cola: §fRefrigerante escuro e efervescente.", rightCol, y2, 0xFFFFFF);
        y2 += 11;
        graphics.drawString(this.font, "§7(Todos possuem baldes finitos na aba criativa)", rightCol, y2, 0x888888);

        y2 += 16;
        graphics.drawString(this.font, "§e§l[ BLOCOS & ITENS ESPECIAIS ]", rightCol, y2, 0xFFFF55);
        y2 += 12;
        graphics.drawString(this.font, "§f• §3Bomba d'Água: §fGera ondas e correntes fortes.", rightCol, y2, 0xFFFFFF);
        y2 += 11;
        graphics.drawString(this.font, "§f• §eAreia Realista: §fDesmorona grão por grão.", rightCol, y2, 0xFFFFFF);
        y2 += 11;
        graphics.drawString(this.font, "§f• §aFolhas Realistas: §fCaem e voam com o vento.", rightCol, y2, 0xFFFFFF);
        y2 += 11;
        graphics.drawString(this.font, "§f• §5Obsidiana Quente: §fReação de água com lava.", rightCol, y2, 0xFFFFFF);
        y2 += 11;
        graphics.drawString(this.font, "§f• §6Barco & Conversor: §fFísica para navios e blocos.", rightCol, y2, 0xFFFFFF);
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.lastScreen);
        }
    }
}