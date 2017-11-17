package com.rwtema.careerbees.helpers;

import com.google.common.collect.Iterables;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.INBTSerializable;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.function.*;

@SuppressWarnings("unused")
public class NBTSerializer<T> {
	private static HashMap<Class<?>, NBTSerializer<?>> clazzMap = new HashMap<>();
	public final List<NBTSerializerEntry<? super T>> serializers;
	public final Iterable<NBTSerializerEntry<? super T>> iterable;

	public NBTSerializer(NBTSerializer<? super T> parent) {
		serializers = new ArrayList<>();
		if (parent != null) {
			iterable = Iterables.concat(parent.iterable, serializers);
		} else {
			iterable = serializers;
		}
	}

	@SuppressWarnings("unchecked")
	@Nonnull
	public static <T, K extends NBTBase> BiConsumer<T, NBTBase> convertSetterSerializable(Function<T, INBTSerializable<K>> nbtSerializable) {
		return (t, nbtBase) -> nbtSerializable.apply(t).deserializeNBT((K) nbtBase);
	}

	@Nonnull
	public static <T, K extends NBTBase> Function<T, NBTBase> convertGetterSerializable(Function<T, INBTSerializable<K>> nbtSerializable) {
		return t -> nbtSerializable.apply(t).serializeNBT();
	}

	public static <T> NBTSerializer<T> getClassSerializer(Class<T> clazz, Class<?> baseParentClass) {
		NBTSerializer<?> nbtSerializer = clazzMap.get(clazz);
		if (nbtSerializer == null) {
			if (clazz == baseParentClass) {
				nbtSerializer = new NBTSerializer<>(null);
			} else {
				nbtSerializer = new NBTSerializer<>(getClassSerializer(clazz.getSuperclass(), baseParentClass));
			}
			clazzMap.put(clazz, nbtSerializer);
		}
		return (NBTSerializer<T>) nbtSerializer;
	}

	public void readFromNBT(T t, NBTTagCompound tag) {
		for (NBTSerializerEntry<? super T> serializerEntry : iterable) {
			if (serializerEntry.expectedType == -1 ?
					tag.hasKey(serializerEntry.key) :
					tag.hasKey(serializerEntry.key, serializerEntry.expectedType)
					) {
				NBTBase base = tag.getTag(serializerEntry.key);
				serializerEntry.setter.accept(t, base);
			}
		}
	}

	public NBTTagCompound writeToNBT(T t, NBTTagCompound tag) {
		for (NBTSerializerEntry<? super T> serializerEntry : iterable) {
			NBTBase apply;
			try {
				apply = serializerEntry.getter.apply(t);
			} catch (Exception err) {
				err.printStackTrace();
				continue;
			}
			tag.setTag(serializerEntry.key, apply);
		}
		return tag;
	}

	private NBTSerializer<T> addEntry(NBTSerializerEntry<T> entry) {
		for (NBTSerializerEntry<? super T> serializer : iterable) {
			if (serializer.key.equals(entry.key)) {
				throw new IllegalStateException("Duplicate key: " + serializer.key);
			}
		}
		serializers.add(entry);
		return this;
	}

	public <K extends NBTBase> NBTSerializer<T> addNBTSerializable(String key, Function<T, INBTSerializable<K>> nbtSerializable) {
		return addEntry(new NBTSerializerEntry<>(key,
				convertGetterSerializable(nbtSerializable),
				convertSetterSerializable(nbtSerializable),
				-1));
	}

	public NBTSerializer<T> addByte(String key, ToByteFunction<T> getter, ObjByteConsumer<T> setter) {
		return addEntry(new NBTSerializer.NBTSerializerEntry<>(key,
				obj -> new NBTTagByte(getter.applyAsByte(obj)),
				(t, nbtBase) -> setter.accept(t, ((NBTTagByte) nbtBase).getByte()),
				Constants.NBT.TAG_BYTE));
	}

	public NBTSerializer<T> addShort(String key, ToShortFunction<T> getter, ObjShortConsumer<T> setter) {
		return addEntry(new NBTSerializer.NBTSerializerEntry<>(key,
				obj -> new NBTTagShort(getter.applyAsShort(obj)),
				(t, nbtBase) -> setter.accept(t, ((NBTTagShort) nbtBase).getShort()),
				Constants.NBT.TAG_SHORT));
	}

	public NBTSerializer<T> addInt(String key, ToIntFunction<T> getter, ObjIntConsumer<T> setter) {
		return addEntry(new NBTSerializer.NBTSerializerEntry<>(key,
				obj -> new NBTTagInt(getter.applyAsInt(obj)),
				(t, nbtBase) -> setter.accept(t, ((NBTTagInt) nbtBase).getInt()),
				Constants.NBT.TAG_INT));
	}

	public NBTSerializer<T> addFloat(String key, ToFloatFunction<T> getter, ObjFloatConsumer<T> setter) {
		return addEntry(new NBTSerializer.NBTSerializerEntry<>(key,
				obj -> new NBTTagFloat(getter.applyAsFloat(obj)),
				(t, nbtBase) -> setter.accept(t, ((NBTTagFloat) nbtBase).getFloat()),
				Constants.NBT.TAG_FLOAT));
	}

	public NBTSerializer<T> addLong(String key, ToLongFunction<T> getter, ObjLongConsumer<T> setter) {
		return addEntry(new NBTSerializer.NBTSerializerEntry<>(key,
				obj -> new NBTTagLong(getter.applyAsLong(obj)),
				(t, nbtBase) -> setter.accept(t, ((NBTTagLong) nbtBase).getLong()),
				Constants.NBT.TAG_LONG));
	}

	public NBTSerializer<T> addDouble(String key, ToDoubleFunction<T> getter, ObjDoubleConsumer<T> setter) {
		return addEntry(new NBTSerializer.NBTSerializerEntry<>(key,
				obj -> new NBTTagDouble(getter.applyAsDouble(obj)),
				(t, nbtBase) -> setter.accept(t, ((NBTTagDouble) nbtBase).getDouble()),
				Constants.NBT.TAG_DOUBLE));
	}

	public NBTSerializer<T> addByteArray(String key, Function<T, byte[]> getter, BiConsumer<T, byte[]> setter) {
		return addEntry(new NBTSerializer.NBTSerializerEntry<>(key,
				obj -> new NBTTagByteArray(getter.apply(obj)),
				(t, nbtBase) -> setter.accept(t, ((NBTTagByteArray) nbtBase).getByteArray()),
				Constants.NBT.TAG_BYTE_ARRAY));
	}

	public NBTSerializer<T> addIntArray(String key, Function<T, int[]> getter, BiConsumer<T, int[]> setter) {
		return addEntry(new NBTSerializer.NBTSerializerEntry<>(key,
				obj -> new NBTTagIntArray(getter.apply(obj)),
				(t, nbtBase) -> setter.accept(t, ((NBTTagIntArray) nbtBase).getIntArray()),
				Constants.NBT.TAG_INT_ARRAY));
	}

	public NBTSerializer<T> addString(String key, Function<T, String> getter, BiConsumer<T, String> setter) {
		return addEntry(new NBTSerializer.NBTSerializerEntry<>(key,
				obj -> new NBTTagString(getter.apply(obj)),
				(t, nbtBase) -> setter.accept(t, ((NBTTagString) nbtBase).getString()),
				Constants.NBT.TAG_STRING));
	}

	public NBTSerializer<T> addNBTTagCompound(String key, Function<T, NBTTagCompound> getter, BiConsumer<T, NBTTagCompound> setter) {
		return addEntry(new NBTSerializer.NBTSerializerEntry<>(key,
				getter::apply,
				(t, nbtBase) -> setter.accept(t, ((NBTTagCompound) nbtBase)),
				Constants.NBT.TAG_COMPOUND));
	}

	public NBTSerializer<T> addBlockPos(String key, Function<T, BlockPos> getter, BiConsumer<T, BlockPos> setter) {
		return addLong(key, value -> getter.apply(value).toLong(), (t, value) -> setter.accept(t, BlockPos.fromLong(value)));
	}

	public NBTSerializer<T> addItemStack(String key, Function<T, ItemStack> getter, BiConsumer<T, ItemStack> setter) {
		return addNBTTagCompound(key, t -> getter.apply(t).writeToNBT(new NBTTagCompound()), (t, nbtTagCompound) -> setter.accept(t, new ItemStack(nbtTagCompound)));
	}

	@SuppressWarnings("unchecked")
	public <E, K extends NBTBase> NBTSerializer<T> addCollection(String key, Function<T, Collection<E>> getter, Function<E, K> writer, Function<K, E> reader, int expectedID) {
		return addEntry(new NBTSerializer.NBTSerializerEntry<>(key,
				t -> {
					Collection<E> collection = getter.apply(t);
					NBTTagList list = new NBTTagList();
					for (E e : collection) {
						list.appendTag(writer.apply(e));
					}
					return list;
				},
				(t, nbtBase) -> {
					Collection<E> collection = getter.apply(t);
					collection.clear();
					NBTTagList list = (NBTTagList) nbtBase;
					for (int i = 0; i < list.tagCount(); i++) {
						NBTBase base = list.get(i);
						E e = reader.apply((K) base);
						collection.add(e);
					}
				}, Constants.NBT.TAG_LIST));
	}

	public interface ToByteFunction<T> {
		byte applyAsByte(T t);
	}

	public interface ToShortFunction<T> {
		short applyAsShort(T t);
	}

	public interface ToFloatFunction<T> {
		float applyAsFloat(T t);
	}

	public interface ObjByteConsumer<T> {
		void accept(T t, byte value);
	}

	public interface ObjShortConsumer<T> {
		void accept(T t, short value);
	}

	public interface ObjFloatConsumer<T> {
		void accept(T t, float value);
	}

	private static class NBTSerializerEntry<T> {
		public final Function<? super T, NBTBase> getter;
		public final BiConsumer<? super T, NBTBase> setter;
		public final int expectedType;
		public final String key;

		private NBTSerializerEntry(String key, Function<T, NBTBase> getter, BiConsumer<T, NBTBase> setter, int expectedType) {
			this.key = key;
			this.getter = getter;
			this.setter = setter;
			this.expectedType = expectedType;
		}

		private NBTSerializerEntry(NBTSerializer.NBTSerializerEntry<? super T> toCopy) {
			this.key = toCopy.key;
			this.getter = toCopy.getter;
			this.setter = toCopy.setter;
			this.expectedType = toCopy.expectedType;

		}
	}
}
