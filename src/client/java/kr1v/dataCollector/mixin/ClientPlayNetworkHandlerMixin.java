package kr1v.dataCollector.mixin;

import kr1v.dataCollector.DataCollectorClient;
import kr1v.dataCollector.Game;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {
	@Unique
	private ScreenHandlerSlotUpdateS2CPacket previousPacket = new ScreenHandlerSlotUpdateS2CPacket(0, 0, 0, ItemStack.EMPTY);

	@Inject(method = "onScreenHandlerSlotUpdate", at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/util/thread/ThreadExecutor;)V"))
	private void injected(ScreenHandlerSlotUpdateS2CPacket packet, CallbackInfo ci) {
		if (!DataCollectorClient.isInPoF) return;
		if (MinecraftClient.getInstance().player == null) return;
		final int count = packet.getStack().getCount();
		final PlayerInventory pi = MinecraftClient.getInstance().player.getInventory();
		final int count2 = packet.getSlot() < 36 ? pi.getMainStacks().get(packet.getSlot()).getCount() : ((PlayerInventoryAccessor) pi).getEquipment().get(PlayerInventory.EQUIPMENT_SLOTS.get(packet.getSlot())).getCount();

		final boolean bl1 = count - count2 == 1;
		final boolean bl2 = previousPacket.getStack().getItem() == packet.getStack().getItem();
		final boolean bl3 = previousPacket.getSyncId() == packet.getSyncId();
		final boolean bl4 = previousPacket.getSlot() == packet.getSlot();
		final boolean bl5 = (packet.getStack().get(DataComponentTypes.DAMAGE) == null) || packet.getStack().get(DataComponentTypes.DAMAGE) == 0;

		if (bl1 && bl2 && bl3 && bl4)
			System.out.println(packet.getStack().get(DataComponentTypes.DAMAGE));
		if (bl1 && bl2 && bl3 && bl4 && bl5) {
			if (DataCollectorClient.shouldStartNewGame) {
				DataCollectorClient.data.PoFListOfGames.add(new Game());
				DataCollectorClient.shouldStartNewGame = false;
			}
			String item = packet.getStack().getItem().toString().replace("minecraft:", "");
			DataCollectorClient.data.PoFListOfGames.getLast().items.add(item);
		}
		previousPacket = packet;
	}
}
