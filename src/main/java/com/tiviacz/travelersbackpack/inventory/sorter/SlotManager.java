package com.tiviacz.travelersbackpack.inventory.sorter;

import com.mojang.datafixers.util.Pair;
import com.tiviacz.travelersbackpack.inventory.ITravelersBackpackContainer;
import com.tiviacz.travelersbackpack.inventory.Tiers;
import com.tiviacz.travelersbackpack.util.Reference;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

import java.util.*;
import java.util.stream.Collectors;

public class SlotManager
{
    protected final ITravelersBackpackContainer container;
    protected List<Integer> unsortableSlots = new ArrayList<>();
    protected List<Pair<Integer, ItemStack>> memorySlots = new ArrayList<>();
    protected int[] craftingSlots = new int[] {6, 7, 8, 15, 16, 17, 24, 25, 26};
    protected boolean isUnsortableActive = false;
    protected boolean isMemoryActive = false;

    private final String UNSORTABLE_SLOTS = "UnsortableSlots";
    private final String MEMORY_SLOTS = "MemorySlots";

    public static final byte UNSORTABLE = 0;
    public static final byte MEMORY = 1;
    public static final byte CRAFTING = 2;

    public SlotManager(ITravelersBackpackContainer container)
    {
        this.container = container;
    }

    public List<Integer> getUnsortableSlots()
    {
        return this.unsortableSlots;
    }

    public List<Pair<Integer, ItemStack>> getMemorySlots()
    {
        return this.memorySlots;
    }

    public boolean isSlot(byte type, int slot)
    {
        if(type == UNSORTABLE)
        {
            return unsortableSlots.contains(slot);
        }

        if(type == MEMORY)
        {
            for(Pair<Integer, ItemStack> pair : memorySlots)
            {
                if(pair.getFirst() == slot) return true;
            }
        }

        if(type == CRAFTING)
        {
            if(container.getTier() == Tiers.LEATHER)
            {
                return Arrays.stream(craftingSlots).anyMatch(i -> i == slot);
            }
            else
            {
                int[] tempCraftingSlots = craftingSlots.clone();

                for(int i = 0; i < 9; i++)
                {
                    tempCraftingSlots[i] += container.getTier().getStorageSlots() - Tiers.LEATHER.getStorageSlots();
                }

                return Arrays.stream(tempCraftingSlots).anyMatch(i -> i == slot);
            }
        }
        return false;
    }

    public void setUnsortableSlots(int[] slots, boolean isFinal)
    {
        if(isSelectorActive(UNSORTABLE))
        {
            unsortableSlots = Arrays.stream(slots).boxed().collect(Collectors.toList());

            if(isFinal)
            {
                setChanged();
            }
        }
    }

    public void setMemorySlots(int[] slots, ItemStack[] stacks, boolean isFinal)
    {
        if(isSelectorActive(MEMORY))
        {
            List<Pair<Integer, ItemStack>> pairs = new ArrayList<>();
            int[] sortOrder = slots;

            if(container.getSettingsManager().isCraftingGridLocked())
            {
                sortOrder = container.getTier().getSortOrder(container.getSettingsManager().isCraftingGridLocked());
                sortOrder = Arrays.stream(sortOrder).filter(i -> Arrays.stream(slots).anyMatch(j -> j == i)).toArray();
                List<Pair<Integer, ItemStack>> stacksList = new ArrayList<>();

                int k = 0;

                for(int j : slots)
                {
                    stacksList.add(Pair.of(j, stacks[k]));
                    k++;
                }

                List<ItemStack> sortedStacks = new ArrayList<>();

                do {
                    for(int i : sortOrder)
                    {
                        for(Iterator<Pair<Integer, ItemStack>> iterator = stacksList.iterator(); iterator.hasNext();)
                        {
                            Pair<Integer, ItemStack> pair = iterator.next();

                            if(i == pair.getFirst())
                            {
                                sortedStacks.add(pair.getSecond());
                                iterator.remove();
                            }
                        }
                    }
                }while(!stacksList.isEmpty());

                stacks = sortedStacks.toArray(ItemStack[]::new);
            }

            for(int i = 0; i < stacks.length; i++)
            {
                pairs.add(Pair.of(sortOrder[i], stacks[i]));
            }

            if(!container.getSettingsManager().isCraftingGridLocked())
            {
                //Sort
                pairs.sort(Comparator.comparing(Pair::getFirst));
            }

            this.memorySlots = pairs;

            if(isFinal)
            {
                setChanged();
            }
        }
    }

    public void setMemorySlot(int slot, ItemStack stack)
    {
        if(isSelectorActive(MEMORY))
        {
            if(slot <= container.getTier().getStorageSlotsWithCrafting() - 1)
            {
                if(isSlot(MEMORY, slot))
                {
                    memorySlots.removeIf(p -> p.getFirst() == slot);
                }
                else
                {
                    memorySlots.add(Pair.of(slot, stack));
                }
            }
        }
    }

    public void setUnsortableSlot(int slot)
    {
        if(isSelectorActive(UNSORTABLE))
        {
            if(slot <= container.getTier().getStorageSlotsWithCrafting() - 1)
            {
                if(isSlot(UNSORTABLE, slot))
                {
                    unsortableSlots.remove((Object)slot);
                }
                else
                {
                    unsortableSlots.add(slot);
                }
            }
        }
    }

    public void clearUnsortables()
    {
        if(isSelectorActive(UNSORTABLE))
        {
            unsortableSlots = new ArrayList<>();
        }
    }

    public void clearMemory()
    {
        if(isSelectorActive(MEMORY))
        {
            memorySlots = new ArrayList<>();
        }
    }

    public void setChanged()
    {
        if(container.getScreenID() != Reference.BLOCK_ENTITY_SCREEN_ID)
        {
            container.setDataChanged(ITravelersBackpackContainer.SLOT_DATA);
        }
        else
        {
            container.setDataChanged();
        }
    }

    public boolean isSelectorActive(byte type)
    {
        return switch (type) {
            case UNSORTABLE -> this.isUnsortableActive;
            case MEMORY -> this.isMemoryActive;
            default -> false;
        };
    }

    public void setSelectorActive(byte type, boolean bool)
    {
        switch(type)
        {
            case UNSORTABLE -> this.isUnsortableActive = bool;
            case MEMORY -> this.isMemoryActive = bool;
        }
    }

    public void saveUnsortableSlots(CompoundTag compound)
    {
        compound.putIntArray(UNSORTABLE_SLOTS, getUnsortableSlots().stream().mapToInt(i -> i).toArray());
    }

    public void loadUnsortableSlots(CompoundTag compound)
    {
        this.unsortableSlots = Arrays.stream(compound.getIntArray(UNSORTABLE_SLOTS)).boxed().collect(Collectors.toList());
    }

    public void saveMemorySlots(CompoundTag compound)
    {
        ListTag memorySlotsList = new ListTag();

        for(Pair<Integer, ItemStack> pair : memorySlots)
        {
            CompoundTag itemTag = new CompoundTag();
            itemTag.putInt("Slot", pair.getFirst());
            pair.getSecond().save(itemTag);
            memorySlotsList.add(itemTag);
        }

        compound.put(MEMORY_SLOTS, memorySlotsList);
    }

    public void loadMemorySlots(CompoundTag compound)
    {
        ListTag tagList = compound.getList(MEMORY_SLOTS, Tag.TAG_COMPOUND);
        List<Pair<Integer, ItemStack>> pairs = new ArrayList<>();

        for(int i = 0; i < tagList.size(); i++)
        {
            CompoundTag itemTag = tagList.getCompound(i);
            int slot = itemTag.getInt("Slot");

            if(slot <= container.getTier().getStorageSlotsWithCrafting() - 1)
            {
                Pair<Integer, ItemStack> pair = Pair.of(slot, ItemStack.of(itemTag));
                pairs.add(pair);
            }
        }

        this.memorySlots = pairs;
    }
}