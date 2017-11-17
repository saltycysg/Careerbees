package com.rwtema.careerbees.networking;

import com.rwtema.careerbees.entity.EntityChunkData;
import com.rwtema.careerbees.gui.ContainerSettings;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.IOException;

public class BeeNetworking {
	public static SimpleNetworkWrapper net;

	public static void init() {
		net = new SimpleNetworkWrapper("PracBees");
		IMessageHandler<MessageBase, IMessage> genericHandler = (message, ctx) -> {
			message.onReceived(ctx);return null;
		};
		net.registerMessage(genericHandler, EntityChunkData.PacketEntityChunkData.class, 0, Side.CLIENT);
		net.registerMessage(genericHandler, ContainerSettings.MessageNBT.class, 1, Side.SERVER);
//		net.registerMessage(MessageClearTile.Handler.class, MessageClearTile.class, 0, Side.SERVER);
//		net.registerMessage(MessageClearTile.Handler.class, MessageClearTile.class, 0, Side.CLIENT);
//		net.registerMessage(MessageObstruction.Handler.class, MessageObstruction.class, 1, Side.SERVER);
//		net.registerMessage(MessageObstruction.Handler.class, MessageObstruction.class, 1, Side.CLIENT);
//		net.registerMessage(MessageOneTimeChat.Handler.class, MessageOneTimeChat.class, 2, Side.SERVER);
//		net.registerMessage(MessageOneTimeChat.Handler.class, MessageOneTimeChat.class, 2, Side.CLIENT);
	}

	public abstract static class MessageBase implements IMessage {

		public void onReceived(MessageContext ctx) {

		}
	}

	public abstract static class MessageClientToServer extends MessageBase {

		@Override
		public void onReceived(MessageContext ctx) {
			EntityPlayerMP player = ctx.getServerHandler().player;
			FMLCommonHandler.instance().getMinecraftServerInstance().addScheduledTask(() -> runServer(ctx, player));
		}

		protected abstract void runServer(MessageContext ctx, EntityPlayerMP player);

		public void writeNBT(NBTTagCompound tag, ByteBuf buf){
			new PacketBuffer(buf).writeCompoundTag(tag);
		}

		public NBTTagCompound readNBT(ByteBuf bf){
			try {
				return new PacketBuffer(bf).readCompoundTag();
			} catch (IOException e) {
				throw new DecoderException(e);
			}
		}
	}

	public abstract static class MessageServerToClient extends MessageBase {
		@Override
		@SideOnly(Side.CLIENT)
		public void onReceived(MessageContext ctx) {
			Minecraft.getMinecraft().addScheduledTask(() -> runClient(ctx, Minecraft.getMinecraft().player));
		}

		@SideOnly(Side.CLIENT)
		protected abstract void runClient(MessageContext ctx, EntityPlayer player);
	}
}
